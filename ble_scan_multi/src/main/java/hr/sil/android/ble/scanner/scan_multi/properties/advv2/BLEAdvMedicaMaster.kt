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
import hr.sil.android.ble.scanner.scan_multi.properties.parsers.BLEAdvPropertyBool
import hr.sil.android.ble.scanner.scan_multi.properties.parsers.BLEAdvPropertyNumber
import hr.sil.android.ble.scanner.scan_multi.properties.parsers.BLEAdvPropertyTimestamp
import java.nio.ByteOrder

/**
 * @author mfatiga
 */
class BLEAdvMedicaMaster : BLEAdvV2Base() {
    //TLM0
    val loraPowered by registerParser(
            listOf(packet(PACKET_CODE_TLM0) to byte(14)),
            BLEAdvPropertyBool())

    val loraJoined by registerParser(
            listOf(packet(PACKET_CODE_TLM0) to byte(15)),
            BLEAdvPropertyBool())

    val loraQueueSize by registerParser(
            listOf(packet(PACKET_CODE_TLM0) to byte(16)),
            BLEAdvPropertyNumber.UInt8(false) { it })

    val loraSNR by registerParser(
            listOf(packet(PACKET_CODE_TLM0) to byte(17)),
            BLEAdvPropertyLoraSnr())

    val nearbyDevices by registerParser(
            listOf(packet(PACKET_CODE_TLM0) to byte(18)),
            BLEAdvPropertyNumber.UInt8(false) { it })

    //TLM1
    val rtcTimestamp by registerParser(
            listOf(packet(PACKET_CODE_TLM1) to bytes(14, 17)),
            BLEAdvPropertyTimestamp(ByteOrder.LITTLE_ENDIAN, true))

    val temperature by registerParser(
            listOf(packet(PACKET_CODE_TLM1) to bytes(18, 19)),
            BLEAdvPropertySensorTemperature())

    val humidity by registerParser(
            listOf(packet(PACKET_CODE_TLM1) to byte(20)),
            BLEAdvPropertySensorHumidity())

    val pressure by registerParser(
            listOf(packet(PACKET_CODE_TLM1) to bytes(21, 22)),
            BLEAdvPropertySensorPressure())

    val batteryVoltage by registerParser(
            listOf(packet(PACKET_CODE_TLM1) to byte(23)),
            BLEAdvPropertyNumber.UInt8(false) { (it ?: 0) * 0.02 })
}