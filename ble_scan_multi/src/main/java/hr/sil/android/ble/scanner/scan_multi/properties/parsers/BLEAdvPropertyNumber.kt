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

package hr.sil.android.ble.scanner.scan_multi.properties.parsers

import hr.sil.android.ble.scanner.scan_multi.properties.base.BLEAdvProperty
import hr.sil.android.ble.scanner.scan_multi.util.extensions.pad
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * @author mfatiga
 */
sealed class BLEAdvPropertyNumber<out R, out T>(protected val byteOrder: ByteOrder,
                                                private val validateBytes: Boolean,
                                                private val transform: (R?) -> T?) : BLEAdvProperty<T>() {

    protected abstract val byteSize: Int
    protected abstract fun parseValue(bytes: ByteArray): R?

    override fun parse(bytes: ByteArray): T? {
        val size = byteSize

        val isValid = !validateBytes || !(bytes.all { it == 0xFF.toByte() })
        val data = bytes.pad(size, 0x00.toByte(), byteOrder)

        val value = if (data.size == size && isValid) parseValue(data) else null

        return transform(value)
    }

    class Int64<out T>(byteOrder: ByteOrder, validateBytes: Boolean, transform: (Long?) -> T?) : BLEAdvPropertyNumber<Long, T>(byteOrder, validateBytes, transform) {
        override val byteSize: Int = 8

        override fun parseValue(bytes: ByteArray): Long? =
                ByteBuffer.wrap(bytes).order(byteOrder).getLong(0)
    }

    class UInt32<out T>(byteOrder: ByteOrder, validateBytes: Boolean, transform: (Long?) -> T?) : BLEAdvPropertyNumber<Long, T>(byteOrder, validateBytes, transform) {
        override val byteSize: Int = 8

        override fun parseValue(bytes: ByteArray): Long? =
                ByteBuffer.wrap(bytes).order(byteOrder).getLong(0)
    }

    class Int32<out T>(byteOrder: ByteOrder, validateBytes: Boolean, transform: (Int?) -> T?) : BLEAdvPropertyNumber<Int, T>(byteOrder, validateBytes, transform) {
        override val byteSize: Int = 4

        override fun parseValue(bytes: ByteArray): Int? =
                ByteBuffer.wrap(bytes).order(byteOrder).getInt(0)
    }

    class UInt16<out T>(byteOrder: ByteOrder, validateBytes: Boolean, transform: (Int?) -> T?) : BLEAdvPropertyNumber<Int, T>(byteOrder, validateBytes, transform) {
        override val byteSize: Int = 4

        override fun parseValue(bytes: ByteArray): Int? =
                ByteBuffer.wrap(bytes).order(byteOrder).getInt(0)
    }

    class Int16<out T>(byteOrder: ByteOrder, validateBytes: Boolean, transform: (Int?) -> T?) : BLEAdvPropertyNumber<Int, T>(byteOrder, validateBytes, transform) {
        override val byteSize: Int = 4

        override fun parseValue(bytes: ByteArray): Int? {
            val intValue = ByteBuffer.wrap(bytes).order(byteOrder).getInt(0)
            val signedMax = 0x7FFF
            val unsignedMax = 0xFFFF
            return if (intValue <= signedMax) intValue else (intValue - unsignedMax + 1) * -1
        }
    }

    class UInt8<out T>(validateBytes: Boolean, transform: (Int?) -> T?) : BLEAdvPropertyNumber<Int, T>(ByteOrder.BIG_ENDIAN, validateBytes, transform) {
        override val byteSize: Int = 4

        override fun parseValue(bytes: ByteArray): Int? =
                ByteBuffer.wrap(bytes).order(byteOrder).getInt(0)
    }

    class Int8<out T>(validateBytes: Boolean, transform: (Int?) -> T?) : BLEAdvPropertyNumber<Int, T>(ByteOrder.BIG_ENDIAN, validateBytes, transform) {
        override val byteSize: Int = 4

        override fun parseValue(bytes: ByteArray): Int? {
            val intValue = ByteBuffer.wrap(bytes).order(byteOrder).getInt(0)
            val signedMax = 0x7F
            val unsignedMax = 0xFF
            return if (intValue <= signedMax) intValue else (intValue - unsignedMax + 1) * -1
        }
    }

    class FloatingSingle<out T>(byteOrder: ByteOrder, validateBytes: Boolean, transform: (Float?) -> T?) : BLEAdvPropertyNumber<Float, T>(byteOrder, validateBytes, transform) {
        override val byteSize: Int = 4

        override fun parseValue(bytes: ByteArray): Float? {
            val result = ByteBuffer.wrap(bytes).order(byteOrder).getFloat(0)
            return if (!result.isNaN()) result else null
        }
    }
}