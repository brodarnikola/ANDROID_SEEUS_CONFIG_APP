/* SWISS INNOVATION LAB CONFIDENTIAL
*
* www.swissinnolab.com
* __________________________________________________________________________
*
* [2016] - [2017] Swiss Innovation Lab AG
* All Rights Reserved.
*
* @author mfatiga
*
* NOTICE:  All information contained herein is, and remains
* the property of Swiss Innovation Lab AG and its suppliers,
* if any.  The intellectual and technical concepts contained
* herein are proprietary to Swiss Innovation Lab AG
* and its suppliers and may be covered by E.U. and Foreign Patents,
* patents in process, and are protected by trade secret or copyright law.
* Dissemination of this information or reproduction of this material
* is strictly forbidden unless prior written permission is obtained
* from Swiss Innovation Lab AG.
*/

package hr.sil.android.blecommunicator.impl.nrfdfu

import android.os.SystemClock
import android.util.Log
import hr.sil.android.blecommunicator.core.BLECharacteristicsHolder
import hr.sil.android.blecommunicator.core.BLECommDeviceHandle
import hr.sil.android.blecommunicator.core.communicator.BLEAsyncCommunicator
import hr.sil.android.blecommunicator.core.model.BLEConnectionPriority
import hr.sil.android.blecommunicator.impl.nrfdfu.internal.ArchiveInputStream
import hr.sil.android.blecommunicator.impl.nrfdfu.internal.DfuFileType
import hr.sil.android.blecommunicator.impl.nrfdfu.model.*
import hr.sil.android.blecommunicator.impl.nrfdfu.progress.DFUProgressListener
import hr.sil.android.blecommunicator.impl.nrfdfu.progress.DFUProgressState
import hr.sil.android.blecommunicator.util.format
import hr.sil.android.rest.core.util.toInt
//import hr.sil.android.util.general.extensions.format
//import hr.sil.android.util.general.extensions.toInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.delay
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.util.*
import java.util.zip.CRC32

/**
 * @author mfatiga
 */
internal class SecureDFU(private val handle: BLECommDeviceHandle, private val progressListener: DFUProgressListener) {
    companion object {
        private const val OP_CODE_RESPONSE_CODE_KEY = 0x60.toByte()
        private val SETTINGS_DEFAULT_MBR_SIZE = 0x1000

        private const val TAG = "SecureDFU"

        private const val CHAR_UUID_DFU_CONTROL_POINT = "8EC90001-F315-4F60-9FB8-838830DAEA50"
        private const val CHAR_UUID_DFU_PACKET = "8EC90002-F315-4F60-9FB8-838830DAEA50"
        fun isValidDevice(handle: BLECommDeviceHandle): Boolean {
            val dfuControlPointExists = handle.doesCharacteristicExist(UUID.fromString(CHAR_UUID_DFU_CONTROL_POINT))
            val dfuPacketExists = handle.doesCharacteristicExist(UUID.fromString(CHAR_UUID_DFU_PACKET))

            return dfuControlPointExists && dfuPacketExists
        }
    }

    //debug mode
    private var debugEnabled: Boolean = false

    fun setDebugMode(enabled: Boolean) {
        this.debugEnabled = enabled
    }

    private fun debug(msg: String, t: Throwable? = null) {
        if (debugEnabled) {
            if (t != null) {
                Log.e(TAG, msg, t)
            } else {
                Log.d(TAG, msg)
            }
        }
    }

    //device handle and characteristics
    private val characteristics = object : BLECharacteristicsHolder(handle) {
        val dfuControlPoint by characteristic(CHAR_UUID_DFU_CONTROL_POINT, 20)
        val dfuPacket by characteristic(CHAR_UUID_DFU_PACKET, 244)
    }

    //response channel
    private var dfuCtrlPointChannel: ReceiveChannel<ByteArray>? = null

    private fun getControlPointChannel(): ReceiveChannel<ByteArray> {
        if (dfuCtrlPointChannel == null || dfuCtrlPointChannel!!.isClosedForReceive) {
            dfuCtrlPointChannel = characteristics.dfuControlPoint.notifyOnChannel()
        }
        return dfuCtrlPointChannel!!
    }

