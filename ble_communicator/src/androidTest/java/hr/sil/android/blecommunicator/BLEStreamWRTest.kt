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

package hr.sil.android.blecommunicator

import android.content.Context
import android.support.test.InstrumentationRegistry
import android.util.Log
import hr.sil.android.blecommunicator.base.BaseBLETest
import hr.sil.android.blecommunicator.core.BLECommDeviceHandle
import hr.sil.android.blecommunicator.core.communicator.BLEAsyncCommunicator
import hr.sil.android.blecommunicator.core.model.BLEConnectionParameters
import hr.sil.android.blecommunicator.core.model.BLEConnectionPriority
import hr.sil.android.blecommunicator.impl.characteristics.logging.BLELoggingCharHolder
import hr.sil.android.blecommunicator.impl.characteristics.streaming.BLEStreamingCharHolder
import hr.sil.android.blecommunicator.impl.characteristics.streaming.StreamingCommand
import hr.sil.android.util.general.extensions.format
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.produce
import org.junit.Test

/**
 * @author mfatiga
 */
class BLEStreamWRTest : BaseBLETest("BLEStreamWRTest") {
    companion object {
        private const val KILOBYTE = 1024
    }

    private fun generateByteArray(size: Int): ByteArray = ByteArray(size) { 0x0A }

    private fun produceByteArrays(totalSize: Int, chunk: Int) = GlobalScope.produce {
        var remainingSize = totalSize
        while (remainingSize > 0) {
            val chunkSize = if (remainingSize > chunk) chunk else remainingSize
            send(generateByteArray(chunkSize))
            remainingSize -= chunkSize
        }
    }

    class TestComm(context: Context, deviceAddress: String) {
        val handle: BLECommDeviceHandle by lazy { BLECommDeviceHandle.create(context, deviceAddress) }

        val streaming = BLEStreamingCharHolder(handle)
        val logging = BLELoggingCharHolder(handle)
    }

    private fun getComm(deviceAddress: String) =
            TestComm(InstrumentationRegistry.getTargetContext(), deviceAddress).apply {
                handle.setDebugMode(enabled = false, logThreadName = false)
            }

    //BLEStreamWRTest|AsyncBLECharCommTest|AsyncBLEOPCancelTest|AsyncBLEReadTest|AsyncBLEWriteTest|AsyncBLEConnTest|BLEAsyncCommunicator|error|exception

    @Test
    fun testLogging() = runBlocking {
        val comm = getComm("F5:83:38:D4:DC:05")
        val connectionParameters = BLEConnectionParameters(
                retryCount = 4,
                retryBackoff = 1000L,
                attemptTimeout = 10000L,
                connectionTimeout = 20000L,
                discoverCharacteristicsTimeout = 10000L
        )

        Log.d(TAG, "Connecting...")
        if (comm.handle.connect(connectionParameters)) {
            val mtuWriteSize = comm.handle.requestMTU(BLEAsyncCommunicator.GATT_MTU_MAXIMUM).mtu.maxWriteSize
            Log.d(TAG, "MTU write size = $mtuWriteSize")

            val maxWriteChunkSize = minOf(mtuWriteSize, 240)
            Log.d(TAG, "MAX WriteChunkSize = $maxWriteChunkSize")

            val writeChunkSize = (maxWriteChunkSize / 16) * 16
            Log.d(TAG, "TARGET WriteChunkSize = $writeChunkSize")

            val connPriority = BLEConnectionPriority.HIGH
            val connPriorityRequestSuccessful = comm.handle.requestConnectionPriority(connPriority)
            Log.d(TAG, "Connection priority $connPriority request ${if (connPriorityRequestSuccessful) "successful!" else "failed!"}")

            val logChannel = comm.logging.listenLoggerNotifications()
            async {
                for (logEntry in logChannel) {
                    Log.d(TAG, "BLE_LOG>" + logEntry.joinToString("") { it.toChar().toString() })
                }
            }
            delay(10_000)
            logChannel.cancel()

            comm.handle.disconnect()
            Log.d(TAG, "Done")
        } else {
            Log.d(TAG, "Connection failed!")
        }

        Unit
    }

    @Test
    fun testStreamingWriteSpeed() = runBlocking {
        val comm = getComm("F5:83:38:D4:DC:05")
        val connectionParameters = BLEConnectionParameters(
                retryCount = 4,
                retryBackoff = 1000L,
                attemptTimeout = 10000L,
                connectionTimeout = 20000L,
                discoverCharacteristicsTimeout = 10000L
        )

        Log.d(TAG, "Connecting...")
        if (comm.handle.connect(connectionParameters)) {
            Log.d(TAG, "Connected")

            val mtuWriteSize = comm.handle.requestMTU(BLEAsyncCommunicator.GATT_MTU_MAXIMUM).mtu.maxWriteSize
            Log.d(TAG, "MTU write size = $mtuWriteSize")

            val maxWriteChunkSize = minOf(mtuWriteSize, 240)
            Log.d(TAG, "MAX WriteChunkSize = $maxWriteChunkSize")

            val writeChunkSize = (maxWriteChunkSize / 16) * 16
            Log.d(TAG, "TARGET WriteChunkSize = $writeChunkSize")

            val connPriority = BLEConnectionPriority.HIGH
            val connPriorityRequestSuccessful = comm.handle.requestConnectionPriority(connPriority)
            Log.d(TAG, "Connection priority $connPriority request ${if (connPriorityRequestSuccessful) "successful!" else "failed!"}")

            val didClearLocations = comm.streaming.writeArray(StreamingCommand(2, 3), byteArrayOf(0x01)).status
            Log.d(TAG, "Locations clear ${if (didClearLocations) "successful!" else "failed!"}")

            Log.d(TAG, "Writing locations...")
            val totalWriteSize = 32 * KILOBYTE
            val writeBeginTimestamp = now()
            val (totalBytesWritten, writeSuccessful) = comm.streaming.writeChannel(StreamingCommand(2, 1), produceByteArrays(totalWriteSize, writeChunkSize))
            val writeDurationSeconds = (now() - writeBeginTimestamp) / 1000.0
            Log.d(TAG, "Write ${if (writeSuccessful) "successful" else "failed"}, written $totalBytesWritten bytes in ${writeDurationSeconds.format(2)}s at ${(totalBytesWritten / writeDurationSeconds).format(2)} bytes/s")

            Log.d(TAG, "Reading locations...")
            var totalBytesRead = 0
            var totalBlocksRead = 0
            val readBeginTimestamp = now()
            val channel = comm.streaming.readChannel(StreamingCommand(2, 1, byteArrayOf(0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte())), Dispatchers.Default)
            if (channel != null) {
                for (data in channel) {
                    totalBytesRead += data.size
                    totalBlocksRead++
                }
                val readDurationSeconds = (now() - readBeginTimestamp) / 1000.0
                Log.d(TAG, "Read $totalBytesRead bytes ($totalBlocksRead blocks) in ${readDurationSeconds.format(2)}s at ${(totalBytesRead / readDurationSeconds).format(2)} bytes/s")
            } else {
                Log.d(TAG, "Reading locations failed!")
            }

            comm.handle.disconnect()
            Log.d(TAG, "Done")
        } else {
            Log.d(TAG, "Connection failed!")
        }

        Unit
    }
}