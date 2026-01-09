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

package hr.sil.android.ble.scanner.scan_multi.dynamic.model

/**
 * @author mfatiga
 */
data class DynamicParserField(
        //define
        var key: String = "",

        /**
         * Define position in packets
         * - same indices in different packets
         * - only used if "packets" is not defined
         **/
        var packetCodesHex: String = "",
        var index: Array<Int> = arrayOf(),

        /** Define position in packets:
         * - different indices in different packets
         * - has priority over "packetCodesHex" and "index"
         **/
        var packets: Array<DynamicParserPacket> = arrayOf(),

        //parse
        var type: DynamicParserFieldType? = null,

        //parse - numeric
        var multiplier: String? = null,
        var addend: String? = null,
        var addFirst: Boolean? = null,
        var math: String? = null,

        //parse - flag
        var maskHex: String? = null,

        //display
        var showInCompact: Boolean? = null,
        var sortPosition: Int? = null,
        var label: String = "",
        var prefix: String? = null,
        var suffix: String? = null,
        var measUnit: String? = null,
        var arraySeparator: String? = null,
        var precision: Int? = null,
        var enumerate: Array<DynamicParserFieldEnum>? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DynamicParserField

        if (key != other.key) return false
        if (packetCodesHex != other.packetCodesHex) return false
        if (!index.contentEquals(other.index)) return false
        if (!packets.contentEquals(other.packets)) return false
        if (type != other.type) return false
        if (multiplier != other.multiplier) return false
        if (addend != other.addend) return false
        if (addFirst != other.addFirst) return false
        if (math != other.math) return false
        if (maskHex != other.maskHex) return false
        if (showInCompact != other.showInCompact) return false
        if (sortPosition != other.sortPosition) return false
        if (label != other.label) return false
        if (prefix != other.prefix) return false
        if (suffix != other.suffix) return false
        if (measUnit != other.measUnit) return false
        if (arraySeparator != other.arraySeparator) return false
        if (precision != other.precision) return false

        val thisEnumerate = enumerate
        val otherEnumerate = other.enumerate
        if (thisEnumerate != null) {
            if (otherEnumerate == null) return false
            if (!thisEnumerate.contentEquals(otherEnumerate)) return false
        } else if (other.enumerate != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = key.hashCode()
        result = 31 * result + packetCodesHex.hashCode()
        result = 31 * result + index.contentHashCode()
        result = 31 * result + packets.contentHashCode()
        result = 31 * result + (type?.hashCode() ?: 0)
        result = 31 * result + (multiplier?.hashCode() ?: 0)
        result = 31 * result + (addend?.hashCode() ?: 0)
        result = 31 * result + (addFirst?.hashCode() ?: 0)
        result = 31 * result + (math?.hashCode() ?: 0)
        result = 31 * result + (maskHex?.hashCode() ?: 0)
        result = 31 * result + (showInCompact?.hashCode() ?: 0)
        result = 31 * result + (sortPosition ?: 0)
        result = 31 * result + label.hashCode()
        result = 31 * result + (prefix?.hashCode() ?: 0)
        result = 31 * result + (suffix?.hashCode() ?: 0)
        result = 31 * result + (measUnit?.hashCode() ?: 0)
        result = 31 * result + (arraySeparator?.hashCode() ?: 0)
        result = 31 * result + (precision ?: 0)
        result = 31 * result + (enumerate?.contentHashCode() ?: 0)
        return result
    }
}