    private fun stopControlPointChannel() {
        dfuCtrlPointChannel?.let {
            if (it.isClosedForReceive) {
                try {
                    it.cancel()
                } catch (exc: Exception) {
                    //ignore
                }
            }
        }
        dfuCtrlPointChannel = null
    }

    private suspend fun receiveControlPointResponse(): ByteArray = getControlPointChannel().receive()

    //util
    private fun intToBytesLittleEndian(value: Int, size: Int): ByteArray =
            ByteArray(size) { i -> ((value shr (8 * i)) and 0xFF).toByte() }

    //operations - core
    private fun parseControlPointResponse(bytes: ByteArray): DfuOpResponse {
        //parse response
        if (bytes.size >= 3 && bytes[0] == OP_CODE_RESPONSE_CODE_KEY) {
            val requestCode = DfuOpCode.parse(bytes[1])
            val resultCode = DfuOpResultCode.parse(bytes[2])
            val extendedErrorCode: Int?
            val resultValue: ByteArray

            if (resultCode == DfuOpResultCode.EXTENDED_ERROR) {
                extendedErrorCode = bytes[3].toInt()
                resultValue = byteArrayOf()
            } else {
                extendedErrorCode = null
                resultValue = bytes.drop(3).toByteArray()
            }

            return DfuOpResponse(
                    requestCode = requestCode,
                    resultCode = DfuOpResultCode.parse(bytes[2]),
                    extendedErrorCode = extendedErrorCode,
                    result = resultValue)
        } else {
            return DfuOpResponse(
                    requestCode = DfuOpCode.UNKNOWN,
                    resultCode = DfuOpResultCode.UNKNOWN,
                    extendedErrorCode = null,
                    result = byteArrayOf()
            )
        }
    }

    private suspend fun callOperation(opCode: DfuOpCode, params: ByteArray): DfuOpResponse {
        //construct request
        val data = byteArrayOf(opCode.code) + params

        //submit request
        val responseBytes = if (characteristics.dfuControlPoint.write(data).status) {
            receiveControlPointResponse()
        } else {
            byteArrayOf()
        }
        return parseControlPointResponse(responseBytes)
    }

    //operations - concrete
    //parsed result types
    private enum class ObjectType(val code: Byte) {
        COMMAND(0x01.toByte()),
        DATA(0x02.toByte())
    }

    private data class ObjectChecksum(val offset: Int, val crc32: Int) {
        override fun toString(): String =
                "{ offset: $offset, crc32: ${String.format(Locale.US, "%08X", crc32)} }"
    }

    private data class ObjectInfo(val maxSize: Int, val offset: Int, val crc32: Int) {
        override fun toString(): String =
                "{ maxSize: $maxSize, offset: $offset, crc32: ${String.format(Locale.US, "%08X", crc32)} }"
    }

    //operation methods
    private suspend fun callCreateObject(type: ObjectType, size: Int): DfuOpCallResult<Unit> {
        val response = callOperation(
                DfuOpCode.CREATE_OBJECT,
                byteArrayOf(type.code) + intToBytesLittleEndian(size, 4))

        return if (response.resultCode == DfuOpResultCode.SUCCESS) {
            DfuOpCallResult.Success(response, Unit)
        } else {
            DfuOpCallResult.Error(response)
        }
    }

    private suspend fun callSetPacketReceiptNotifications(number: Int): DfuOpCallResult<Unit> {
        val response = callOperation(
                DfuOpCode.SET_PRN,
                intToBytesLittleEndian(number, 2))

        return if (response.resultCode == DfuOpResultCode.SUCCESS) {
            DfuOpCallResult.Success(response, Unit)
        } else {
            DfuOpCallResult.Error(response)
        }
    }

    private suspend fun callCalculateCRC(): DfuOpCallResult<ObjectChecksum> {
        val response = callOperation(
                DfuOpCode.CALCULATE_CRC,
                byteArrayOf())

        return if (response.resultCode == DfuOpResultCode.SUCCESS) {
            val objChecksum = ObjectChecksum(
                    offset = response.result.take(4).reversed().toInt(),
                    crc32 = response.result.drop(4).take(4).reversed().toInt())

            DfuOpCallResult.Success(response, objChecksum)
        } else {
            DfuOpCallResult.Error(response)
        }
    }

