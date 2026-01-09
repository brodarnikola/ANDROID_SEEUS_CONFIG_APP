package hr.sil.android.ble.scanner.util

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