package hr.sil.android.ble.scanner.util.background

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import java.util.*

/**
 * @author mfatiga
 */
class BackgroundDetector private constructor(context: Context) : Application.ActivityLifecycleCallbacks, BackgroundDetectorBase {
    companion object {
        private const val TAG = "BackgroundDetector"
        private var instance: BackgroundDetector? = null
        fun getInstanceForApplication(context: Context): BackgroundDetector {
            if (instance == null) {
                synchronized(this) {
                    if (instance == null) {
                        instance = BackgroundDetector(context)
                    }
                }
            }
            return instance!!
        }
    }

    init {
        (context.applicationContext as Application).registerActivityLifecycleCallbacks(this)
    }

    private var stateChangeReportDelay: Long = 0L
    override fun setReportDelay(reportDelay: Long) {
        stateChangeReportDelay = reportDelay.coerceAtLeast(0L)
    }

    private var activeActivityCount: Int = 0
        get() = synchronized(this) { field }
        set(value) = synchronized(this) { field = value }

    override val inBackground: Boolean
        get() = _inBackground

    private var _inBackground: Boolean = true
        get() = synchronized(this) { field }
        set(value) = synchronized(this) { field = value }

    private val listeners = mutableMapOf<String, (Boolean) -> Unit>()
    override fun addStateChangeListener(listener: (Boolean) -> Unit): String {
        val key = UUID.randomUUID().toString()
        listeners[key] = listener
        return key
    }

    override fun removeStateChangeListener(key: String) {
        listeners.remove(key)
    }

    private var handlerRunning: Boolean = false
        get() = synchronized(this) { field }
        set(value) = synchronized(this) { field = value }

    private val reportDelayHandler = Handler(Looper.getMainLooper())
    private fun notifyBackgroundState() {
        synchronized(this) {
            if (stateChangeReportDelay > 0) {
                if (!handlerRunning) {
                    handlerRunning = true
                    reportDelayHandler.postDelayed({
                        Log.i(TAG, "Notifying background state: $_inBackground")
                        listeners.forEach { it.value.invoke(_inBackground) }
                        handlerRunning = false
                    }, stateChangeReportDelay)
                }
            } else {
                listeners.forEach { it.value.invoke(_inBackground) }
            }
        }
    }

    private fun checkBackgroundState() {
        if (_inBackground && activeActivityCount > 0) {
            _inBackground = false
            notifyBackgroundState()
        } else if (!_inBackground && activeActivityCount == 0) {
            _inBackground = true
            notifyBackgroundState()
        }

        Log.i(TAG, "Checking background state: $_inBackground")
    }

    override fun onActivityCreated(a: Activity, b: Bundle?) {
    }

    override fun onActivityStarted(a: Activity) {
    }

    override fun onActivityResumed(a: Activity) {
        activeActivityCount++
        checkBackgroundState()
    }

    override fun onActivityPaused(a: Activity) {
        activeActivityCount--
        checkBackgroundState()
    }

    override fun onActivitySaveInstanceState(a: Activity, b: Bundle) {
    }

    override fun onActivityStopped(a: Activity) {
    }

    override fun onActivityDestroyed(a: Activity) {
    }
}