    private suspend fun callExecute(): DfuOpCallResult<Unit> {
        val response = callOperation(
                DfuOpCode.EXECUTE,
                byteArrayOf())

        return if (response.resultCode == DfuOpResultCode.SUCCESS) {
            DfuOpCallResult.Success(response, Unit)
        } else {
            DfuOpCallResult.Error(response)
        }
    }

    private suspend fun callSelectObject(type: ObjectType): DfuOpCallResult<ObjectInfo> {
        val response = callOperation(
                DfuOpCode.SELECT_OBJECT,
                byteArrayOf(type.code))

        return if (response.resultCode == DfuOpResultCode.SUCCESS) {
            val objectInfo = ObjectInfo(
                    maxSize = response.result.take(4).reversed().toInt(),
                    offset = response.result.drop(4).take(4).reversed().toInt(),
                    crc32 = response.result.drop(8).take(4).reversed().toInt())

            DfuOpCallResult.Success(response, objectInfo)
        } else {
            DfuOpCallResult.Error(response)
        }
    }

    //DFU implementation
    private suspend fun sendStream(objectType: ObjectType, stream: InputStream, streamSize: Int): DfuSendResult {
        //notify progress
        progressListener.onStateChange(when (objectType) {
            ObjectType.COMMAND -> DFUProgressState.DFU_WRITING_INIT
            ObjectType.DATA -> DFUProgressState.DFU_WRITING_FIRMWARE
        })

        // packets before receiving a receipt notification
        val prn = 0

        // Send the number of packets of firmware before receiving a receipt notification
        debug("Calling SET_PRN($prn)..")
        val prnCallResult = callSetPacketReceiptNotifications(prn)
        if (prnCallResult is DfuOpCallResult.Error) {
            debug(prnCallResult.getLogString())
            return DfuSendResult.Error.OperationError.fromOpCallResult(prnCallResult)
        }
        debug("SET_PRN call complete")

        // Select the last object to receive the info
        debug("Calling SELECT_OBJECT($objectType)...")
        val objectInfoCallResult = callSelectObject(objectType)
        if (objectInfoCallResult is DfuOpCallResult.Error) {
            debug(objectInfoCallResult.getLogString())
            return DfuSendResult.Error.OperationError.fromOpCallResult(objectInfoCallResult)
        }
        val objectInfo = (objectInfoCallResult as DfuOpCallResult.Success).data
        debug("SELECT_OBJECT($objectType) complete, objectInfo=$objectInfo")

        // Parameters
        val maxSize = objectInfo.maxSize
        var offset = objectInfo.offset
        var crc32Last = objectInfo.crc32

        val crc32LocalExecuted = CRC32()
        val crc32LocalCalculated = CRC32()
        var crc32Local = crc32LocalExecuted

        var bytesSent = 0
        var bytesReceived = 0

        // Number of chunks in which the data will be sent
        val chunkCount = (streamSize + maxSize - 1) / maxSize
        var currentChunk = 0

        // Boolean that will be true if we need to send a partial object block
        var resumeSendingData = false

        // Can we resume? If the offset obtained from the device is greater then zero we can compare it with the local CRC
        // and resume sending the data.
        if (offset > 0) {
            try {
                currentChunk = offset / maxSize
                var bytesSentAndExecuted = maxSize * currentChunk
                var bytesSentNotExecuted = offset - bytesSentAndExecuted

                // If the offset is dividable by maxSize, assume that the last page was not executed
                if (bytesSentNotExecuted == 0) {
                    bytesSentAndExecuted -= maxSize
                    bytesSentNotExecuted = maxSize
                }

                // Read the same number of bytes from the current init packet to calculate local CRC32
                if (bytesSentAndExecuted > 0) {
                    // Read executed bytes
                    val sentAndExecutedBytes = ByteArray(bytesSentAndExecuted)
                    stream.read(sentAndExecutedBytes)
                    crc32LocalCalculated.update(sentAndExecutedBytes)
                    crc32LocalExecuted.update(sentAndExecutedBytes)

                    // Mark here
                    stream.mark(maxSize)
                }
                // Here the bytesSentNotExecuted is for sure greater then 0
                val sentNotExecutedBytes = ByteArray(bytesSentNotExecuted)
                stream.read(ByteArray(bytesSentNotExecuted)) // Read the rest
                crc32LocalCalculated.update(sentNotExecutedBytes)

                // Calculate the CRC32
                val crc = (crc32LocalCalculated.value and 0xFFFFFFFFL).toInt()

                if (crc == crc32Last) {
                    debug("$offset  bytes of data sent before, CRC match")
                    crc32Local = crc32LocalCalculated

                    bytesSent = offset
                    bytesReceived = offset

                    // If the whole page was sent and CRC match, we have to make sure it was executed
                    if (bytesSentNotExecuted == maxSize && offset < streamSize) {
                        debug("Calling EXECUTE...")
                        val executeCallResult = callExecute()
                        if (executeCallResult is DfuOpCallResult.Error) {
                            debug(executeCallResult.getLogString())
                            return DfuSendResult.Error.OperationError.fromOpCallResult(executeCallResult)
                        }
                        debug("EXECUTE command complete")
                    } else {
                        resumeSendingData = true
                    }
                } else {
                    debug("$offset bytes sent before, CRC does not match")
                    crc32Local = crc32LocalExecuted

                    // The CRC of the current object is not correct. If there was another Data object sent before, its CRC must have been correct,
                    // as it has been executed. Either way, we have to create the current object again.
                    bytesSent = bytesSentAndExecuted
                    bytesReceived = bytesSentAndExecuted
                    offset -= bytesSentNotExecuted
                    crc32Last = 0 // invalidate last block crc32
                    stream.reset()

                    debug("Resuming from byte $offset...")
                }
            } catch (e: IOException) {
                debug("Error while reading stream!", e)
                return DfuSendResult.Error.OtherError(Exception("Error while reading stream!", e))
            }
        }

        val startTime = SystemClock.elapsedRealtime()
        if (offset < streamSize) {
            var error: DfuSendResult.Error? = null
            var writeSuccessful = true
            while (true) {
                //notify progress
                progressListener.onProgressChange(currentChunk, chunkCount)

                val nextObjectSize = Math.min(streamSize - bytesSent, maxSize - bytesSent % maxSize)
                if (nextObjectSize <= 0) break

                if (!resumeSendingData) {
                    // Create the Data object
                    debug("Calling CREATE_OBJECT($objectType, $nextObjectSize)")
                    val createObjectCallResult = callCreateObject(objectType, nextObjectSize)
                    if (createObjectCallResult is DfuOpCallResult.Error) {
                        debug(createObjectCallResult.getLogString())
                        error = DfuSendResult.Error.OperationError.fromOpCallResult(createObjectCallResult)
                        break
                    }
                    debug("CREATE_OBJECT command complete, size=$nextObjectSize (${currentChunk + 1}/$chunkCount)")
                } else {
                    resumeSendingData = false
                }

                // Send the current object part
                debug("Writing $nextObjectSize bytes of data for chunk ${currentChunk + 1}/$chunkCount...")
                writeSuccessful = characteristics.dfuPacket.writeChannel(GlobalScope.produce(Dispatchers.Main) {
                    var currentObjectSize = nextObjectSize
                    val bufferSize = characteristics.dfuPacket.getMaxWriteSize()
                    try {
                        while (currentObjectSize > 0) {
                            val nextReadSize = if (currentObjectSize < bufferSize) currentObjectSize else bufferSize
                            if (nextReadSize > 0) {
                                val buffer = ByteArray(nextReadSize)
                                val size = stream.read(buffer)
                                if (size == -1) {
                                    error = DfuSendResult.Error.OtherError(Exception("Stream read failed! Stream ended before it was read completely!"))
                                    break
                                }

                                send(buffer.take(size).toByteArray())
                                crc32Local.update(buffer, 0, size)
                                bytesSent += size

                                currentObjectSize -= size
                            } else {
                                break
                            }
                        }
                    } catch (e: IOException) {
                        error = DfuSendResult.Error.OtherError(Exception("Stream read failed!", e))
                    }
                }).status
                debug("Chunk ${currentChunk + 1}/$chunkCount write done...")
                if (error != null) break
                if (!writeSuccessful) break

                // Calculate Checksum
                debug("Calling CALCULATE_CRC...")
                val calculateCrcCallResult = callCalculateCRC()
                if (calculateCrcCallResult is DfuOpCallResult.Error) {
                    debug(calculateCrcCallResult.getLogString())
                    error = DfuSendResult.Error.OperationError.fromOpCallResult(calculateCrcCallResult)
                    break
                }
                val objectChecksum = (calculateCrcCallResult as DfuOpCallResult.Success).data
                debug("CALCULATE_CRC command complete, objectChecksum=$objectChecksum")
                bytesReceived = objectChecksum.offset

                val bytesLost = bytesSent - bytesReceived
                if (bytesLost > 0) {
                    val msg = "After sending an object, $bytesLost were lost, cancelling DFU!"
                    debug(msg)
                    error = DfuSendResult.Error.OtherError(Exception(msg))
                    break
                }

                val localCrc = (crc32Local.value and 0xFFFFFFFFL).toInt()
                if (localCrc != objectChecksum.crc32) {
                    val msg = "After sending an object, local and returned CRC are not equal, cancelling DFU!"
                    debug(msg)
                    error = DfuSendResult.Error.OtherError(Exception(msg))
                    break
                }

                // Execute object
                debug("Calling EXECUTE...")
                val executeCallResult = callExecute()
                if (executeCallResult is DfuOpCallResult.Error) {
                    debug(executeCallResult.getLogString())
                    error = DfuSendResult.Error.OperationError.fromOpCallResult(executeCallResult)
                    break
                }
                debug("EXECUTE command complete")

                // Increment iterator
                currentChunk++
            }

            if (error != null) {
                return error!!
            }
            if (!writeSuccessful) {
                debug("Characteristic write failed!")
                return DfuSendResult.Error.OtherError(Exception("Characteristic write failed!"))
            }
        } else {
            // Looks as if the whole file was sent correctly but has not been executed
            debug("Calling EXECUTE...")
            val executeCallResult = callExecute()
            if (executeCallResult is DfuOpCallResult.Error) {
                debug(executeCallResult.getLogString())
                return DfuSendResult.Error.OperationError.fromOpCallResult(executeCallResult)
            }
            debug("EXECUTE command complete")
        }

        val endTime = SystemClock.elapsedRealtime()
        val durationMillis = endTime - startTime
        val durationSeconds = durationMillis / 1000.0

        val totalBytesSent = bytesSent - offset

        val speedBytesPerSecond = if (durationSeconds > 0.0) {
            totalBytesSent / durationSeconds
        } else 0.0

        debug("Transfer of $totalBytesSent bytes completed in ${durationSeconds.format(2)}, with ${speedBytesPerSecond.format(2)} bytes/second")
        return DfuSendResult.Success()
    }

