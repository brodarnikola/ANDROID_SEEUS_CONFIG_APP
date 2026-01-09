package hr.sil.android.ble.scanner.scan_multi.properties.advv2

import hr.sil.android.ble.scanner.scan_multi.properties.advv1.parsers.BLEAdvPropertySensorHumidity
import hr.sil.android.ble.scanner.scan_multi.properties.advv1.parsers.BLEAdvPropertySensorPressure
import hr.sil.android.ble.scanner.scan_multi.properties.advv1.parsers.BLEAdvPropertySensorTemperature
import hr.sil.android.ble.scanner.scan_multi.properties.advv2.base.BLEAdvV2Base
import hr.sil.android.ble.scanner.scan_multi.properties.advv2.common.ParcelLockerKeyboardType
import hr.sil.android.ble.scanner.scan_multi.properties.advv2.common.MPLDeviceStatus
import hr.sil.android.ble.scanner.scan_multi.properties.advv2.common.MPLModemRAT
import hr.sil.android.ble.scanner.scan_multi.properties.advv2.common.MPLModemStatus
import hr.sil.android.ble.scanner.scan_multi.properties.parsers.BLEAdvPropertyFirmwareVersion
import hr.sil.android.ble.scanner.scan_multi.properties.parsers.BLEAdvPropertyNumber
import hr.sil.android.ble.scanner.scan_multi.properties.parsers.BLEAdvPropertyRaw
import hr.sil.android.ble.scanner.scan_multi.properties.parsers.BLEAdvPropertyTimestamp
import java.nio.ByteOrder

/**
 * @author mfatiga
 */
class BLEAdvSplPlus : BLEAdvV2Base() {
    //TLM0
    val batteryRaw by registerParser(
            listOf(packet(PACKET_CODE_TLM0) to byte(14)),
            BLEAdvPropertyNumber.UInt8(false) { it })

    val modemStatus by registerParser(
            listOf(packet(PACKET_CODE_TLM0) to byte(15)),
            BLEAdvPropertyRaw { MPLModemStatus.parse(it.firstOrNull()) })

    val modemQueue by registerParser(
            listOf(packet(PACKET_CODE_TLM0) to byte(16)),
            BLEAdvPropertyNumber.UInt8(false) { it })

    val temperature by registerParser(
            listOf(packet(PACKET_CODE_TLM0) to bytes(17, 18)),
            BLEAdvPropertySensorTemperature())

    val humidity by registerParser(
            listOf(packet(PACKET_CODE_TLM0) to byte(19)),
            BLEAdvPropertySensorHumidity())

    val pressure by registerParser(
            listOf(packet(PACKET_CODE_TLM0) to bytes(20, 21)),
            BLEAdvPropertySensorPressure())

    val numberOfLockers by registerParser(
            listOf(packet(PACKET_CODE_TLM0) to byte(22)),
            BLEAdvPropertyNumber.UInt8(false) { it })

    val rtcTimestamp by registerParser(
            listOf(packet(PACKET_CODE_TLM0) to bytes(23, 26)),
            BLEAdvPropertyTimestamp(ByteOrder.LITTLE_ENDIAN, true))

    val deviceStatus by registerParser(
            listOf(packet(PACKET_CODE_TLM0) to byte(27)),
            BLEAdvPropertyRaw { MPLDeviceStatus.parse(it.firstOrNull()) })

    //TLM1
    val modemRSSI by registerParser( //dBm
            listOf(packet(PACKET_CODE_TLM1) to byte(14)),
            BLEAdvPropertyNumber.UInt8(false) {
                if (it != null) {
                    if (it >= 128) it - 256 else it
                } else null
            })

    val modemSINR by registerParser( //-20 - 30 dB
            listOf(packet(PACKET_CODE_TLM1) to byte(15)),
            BLEAdvPropertyNumber.UInt8(false) {
                if (it != null) {
                    ((it.toDouble() / 5.0) - 20.0).coerceIn(-20.0, 30.0)
                } else null
            })

    val stmFirmwareVersion by registerParser(
            listOf(packet(PACKET_CODE_TLM1) to bytes(16, 18)),
            BLEAdvPropertyFirmwareVersion())

    val modemRadioAccessTechnology by registerParser(
            listOf(packet(PACKET_CODE_TLM1) to byte(19)),
            BLEAdvPropertyNumber.UInt8(false) { MPLModemRAT.parse(it?.toByte()) })


    val lockersFreeXS by registerParser(
            listOf(packet(PACKET_CODE_TLM1) to byte(20)),
            BLEAdvPropertyNumber.UInt8(false) { it })

    val lockersFreeS by registerParser(
            listOf(packet(PACKET_CODE_TLM1) to byte(21)),
            BLEAdvPropertyNumber.UInt8(false) { it })

    val lockersFreeM by registerParser(
            listOf(packet(PACKET_CODE_TLM1) to byte(22)),
            BLEAdvPropertyNumber.UInt8(false) { it })

    val lockersFreeL by registerParser(
            listOf(packet(PACKET_CODE_TLM1) to byte(23)),
            BLEAdvPropertyNumber.UInt8(false) { it })

    val lockersFreeXL by registerParser(
            listOf(packet(PACKET_CODE_TLM1) to byte(24)),
            BLEAdvPropertyNumber.UInt8(false) { it })

    val keyboardType by registerParser(
            listOf(packet(PACKET_CODE_TLM1) to byte(25)),
            BLEAdvPropertyRaw { ParcelLockerKeyboardType.parse(it.firstOrNull()) })
}