package hr.sil.android.ble.scanner.scan_multi.properties.advv2.common

/**
 * @author mfatiga
 */
enum class NinaResetReason(val code: Byte, val description: String) {
    UNKNOWN(0xFE.toByte(), "Unknown"),
    A_RESETPIN(0x01.toByte(), "Pin-Reset"),
    B_DOG(0x02.toByte(), "Watchdog"),
    C_SREQ(0x03.toByte(), "Soft-Reset"),
    D_LOCKUP(0x04.toByte(), "CPU Lock-Up"),
    E_OFF(0x05.toByte(), "Wake-Up from GPIO"),
    F_LPCOMP(0x06.toByte(), "Wake-Up from LPCOMP"),
    G_DIF(0x07.toByte(), "Wake-Up from debug interface"),
    H_NFC(0x08.toByte(), "Wake-Up by NFC"),
    I_VBUS(0x09.toByte(), "Wake-Up by VBUS"),
    BROWNOUT(0x0A.toByte(), "Brownout");

    companion object {
        fun parse(code: Byte?) = entries.firstOrNull { it.code == code }
    }
}