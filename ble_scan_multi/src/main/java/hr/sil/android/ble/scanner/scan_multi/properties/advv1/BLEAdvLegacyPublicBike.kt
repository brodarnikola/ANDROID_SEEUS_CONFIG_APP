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
import hr.sil.android.ble.scanner.scan_multi.properties.parsers.BLEAdvPropertyTimestamp
import java.nio.ByteOrder

/**
 * @author mfatiga
 */
class BLEAdvLegacyPublicBike : BLEAdvV1Base() {
    //GPS
    val gpsLatitude by registerParser(
            listOf(packet(PACKET_CODE_TELEMETRY) to bytes(12, 15)),
            BLEAdvPropertyNumber.FloatingSingle(ByteOrder.LITTLE_ENDIAN, true) { it?.toDouble() })

    val gpsLongitude by registerParser(
            listOf(packet(PACKET_CODE_TELEMETRY) to bytes(16, 19)),
            BLEAdvPropertyNumber.FloatingSingle(ByteOrder.LITTLE_ENDIAN, true) { it?.toDouble() })

    val gpsTimestamp by registerParser(
            listOf(packet(PACKET_CODE_TELEMETRY) to bytes(20, 23)),
            BLEAdvPropertyTimestamp(ByteOrder.LITTLE_ENDIAN, true))

    //SENSORS
    val temperature by registerParser(
            listOf(packet(PACKET_CODE_TELEMETRY) to bytes(24, 25)),
            BLEAdvPropertySensorTemperature())

    val humidity by registerParser(
            listOf(packet(PACKET_CODE_TELEMETRY) to byte(26)),
            BLEAdvPropertySensorHumidity())

    val pressure by registerParser(
            listOf(packet(PACKET_CODE_TELEMETRY) to bytes(27, 28)),
            BLEAdvPropertySensorPressure())

    val gpsAccuracy by registerParser(
            listOf(packet(PACKET_CODE_TELEMETRY) to byte(29)),
            BLEAdvPropertyNumber.UInt8(false) { it })
}