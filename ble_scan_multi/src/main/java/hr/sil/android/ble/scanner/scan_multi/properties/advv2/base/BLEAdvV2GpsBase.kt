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

package hr.sil.android.ble.scanner.scan_multi.properties.advv2.base

import hr.sil.android.ble.scanner.scan_multi.properties.parsers.BLEAdvPropertyNumber
import hr.sil.android.ble.scanner.scan_multi.properties.parsers.BLEAdvPropertyTimestamp
import java.nio.ByteOrder

/**
 * @author mfatiga
 */
abstract class BLEAdvV2GpsBase : BLEAdvV2Base() {
    val locationId by registerParser(commonLocationIdMapping(), commonLocationIdParser())
    val deviceId by registerParser(commonDeviceIdMapping(), commonDeviceIdParser())
    val definitionId by registerParser(commonDefinitionIdMapping(), commonDefinitionIdParser())
    val ownerId by registerParser(commonOwnerIdMapping(), commonOwnerIdParser())

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
}