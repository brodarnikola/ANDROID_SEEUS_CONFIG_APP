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

import hr.sil.android.ble.scanner.scan_multi.properties.advv2.base.BLEAdvV2Base
import hr.sil.android.ble.scanner.scan_multi.properties.advv2.common.PublicBikePowerMode
import hr.sil.android.ble.scanner.scan_multi.properties.advv2.parsers.BLEAdvPropertyLoraSnr
import hr.sil.android.ble.scanner.scan_multi.properties.parsers.BLEAdvPropertyBool
import hr.sil.android.ble.scanner.scan_multi.properties.parsers.BLEAdvPropertyNumber
import hr.sil.android.ble.scanner.scan_multi.properties.parsers.BLEAdvPropertyRaw
import hr.sil.android.ble.scanner.scan_multi.properties.parsers.BLEAdvPropertyTimestamp
import java.nio.ByteOrder

/**
 * @author mfatiga
 */
class BLEAdvPublicBike : BLEAdvV2Base() {
    // TLM0
    val loraNextEventTimer by registerParser(
            listOf(packet(PACKET_CODE_TLM0) to bytes(14, 17)),
            BLEAdvPropertyNumber.UInt32(ByteOrder.LITTLE_ENDIAN, true) { it })

    val loraLastTimestamp by registerParser(
            listOf(packet(PACKET_CODE_TLM0) to bytes(18, 21)),
            BLEAdvPropertyTimestamp(ByteOrder.LITTLE_ENDIAN, true))

    val loraPowered by registerParser(
            listOf(packet(PACKET_CODE_TLM0) to byte(22)),
            BLEAdvPropertyBool())

    val loraJoined by registerParser(
            listOf(packet(PACKET_CODE_TLM0) to byte(23)),
            BLEAdvPropertyBool())

    val loraQueueSize by registerParser(
            listOf(packet(PACKET_CODE_TLM0) to byte(24)),
            BLEAdvPropertyNumber.UInt8(false) { it })

    val loraSNR by registerParser(
            listOf(packet(PACKET_CODE_TLM0) to byte(25)),
            BLEAdvPropertyLoraSnr())

    val batteryVoltage by registerParser(
            listOf(packet(PACKET_CODE_TLM0) to byte(26)),
            BLEAdvPropertyNumber.UInt8(false) { ((it ?: 0) * 0.02) + 4.0 })

    val gpsFix by registerParser(
            listOf(packet(PACKET_CODE_TLM0) to byte(27)),
            BLEAdvPropertyNumber.UInt8(false) { it })

    val tamperSensor by registerParser(
            listOf(packet(PACKET_CODE_TLM0) to byte(28)),
            BLEAdvPropertyBool())

    // TLM1
    val gpsLatitude by registerParser(
            listOf(packet(PACKET_CODE_TLM1) to bytes(14, 17)),
            BLEAdvPropertyNumber.FloatingSingle(ByteOrder.LITTLE_ENDIAN, true) { it?.toDouble() })

    val gpsLongitude by registerParser(
            listOf(packet(PACKET_CODE_TLM1) to bytes(18, 21)),
            BLEAdvPropertyNumber.FloatingSingle(ByteOrder.LITTLE_ENDIAN, true) { it?.toDouble() })

    val gpsTimestamp by registerParser(
            listOf(packet(PACKET_CODE_TLM1) to bytes(22, 25)),
            BLEAdvPropertyTimestamp(ByteOrder.LITTLE_ENDIAN, true))

    val gpsAccuracy by registerParser(
            listOf(packet(PACKET_CODE_TLM1) to byte(26)),
            BLEAdvPropertyNumber.UInt8(false) { it })

    val gpsGroundSpeed by registerParser(
            listOf(packet(PACKET_CODE_TLM1) to bytes(27, 28)),
            BLEAdvPropertyNumber.Int16(ByteOrder.LITTLE_ENDIAN, true) { it })

    // TLM2
    val powerMode by registerParser(
            listOf(packet(PACKET_CODE_TLM2) to byte(14)),
            BLEAdvPropertyRaw { PublicBikePowerMode.parse(it.firstOrNull()) })

    val rtcTimestamp by registerParser(
            listOf(packet(PACKET_CODE_TLM2) to bytes(15, 18)),
            BLEAdvPropertyTimestamp(ByteOrder.LITTLE_ENDIAN, true))

    val accelX by registerParser(
            listOf(packet(PACKET_CODE_TLM2) to bytes(19, 20)),
            BLEAdvPropertyNumber.UInt16(ByteOrder.LITTLE_ENDIAN, false) { value ->
                value?.let { (it / 32.0f) - 4.0f }
            })

    val accelY by registerParser(
            listOf(packet(PACKET_CODE_TLM2) to bytes(21, 22)),
            BLEAdvPropertyNumber.UInt16(ByteOrder.LITTLE_ENDIAN, false) { value ->
                value?.let { (it / 32.0f) - 4.0f }
            })

    val accelZ by registerParser(
            listOf(packet(PACKET_CODE_TLM2) to bytes(23, 24)),
            BLEAdvPropertyNumber.UInt16(ByteOrder.LITTLE_ENDIAN, false) { value ->
                value?.let { (it / 32.0f) - 4.0f }
            })

    val lightIntensity by registerParser(
            listOf(packet(PACKET_CODE_TLM2) to bytes(25, 26)),
            BLEAdvPropertyNumber.UInt16(ByteOrder.LITTLE_ENDIAN, false) { it })

    val gpsSpeedAccuracy by registerParser(
            listOf(packet(PACKET_CODE_TLM2) to bytes(27, 28)),
            BLEAdvPropertyNumber.Int16(ByteOrder.LITTLE_ENDIAN, true) { it })

}