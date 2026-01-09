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

package hr.sil.android.ble.scanner.scan_multi.util.extensions

import java.nio.ByteOrder

//constant
private val hexArray = "0123456789ABCDEF".toCharArray()

/**
 * @author mfatiga
 */
internal fun ByteArray.padStart(size: Int, padByte: Byte): ByteArray {
    return if (this.size < size) {
        val pad = ByteArray(size - this.size) { padByte }
        pad + this
    } else this
}

/**
 * @author mfatiga
 */
internal fun ByteArray.padEnd(size: Int, padByte: Byte): ByteArray {
    return if (this.size < size) {
        val pad = ByteArray(size - this.size) { padByte }
        this + pad
    } else this
}

/**
 * @author mfatiga
 */
internal fun ByteArray.pad(size: Int, padByte: Byte, byteOrder: ByteOrder): ByteArray {
    return if (byteOrder == ByteOrder.BIG_ENDIAN) {
        this.padStart(size, padByte)
    } else {
        this.padEnd(size, padByte)
    }
}

/**
 * @author mfatiga
 */
internal fun Byte.toHexString(): String {
    val v = this.toInt() and 0xFF
    return charArrayOf(hexArray[v ushr 4], hexArray[v and 0x0F]).joinToString("") { it.toString() }.toString()
}

/**
 * @author mfatiga
 */
internal fun ByteArray.toHexString(): String {
    val result = CharArray(this.size * 2)
    for (i in this.indices) {
        val v = this[i].toInt() and 0xFF
        result[i * 2] = hexArray[v ushr 4]
        result[i * 2 + 1] = hexArray[v and 0x0F]
    }
    return result.joinToString(separator = "", transform = Char::toString)
}

/**
 * @author mfatiga
 */
internal fun String.hexToByteArray(): ByteArray {
    val chars = this.toCharArray()
    val result = ByteArray(this.length / 2)

    var i = 0
    while (i < this.length) {
        result[i / 2] = ((Character.digit(chars[i], 16) shl 4) + Character.digit(chars[i + 1], 16)).toByte()
        i += 2
    }
    return result
}

/**
 * @author mfatiga
 */
internal fun Int.toByteArray(size: Int = 4)
        = ((size - 1) downTo 0)
        .map { (this shr (it * 8)) and 0xFF }
        .map(Int::toByte)
        .toByteArray()

/**
 * @author mfatiga
 */
internal fun Long.toByteArray(size: Int = 8)
        = ((size - 1) downTo 0)
        .map { (this shr (it * 8)) and 0xFF }
        .map(Long::toByte)
        .toByteArray()

/**
 * @author mfatiga
 */
internal fun Iterable<Byte>.toInt() = this.toList().toByteArray().toInt()

/**
 * @author mfatiga
 */
internal fun ByteArray.toInt() =
        if (this.isNotEmpty()) {
            this.indices
                .map { (this[it].toInt() and 0xFF) shl ((this.size - 1 - it) * 8) }
                .reduce { num, sum -> num + sum }
        } else 0

/**
 * @author mfatiga
 */
internal fun Iterable<Byte>.toLong() = this.toList().toByteArray().toLong()

/**
 * @author mfatiga
 */
internal fun ByteArray.toLong() =
        if (this.isNotEmpty()) {
            this.indices
                .map { (this[it].toLong() and 0xFF) shl ((this.size - 1 - it) * 8) }
                .reduce { num, sum -> num + sum }
        } else 0L