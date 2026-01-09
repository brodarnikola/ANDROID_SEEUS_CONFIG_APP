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

package hr.sil.android.seeusadmin.store.model

import android.content.Context
import hr.sil.android.ble.scanner.model.device.BLEDevice
import hr.sil.android.ble.scanner.scan_multi.model.BLEDeviceData
import hr.sil.android.ble.scanner.scan_multi.properties.advv2.BLEAdvMplMaster
import hr.sil.android.ble.scanner.scan_multi.properties.advv2.BLEAdvSpl
import hr.sil.android.ble.scanner.scan_multi.properties.advv2.base.BLEAdvV2Base
import hr.sil.android.ble.scanner.scan_multi.properties.advv3.BLEAdvDynamic
import hr.sil.android.ble.scanner.scan_multi.properties.base.BLEAdvProperties
import hr.sil.android.mplhuber.core.ble.DeviceStatus
import hr.sil.android.mplhuber.core.ble.comm.MPLAdminBLECommunicator
import hr.sil.android.mplhuber.core.ble.telemetry.BLEBeaconTelemetry
import hr.sil.android.mplhuber.core.model.MPLDeviceType
import hr.sil.android.mplhuber.core.remote.model.*
import hr.sil.android.mplhuber.core.util.logger
import hr.sil.android.seeusadmin.App

import hr.sil.android.rest.core.lerpInDomain


/**
 * @author mfatiga
 */
