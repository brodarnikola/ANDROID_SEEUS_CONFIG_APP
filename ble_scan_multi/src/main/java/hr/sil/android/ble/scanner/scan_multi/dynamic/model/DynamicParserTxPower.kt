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

import hr.sil.android.ble.scanner.scan_multi.util.extensions.hexCleanToBytes

/**
 * @author mfatiga
 */
class DynamicParserTxPower(
        var packetCodesHex: String = "",
        var index: Int = -1
) {

    //util
    val packetCodes: Array<Byte>
        get() = packetCodesHex.hexCleanToBytes()

    //data class
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DynamicParserTxPower

        if (packetCodesHex != other.packetCodesHex) return false
        if (index != other.index) return false

        return true
    }

    override fun hashCode(): Int {
        var result = packetCodesHex.hashCode()
        result = 31 * result + index
        return result
    }
}