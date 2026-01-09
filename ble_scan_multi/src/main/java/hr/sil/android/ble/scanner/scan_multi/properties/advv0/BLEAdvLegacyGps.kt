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

package hr.sil.android.ble.scanner.scan_multi.properties.advv0

import hr.sil.android.ble.scanner.scan_multi.properties.advv0.base.BLEAdvV0Base
import hr.sil.android.ble.scanner.scan_multi.properties.advv0.parsers.BLEAdvPropertyLegacyGpsLatLng
import hr.sil.android.ble.scanner.scan_multi.properties.parsers.BLEAdvPropertyNumber

/**
 * @author mfatiga
 */
class BLEAdvLegacyGps : BLEAdvV0Base() {
    //ex-rotating SR gps-data
    val gpsLatitude by registerParser(
            listOf(packet(PACKET_CODE_GPS_DATA) to bytes(50, 53)),
            BLEAdvPropertyLegacyGpsLatLng())

    val gpsLongitude by registerParser(
            listOf(packet(PACKET_CODE_GPS_DATA) to bytes(54, 57)),
            BLEAdvPropertyLegacyGpsLatLng())

    val gpsSpeed by registerParser(
            listOf(packet(PACKET_CODE_GPS_DATA) to byte(58)),
            BLEAdvPropertyNumber.UInt8(false) { it })

    val gpsAzimuth by registerParser(
            listOf(packet(PACKET_CODE_GPS_DATA) to byte(59)),
            BLEAdvPropertyNumber.UInt8(false) { it })
}