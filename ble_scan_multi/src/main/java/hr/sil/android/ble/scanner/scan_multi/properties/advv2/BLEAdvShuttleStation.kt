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
import hr.sil.android.ble.scanner.scan_multi.properties.advv2.common.MyAidLEDState
import hr.sil.android.ble.scanner.scan_multi.properties.advv2.parsers.BLEAdvPropertyLoraSnr
import hr.sil.android.ble.scanner.scan_multi.properties.parsers.BLEAdvPropertyBool
import hr.sil.android.ble.scanner.scan_multi.properties.parsers.BLEAdvPropertyNumber
import hr.sil.android.ble.scanner.scan_multi.properties.parsers.BLEAdvPropertyRaw
import hr.sil.android.ble.scanner.scan_multi.properties.parsers.BLEAdvPropertyString
import java.nio.ByteOrder
import java.nio.charset.StandardCharsets

/**
 * @author mfatiga
 */
class BLEAdvShuttleStation : BLEAdvV2Base() {
    //TLM0
    val locationId by registerParser(
            listOf(packets(PACKET_CODE_TLM0, PACKET_CODE_TLM1) to bytes(14, 17)),
            BLEAdvPropertyNumber.UInt32(ByteOrder.LITTLE_ENDIAN, true) { it })

    val deviceId by registerParser(
            listOf(packets(PACKET_CODE_TLM0, PACKET_CODE_TLM1) to bytes(18, 21)),
            BLEAdvPropertyNumber.UInt32(ByteOrder.LITTLE_ENDIAN, true) { it })

    val pairIdentifier by registerParser(
            listOf(packet(PACKET_CODE_TLM0) to byte(22)),
            BLEAdvPropertyString(charset = StandardCharsets.US_ASCII, reverseBytes = false))

    val configurationIdentifier by registerParser(
            listOf(packet(PACKET_CODE_TLM0) to byte(23)),
            BLEAdvPropertyNumber.UInt8(false) { it })

    val temperature by registerParser(
            listOf(packet(PACKET_CODE_TLM0) to bytes(24, 25)),
            BLEAdvPropertySensorTemperature())

    val humidity by registerParser(
            listOf(packet(PACKET_CODE_TLM0) to byte(26)),
            BLEAdvPropertySensorHumidity())

    val pressure by registerParser(
            listOf(packet(PACKET_CODE_TLM0) to bytes(27, 28)),
            BLEAdvPropertySensorPressure())

    //TLM1
    val loraPowered by registerParser(
            listOf(packet(PACKET_CODE_TLM1) to byte(22)),
            BLEAdvPropertyBool())

    val loraJoined by registerParser(
            listOf(packet(PACKET_CODE_TLM1) to byte(23)),
            BLEAdvPropertyBool())

    val loraQueueSize by registerParser(
            listOf(packet(PACKET_CODE_TLM1) to byte(24)),
            BLEAdvPropertyNumber.UInt8(false) { it })

    val loraSNR by registerParser(
            listOf(packet(PACKET_CODE_TLM1) to byte(25)),
            BLEAdvPropertyLoraSnr())

    val ledStopRequested by registerParser(
            listOf(packet(PACKET_CODE_TLM1) to byte(26)),
            BLEAdvPropertyRaw { MyAidLEDState.parse(it.firstOrNull() ?: 0x00.toByte()) })

    val ledDisability by registerParser(
            listOf(packet(PACKET_CODE_TLM1) to byte(27)),
            BLEAdvPropertyRaw { MyAidLEDState.parse(it.firstOrNull() ?: 0x00.toByte()) })

    val batteryVoltage by registerParser(
            listOf(packet(PACKET_CODE_TLM1) to byte(28)),
            BLEAdvPropertyNumber.UInt8(false) { (it ?: 0) * 0.02 })
}