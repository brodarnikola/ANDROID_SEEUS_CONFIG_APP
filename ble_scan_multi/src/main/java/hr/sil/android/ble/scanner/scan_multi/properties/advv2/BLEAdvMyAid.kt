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

import hr.sil.android.ble.scanner.scan_multi.properties.advv2.base.BLEAdvV2GpsBase
import hr.sil.android.ble.scanner.scan_multi.properties.advv2.common.MyAidLEDStates
import hr.sil.android.ble.scanner.scan_multi.properties.parsers.BLEAdvPropertyBool
import hr.sil.android.ble.scanner.scan_multi.properties.parsers.BLEAdvPropertyNumber
import hr.sil.android.ble.scanner.scan_multi.properties.parsers.BLEAdvPropertyRaw
import java.nio.ByteOrder

/**
 * @author mfatiga
 */
class BLEAdvMyAid : BLEAdvV2GpsBase() {
    val ledStates by registerParser(
            listOf(packet(PACKET_CODE_TLM0) to bytes(16, 19)),
            BLEAdvPropertyRaw { MyAidLEDStates.create(it) })

    val uwbDistance by registerParser(
            listOf(packet(PACKET_CODE_TLM0) to bytes(20, 23)),
            BLEAdvPropertyNumber.FloatingSingle(ByteOrder.LITTLE_ENDIAN, true) { it })

    val uwbTimeSinceLastUpdate by registerParser(
            listOf(packet(PACKET_CODE_TLM0) to bytes(24, 27)),
            BLEAdvPropertyNumber.UInt32(ByteOrder.LITTLE_ENDIAN, true) { it })

    val uwbIsVisible by registerParser(
            listOf(packet(PACKET_CODE_TLM0) to byte(28)),
            BLEAdvPropertyBool())

    val gpsGroundSpeed by registerParser(
            listOf(packet(PACKET_CODE_TLM2) to bytes(8, 9)),
            BLEAdvPropertyNumber.Int16(ByteOrder.LITTLE_ENDIAN, true) { it })

    val headingOfMotion by registerParser(
            listOf(packet(PACKET_CODE_TLM2) to bytes(10, 11)),
            BLEAdvPropertyNumber.Int16(ByteOrder.LITTLE_ENDIAN, true) { it })

    val gpsSpeedAccuracy by registerParser(
            listOf(packet(PACKET_CODE_TLM2) to bytes(12, 13)),
            BLEAdvPropertyNumber.Int16(ByteOrder.LITTLE_ENDIAN, true) { it })

    val headingAccuracy by registerParser(
            listOf(packet(PACKET_CODE_TLM2) to bytes(14, 15)),
            BLEAdvPropertyNumber.Int16(ByteOrder.LITTLE_ENDIAN, true) { it })

    val headingOfVehicle by registerParser(
            listOf(packet(PACKET_CODE_TLM2) to bytes(16, 17)),
            BLEAdvPropertyNumber.Int16(ByteOrder.LITTLE_ENDIAN, true) { it })
}