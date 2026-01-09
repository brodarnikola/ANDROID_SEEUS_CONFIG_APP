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

package hr.sil.android.ble.scanner.scan_multi.properties.base

import hr.sil.android.ble.scanner.model.scan.BLERawScanResult
import hr.sil.android.ble.scanner.parser.btcore.BTCoreAdvertisement
import hr.sil.android.ble.scanner.scan_multi.model.BLEManufacturer

/**
 * @author mfatiga
 */
abstract class BLEAdvProperties {
    companion object {
        //util
        internal fun bytes(intRange: IntRange): IntArray = intRange.toList().toIntArray()

        internal fun bytes(startIndex: Int, endIndex: Int): IntArray =
                (startIndex..endIndex).toList().toIntArray()

        internal fun bytes(indices: IntArray): IntArray = indices

        internal fun byte(index: Int): IntArray = intArrayOf(index)

        internal fun packets(vararg packetCodes: Byte): ByteArray = packetCodes

        internal fun packets(vararg packetCodes: Int): ByteArray =
                packetCodes.map { it.toByte() }.toByteArray()

        internal fun packet(packetCode: Byte): ByteArray = byteArrayOf(packetCode)

        internal fun packet(packetCode: Int): ByteArray = byteArrayOf(packetCode.toByte())
    }

    private val parserForParserReference = mutableMapOf<Int, BLEAdvProperty<*>>()
    private val packetsMapForParserReference = mutableMapOf<Int, Map<Byte, IntArray>>()

    private var autoReference: Int = -1
    private fun nextReference(): Int {
        autoReference++
        return autoReference
    }

    private fun resetReference() {
        autoReference = -1
    }

    //config
    protected fun <T> registerParser(packetsMap: Map<Byte, IntArray>, parser: BLEAdvProperty<T>): BLEAdvProperty<T> {
        val parserReference = nextReference()
        parserForParserReference[parserReference] = parser
        packetsMapForParserReference[parserReference] = packetsMap
        return parser
    }

    protected fun <T> registerParser(mappings: List<Pair<ByteArray, IntArray>>, parser: BLEAdvProperty<T>): BLEAdvProperty<T> {
        val packetsMap = mutableMapOf<Byte, IntArray>()
        for ((packetCodes, indices) in mappings) {
            for (packetCode in packetCodes) {
                packetsMap[packetCode] = indices
            }
        }
        return registerParser(packetsMap, parser)
    }

    protected fun clearPropertyParsers() {
        resetReference()
        parserForParserReference.clear()
        packetsMapForParserReference.clear()
    }

    //parsing
    protected abstract fun getPacketCode(
            manufacturer: BLEManufacturer,
            rawScanResult: BLERawScanResult,
            btCoreAdvertisement: BTCoreAdvertisement): Byte?

    protected open fun beforeUpdate(
            rawScanResult: BLERawScanResult,
            btCoreAdvertisement: BTCoreAdvertisement) {
    }

    internal fun update(manufacturer: BLEManufacturer, rawScanResult: BLERawScanResult, btCoreAdvertisement: BTCoreAdvertisement) {
        beforeUpdate(rawScanResult, btCoreAdvertisement)

        val packetCode = getPacketCode(manufacturer, rawScanResult, btCoreAdvertisement)
        if (packetCode != null) {
            for ((parserReference, parser) in parserForParserReference) {
                val indices = packetsMapForParserReference[parserReference]?.get(packetCode)
                val bytes = indices?.map { rawScanResult.scanRecord[it] }?.toByteArray()
                parser.update(bytes)
            }
        }
    }
}