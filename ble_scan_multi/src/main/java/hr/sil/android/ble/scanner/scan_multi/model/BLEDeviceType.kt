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

package hr.sil.android.ble.scanner.scan_multi.model

import hr.sil.android.ble.scanner.model.scan.BLERawScanResult
import hr.sil.android.ble.scanner.parser.btcore.BTCoreAdvertisement
import hr.sil.android.ble.scanner.scan_multi.properties.BLEAdvPropertiesUnknown
import hr.sil.android.ble.scanner.scan_multi.properties.advv0.BLEAdvLegacyGps
import hr.sil.android.ble.scanner.scan_multi.properties.advv0.BLEAdvLegacyStatic
import hr.sil.android.ble.scanner.scan_multi.properties.advv1.BLEAdvLegacyPublicBike
import hr.sil.android.ble.scanner.scan_multi.properties.advv1.BLEAdvSmartLetterbox
import hr.sil.android.ble.scanner.scan_multi.properties.advv2.*
import hr.sil.android.ble.scanner.scan_multi.properties.advv3.BLEAdvDynamic
import hr.sil.android.ble.scanner.scan_multi.properties.base.BLEAdvProperties
import java.util.*

/**
 * @author mfatiga
 */
enum class BLEDeviceType(val code: ByteArray, val protocolVersion: BLEAdvProtocolVersion, val createProperties: () -> BLEAdvProperties) {
    UNKNOWN(byteArrayOf(), BLEAdvProtocolVersion.UNKNOWN, { BLEAdvPropertiesUnknown() }),

    //v0:
    LEGACY_STATIC(byteArrayOf(0x53.toByte()), BLEAdvProtocolVersion.V0, { BLEAdvLegacyStatic() }),
    LEGACY_GPS(byteArrayOf(0x47.toByte()), BLEAdvProtocolVersion.V0, { BLEAdvLegacyGps() }),

    //v1:
    SMART_LETTERBOX(byteArrayOf(0x6C.toByte()), BLEAdvProtocolVersion.V1, { BLEAdvSmartLetterbox() }),
    LEGACY_PUBLIC_BIKE(byteArrayOf(0x48.toByte()), BLEAdvProtocolVersion.V1, { BLEAdvLegacyPublicBike() }),

