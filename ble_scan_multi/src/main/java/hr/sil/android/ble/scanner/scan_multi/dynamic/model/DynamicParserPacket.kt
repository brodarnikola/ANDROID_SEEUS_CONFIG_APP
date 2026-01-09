package hr.sil.android.ble.scanner.scan_multi.dynamic.model

/**
 * @author mfatiga
 */
data class DynamicParserPacket(
        var codes: String = "",
        var index: Array<Int> = arrayOf()) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DynamicParserPacket

        if (codes != other.codes) return false
        if (!index.contentEquals(other.index)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = codes.hashCode()
        result = 31 * result + index.contentHashCode()
        return result
    }
}