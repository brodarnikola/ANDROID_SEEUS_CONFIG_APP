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
import hr.sil.android.mplhuber.core.ble.comm.model.MPLGenericCommand
import hr.sil.android.mplhuber.core.remote.WSSeeUsAdmin
import hr.sil.android.mplhuber.core.remote.model.RNetworkConfiguration
import hr.sil.android.mplhuber.core.remote.model.RPowerType
import hr.sil.android.mplhuber.core.util.BLEScannerStateHolder
import hr.sil.android.mplhuber.core.util.macRealToBytes
import hr.sil.android.util.general.extensions.toByteArray
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets

/**
 * @author mfatiga
 */
class MPLAdminBLECommunicator(
        ctx: Context,
        deviceAddress: String,
        bleScannerStateHolder: BLEScannerStateHolder
) : BaseMPLCommunicator(
        ctx,
        deviceAddress,
        bleScannerStateHolder,
        WSSeeUsAdmin
) {
    companion object {
        private val cmdCfgRegisterMaster = StreamingCommand(0xD0, 0x01)
        private val cmdCfgNetApnUrl = StreamingCommand(0xD1, 0x02)
        private val cmdCfgNetApnUser = StreamingCommand(0xD1, 0x03)
        private val cmdCfgNetApnPass = StreamingCommand(0xD1, 0x04)
        private val cmdCfgNetSimPin = StreamingCommand(0xD1, 0x05)
        private val cmdCfgNetPlmn = StreamingCommand(0xD1, 0x08)
        private val cmdCfgNetBackendUrl = StreamingCommand(0xD1, 0x06)
        private val cmdCfgNetBackendApiKey = StreamingCommand(0xD1, 0x07)
        private val cmdCfgNetEnableRadioAccessTech = StreamingCommand(0xD1, 0x01)
        private val cmdCfgSleepMode = StreamingCommand(0xD1, 0x09)
        private val cmdCfgPsmMode = StreamingCommand(0xD1, 0x0C)
        private val cmdCfgSetBand = StreamingCommand(0xD1, 0x0A)
        private val cmdApplyConfiguration = StreamingCommand(0xD1, 0x0B)


        //Button registration
        private val cmdRegisterSlave = StreamingCommand(0x01, 0xFE)
        private val cmdAssistanceTest = StreamingCommand(0x01, 0x00)
        private val cmdRebootButton = StreamingCommand(0x01, 0xFF)
        private val cmdDeregisterSlave = StreamingCommand(0x01, 0xEE)
        private val cmdDebugMode = StreamingCommand(0x01, 0xFD)

        private val cmdButtonTest = StreamingCommand(0x01, 0x00)
        private val cmdResetDevice = StreamingCommand(0x01, 0xFC)
    }

    // core access
    private suspend fun writeEncryptData(cmd: StreamingCommand, data: ByteArray): Boolean {
        val encrypted = wrapEncryptData(data)
        if (encrypted != null) {
            if (streaming.writeArray(cmd, encrypted).status) {
                streaming.writeEmpty()
                return true
            }
        }
        return false
    }

    private suspend fun writeNetApnUrl(apnUrl: String): Boolean {
        return writeEncryptData(cmdCfgNetApnUrl, apnUrl.toByteArray(StandardCharsets.US_ASCII))
    }

    private suspend fun writeNetApnUser(apnUser: String): Boolean {
        return writeEncryptData(cmdCfgNetApnUser, apnUser.toByteArray(StandardCharsets.US_ASCII))
    }

    private suspend fun writeNetApnPass(apnPass: String): Boolean {
        return writeEncryptData(cmdCfgNetApnPass, apnPass.toByteArray(StandardCharsets.US_ASCII))
    }

    private suspend fun writeNetSimPin(simPin: String): Boolean {
        return writeEncryptData(cmdCfgNetSimPin, simPin.toByteArray(StandardCharsets.US_ASCII))
    }

    private suspend fun writeNetPlmn(plmn: String?): Boolean {
        val bytes = if (plmn.isNullOrBlank()) byteArrayOf() else plmn.toByteArray(StandardCharsets.US_ASCII)
        return writeEncryptData(cmdCfgNetPlmn, bytes)
    }

    private suspend fun writeNetBackendUrl(backendUrl: String): Boolean {
        return writeEncryptData(cmdCfgNetBackendUrl, backendUrl.toByteArray(StandardCharsets.US_ASCII))
    }

    suspend fun enterSleepMode(seconds: Long): Boolean {
        return writeEncryptData(cmdCfgSleepMode, seconds.toU32Bytes().reversedArray())
    }

    suspend fun enterPsmMode(activate:Boolean): Boolean {
        val psmDuration = if(activate) 21600L else 0L
        return writeEncryptData(cmdCfgPsmMode, psmDuration.toU32Bytes().reversedArray())
    }

    suspend fun applyConfiguration(): Boolean {
        val data = byteArrayOf(0x01.toByte())
        val encrypted = wrapEncryptData(data)
        if (encrypted != null) {
            if (streaming.writeArray(cmdApplyConfiguration, encrypted).status) {
                streaming.writeEmpty()
                return true
            }
        }
        return false
    }

    suspend fun resetConfiguration(): Boolean {
        val data = byteArrayOf(0x01.toByte())
        val encrypted = wrapEncryptData(data)
        if (encrypted != null) {
            if (streaming.writeArray(cmdResetDevice, encrypted).status) {
                streaming.writeEmpty()
                return true
            }
        }
        return false
    }

    suspend fun setBand(bandValue: Int?): Boolean {
        val band = if (bandValue == null) 11 else {
            log.info("Set band value to $bandValue")
            bandValue
        }
        return writeEncryptData(cmdCfgSetBand, band.toU8Bytes())
    }


    fun Int.toU8Bytes(): ByteArray {
        val bytes = ByteArray(4)
        val buf = ByteBuffer.allocate(4).putInt(this and 0x0000FFFF)
        buf.position(0)
        buf.get(bytes)
        return bytes.drop(3).toByteArray()
    }

    private suspend fun writeNetRadioAccessTechnology(enableHttps: Int): Boolean {
        return writeEncryptData(cmdCfgNetEnableRadioAccessTech, (enableHttps).toByteArray(1))
    }

    private suspend fun writeGlobalConfiguration(networkConfiguration: RNetworkConfiguration): Boolean {
        val configuration = WSSeeUsAdmin.getGlobalConfigurationData() ?: return false

        val backendBaseUrl = configuration.backendBaseUrl
        val backendRadioAccessTechnology = configuration.backendRadioAccessTechnology

        if (backendBaseUrl != null) if (!writeNetBackendUrl(backendBaseUrl)) return false

        if (backendRadioAccessTechnology != null) if (!writeNetRadioAccessTechnology(networkConfiguration.networkMode.type)) return false

        return true
    }

    private suspend fun writeNetBackendApiKey(): Boolean {
        val challenge = readChallenge()
        val encrypted = if (challenge != null) WSSeeUsAdmin.getDeviceApiKey(challenge, deviceAddress) else null
        log.info("APiKey Encripted" + encrypted.toString())
        return if (encrypted != null) {
            if (streaming.writeArray(cmdCfgNetBackendApiKey, encrypted).status) {
                streaming.writeEmpty()
                true
            } else false
        } else {
            false
        }
    }

    private suspend fun writeMasterRegistration(customerId: Int): Boolean {
        val data = customerId.toByteArray(4)
        val encrypted = wrapEncryptData(data)
        if (encrypted != null) {
            if (streaming.writeArray(cmdCfgRegisterMaster, encrypted).status) {
                streaming.writeEmpty()
                return true
            }
        }
        return false
    }

    suspend fun writeNetworkConfiguration(networkConfiguration: RNetworkConfiguration, simPin: String?, sleepPeriod: Long, psm: Boolean): Boolean {
        val apnUrl = networkConfiguration.apnUrl
        val apnUser = networkConfiguration.apnUser
        val apnPass = networkConfiguration.apnPass
        val plmn = networkConfiguration.plmn

        if (apnUrl != null) if (!writeNetApnUrl(apnUrl)) return false
        if (apnUser != null) if (!writeNetApnUser(apnUser)) return false
        if (apnPass != null) if (!writeNetApnPass(apnPass)) return false
        if (simPin != null) if (!writeNetSimPin(simPin)) return false
        if (!writeNetPlmn(plmn)) return false
        if (!setBand(networkConfiguration.band)) return false
        if (!enterSleepMode(sleepPeriod)) return false
        if (!enterPsmMode(psm)) return false


        log.info("Network configuration ${networkConfiguration.networkMode.type}")

        writeNetRadioAccessTechnology(networkConfiguration.networkMode.type)

        return true
    }

    suspend fun registerMaster(customerId: Int, networkConfiguration: RNetworkConfiguration, simPin: String?, sleepPeriod: Long, psmEnabled: Boolean): Boolean {
        //reset registration
        if (!writeMasterRegistration(0)) return false

        //write network configuration
        if (!writeNetworkConfiguration(networkConfiguration, simPin, sleepPeriod, psmEnabled)) return false

        //write global configuration
        if (!writeGlobalConfiguration(networkConfiguration)) return false

        //write api-key
        if (!writeNetBackendApiKey()) return false

        //run registration
        if (!writeMasterRegistration(customerId)) return false


        //success
        return true
    }

    suspend fun registerButton(slaveMacAddress: String): Boolean {
        val data = slaveMacAddress.macRealToBytes().reversedArray()
        val encrypted = wrapEncryptData(data)
        if (encrypted != null) {
            if (streaming.writeArray(cmdRegisterSlave, encrypted).status) {
                streaming.writeEmpty()
                return true
            }
        }
        return false
    }

    suspend fun deregisterSlave(slaveMacAddress: String): Boolean {
        val data = slaveMacAddress.macRealToBytes().reversedArray()
        val encrypted = wrapEncryptData(data)
        if (encrypted != null) {
            if (streaming.writeArray(cmdDeregisterSlave, encrypted).status) {
                streaming.writeEmpty()
                return true
            }
        }
        return false
    }




    suspend fun stopTheBusTest(): Boolean {
        val data = byteArrayOf(0x01.toByte())
        val encryptedButtonCommand = wrapEncryptData(data)
        if (encryptedButtonCommand != null) {
            if (streaming.writeArray(cmdButtonTest, encryptedButtonCommand).status) {
                streaming.writeEmpty()
                return true
            }
        }
        return false
    }

    suspend fun assistanceTest(): Boolean {
        val data = byteArrayOf(0x02.toByte())
        val encryptedButtonCommand = wrapEncryptData(data)
        if (encryptedButtonCommand != null) {
            if (streaming.writeArray(cmdAssistanceTest, encryptedButtonCommand).status) {
                streaming.writeEmpty()
                return true
            }
        }
        return false
    }

    fun Long.toU32Bytes(): ByteArray {
        val bytes = ByteArray(8)
        val buf = ByteBuffer.allocate(8).putLong(this and 0xFFFFFFFFL)
        buf.position(0)
        buf.get(bytes)
        return bytes.drop(4).toByteArray()
    }

    suspend fun rebootSystem(): Boolean {
        val data = byteArrayOf(0x01.toByte())
        val encrypted = wrapEncryptData(data)
        if (encrypted != null) {
            if (streaming.writeArray(cmdRebootButton, encrypted).status) {
                streaming.writeEmpty()
                return true
            }
        }
        return false
    }

    suspend fun setSystemInDebugMode(): Boolean {

        val data = byteArrayOf(0x01.toByte())
        val encrypted = wrapEncryptData(data)
        if (encrypted != null) {
            if (streaming.writeArray(cmdDebugMode, encrypted).status) {
                streaming.writeEmpty()
                return true
            }
        }
        return false
    }
}
