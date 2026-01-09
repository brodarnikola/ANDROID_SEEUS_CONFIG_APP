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

package hr.sil.android.ble.scanner.scan_multi.properties.advv1.base

import hr.sil.android.ble.scanner.model.scan.BLERawScanResult
import hr.sil.android.ble.scanner.parser.btcore.BTCoreAdvertisement
import hr.sil.android.ble.scanner.scan_multi.model.BLEManufacturer
import hr.sil.android.ble.scanner.scan_multi.properties.base.BLEAdvProperties
import hr.sil.android.ble.scanner.scan_multi.properties.parsers.BLEAdvPropertyFirmwareVersion
import hr.sil.android.ble.scanner.scan_multi.properties.parsers.BLEAdvPropertyHex
import hr.sil.android.ble.scanner.scan_multi.properties.parsers.BLEAdvPropertyNumber
import java.nio.ByteOrder

/**
 * @author mfatiga
 */
abstract class BLEAdvV1Base : BLEAdvProperties() {
    companion object {
        const val PACKET_CODE_SECURITY = 0x73.toByte()
        const val PACKET_CODE_TELEMETRY = 0x74.toByte()
    }

    override fun getPacketCode(manufacturer: BLEManufacturer, rawScanResult: BLERawScanResult, btCoreAdvertisement: BTCoreAdvertisement): Byte? =
            rawScanResult.scanRecord[7]

    val locationId by registerParser(
            listOf(packets(PACKET_CODE_TELEMETRY, PACKET_CODE_SECURITY) to bytes(8, 11)),
            BLEAdvPropertyNumber.UInt32(ByteOrder.BIG_ENDIAN, false) { it })

    val deviceId by registerParser(
            listOf(packets(PACKET_CODE_TELEMETRY, PACKET_CODE_SECURITY) to bytes(40, 43)),
            BLEAdvPropertyNumber.UInt32(ByteOrder.BIG_ENDIAN, false) { it })

    val firmwareVersion by registerParser(
            listOf(packets(PACKET_CODE_TELEMETRY, PACKET_CODE_SECURITY) to bytes(50, 51)),
            BLEAdvPropertyFirmwareVersion())

    val features by registerParser(
            listOf(packets(PACKET_CODE_TELEMETRY, PACKET_CODE_SECURITY) to bytes(52, 55)),
            BLEAdvPropertyHex { it })
}