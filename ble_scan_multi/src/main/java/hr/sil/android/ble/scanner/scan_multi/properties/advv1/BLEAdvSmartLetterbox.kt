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

package hr.sil.android.ble.scanner.scan_multi.properties.advv1

import hr.sil.android.ble.scanner.scan_multi.properties.advv1.base.BLEAdvV1Base
import hr.sil.android.ble.scanner.scan_multi.properties.advv1.parsers.BLEAdvPropertySensorHumidity
import hr.sil.android.ble.scanner.scan_multi.properties.advv1.parsers.BLEAdvPropertySensorPressure
import hr.sil.android.ble.scanner.scan_multi.properties.advv1.parsers.BLEAdvPropertySensorTemperature
import hr.sil.android.ble.scanner.scan_multi.properties.parsers.BLEAdvPropertyNumber
import java.nio.ByteOrder

/**
 * @author mfatiga
 */
class BLEAdvSmartLetterbox : BLEAdvV1Base() {
    companion object {
        const val DAB_BATTERY_VOLTAGE_OFFSET = 3.0
    }

    val lockedStatus by registerParser(
            listOf(packet(PACKET_CODE_TELEMETRY) to byte(12)),
            BLEAdvPropertyNumber.UInt8(false, { it == 0 }))

    val temperature by registerParser(
            listOf(packet(PACKET_CODE_TELEMETRY) to bytes(13, 14)),
            BLEAdvPropertySensorTemperature())

    val humidity by registerParser(
            listOf(packet(PACKET_CODE_TELEMETRY) to byte(15)),
            BLEAdvPropertySensorHumidity())

    val pressure by registerParser(
            listOf(packet(PACKET_CODE_TELEMETRY) to bytes(16, 17)),
            BLEAdvPropertySensorPressure())

    val batteryVoltage by registerParser(
            listOf(packet(PACKET_CODE_TELEMETRY) to byte(18)),
            BLEAdvPropertyNumber.UInt8(false) { (it ?: 0) * 0.02 })

    val noiseLevel by registerParser(
            listOf(packet(PACKET_CODE_TELEMETRY) to bytes(19, 20)),
            BLEAdvPropertyNumber.UInt16(ByteOrder.BIG_ENDIAN, false) { (it ?: 0) * 0.01 })

    val fillLevel by registerParser(
            listOf(packet(PACKET_CODE_TELEMETRY) to byte(21)),
            BLEAdvPropertyNumber.UInt8(false) { (it ?: 0) * 0.5 })

    val dataBlockCount by registerParser(
            listOf(packet(PACKET_CODE_TELEMETRY) to bytes(22, 25)),
            BLEAdvPropertyNumber.UInt32(ByteOrder.BIG_ENDIAN, false) { it })

    val dabBatteryVoltage by registerParser(
            listOf(packet(PACKET_CODE_TELEMETRY) to byte(26)),
            BLEAdvPropertyNumber.UInt8(false) {
                val readValue = (it ?: 0) * 0.02
                if (readValue == 0.0) readValue else readValue + DAB_BATTERY_VOLTAGE_OFFSET
            })
}