class Device private constructor(
        val macAddress: String,
        // type
        val type: MPLDeviceType,
        val unitType: RUnitType,
        // from BLE
        val deviceStatus: DeviceStatus,
        val mplMasterModemStatus: String,
        val mplMasterModemQueueSize: String,
        val modemRssi: String,
        val numOfButtons: String,
        val bleRssi: Int?,
        val bleTxPower: Int?,
        val bleDistance: Double?,
        val batteryVoltage: String?,
        val firmwareVersion: String,
        val modemRat: String,
        val buttonUnits: List<RButtonUnit>,
        val masterUnitId: Int?,
        val masterUnitMac: String?,
        val unitName: String?,
        var isInProximity: Boolean,
        val stmVersion: String?,
        val stationLatitude: Double,
        val stationLongitude: Double,
        val latitude: Double,
        val longitude: Double,
        val stationName: String,
        val stationReference: String,
        val stopPoint: String?,
        val radius: Int?,
        val stationId: Int?,
        val polygon: List<RLatLng>,
        val networkConfigurationId: Int?,
        val isVirtual: Boolean,
        val modemWorkingType: RPowerType,
        val modemLineSleepTime: Int,
        val modemBatterySleepTime: Int,
        val isPlaceholder: Boolean,
        val epdId: Int

) {


    val displayName = if (stationName.isNullOrBlank()) {
        if (unitName.isNullOrBlank()) macAddress else unitName
    } else {
        stationName
    }

    companion object {
        val log = logger()

        fun create(macAddress: String, bleData: BLEDevice<BLEDeviceData>?, remoteData: RStationUnit?): Device {

            // remote
            val id = remoteData?.id ?: -1
            val stationUnit = remoteData

            // ble

            var mplDeviceType = MPLDeviceType.UNKNOWN
            var mplMasterDeviceStatus = if (id != -1) DeviceStatus.REGISTERED else DeviceStatus.UNKNOWN
            var bleDynamicUnitType: RUnitType = RUnitType.SEEUS_SCU
            var mplMasterModemStatus = ""
            var firmwareVersion = ""
            var mplMasterModemQueueSize = "0"
            var modemRssi = "NaN"
            var modemRat = "NaN"
            var numberOfButtons = "0"
            val bleProps = bleData?.data?.properties

            var stmVer = ""
            var batteryVoltage: String? = null
            val dynamicTelemetry: BLEBeaconTelemetry
            when (bleProps) {
                is BLEAdvDynamic -> {
                    dynamicTelemetry = BLEBeaconTelemetry.create(props = bleProps)
                    batteryVoltage = dynamicTelemetry.batteryVoltage.display
                    modemRssi = dynamicTelemetry.modemRssi.display ?: ""
                    modemRat = dynamicTelemetry.modemRat
                    mplMasterModemQueueSize = dynamicTelemetry.modemQueue
                    stmVer = dynamicTelemetry.stmVer
                    numberOfButtons = dynamicTelemetry.numOfButtons.display ?: "0"
                    bleDynamicUnitType = RUnitType.parse(bleProps.definition?.key)
                    mplMasterDeviceStatus = dynamicTelemetry.deviceStatus.value
                            ?: DeviceStatus.UNKNOWN
                    mplMasterModemStatus = dynamicTelemetry.modemStatus
                    firmwareVersion = dynamicTelemetry.stmVer
                }
            }

            return Device(
                    macAddress = macAddress,
                    type = mplDeviceType,
                    // from BLE
                    deviceStatus = mplMasterDeviceStatus,
                    mplMasterModemStatus = mplMasterModemStatus,
                    mplMasterModemQueueSize = mplMasterModemQueueSize,
                    bleRssi = bleData?.rssi,
                    bleTxPower = bleData?.data?.txPower,
                    bleDistance = bleData?.data?.distance,
                    batteryVoltage = batteryVoltage,
                    firmwareVersion = firmwareVersion,
                    // from remote
                    unitType = bleDynamicUnitType,
                    buttonUnits = stationUnit?.buttons ?: listOf(),
                    masterUnitId = stationUnit?.id ?: -1,
                    masterUnitMac = stationUnit?.mac ?: "",
                    unitName = stationUnit?.name ?: "",
                    stationName = stationUnit?.stationName ?: "",
                    stationId = stationUnit?.stationId,
                    isInProximity = bleData != null,
                    modemRssi = modemRssi,
                    modemRat = modemRat,
                    numOfButtons = numberOfButtons,
                    stmVersion = stmVer,
                    stationLatitude = stationUnit?.stationLatitude ?: 0.0,
                    stationLongitude = stationUnit?.stationLongitude ?: 0.0,
                    stationReference = stationUnit?.stationReference ?: "",
                    stopPoint = stationUnit?.stopPoint,
                    latitude = stationUnit?.latitude ?: 0.0,
                    longitude = stationUnit?.longitude ?: 0.0,
                    radius = stationUnit?.radiusMeters ?: 0,
                    polygon = remoteData?.polygon ?: listOf(),
                    networkConfigurationId = stationUnit?.networkConfigurationId,
                    isVirtual = stationUnit?.isVirtual ?: false,
                    modemWorkingType = stationUnit?.modemWorkingType?:RPowerType.BATTERY,
                    modemBatterySleepTime= stationUnit?.modemBatterySleepTime?:60,
                    modemLineSleepTime= stationUnit?.modemBatterySleepTime?:21600,
                    isPlaceholder = stationUnit?.isPlaceholder?: false,
                    epdId = stationUnit?.ePdId?: 0


            )
        }


        private fun getBatteryVoltage(bleProps: BLEAdvProperties): Double {
            when (bleProps) {
                is BLEAdvMplMaster -> {
                    val raw = bleProps.batteryRaw.value?.toDouble()?.lerpInDomain(0.0, 255.0, 0.0, 65535.0)
                            ?: 0.0
                    return 0.0005895 * raw - 18.65
                }
                is BLEAdvSpl -> {
                    val raw = bleProps.batteryRaw.value?.toDouble()?.lerpInDomain(0.0, 255.0, 0.0, 65535.0)
                            ?: 0.0
                    return 0.0005895 * raw - 18.65
                }
                else -> return 0.0
            }
        }
    }


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as Device

        return (macAddress == other.macAddress &&

                // type
                type == other.type &&

                // from BLE
                deviceStatus == other.deviceStatus &&
                mplMasterModemStatus == other.mplMasterModemStatus &&
                mplMasterModemQueueSize == other.mplMasterModemQueueSize &&

                batteryVoltage == other.batteryVoltage &&
                firmwareVersion == other.firmwareVersion &&
                masterUnitId == other.masterUnitId &&
                masterUnitMac == other.masterUnitMac &&
                unitName == other.unitName &&
                isInProximity == other.isInProximity &&
                stmVersion == other.stmVersion
                )


    }

    override fun hashCode(): Int {
        return macAddress.hashCode()
    }

    // util
    fun createBLECommunicator(context: Context): MPLAdminBLECommunicator {
        return MPLAdminBLECommunicator(context, macAddress, App.ref)
    }
}