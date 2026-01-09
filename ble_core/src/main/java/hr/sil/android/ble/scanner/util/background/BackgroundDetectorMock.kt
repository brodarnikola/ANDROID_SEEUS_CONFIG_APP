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

package hr.sil.android.ble.scanner.util.background

import java.util.*

/**
 * @author mfatiga
 */
class BackgroundDetectorMock : BackgroundDetectorBase {
    @Volatile
    private var _inBackground: Boolean = true

    override val inBackground: Boolean
        get() = _inBackground

    override fun setReportDelay(reportDelay: Long) {
        // not necessary for testing
    }

    private val listeners = mutableMapOf<String, (Boolean) -> Unit>()
    override fun addStateChangeListener(listener: (Boolean) -> Unit): String {
        val key = UUID.randomUUID().toString()
        listeners[key] = listener
        return key
    }

    override fun removeStateChangeListener(key: String) {
        listeners.remove(key)
    }

    fun emitBackgroundChange(inBackground: Boolean) {
        if (inBackground != this._inBackground) {
            this._inBackground = inBackground
            listeners.forEach { it.value.invoke(_inBackground) }
        }
    }
}