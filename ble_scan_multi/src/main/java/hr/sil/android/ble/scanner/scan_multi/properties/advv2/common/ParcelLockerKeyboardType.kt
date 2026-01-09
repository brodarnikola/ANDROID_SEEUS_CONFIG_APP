package hr.sil.android.ble.scanner.scan_multi.properties.advv2.common

/**
 * @author mfatiga
 */
enum class ParcelLockerKeyboardType(val code: Byte?) {
    UNKNOWN(null),
    SPL_PLUS(0x01.toByte()),
    SPL(0x02.toByte()),
    MPL(0x03.toByte());

    companion object {
        fun parse(code: Byte?) = entries.firstOrNull { it.code == code } ?: UNKNOWN
    }
}