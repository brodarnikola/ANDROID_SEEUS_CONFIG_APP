package hr.sil.android.ble.scanner.scan_multi.properties.advv2

import hr.sil.android.ble.scanner.scan_multi.properties.advv1.parsers.BLEAdvPropertySensorHumidity
import hr.sil.android.ble.scanner.scan_multi.properties.advv1.parsers.BLEAdvPropertySensorPressure
import hr.sil.android.ble.scanner.scan_multi.properties.advv1.parsers.BLEAdvPropertySensorTemperature
import hr.sil.android.ble.scanner.scan_multi.properties.advv2.base.BLEAdvV2Base
import hr.sil.android.ble.scanner.scan_multi.properties.advv2.common.MPLDeviceStatus
import hr.sil.android.ble.scanner.scan_multi.properties.parsers.BLEAdvPropertyFirmwareVersion
import hr.sil.android.ble.scanner.scan_multi.properties.parsers.BLEAdvPropertyNumber
import hr.sil.android.ble.scanner.scan_multi.properties.parsers.BLEAdvPropertyRaw

/**
 * @author mfatiga
 */
class BLEAdvMplTablet : BLEAdvV2Base() {
    //TLM0
    val temperature by registerParser(
            listOf(packet(PACKET_CODE_TLM0) to bytes(14, 15)),
            BLEAdvPropertySensorTemperature())

    val humidity by registerParser(
            listOf(packet(PACKET_CODE_TLM0) to byte(16)),
            BLEAdvPropertySensorHumidity())

    val pressure by registerParser(
            listOf(packet(PACKET_CODE_TLM0) to bytes(17, 18)),
            BLEAdvPropertySensorPressure())

    val numberOfSlaves by registerParser(
            listOf(packet(PACKET_CODE_TLM0) to byte(19)),
            BLEAdvPropertyNumber.UInt8(false) { it })

    val slavesFreeXS by registerParser(
            listOf(packet(PACKET_CODE_TLM0) to byte(20)),
            BLEAdvPropertyNumber.UInt8(false) { it })

    val slavesFreeS by registerParser(
            listOf(packet(PACKET_CODE_TLM0) to byte(21)),
            BLEAdvPropertyNumber.UInt8(false) { it })

    val slavesFreeM by registerParser(
            listOf(packet(PACKET_CODE_TLM0) to byte(22)),
            BLEAdvPropertyNumber.UInt8(false) { it })

    val slavesFreeL by registerParser(
            listOf(packet(PACKET_CODE_TLM0) to byte(23)),
            BLEAdvPropertyNumber.UInt8(false) { it })

    val slavesFreeXL by registerParser(
            listOf(packet(PACKET_CODE_TLM0) to byte(24)),
            BLEAdvPropertyNumber.UInt8(false) { it })

    val applicationVersion by registerParser(
            listOf(packet(PACKET_CODE_TLM0) to bytes(25, 27)),
            BLEAdvPropertyFirmwareVersion())

    val deviceStatus by registerParser(
            listOf(packet(PACKET_CODE_TLM0) to byte(28)),
            BLEAdvPropertyRaw { MPLDeviceStatus.parse(it.firstOrNull()) })
}