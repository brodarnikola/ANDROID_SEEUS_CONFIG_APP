package hr.sil.android.rest.core

/**
 * @author mfatiga
 */
//convert hex string to byte array
fun String.hexToByteArray(): ByteArray {
    val chars = this.toCharArray()
    val result = ByteArray(this.length / 2)

    var i = 0
    while (i < this.length) {
        result[i / 2] = ((Character.digit(chars[i], 16) shl 4) + Character.digit(chars[i + 1], 16)).toByte()
        i += 2
    }
    return result
}

//convert bytes to hex string
fun Iterable<Byte>.toHexString() = this.toList().toByteArray().toHexString()

fun ByteArray.toHexString(): String {
    val hexArray = "0123456789ABCDEF".toCharArray()
    val result = CharArray(this.size * 2)
    for (i in 0..(this.size - 1)) {
        val v = this[i].toInt() and 0xFF
        result[i * 2] = hexArray[v ushr 4]
        result[i * 2 + 1] = hexArray[v and 0x0F]
    }
    return result.joinToString(separator = "", transform = Char::toString)
}

fun Int.toByteArray(size: Int = 4)
        = ((size - 1) downTo 0)
    .map { (this shr (it * 8)) and 0xFF }
    .map(Int::toByte)
    .toByteArray()

fun Long.toByteArray(size: Int = 8)
        = ((size - 1) downTo 0)
    .map { (this shr (it * 8)) and 0xFF }
    .map(Long::toByte)
    .toByteArray()

fun Iterable<Byte>.toInt() = this.toList().toByteArray().toInt()

fun ByteArray.toInt() =
    if (this.isNotEmpty()) {
        (0..(this.size - 1))
            .map { (this[it].toInt() and 0xFF) shl ((this.size - 1 - it) * 8) }
            .reduce { num, sum -> num + sum }
    } else 0

fun Iterable<Byte>.toLong() = this.toList().toByteArray().toLong()

fun ByteArray.toLong() =
    if (this.isNotEmpty()) {
        (0..(this.size - 1))
            .map { (this[it].toLong() and 0xFF) shl ((this.size - 1 - it) * 8) }
            .reduce { num, sum -> num + sum }
    } else 0L