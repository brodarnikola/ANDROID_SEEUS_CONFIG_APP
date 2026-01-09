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

import android.content.Context
import android.util.Log
import hr.sil.android.blecommunicator.core.BLECommDeviceHandle
import hr.sil.android.blecommunicator.core.model.BLEConnectionParameters
import hr.sil.android.blecommunicator.impl.nrfdfu.model.DfuSendResult
import hr.sil.android.blecommunicator.impl.nrfdfu.progress.DFUProgressListener
import hr.sil.android.blecommunicator.impl.nrfdfu.progress.DFUProgressState
import hr.sil.android.blecommunicator.impl.nrfdfu.scanner.BootloaderScanner
import kotlinx.coroutines.delay
import java.io.InputStream

/**
 * @author mfatiga
 */
class DFUHandler(private val context: Context, private val progressListener: DFUProgressListener) {
    companion object {
        private const val TAG = "DFUHandler"
    }

    //debug mode
    private var debugEnabled: Boolean = false
    private var bleDebugEnabled: Boolean = false

    fun setDebugMode(enabled: Boolean, bleDebugEnabled: Boolean = false) {
        this.debugEnabled = enabled
        this.bleDebugEnabled = bleDebugEnabled
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

    private suspend fun runSecureDfu(handle: BLECommDeviceHandle, zipFileInputStream: InputStream): DfuSendResult {
        val secureDFU = SecureDFU(handle, progressListener)
        secureDFU.setDebugMode(debugEnabled)

        return secureDFU.runDFU(zipFileInputStream)
    }

    private suspend fun handleDfuAfterBootloaderSwitch(deviceAddress: String, connectionParameters: BLEConnectionParameters, zipFileInputStream: InputStream): DfuSendResult {
        progressListener.onStateChange(DFUProgressState.CONNECTING)
        val handle = BLECommDeviceHandle.create(context, deviceAddress)
        handle.setDebugMode(bleDebugEnabled)
        if (handle.connect(connectionParameters)) {
            debug("Connected! Checking if it's a valid device...")
            progressListener.onStateChange(DFUProgressState.CONNECTED)

            return if (SecureDFU.isValidDevice(handle)) {
                debug("Device valid, starting Secure DFU...")
                val result = runSecureDfu(handle, zipFileInputStream)
                handle.disconnect()
                result
            } else {
                val msg = "Switch to bootloader mode failed!"
                debug("$msg Disconnecting...")
                handle.disconnect()
                return failOther(msg)
            }
        } else {
            val msg = "Connect after switching to bootloader mode failed!"
            debug(msg)
            return failOther(msg)
        }
    }

    suspend fun run(deviceAddress: String, connectionParameters: BLEConnectionParameters, zipFileInputStream: InputStream): DfuSendResult {
        val handle = BLECommDeviceHandle.create(context, deviceAddress)
        handle.setDebugMode(bleDebugEnabled)

        debug("Connecting...")
        progressListener.onStateChange(DFUProgressState.CONNECTING)
        if (handle.connect(connectionParameters)) {
            debug("Connected!")
            progressListener.onStateChange(DFUProgressState.CONNECTED)

            if (SecureDFU.isValidDevice(handle)) {
                debug("Device already in bootloader mode, starting Secure DFU..")
                val result = runSecureDfu(handle, zipFileInputStream)
                handle.disconnect()
                return result
            } else {
                debug("Device not in bootloader mode, checking for buttonless availability...")
                val isValidButtonlessDfuDevice = ButtonlessDFUInitiator.isValidDevice(handle)
                if (isValidButtonlessDfuDevice) {
                    val buttonless = ButtonlessDFUInitiator(handle)
                    buttonless.setDebugMode(debugEnabled)

                    val withBondSharing = buttonless.isWithBondSharing()
                    debug("Device supports buttonless DFU, switching device to bootloader mode...")
                    progressListener.onStateChange(DFUProgressState.ENABLING_DFU)

                    //this method will disconnect from the device, so we will need to reconnect
                    // once the switch to bootloader mode is complete
                    if (buttonless.execute()) {
                        debug("Switch to bootloader mode request sent! Waiting for device...")
                        delay(1000)

                        return if (withBondSharing) {
                            debug("Bond sharing supported, connecting to bootloader...")
                            handleDfuAfterBootloaderSwitch(deviceAddress, connectionParameters, zipFileInputStream)
                        } else {
                            debug("Bond sharing not supported, device MAC address may change to current MAC + 1, scanning for bootloader...")
                            BootloaderScanner.setDebugMode(debugEnabled)

                            val foundDevice = BootloaderScanner.searchFor(deviceAddress)
                            if (foundDevice != null) {
                                debug("Bootloader device found! Connecting..")
                                handleDfuAfterBootloaderSwitch(foundDevice, connectionParameters, zipFileInputStream)
                            } else {
                                val msg = "Failed to find bootloader after requesting switch to bootloader mode!"
                                debug(msg)
                                failOther(msg)
                            }
                        }
                    } else {
                        val msg = "Switch to bootloader mode request send failed!"
                        debug("$msg Disconnecting...")
                        handle.disconnect()
                        return failOther(msg)
                    }
                } else {
                    val msg = "Device does not support buttonless DFU!"
                    debug("$msg Disconnecting...")
                    handle.disconnect()
                    return failOther(msg)
                }
            }
        } else {
            val msg = "Connection failed!"
            debug(msg)
            return failOther(msg)
        }
    }

    private fun failOther(msg: String, e: Throwable? = null): DfuSendResult.Error.OtherError {
        val error = DfuSendResult.Error.OtherError(Exception(msg, e))
        progressListener.onError(error)
        return error
    }
}