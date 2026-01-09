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

package hr.sil.android.ble.scanner.scan_multi.properties.advv3

import hr.sil.android.ble.scanner.model.scan.BLERawScanResult
import hr.sil.android.ble.scanner.parser.btcore.BTCoreAdvertisement
import hr.sil.android.ble.scanner.scan_multi.dynamic.BLEDynamicParser
import hr.sil.android.ble.scanner.scan_multi.dynamic.model.DynamicParserDefinition
import hr.sil.android.ble.scanner.scan_multi.dynamic.model.DynamicParserField
import hr.sil.android.ble.scanner.scan_multi.model.BLEManufacturer
import hr.sil.android.ble.scanner.scan_multi.properties.advv3.util.FieldPacketMapping
import hr.sil.android.ble.scanner.scan_multi.properties.base.BLEAdvProperties
import java.util.concurrent.ConcurrentHashMap

/**
 * @author mfatiga
 */
class BLEAdvDynamic : BLEAdvProperties() {
    //parser definition get and update
    private var lastParserUpdate: Long = 0
    private var mParserDefinition: DynamicParserDefinition? = null
    private fun getParserDefinition(
            rawScanResult: BLERawScanResult
    ): Pair<DynamicParserDefinition?, Boolean> {
        var didUpdate = false
        //lazily fetch parser definition if it has been set and/or updated
        if (lastParserUpdate != BLEDynamicParser.definitionsUpdateTimestamp) {
            mParserDefinition = BLEDynamicParser.findDefinition(rawScanResult)
            lastParserUpdate = BLEDynamicParser.definitionsUpdateTimestamp
            didUpdate = true
        }
        return Pair(mParserDefinition, didUpdate)
    }

    //extracted parser utility fields
    private var realPacketCodeIndex: Int = -1

    //extract packet code from scan bytes using the offset packet byte index
    private fun extractPacketCode(rawScanResult: BLERawScanResult): Byte? {
        val index = realPacketCodeIndex
        return mParserDefinition?.let {
            if (it.indexValid(index, rawScanResult)) {
                rawScanResult.scanRecord[index]
            } else null
        }
    }

    //tx power
    internal fun extractTxPower(rawScanResult: BLERawScanResult): Int? {
        checkRebuildParsers(rawScanResult)

        //get definition
        val definition = mParserDefinition ?: return null

        //get packet code
        val packetCode = extractPacketCode(rawScanResult) ?: return null

        //get first matching tx-power definition
        val txPowerDefinition = definition
                .txPower
                .firstOrNull {
                    it.packetCodes.contains(packetCode)
                } ?: return null

        //convert index
        val txPowerIndex = definition.rawIndex(txPowerDefinition.index)
        if (!definition.indexValid(txPowerIndex, rawScanResult)) {
            return null
        }

        //return result
        return rawScanResult.scanRecord[txPowerIndex].toInt()
    }

    //re-register parsers
    private val properties = ConcurrentHashMap<String, BLEAdvPropertyDynamic>()

    private fun isFieldDefinitionValid(fieldDefinition: DynamicParserField): Boolean {
        if (fieldDefinition.key.isBlank()) return false
        if (fieldDefinition.type == null) return false
        return true
    }

    private fun rebuildPropertyParsers(parserDefinition: DynamicParserDefinition?) {
        if (parserDefinition != null) {
            for (fieldDefinition in parserDefinition.fields) {
                //skip if field is invalid
                if (!isFieldDefinitionValid(fieldDefinition)) continue

                //build packet mappings
                val packetMappings = mutableListOf<FieldPacketMapping>()
                if (fieldDefinition.packets.isNotEmpty()) {
                    //different indices in different packets
                    for (packet in fieldDefinition.packets) {
                        val mapping = FieldPacketMapping.create(parserDefinition, packet.codes, packet.index)
                        if (mapping.isValid) {
                            packetMappings.add(mapping)
                        }
                    }
                } else {
                    //same indices in different packets
                    val mapping = FieldPacketMapping.create(parserDefinition, fieldDefinition.packetCodesHex, fieldDefinition.index)
                    if (mapping.isValid) {
                        packetMappings.add(mapping)
                    }
                }

                if (packetMappings.isNotEmpty()) {
                    //create property
                    val prop = BLEAdvPropertyDynamic(fieldDefinition)
                    registerParser(packetMappings.map { Pair(it.packetCodes, it.valueIndices) }, prop)
                    properties[fieldDefinition.key] = prop
                }
            }
        }
    }

    private fun checkRebuildParsers(rawScanResult: BLERawScanResult) {
        val (parserDefinition, didParserUpdate) = getParserDefinition(rawScanResult)
        if (didParserUpdate) {
            //rebuild packet code index
            realPacketCodeIndex = parserDefinition?.rawIndex(parserDefinition.packetCodeIndex) ?: -1

            //property parsers
            clearPropertyParsers()
            properties.clear()
            rebuildPropertyParsers(parserDefinition)
        }
    }

    override fun beforeUpdate(rawScanResult: BLERawScanResult, btCoreAdvertisement: BTCoreAdvertisement) {
        checkRebuildParsers(rawScanResult)
    }

    override fun getPacketCode(manufacturer: BLEManufacturer, rawScanResult: BLERawScanResult, btCoreAdvertisement: BTCoreAdvertisement): Byte? {
        return if (manufacturer.isSIL() && !manufacturer.isSILBootloader()) {
            extractPacketCode(rawScanResult)
        } else null
    }

    //util
    val definition: DynamicParserDefinition?
        get() = mParserDefinition

    //map-like interface
    val size: Int
        get() = properties.size

    fun isEmpty() = properties.isEmpty()

    fun isNotEmpty() = properties.isNotEmpty()

    val entries: Set<Map.Entry<String, BLEAdvPropertyDynamic>>
        get() = properties.entries

    val keys: Set<String>
        get() = properties.keys

    val values: Collection<BLEAdvPropertyDynamic>
        get() = properties.values

    //map-like interface operators
    operator fun get(fieldKey: String) = properties[fieldKey]

    operator fun contains(fieldKey: String) = properties.containsKey(fieldKey)
}