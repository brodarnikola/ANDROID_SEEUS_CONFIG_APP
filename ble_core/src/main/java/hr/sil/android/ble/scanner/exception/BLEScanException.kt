package hr.sil.android.ble.scanner.exception

import android.bluetooth.le.ScanCallback

/**
 * @author mfatiga
 */
class BLEScanException : Exception {
    private companion object {
        private fun constructMessage(message: String, errorCode: ErrorCode?): String {
            return if (errorCode != null) "$message Reason: $errorCode"
            else message
        }

        private fun constructMessage(message: String, errorCode: Int): String =
                constructMessage(message, getErrorForCode(errorCode))

        private fun getErrorForCode(code: Int): ErrorCode =
                ErrorCode.entries.firstOrNull { it.code == code } ?: ErrorCode.UNKNOWN
    }

    enum class ErrorCode(val code: Int) {
        UNKNOWN(-1),
        SCAN_FAILED_ALREADY_STARTED(
                ScanCallback.SCAN_FAILED_ALREADY_STARTED
        ),
        SCAN_FAILED_APPLICATION_REGISTRATION_FAILED(
                ScanCallback.SCAN_FAILED_APPLICATION_REGISTRATION_FAILED
        ),
        SCAN_FAILED_INTERNAL_ERROR(
                ScanCallback.SCAN_FAILED_INTERNAL_ERROR
        ),
        SCAN_FAILED_FEATURE_UNSUPPORTED(
                ScanCallback.SCAN_FAILED_FEATURE_UNSUPPORTED
        ),
        SCAN_FAILED_OUT_OF_HARDWARE_RESOURCES(
                ScanCallback.SCAN_FAILED_OUT_OF_HARDWARE_RESOURCES
        ),
        SCAN_FAILED_BLUETOOTH_DISABLED(90),
        SCAN_FAILED_BLUETOOTH_PERMISSION_MISSING(91),
        SCAN_FAILED_LOCATION_PERMISSION_MISSING(92)
    }

    val errorCode: ErrorCode

    constructor(message: String) : super(constructMessage(message, ErrorCode.UNKNOWN)) {
        this.errorCode = ErrorCode.UNKNOWN
    }

    constructor(message: String, errorCode: Int) : super(constructMessage(message, errorCode)) {
        this.errorCode = getErrorForCode(errorCode)
    }

    constructor(message: String, errorCode: ErrorCode) : super(constructMessage(message, errorCode)) {
        this.errorCode = errorCode
    }

    constructor(message: String, cause: Throwable) : super(constructMessage(message, ErrorCode.UNKNOWN), cause) {
        this.errorCode = ErrorCode.UNKNOWN
    }

    constructor(message: String, errorCode: Int, cause: Throwable) : super(constructMessage(message, errorCode), cause) {
        this.errorCode = getErrorForCode(errorCode)
    }

    constructor(message: String, errorCode: ErrorCode, cause: Throwable) : super(constructMessage(message, errorCode), cause) {
        this.errorCode = errorCode
    }
}