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
import hr.sil.android.ble.scanner.scan_multi.properties.advv2.common.VABAccEvent
import hr.sil.android.ble.scanner.scan_multi.properties.advv2.common.VABAccLogicEvent
import hr.sil.android.ble.scanner.scan_multi.properties.advv2.common.VABEventType
import hr.sil.android.ble.scanner.scan_multi.properties.advv2.common.VABUwbEvent
import hr.sil.android.ble.scanner.scan_multi.properties.advv2.parsers.BLEAdvPropertyLoraSnr
import hr.sil.android.ble.scanner.scan_multi.properties.parsers.BLEAdvPropertyBool
import hr.sil.android.ble.scanner.scan_multi.properties.parsers.BLEAdvPropertyNumber
import hr.sil.android.ble.scanner.scan_multi.properties.parsers.BLEAdvPropertyRaw
import hr.sil.android.ble.scanner.scan_multi.properties.parsers.BLEAdvPropertyTimestamp
import java.nio.ByteOrder

/**
 * @author mfatiga
 */
class BLEAdvVABTag : BLEAdvV2Base() {
    //SR
    val definitionId by registerParser(
            listOf(packets(PACKET_CODE_TLM2, PACKET_CODE_TLM1, PACKET_CODE_TLM0) to bytes(44, 47)),
            commonDefinitionIdParser())

    val ownerId by registerParser(
            listOf(packets(PACKET_CODE_TLM2, PACKET_CODE_TLM1, PACKET_CODE_TLM0) to bytes(48, 49)),
            commonOwnerIdParser())

    // TLM0
    val locationId by registerParser(
            listOf(packet(PACKET_CODE_TLM0) to bytes(14, 17)),
            commonLocationIdParser())

    val deviceId by registerParser(
            listOf(packet(PACKET_CODE_TLM0) to bytes(18, 21)),
            commonDeviceIdParser())

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
            BLEAdvPropertyNumber.UInt8(false) { (it ?: 0) * 0.02 })

    val gpsFix by registerParser(
            listOf(packet(PACKET_CODE_TLM0) to byte(27)),
            BLEAdvPropertyNumber.UInt8(false) { it })

    val uwbInUse by registerParser(
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
    val eventType by registerParser(
            listOf(packet(PACKET_CODE_TLM2) to byte(14)),
            BLEAdvPropertyRaw { VABEventType.parse(it.firstOrNull()) })

    val eventTimestamp by registerParser(
            listOf(packet(PACKET_CODE_TLM2) to bytes(15, 18)),
            BLEAdvPropertyTimestamp(ByteOrder.LITTLE_ENDIAN, true))

    val uwbDistanceA by registerParser(
            listOf(packet(PACKET_CODE_TLM2) to bytes(19, 20)),
            BLEAdvPropertyNumber.UInt16(ByteOrder.LITTLE_ENDIAN, true) {
                if (it != null) it * 0.01 else null
            })

    val uwbDistanceB by registerParser(
            listOf(packet(PACKET_CODE_TLM2) to bytes(21, 22)),
            BLEAdvPropertyNumber.UInt16(ByteOrder.LITTLE_ENDIAN, true) {
                if (it != null) it * 0.01 else null
            })

    val uwbDistanceC by registerParser(
            listOf(packet(PACKET_CODE_TLM2) to bytes(23, 24)),
            BLEAdvPropertyNumber.UInt16(ByteOrder.LITTLE_ENDIAN, true) {
                if (it != null) it * 0.01 else null
            })

    val uwbDistanceD by registerParser(
            listOf(packet(PACKET_CODE_TLM2) to bytes(25, 26)),
            BLEAdvPropertyNumber.UInt16(ByteOrder.LITTLE_ENDIAN, true) {
                if (it != null) it * 0.01 else null
            })

    val gpsSpeedAccuracy by registerParser(
            listOf(packet(PACKET_CODE_TLM2) to bytes(27, 28)),
            BLEAdvPropertyNumber.Int16(ByteOrder.LITTLE_ENDIAN, true, { it }))

    //TLM3
    val accActive by registerParser(
            listOf(packet(PACKET_CODE_TLM3) to byte(14)),
            BLEAdvPropertyBool())

    val accEvent by registerParser(
            listOf(packet(PACKET_CODE_TLM3) to byte(15)),
            BLEAdvPropertyRaw { VABAccEvent.parse(it.firstOrNull()) })

    val accLogicActive by registerParser(
            listOf(packet(PACKET_CODE_TLM3) to byte(16)),
            BLEAdvPropertyBool())

    val accLogicEvent by registerParser(
            listOf(packet(PACKET_CODE_TLM3) to byte(17)),
            BLEAdvPropertyRaw { VABAccLogicEvent.parse(it.firstOrNull()) })

    val uwbLastEvent by registerParser(
            listOf(packet(PACKET_CODE_TLM3) to byte(18)),
            BLEAdvPropertyRaw { VABUwbEvent.parse(it.firstOrNull()) })
}