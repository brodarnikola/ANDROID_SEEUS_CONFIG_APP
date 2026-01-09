/* SWISS INNOVATION LAB CONFIDENTIAL
*
* www.swissinnolab.com
* __________________________________________________________________________
*
* [2016] - [2018] Swiss Innovation Lab AG
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

package hr.sil.android.mplhuber.core.ble.comm

import android.content.Context
import hr.sil.android.blecommunicator.impl.characteristics.streaming.StreamingCommand
import hr.sil.android.mplhuber.core.ble.comm.model.BLEDoorOpenResult
import hr.sil.android.mplhuber.core.remote.WSUser
import hr.sil.android.mplhuber.core.util.BLEScannerStateHolder
import hr.sil.android.mplhuber.core.util.macRealToBytes
import hr.sil.android.util.general.extensions.toByteArray
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay

/**
 * @author mfatiga
 */
class MPLUserBLECommunicator(
        ctx: Context,
        deviceAddress: String,
        bleScannerStateHolder: BLEScannerStateHolder
) : BaseMPLCommunicator(
        ctx,
        deviceAddress,
        bleScannerStateHolder,
        WSUser
) {
    companion object {
        private const val POLL_OPEN_DOOR_STATUS_TIMEOUT = 20_000L
        private const val POLL_OPEN_DOOR_STATUS_PERIOD = 500L
        private val cmdReadOpenDoorResult = StreamingCommand(0x04, 0x00)
        private val cmdParcelPickup = StreamingCommand(0x04, 0x03)
        private val cmdParcelSendCreate = StreamingCommand(0x04, 0x04)
        private val cmdParcelSendCancel = StreamingCommand(0x04, 0x05)
    }

    // core access
    private suspend fun readOpenDoorStatusCode(): Byte? {
        val resultSize = 1
        val readResult = streaming.readArray(cmdReadOpenDoorResult, resultSize)
        return if (readResult.status && readResult.data.size == resultSize) {
            readResult.data.first()
        } else {
            null
        }
    }

    private suspend fun pollOpenDoorStatus(
            timeout: Long = POLL_OPEN_DOOR_STATUS_TIMEOUT,
            period: Long = POLL_OPEN_DOOR_STATUS_PERIOD
    ): BLEDoorOpenResult.BLESlaveErrorCode? {
        var result: BLEDoorOpenResult.BLESlaveErrorCode? = null
        val start = System.currentTimeMillis()
        while ((System.currentTimeMillis() - start) < timeout) {
            //read status code or break if null
            val statusCode = readOpenDoorStatusCode() ?: break

            //when status code is set (not equal to 0xFF), set and break
            if (statusCode != 0xFF.toByte()) {
                result = BLEDoorOpenResult.BLESlaveErrorCode.parse(statusCode)
                break
            }

            //wait for period between checks
            delay(period)
        }
        return result
    }

    suspend fun requestParcelPickup(lockerBLEMac: String, endUserId: Int): BLEDoorOpenResult {
        //result
        var bleDeviceErrorCode: BLEDoorOpenResult.BLEDeviceErrorCode = BLEDoorOpenResult.BLEDeviceErrorCode.OK
        var bleSlaveErrorCode: BLEDoorOpenResult.BLESlaveErrorCode = BLEDoorOpenResult.BLESlaveErrorCode.NONE

        //parse parameters
        val data = lockerBLEMac.macRealToBytes().reversedArray() + endUserId.toByteArray(4)

        val encrypted = wrapEncryptData(data)
        if (encrypted != null) {
            if (streaming.writeArray(cmdParcelPickup, encrypted).status) {
                streaming.writeEmpty()
                val openDoorStatus = pollOpenDoorStatus()
                if (openDoorStatus != null) {
                    bleSlaveErrorCode = openDoorStatus
                } else {
                    bleDeviceErrorCode = BLEDoorOpenResult.BLEDeviceErrorCode.READ_RESULT_FAILED
                }
            } else {
                bleDeviceErrorCode = BLEDoorOpenResult.BLEDeviceErrorCode.COMMAND_WRITE_FAILED
            }
        } else {
            bleDeviceErrorCode = BLEDoorOpenResult.BLEDeviceErrorCode.ENCRYPTION_FAILED
        }

        val result = BLEDoorOpenResult.create(bleDeviceErrorCode, bleSlaveErrorCode)
        log.info("OpenDoorResult: $result")
        return result
    }



    suspend fun requestParcelSendCancel(lockerBLEMac: String, endUserId: Int): BLEDoorOpenResult {
        //result
        var bleDeviceErrorCode: BLEDoorOpenResult.BLEDeviceErrorCode = BLEDoorOpenResult.BLEDeviceErrorCode.OK
        var bleSlaveErrorCode: BLEDoorOpenResult.BLESlaveErrorCode = BLEDoorOpenResult.BLESlaveErrorCode.NONE

        //parse parameters
        val data = lockerBLEMac.macRealToBytes().reversedArray() + endUserId.toByteArray(4)

        val encrypted = wrapEncryptData(data)
        if (encrypted != null) {
            if (streaming.writeArray(cmdParcelSendCancel, encrypted).status) {
                streaming.writeEmpty()
                val openDoorStatus = pollOpenDoorStatus()
                if (openDoorStatus != null) {
                    bleSlaveErrorCode = openDoorStatus
                } else {
                    bleDeviceErrorCode = BLEDoorOpenResult.BLEDeviceErrorCode.READ_RESULT_FAILED
                }
            } else {
                bleDeviceErrorCode = BLEDoorOpenResult.BLEDeviceErrorCode.COMMAND_WRITE_FAILED
            }
        } else {
            bleDeviceErrorCode = BLEDoorOpenResult.BLEDeviceErrorCode.ENCRYPTION_FAILED
        }

        val result = BLEDoorOpenResult.create(bleDeviceErrorCode, bleSlaveErrorCode)
        log.info("OpenDoorResult: $result")
        return result
    }
}