    private suspend fun sendInitPacket(stream: InputStream, streamSize: Int): DfuSendResult = sendStream(ObjectType.COMMAND, stream, streamSize)
    private suspend fun sendFirmware(stream: InputStream, streamSize: Int): DfuSendResult = sendStream(ObjectType.DATA, stream, streamSize)

    //external API
    fun isValidDevice(): Boolean = isValidDevice(handle)

    private fun endWithErrorResult(throwable: Throwable): DfuSendResult {
        val result = DfuSendResult.Error.OtherError(throwable)
        progressListener.onError(result)
        return result
    }

    suspend fun runDFU(zipFileInputStream: InputStream): DfuSendResult {
        progressListener.onStateChange(DFUProgressState.DFU_STARTING)

        //settings
        val mbrSize = SETTINGS_DEFAULT_MBR_SIZE
        var fileType = DfuFileType.TYPE_AUTO

        debug("Parsing DFU ZIP stream...")
        val zipInputStream = try {
            ArchiveInputStream(zipFileInputStream, mbrSize, fileType)
        } catch (error: Throwable) {
            debug("DFU ZIP parsing failed!", error)
            return endWithErrorResult(error)
        }
        fileType = zipInputStream.contentType

        val typesInZip = mutableListOf<String>()
        if ((fileType and DfuFileType.TYPE_SOFT_DEVICE) == DfuFileType.TYPE_SOFT_DEVICE) {
            typesInZip.add("SoftDevice")
        }
        if ((fileType and DfuFileType.TYPE_BOOTLOADER) == DfuFileType.TYPE_BOOTLOADER) {
            typesInZip.add("Bootloader")
        }
        if ((fileType and DfuFileType.TYPE_APPLICATION) == DfuFileType.TYPE_APPLICATION) {
            typesInZip.add("Application")
        }
        debug("ZIP contains: ${typesInZip.joinToString(", ") { it }}")

        //dfu parameters
        val initPacketStream: InputStream? = if (fileType == DfuFileType.TYPE_APPLICATION) {
            if (zipInputStream.applicationInit != null) {
                debug("Init packet is: \"applicationInit\"")
                ByteArrayInputStream(zipInputStream.applicationInit)
            } else {
                null
            }
        } else {
            if (zipInputStream.systemInit != null) {
                debug("Init packet is: \"systemInit\"")
                ByteArrayInputStream(zipInputStream.systemInit)
            } else {
                null
            }
        }
        val initPacketSize: Int = initPacketStream?.available() ?: 0

        val firmwareStream: InputStream = zipInputStream
        val firmwareSize: Int = firmwareStream.available()

        debug("Starting DFU with init $initPacketSize bytes and firmware $firmwareSize bytes...")
        val result = if (initPacketStream != null && initPacketSize > 0) {
            internalRunDfu(initPacketStream, initPacketSize, firmwareStream, firmwareSize)
        } else {
            DfuSendResult.Error.OtherError(Exception("Invalid init packet!"))
        }
        debug("Internal DFU ended, closing DFU ZIP stream...")

        zipInputStream.close()

        when (result) {
            is DfuSendResult.Success -> progressListener.onComplete()
            is DfuSendResult.Error -> progressListener.onError(result)
        }

        return result
    }

