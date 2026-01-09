package hr.sil.android.ble.scanner.scan_multi.properties.advv3.util

import hr.sil.android.ble.scanner.scan_multi.dynamic.model.DynamicParserDefinition
import hr.sil.android.ble.scanner.scan_multi.util.extensions.hexCleanToBytes

/**
 * @author mfatiga
 */
data class FieldPacketMapping(val packetCodes: ByteArray, val valueIndices: IntArray) {
    companion object {
        fun create(parserDefinition: DynamicParserDefinition, codes: String, index: Array<Int>): FieldPacketMapping {
            val packetCodes = codes.hexCleanToBytes().toByteArray()
            val valueIndices = index
                    .map { parserDefinition.rawIndex(it) }
                    .toTypedArray()
                    .toIntArray()
            return FieldPacketMapping(packetCodes, valueIndices)
        }
    }

    val isValid = packetCodes.isNotEmpty() && valueIndices.isNotEmpty() && valueIndices.all { it >= 0 }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FieldPacketMapping

        if (!packetCodes.contentEquals(other.packetCodes)) return false
        if (!valueIndices.contentEquals(other.valueIndices)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = packetCodes.contentHashCode()
        result = 31 * result + valueIndices.contentHashCode()
        return result
    }
}