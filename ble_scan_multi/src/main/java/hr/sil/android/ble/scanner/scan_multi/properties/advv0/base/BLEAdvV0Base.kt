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

package hr.sil.android.ble.scanner.scan_multi.properties.advv0.base

import hr.sil.android.ble.scanner.model.scan.BLERawScanResult
import hr.sil.android.ble.scanner.parser.btcore.BTCoreAdvertisement
import hr.sil.android.ble.scanner.scan_multi.model.BLEManufacturer
import hr.sil.android.ble.scanner.scan_multi.properties.base.BLEAdvProperties
import hr.sil.android.ble.scanner.scan_multi.properties.parsers.BLEAdvPropertyHex
import hr.sil.android.ble.scanner.scan_multi.properties.parsers.BLEAdvPropertyNumber
import hr.sil.android.ble.scanner.scan_multi.properties.parsers.BLEAdvPropertyUUID
import java.nio.ByteOrder

/**
 * @author mfatiga
 */
abstract class BLEAdvV0Base : BLEAdvProperties() {
    companion object {
        const val PACKET_CODE_BEACON_DATA = 0x42.toByte()
        const val PACKET_CODE_GPS_DATA = 0x47.toByte()
        const val PACKET_CODE_ARBITRARY_DATA = 0x41.toByte()
    }

    override fun getPacketCode(manufacturer: BLEManufacturer, rawScanResult: BLERawScanResult, btCoreAdvertisement: BTCoreAdvertisement): Byte? =
            rawScanResult.scanRecord[33]

    //base iBeacon identity
    val uuid by registerParser(
            listOf(packets(PACKET_CODE_BEACON_DATA, PACKET_CODE_GPS_DATA, PACKET_CODE_ARBITRARY_DATA) to bytes(9, 24)),
            BLEAdvPropertyUUID())

    val major by registerParser(
            listOf(packets(PACKET_CODE_BEACON_DATA, PACKET_CODE_GPS_DATA, PACKET_CODE_ARBITRARY_DATA) to bytes(25, 26)),
            BLEAdvPropertyNumber.UInt16(ByteOrder.BIG_ENDIAN, false) { it })

    val minor by registerParser(
            listOf(packets(PACKET_CODE_BEACON_DATA, PACKET_CODE_GPS_DATA, PACKET_CODE_ARBITRARY_DATA) to bytes(27, 28)),
            BLEAdvPropertyNumber.UInt16(ByteOrder.BIG_ENDIAN, false) { it })

    //ex-rotating SR beacon-data
    val firmwareVersion by registerParser(
            listOf(packet(PACKET_CODE_BEACON_DATA) to byte(50)),
            BLEAdvPropertyNumber.UInt8(false) { it })

    val features by registerParser(
            listOf(packet(PACKET_CODE_BEACON_DATA) to bytes(51, 52)),
            BLEAdvPropertyHex({ it }))

    val battery by registerParser(
            listOf(packet(PACKET_CODE_BEACON_DATA) to byte(53)),
            BLEAdvPropertyNumber.UInt8(false) { it })

    val temperature by registerParser(
            listOf(packet(PACKET_CODE_BEACON_DATA) to byte(54)),
            BLEAdvPropertyNumber.UInt8(false) {
                if (it != null && it > 0) it - 127 else null
            })
}