    private suspend fun internalRunDfu(initPacketStream: InputStream, initPacketSize: Int, firmwareStream: InputStream, firmwareSize: Int): DfuSendResult {
        if (handle.isConnected()) {
            if (isValidDevice()) {
                val mtuResponse = handle.requestMTU(BLEAsyncCommunicator.GATT_MTU_MAXIMUM)
                debug("MTU=${mtuResponse.mtu.value}")

                val connPriority = BLEConnectionPriority.HIGH
                val connPriorityRequestSuccessful = handle.requestConnectionPriority(connPriority)
                debug("Connection priority $connPriority request ${if (connPriorityRequestSuccessful) "successful!" else "failed!"}")

                debug("Subscribing to ControlPoint notifications...")
                getControlPointChannel()
                delay(1000)

                debug("Sending init packet...")
                val initPacketSendResult = sendInitPacket(initPacketStream, initPacketSize)

                // return and log result
                val result = if (initPacketSendResult is DfuSendResult.Success) {
                    debug("Init packet sent, sending firmware...")
                    val firmwareImageSendResult = sendFirmware(firmwareStream, firmwareSize)
                    if (firmwareImageSendResult is DfuSendResult.Success) {
                        debug("Firmware sent, ending")
                    } else {
                        if (firmwareImageSendResult is DfuSendResult.Error.OtherError) {
                            debug("Firmware send failed!", firmwareImageSendResult.throwable)
                        } else {
                            debug("Firmware send failed! Error: $firmwareImageSendResult")
                        }
                    }
                    firmwareImageSendResult
                } else {
                    if (initPacketSendResult is DfuSendResult.Error.OtherError) {
                        debug("Init packet send failed!", initPacketSendResult.throwable)
                    } else {
                        debug("Init packet send failed! Error: $initPacketSendResult")
                    }
                    initPacketSendResult

                }
                stopControlPointChannel()

                return result
            } else {
                return DfuSendResult.Error.OtherError(Exception("Device invalid!"))
            }
        } else {
            return DfuSendResult.Error.OtherError(Exception("Not connected!"))
        }
    }
}