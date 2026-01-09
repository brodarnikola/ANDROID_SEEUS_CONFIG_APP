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

import android.graphics.Color
import hr.sil.android.ble.scanner.model.scan.BLERawScanResult
import hr.sil.android.ble.scanner.scan_multi.util.extensions.hexCleanToBytes
import java.util.*

/**
 * @author mfatiga
 */
class DynamicParserDefinition(
        //define
        var key: String = "",
        var offset: Int = 0,
        var deviceTypeIndices: Array<Int> = arrayOf(),
        var deviceTypeHex: String = "",
        var packetCodeIndex: Int = -1,

        //display
        var name: String = "",
        var color: String = "",

        //parse
        var txPower: Array<DynamicParserTxPower> = arrayOf(),
        var fields: Array<DynamicParserField> = arrayOf()
) {
    //property utils
    val deviceTypeCodes: Array<Byte>
        get() = deviceTypeHex.hexCleanToBytes()

    fun parseColor(default: Int = Color.WHITE): Int {
        val colorString = color
        return if (colorString.isNotEmpty()) {
            try {
                Color.parseColor(colorString)
            } catch (exc: Exception) {
                default
            }
        } else default
    }

    //index utils
    internal fun rawIndex(definitionIndex: Int): Int =
            if (definitionIndex >= 0) offset + definitionIndex else -1

    private fun indexValid(index: Int?, scanRecord: ByteArray): Boolean =
            index != null && index >= 0 && index < scanRecord.size

    internal fun indexValid(index: Int?, rawScanResult: BLERawScanResult): Boolean =
            indexValid(index, rawScanResult.scanRecord)

    //pre-calculate device and packet type indices
    private val realDeviceTypeIndices: Array<Int>
        get() = deviceTypeIndices.map(::rawIndex).toTypedArray()

    //check if scan result bytes contain the defined device type bytes at defined indices
    fun matchesDefinition(rawScanResult: BLERawScanResult): Boolean {
        return if (indexValid(realDeviceTypeIndices.maxOrNull(), rawScanResult)) {
            val deviceTypeBytes = realDeviceTypeIndices
                    .map { rawScanResult.scanRecord[it] }
                    .toTypedArray()
            return deviceTypeBytes.contentEquals(deviceTypeCodes)
        } else false
    }
}