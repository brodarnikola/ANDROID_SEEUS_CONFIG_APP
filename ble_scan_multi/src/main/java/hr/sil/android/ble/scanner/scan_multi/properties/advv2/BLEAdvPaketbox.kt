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

package hr.sil.android.ble.scanner.scan_multi.properties.advv2

import hr.sil.android.ble.scanner.scan_multi.properties.advv1.parsers.BLEAdvPropertySensorHumidity
import hr.sil.android.ble.scanner.scan_multi.properties.advv1.parsers.BLEAdvPropertySensorPressure
import hr.sil.android.ble.scanner.scan_multi.properties.advv1.parsers.BLEAdvPropertySensorTemperature
import hr.sil.android.ble.scanner.scan_multi.properties.advv2.base.BLEAdvV2Base
import hr.sil.android.ble.scanner.scan_multi.properties.advv2.parsers.BLEAdvPropertyLoraSnr
import hr.sil.android.ble.scanner.scan_multi.properties.parsers.BLEAdvPropertyBool
import hr.sil.android.ble.scanner.scan_multi.properties.parsers.BLEAdvPropertyHex
import hr.sil.android.ble.scanner.scan_multi.properties.parsers.BLEAdvPropertyNumber
import java.nio.ByteOrder

/**
 * @author mfatiga
 */
class BLEAdvPaketbox : BLEAdvV2Base() {
    companion object {
        const val BATTERY_VOLTAGE_OFFSET = 12.0
    }

    val temperature by registerParser(
            listOf(packet(PACKET_CODE_TLM1) to bytes(16, 17)),
            BLEAdvPropertySensorTemperature())

    val humidity by registerParser(
            listOf(packet(PACKET_CODE_TLM1) to byte(18)),
            BLEAdvPropertySensorHumidity())

    val pressure by registerParser(
            listOf(packet(PACKET_CODE_TLM1) to bytes(19, 20)),
            BLEAdvPropertySensorPressure())

    val batteryVoltage by registerParser(
            listOf(packet(PACKET_CODE_TLM1) to byte(21)),
            BLEAdvPropertyNumber.UInt8(false) {
                (it ?: 0) * 0.02 + BATTERY_VOLTAGE_OFFSET
            })

    val loraSNR by registerParser(
            listOf(packet(PACKET_CODE_TLM1) to byte(22)),
            BLEAdvPropertyLoraSnr())

    val loraPowered by registerParser(
            listOf(packet(PACKET_CODE_TLM1) to byte(23)),
            BLEAdvPropertyBool())

    val loraJoined by registerParser(
            listOf(packet(PACKET_CODE_TLM1) to byte(24)),
            BLEAdvPropertyBool())

    val loraQueueSize by registerParser(
            listOf(packet(PACKET_CODE_TLM1) to byte(25)),
            BLEAdvPropertyNumber.UInt8(false) { it })

    val loraQueueFull by registerParser(
            listOf(packet(PACKET_CODE_TLM1) to byte(26)),
            BLEAdvPropertyBool())

    val stateCode by registerParser(
            listOf(packet(PACKET_CODE_TLM1) to byte(28)),
            BLEAdvPropertyHex { it })

    val state by registerParser(
            listOf(packet(PACKET_CODE_TLM1) to byte(28)),
            BLEAdvPropertyHex { PKBState.parse(it) })

    val loraMacErrorCount by registerParser(
            listOf(packet(PACKET_CODE_TLM0) to bytes(14, 15)),
            BLEAdvPropertyNumber.UInt16(ByteOrder.BIG_ENDIAN, false) { it })

    val loraNoFreeChannelErrorCount by registerParser(
            listOf(packet(PACKET_CODE_TLM0) to bytes(16, 17)),
            BLEAdvPropertyNumber.UInt16(ByteOrder.BIG_ENDIAN, false) { it })

    val loraReJoinCount by registerParser(
            listOf(packet(PACKET_CODE_TLM0) to bytes(18, 19)),
            BLEAdvPropertyNumber.UInt16(ByteOrder.BIG_ENDIAN, false) { it })

    enum class PKBState(val code: String?, val description: String) {
        UNKNOWN(null, "unknown"),
        UNCONFIGURED_SILENT("FF", "Virgin beacon, no BLE"),
        UNCONFIGURED_ADVERT("F8", "Registration mode"),
        UNLOCKED_OPEN("40", "Not locked, door open"),
        UNLOCKED_CLOSED("41", "Not locked, door closed"),
        LOCKED_OPEN("50", "BLE locked, door open"),
        LOCKED_CLOSED("51", "BLE locked, door closed"),
        LOCKED_ERROR("58", "BLE locked, ext.button pressed"),
        TANLOCKED_OPEN("60", "TAN-locked, door open (owner only)"),
        TANLOCKED_CLOSED("61", "TAN-locked, door closed (TAN-openable)"),
        TANLOCKED_ERROR("68", "TAN-locked, ext.button pressed or TAN error"),
        TANLOCKED_KEYPAD("70", "TAN-locked, keypad entry in progress");

        companion object {
            fun parse(code: String?) = entries.firstOrNull { it.code == code } ?: UNKNOWN
        }
    }
}