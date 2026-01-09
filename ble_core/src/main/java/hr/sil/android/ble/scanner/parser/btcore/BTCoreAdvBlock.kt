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
sealed class BTCoreAdvBlock(val index: Int, val length: Int, val dataType: BTCoreDataType, val value: ByteArray) {
    companion object {
        fun create(index: Int, length: Int, dataType: BTCoreDataType, value: ByteArray): BTCoreAdvBlock = when (dataType) {
            BTCoreDataType.FLAGS -> {
                Flags(index, length, dataType, value)
            }
            BTCoreDataType.MANUFACTURER_SPECIFIC_DATA -> {
                ManufacturerSpecificData(index, length, dataType, value)
            }
            BTCoreDataType.SHORTENED_LOCAL_NAME -> {
                LocalName(index, length, dataType, value)
            }
            BTCoreDataType.COMPLETE_LOCAL_NAME -> {
                LocalName(index, length, dataType, value)
            }
            else -> Other(index, length, dataType, value)
        }
    }

    fun isValid() = length == 1 + value.size

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BTCoreAdvBlock

        if (index != other.index) return false
        if (length != other.length) return false
        if (dataType != other.dataType) return false
        if (!value.contentEquals(other.value)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = index
        result = 31 * result + length
        result = 31 * result + dataType.hashCode()
        result = 31 * result + value.contentHashCode()
        return result
    }

    class Other(index: Int, length: Int, dataType: BTCoreDataType, value: ByteArray) : BTCoreAdvBlock(index, length, dataType, value)

    class Flags(index: Int, length: Int, dataType: BTCoreDataType, value: ByteArray) : BTCoreAdvBlock(index, length, dataType, value) {
        enum class Type(val mask: Int) {
            UNKNOWN(0x00000000),
            LIMITED_DISCOVERABLE(0x00000001),
            GENERAL_DISCOVERABLE(0x00000002),
            BR_EDR_NOT_SUPPORTED(0x00000004),
            SIMULTANEOUS_LE_AND_BR_EDR_CAPABLE_CTRL(0x00000008),
            SIMULTANEOUS_LE_AND_BR_EDR_CAPABLE_HOST(0x00000010)
        }

        val flags: List<Type> = if (value.isNotEmpty()) {
            value.last().let { byte ->
                val result = mutableListOf<Type>()

                if (byte.toInt() and Type.LIMITED_DISCOVERABLE.mask != 0)
                    result.add(Type.LIMITED_DISCOVERABLE)

                if (byte.toInt() and Type.GENERAL_DISCOVERABLE.mask != 0)
                    result.add(Type.GENERAL_DISCOVERABLE)

                if (byte.toInt() and Type.BR_EDR_NOT_SUPPORTED.mask != 0)
                    result.add(Type.BR_EDR_NOT_SUPPORTED)

                if (byte.toInt() and Type.SIMULTANEOUS_LE_AND_BR_EDR_CAPABLE_CTRL.mask != 0)
                    result.add(Type.SIMULTANEOUS_LE_AND_BR_EDR_CAPABLE_CTRL)

                if (byte.toInt() and Type.SIMULTANEOUS_LE_AND_BR_EDR_CAPABLE_HOST.mask != 0)
                    result.add(Type.SIMULTANEOUS_LE_AND_BR_EDR_CAPABLE_HOST)

                if (result.isEmpty()) {
                    result.add(Type.UNKNOWN)
                }

                result
            }
        } else listOf()
    }

    class ManufacturerSpecificData(index: Int, length: Int, dataType: BTCoreDataType, value: ByteArray) : BTCoreAdvBlock(index, length, dataType, value) {
        val companyIdentifier: ByteArray = if (value.size >= 2) value.take(2).reversed().toByteArray() else byteArrayOf()
        val data: ByteArray = if (value.size > 2) value.drop(2).toByteArray() else byteArrayOf()
    }

    class LocalName(index: Int, length: Int, dataType: BTCoreDataType, value: ByteArray) : BTCoreAdvBlock(index, length, dataType, value) {
        val name: String = value.joinToString("") { it.toInt().toChar().toString() }
    }
}