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

import android.bluetooth.BluetoothDevice
import android.util.Log
import hr.sil.android.blecommunicator.core.BLECharacteristicsHolder
import hr.sil.android.blecommunicator.core.BLECommDeviceHandle
import hr.sil.android.blecommunicator.core.characteristics.BLECharacteristic
//import hr.sil.android.util.general.extensions.toHexString
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import java.util.*
import java.util.concurrent.TimeUnit
import hr.sil.android.blecommunicator.util.toHexString

/**
 * @author mfatiga
 */
internal class ButtonlessDFUInitiator(private val handle: BLECommDeviceHandle) {
    companion object {
        private const val TAG = "ButtonlessDFUInitiator"

        private const val CHAR_UUID_DFU_WITHOUT_BOND_SHARING = "8EC90003-F315-4F60-9FB8-838830DAEA50"
        private const val CHAR_UUID_DFU_WITH_BOND_SHARING = "8EC90004-F315-4F60-9FB8-838830DAEA50"
        fun isValidDevice(handle: BLECommDeviceHandle): Boolean {
            val btDevice = handle.getBluetoothDevice()
            return if (btDevice != null) {
                val withBondSharingExists = handle.doesCharacteristicExist(UUID.fromString(CHAR_UUID_DFU_WITH_BOND_SHARING))
                val withoutBondSharingExists = handle.doesCharacteristicExist(UUID.fromString(CHAR_UUID_DFU_WITHOUT_BOND_SHARING))

                if (withBondSharingExists || withoutBondSharingExists) {
                    if (withBondSharingExists) btDevice.bondState == BluetoothDevice.BOND_BONDED
                    else withoutBondSharingExists
                } else {
                    false
                }
            } else {
                false
            }
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
        val withoutBondSharing by characteristic(CHAR_UUID_DFU_WITHOUT_BOND_SHARING, 20)
        val withBondSharing by characteristic(CHAR_UUID_DFU_WITH_BOND_SHARING, 20)
    }

    fun isValidDevice(): Boolean = isValidDevice(handle)

    fun isWithBondSharing(): Boolean {
        return characteristics.withBondSharing.exists()
                && handle.getBluetoothDevice()?.bondState == BluetoothDevice.BOND_BONDED
    }

    private fun getTargetCharacteristic(): BLECharacteristic? {
        return if (isValidDevice()) {
            if (isWithBondSharing()) {
                debug("getTargetCharacteristic() -> withBondSharing")
                characteristics.withBondSharing
            } else {
                debug("getTargetCharacteristic() -> withoutBondSharing")
                characteristics.withoutBondSharing
            }
        } else {
            debug("getTargetCharacteristic() -> NULL")
            null
        }
    }


    suspend fun execute(): Boolean {
        debug("Starting...")
        val char = getTargetCharacteristic()
        return if (char != null) {
            debug("Found initiator characteristic, subscribing to notifications...")
            val channel = char.notifyOnChannel()
            debug("Waiting for subscribe...")
            delay(1000)

            debug("Writing initiating data...")
            char.write(byteArrayOf(0x01))
            debug("Write complete")

            try {
                debug("Waiting for notification response...")
                val data = withTimeout(TimeUnit.MILLISECONDS.toMillis(5_000L)) { channel.receive() }
                debug("Got notification data: ${data.toHexString()}")
            } catch (e: Exception) {
                debug("Notification data receive failed!")
            }

            debug("Waiting before disconnect...")
            delay(1_000)

            try {
                debug("Closing notification channel...")
                channel.cancel()
                debug("Notification channel closed")
            } catch (e: Exception) {
                debug("Notification channel close failed!")
            }

            try {
                debug("Calling disconnect...")
                handle.disconnect()
                debug("Disconnect done")
            } catch (e: Exception) {
                debug("Disconnect failed!")
            }

            debug("Ending...")
            true
        } else false
    }
}