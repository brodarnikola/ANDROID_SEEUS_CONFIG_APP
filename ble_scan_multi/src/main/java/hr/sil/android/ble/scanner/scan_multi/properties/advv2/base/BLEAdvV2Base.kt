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

import hr.sil.android.ble.scanner.model.scan.BLERawScanResult
import hr.sil.android.ble.scanner.parser.btcore.BTCoreAdvertisement
import hr.sil.android.ble.scanner.scan_multi.model.BLEManufacturer
import hr.sil.android.ble.scanner.scan_multi.properties.base.BLEAdvProperties
import hr.sil.android.ble.scanner.scan_multi.properties.parsers.BLEAdvPropertyFirmwareVersion
import hr.sil.android.ble.scanner.scan_multi.properties.parsers.BLEAdvPropertyNumber
import java.nio.ByteOrder

/**
 * @author mfatiga
 */
abstract class BLEAdvV2Base : BLEAdvProperties() {
    companion object {
        //ex-SECURITY
        const val PACKET_CODE_TLM0 = 0x73.toByte()
        //ex-TELEMETRY
        const val PACKET_CODE_TLM1 = 0x74.toByte()
        //new-TLM2
        const val PACKET_CODE_TLM2 = 0x75.toByte()
        //new-TLM3
        const val PACKET_CODE_TLM3 = 0x76.toByte()

        fun commonLocationIdMapping() = listOf(packets(PACKET_CODE_TLM1, PACKET_CODE_TLM0) to bytes(8, 11))
        fun commonLocationIdParser() =  BLEAdvPropertyNumber.UInt32(ByteOrder.BIG_ENDIAN, false) { it }

        fun commonDeviceIdMapping() = listOf(packets(PACKET_CODE_TLM1, PACKET_CODE_TLM0) to bytes(12, 15))
        fun commonDeviceIdParser() =  BLEAdvPropertyNumber.UInt32(ByteOrder.BIG_ENDIAN, false) { it }

        fun commonDefinitionIdMapping() = listOf(packets(PACKET_CODE_TLM1, PACKET_CODE_TLM0) to bytes(44, 47))
        fun commonDefinitionIdParser() =  BLEAdvPropertyNumber.UInt32(ByteOrder.BIG_ENDIAN, false) { it }

        fun commonOwnerIdMapping() = listOf(packets(PACKET_CODE_TLM1, PACKET_CODE_TLM0) to bytes(48, 49))
        fun commonOwnerIdParser() =  BLEAdvPropertyNumber.UInt32(ByteOrder.BIG_ENDIAN, false) { it }
    }

    override fun getPacketCode(manufacturer: BLEManufacturer, rawScanResult: BLERawScanResult, btCoreAdvertisement: BTCoreAdvertisement): Byte? =
            rawScanResult.scanRecord[7]

    val firmwareVersion by registerParser(
            listOf(packets(PACKET_CODE_TLM1, PACKET_CODE_TLM0) to bytes(50, 53)),
            BLEAdvPropertyFirmwareVersion())
}