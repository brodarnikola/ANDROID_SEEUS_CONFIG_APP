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
import hr.sil.android.ble.scanner.scan_multi.properties.advv2.common.MyAidLEDStates
import hr.sil.android.ble.scanner.scan_multi.properties.parsers.BLEAdvPropertyNumber
import hr.sil.android.ble.scanner.scan_multi.properties.parsers.BLEAdvPropertyRaw
import java.nio.ByteOrder

/**
 * @author mfatiga
 */
class BLEAdvSorStation : BLEAdvV2Base() {
    val locationId by registerParser(commonLocationIdMapping(), commonLocationIdParser())
    val deviceId by registerParser(commonDeviceIdMapping(), commonDeviceIdParser())
    val definitionId by registerParser(commonDefinitionIdMapping(), commonDefinitionIdParser())
    val ownerId by registerParser(commonOwnerIdMapping(), commonOwnerIdParser())

    val batteryVoltage by registerParser(
            listOf(packet(PACKET_CODE_TLM1) to byte(16)),
            BLEAdvPropertyNumber.UInt8(false) { (it ?: 0) * 0.02 })

    val ledStates by registerParser(
            listOf(packet(PACKET_CODE_TLM1) to bytes(17, 20)),
            BLEAdvPropertyRaw { MyAidLEDStates.create(it) })

    val azimuthRangeBegin by registerParser(
            listOf(packet(PACKET_CODE_TLM1) to bytes(21, 24)),
            BLEAdvPropertyNumber.FloatingSingle(ByteOrder.LITTLE_ENDIAN, true) { it })

    val azimuthRangeEnd by registerParser(
            listOf(packet(PACKET_CODE_TLM1) to bytes(25, 28)),
            BLEAdvPropertyNumber.FloatingSingle(ByteOrder.LITTLE_ENDIAN, true) { it })

    val uwbDistance by registerParser(
            listOf(packet(PACKET_CODE_TLM0) to bytes(16, 19)),
            BLEAdvPropertyNumber.FloatingSingle(ByteOrder.LITTLE_ENDIAN, true) { it })

    val uwbTimeSinceLastUpdate by registerParser(
            listOf(packet(PACKET_CODE_TLM0) to bytes(20, 23)),
            BLEAdvPropertyNumber.UInt32(ByteOrder.LITTLE_ENDIAN, true) { it })
}