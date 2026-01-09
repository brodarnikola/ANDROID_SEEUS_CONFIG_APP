/* SWISS INNOVATION LAB CONFIDENTIAL
*
* www.swissinnolab.com
* __________________________________________________________________________
*
* [2016] - [2017] Swiss Innovation Lab AG
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

package hr.sil.android.ble.scanner.parser.btcore

import java.util.*

/**
 * @author mfatiga
 */
class BTCoreAdvertisement private constructor(val scanRecord: ByteArray) {
    companion object {
        fun create(scanRecord: ByteArray) = BTCoreAdvertisement(scanRecord)
    }

    val blocks: List<BTCoreAdvBlock> by lazy { parseBlocks() }

    private fun getInt(data: ByteArray): Int {
        return if (data.isNotEmpty()) {
            (data.indices)
                    .map { (data[it].toInt() and 0xFF) shl ((data.size - 1 - it) * 8) }
                    .reduce { num, sum -> num + sum }
        } else 0
    }

    private fun parseBlocks(): List<BTCoreAdvBlock> {
        var index = 0
        val parsedBlocks = mutableListOf<BTCoreAdvBlock>()
        while (index < scanRecord.size) {
            val length = getInt(byteArrayOf(scanRecord[index]))
            val data = scanRecord.drop(index + 1).take(length)

            val (dataType, value) = if (data.isNotEmpty()) {
                Pair(BTCoreDataType.parse(data[0]), data.drop(1).toByteArray())
            } else {
                Pair(BTCoreDataType.UNKNOWN, byteArrayOf())
            }
            parsedBlocks.add(BTCoreAdvBlock.create(
                    index = index,
                    length = length,
                    dataType = dataType,
                    value = value
            ))
            index += (length + 1)
        }
        return parsedBlocks
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BTCoreAdvertisement

        return scanRecord.contentEquals(other.scanRecord)
    }

    override fun hashCode(): Int = scanRecord.contentHashCode()
}