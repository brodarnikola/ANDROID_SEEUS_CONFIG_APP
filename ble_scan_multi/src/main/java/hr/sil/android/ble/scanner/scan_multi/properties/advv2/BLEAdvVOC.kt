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
import hr.sil.android.ble.scanner.scan_multi.properties.advv2.parsers.BLEAdvPropertyLoraSnr
import hr.sil.android.ble.scanner.scan_multi.properties.parsers.BLEAdvPropertyBool
import hr.sil.android.ble.scanner.scan_multi.properties.parsers.BLEAdvPropertyNumber
import hr.sil.android.ble.scanner.scan_multi.properties.parsers.BLEAdvPropertyTimestamp
import java.nio.ByteOrder

/**
 * @author mfatiga
 */
class BLEAdvVOC : BLEAdvV2Base() {
    //TLM1
    val gpsLatitude by registerParser(
            listOf(packet(PACKET_CODE_TLM1) to bytes(16, 19)),
            BLEAdvPropertyNumber.FloatingSingle(ByteOrder.LITTLE_ENDIAN, true) { it?.toDouble() })

    val gpsLongitude by registerParser(
            listOf(packet(PACKET_CODE_TLM1) to bytes(20, 23)),
            BLEAdvPropertyNumber.FloatingSingle(ByteOrder.LITTLE_ENDIAN, true) { it?.toDouble() })

    val gpsTimestamp by registerParser(
            listOf(packet(PACKET_CODE_TLM1) to bytes(24, 27)),
            BLEAdvPropertyTimestamp(ByteOrder.LITTLE_ENDIAN, true))

    val gpsAccuracy by registerParser(
            listOf(packet(PACKET_CODE_TLM1) to byte(28)),
            BLEAdvPropertyNumber.UInt8(false) { it })

    //TLM0
    val batteryVoltage by registerParser(
            listOf(packet(PACKET_CODE_TLM0) to byte(16)),
            BLEAdvPropertyNumber.UInt8(false) { (it ?: 0) * 0.02 })

    val occupantsEntered by registerParser(
            listOf(packet(PACKET_CODE_TLM0) to byte(17)),
            BLEAdvPropertyNumber.UInt8(false) { it })

    val occupantsLeft by registerParser(
            listOf(packet(PACKET_CODE_TLM0) to byte(18)),
            BLEAdvPropertyNumber.UInt8(false) { it })

    val loraPowered by registerParser(
            listOf(packet(PACKET_CODE_TLM0) to byte(19)),
            BLEAdvPropertyBool())

    val loraJoined by registerParser(
            listOf(packet(PACKET_CODE_TLM0) to byte(20)),
            BLEAdvPropertyBool())

    val loraQueueSize by registerParser(
            listOf(packet(PACKET_CODE_TLM0) to byte(21)),
            BLEAdvPropertyNumber.UInt8(false) { it })

    val rtcTimestamp by registerParser(
            listOf(packet(PACKET_CODE_TLM0) to bytes(22, 25)),
            BLEAdvPropertyTimestamp(ByteOrder.LITTLE_ENDIAN, true))

    val gpsPowered by registerParser(
            listOf(packet(PACKET_CODE_TLM0) to byte(26)),
            BLEAdvPropertyBool())

    val gpsFix by registerParser(
            listOf(packet(PACKET_CODE_TLM0) to byte(27)),
            BLEAdvPropertyNumber.UInt8(false) { it })

    val loraSNR by registerParser(
            listOf(packet(PACKET_CODE_TLM0) to byte(28)),
            BLEAdvPropertyLoraSnr())
}