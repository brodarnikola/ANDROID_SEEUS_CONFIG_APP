package hr.sil.android.ble.scanner.util

import android.util.Log

/**
 * @author mfatiga
 */
interface Debuggable {
    private val debugTag: String
        get() {
            val tag = javaClass.simpleName
            return if (tag.length <= 23) {
                tag
            } else {
                tag.substring(0, 23)
            }
        }

    var DEBUG_MODE: Boolean
    fun debug(message: String, throwable: Throwable? = null) {
        if (DEBUG_MODE) {
            Log.d(debugTag, message, throwable)
        }
    }

    fun debug(message: () -> String, throwable: Throwable? = null) {
        if (DEBUG_MODE) {
            Log.d(debugTag, message.invoke(), throwable)
        }
    }
}