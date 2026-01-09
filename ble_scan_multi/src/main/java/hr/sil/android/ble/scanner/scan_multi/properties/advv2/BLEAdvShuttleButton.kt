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
import hr.sil.android.ble.scanner.scan_multi.properties.parsers.BLEAdvPropertyHex
import hr.sil.android.ble.scanner.scan_multi.properties.parsers.BLEAdvPropertyNumber
import hr.sil.android.ble.scanner.scan_multi.properties.parsers.BLEAdvPropertyString
import java.nio.charset.StandardCharsets
import java.util.*

/**
 * @author mfatiga
 */
class BLEAdvShuttleButton : BLEAdvV2Base() {
    //TLM0
    val bleStationMac by registerParser(
            listOf(packet(PACKET_CODE_TLM0) to bytes(14, 19)),
            BLEAdvPropertyHex { it?.chunked(2)?.joinToString(":")?.uppercase(Locale.getDefault()) })

    val temperature by registerParser(
            listOf(packet(PACKET_CODE_TLM0) to bytes(20, 21)),
            BLEAdvPropertySensorTemperature())

    val humidity by registerParser(
            listOf(packet(PACKET_CODE_TLM0) to byte(22)),
            BLEAdvPropertySensorHumidity())

    val pressure by registerParser(
            listOf(packet(PACKET_CODE_TLM0) to bytes(23, 24)),
            BLEAdvPropertySensorPressure())

    val batteryVoltage by registerParser(
            listOf(packet(PACKET_CODE_TLM0) to byte(25)),
            BLEAdvPropertyNumber.UInt8(false) { (it ?: 0) * 0.02 })

    val pairIdentifier by registerParser(
            listOf(packet(PACKET_CODE_TLM0) to byte(26)),
            BLEAdvPropertyString(charset = StandardCharsets.US_ASCII, reverseBytes = false))

    val configurationIdentifier by registerParser(
            listOf(packet(PACKET_CODE_TLM0) to byte(27)),
            BLEAdvPropertyNumber.UInt8(false) { it })
}