    //v2:
    GPS_BEACON(byteArrayOf(0x47.toByte()), BLEAdvProtocolVersion.V2, { BLEAdvGpsBeacon() }),
    MY_AID(byteArrayOf(0x49.toByte()), BLEAdvProtocolVersion.V2, { BLEAdvMyAid() }),
    PAKETBOX(byteArrayOf(0x70.toByte()), BLEAdvProtocolVersion.V2, { BLEAdvPaketbox() }),
    MY_OP(byteArrayOf(0x4A.toByte()), BLEAdvProtocolVersion.V2, { BLEAdvMyOp() }),
    BIN_WATCH_MASTER(byteArrayOf(0xB0.toByte()), BLEAdvProtocolVersion.V2, { BLEAdvBinWatchMaster() }),
    BIN_WATCH_SLAVE(byteArrayOf(0xB1.toByte()), BLEAdvProtocolVersion.V2, { BLEAdvBinWatchSlave() }),
    SOR_STATION(byteArrayOf(0x4B.toByte()), BLEAdvProtocolVersion.V2, { BLEAdvSorStation() }),
    E_BUTTON(byteArrayOf(0xE0.toByte()), BLEAdvProtocolVersion.V2, { BLEAdvEButton() }),
    VEHICLE_OCCUPANT_COUNTER(byteArrayOf(0x56.toByte()), BLEAdvProtocolVersion.V2, { BLEAdvVOC() }),
    EPAPER_POC(byteArrayOf(0xEE.toByte()), BLEAdvProtocolVersion.V2, { BLEAdvEpaperPoc() }),
    CHARGING_STATION(byteArrayOf(0xB2.toByte()), BLEAdvProtocolVersion.V2, { BLEAdvChargingStation() }),
    MEDICA_MASTER(byteArrayOf(0x4D.toByte()), BLEAdvProtocolVersion.V2, { BLEAdvMedicaMaster() }),
    MEDICA_SLAVE(byteArrayOf(0x6D.toByte()), BLEAdvProtocolVersion.V2, { BLEAdvMedicaSlave() }),
    SAC_GARAGE(byteArrayOf(0x50.toByte()), BLEAdvProtocolVersion.V2, { BLEAdvSacGarage() }),
    SAC_VEHICLE(byteArrayOf(0x51.toByte()), BLEAdvProtocolVersion.V2, { BLEAdvSacVehicle() }),
    SOR_BUTTON_LICHT(byteArrayOf(0x4C.toByte()), BLEAdvProtocolVersion.V2, { BLEAdvSorButtonLicht() }),
    VAB_TAG(byteArrayOf(0x52.toByte()), BLEAdvProtocolVersion.V2, { BLEAdvVABTag() }),
    VAB_ANCHOR(byteArrayOf(0x53.toByte()), BLEAdvProtocolVersion.V2, { BLEAdvVABAnchor() }),
    MPL_MASTER(byteArrayOf(0x4E), BLEAdvProtocolVersion.V2, { BLEAdvMplMaster() }),
    MPL_TABLET(byteArrayOf(0x40), BLEAdvProtocolVersion.V2, { BLEAdvMplTablet() }),
    MPL_SLAVE(byteArrayOf(0x6E), BLEAdvProtocolVersion.V2, { BLEAdvMplSlave() }),
    MPL_SLAVE_P16(byteArrayOf(0x6F), BLEAdvProtocolVersion.V2, { BLEAdvMplSlaveP16() }),
    SPL(byteArrayOf(0x5E), BLEAdvProtocolVersion.V2, { BLEAdvSpl() }),
    SPL_PLUS(byteArrayOf(0x5F), BLEAdvProtocolVersion.V2, { BLEAdvSplPlus() }),
    SHUTTLE_STATION(byteArrayOf(0x71), BLEAdvProtocolVersion.V2, { BLEAdvShuttleStation() }),
    SHUTTLE_BUTTON(byteArrayOf(0x72), BLEAdvProtocolVersion.V2, { BLEAdvShuttleButton() }),
    SHUTTLE_BUS(byteArrayOf(0x73), BLEAdvProtocolVersion.V2, { BLEAdvShuttleBus() }),
    PUBLIC_BIKE(byteArrayOf(0x4F), BLEAdvProtocolVersion.V2, { BLEAdvPublicBike() }),

    //v3:
    DYNAMIC(byteArrayOf(), BLEAdvProtocolVersion.V3, { BLEAdvDynamic() });

    fun filters(): List<Pair<IntArray, ByteArray>> {
        return if (this != UNKNOWN) {
            val indicesManufacturer = intArrayOf(5, 6)

            val indicesDeviceType = protocolVersion.deviceTypeByteIndex
            val bytesDeviceType = code

            protocolVersion.manufacturers.map { manufacturer ->
                if (indicesDeviceType != -1) {
                    Pair(
                            indicesManufacturer + indicesDeviceType,
                            manufacturer.code + bytesDeviceType)
                } else {
                    Pair(indicesManufacturer, manufacturer.code)
                }
            }
        } else {
            listOf()
        }
    }

    companion object {
        private fun parse(code: ByteArray, protocolVersion: BLEAdvProtocolVersion): BLEDeviceType {
            return values()
                    .firstOrNull {
                        it.code.contentEquals(code) && it.protocolVersion == protocolVersion
                    } ?: UNKNOWN
        }

        internal fun getDeviceType(manufacturer: BLEManufacturer, rawScanResult: BLERawScanResult, btCoreAdvertisement: BTCoreAdvertisement): BLEDeviceType {
            val protocolVersion = BLEAdvProtocolVersion.forManufacturer(manufacturer)
            if (protocolVersion == BLEAdvProtocolVersion.V3) {
                return DYNAMIC
            }

            val deviceTypeByteIndex = protocolVersion.deviceTypeByteIndex
            if (protocolVersion != BLEAdvProtocolVersion.UNKNOWN && deviceTypeByteIndex >= 0 && deviceTypeByteIndex < rawScanResult.scanRecord.size) {
                val deviceTypeCode = byteArrayOf(rawScanResult.scanRecord[deviceTypeByteIndex])
                val deviceType = parse(deviceTypeCode, protocolVersion)
                if (deviceType != UNKNOWN) {
                    return deviceType
                }
            }

            return UNKNOWN
        }
    }
}