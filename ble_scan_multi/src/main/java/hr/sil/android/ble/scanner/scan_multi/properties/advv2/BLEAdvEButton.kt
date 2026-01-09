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
import hr.sil.android.ble.scanner.scan_multi.properties.parsers.BLEAdvPropertyHex
import hr.sil.android.ble.scanner.scan_multi.properties.parsers.BLEAdvPropertyNumber

/**
 * @author mfatiga
 */
class BLEAdvEButton : BLEAdvV2Base() {
    companion object {
        const val BATTERY_VOLTAGE_OFFSET = 0.0
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

    val stateCode by registerParser(
            listOf(packet(PACKET_CODE_TLM1) to byte(28)),
            BLEAdvPropertyHex { it })

    val state by registerParser(
            listOf(packet(PACKET_CODE_TLM1) to byte(28)),
            BLEAdvPropertyHex { EButtonState.parse(it) })

    enum class EButtonState(val code: String?, val description: String) {
        UNKNOWN(null, "unknown"),
        STANDBY("00", "Stand-By"),
        EMERGENCY("E0", "Emergency"),
        BUSY("F0", "Device busy");

        companion object {
            fun parse(code: String?) = entries.firstOrNull { it.code == code } ?: UNKNOWN
        }
    }
}