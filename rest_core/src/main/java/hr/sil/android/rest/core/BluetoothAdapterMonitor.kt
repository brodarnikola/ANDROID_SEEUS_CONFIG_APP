package hr.sil.android.rest.core

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import java.util.*

//import hr.sil.android.datacache.synchronizedDelegate

import hr.sil.android.rest.core.synchronizedDelegate

/**
 * @author mfatiga
 */
class BluetoothAdapterMonitor private constructor(context: Context) {
    //singleton initialization
    companion object {
        private var instance: BluetoothAdapterMonitor? = null
        fun create(context: Context): BluetoothAdapterMonitor {
            if (instance == null) {
                instance = BluetoothAdapterMonitor(context)
            }
            return instance!!
        }
    }

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent != null) {
                if (intent.action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                    val state = intent.extras?.getInt(BluetoothAdapter.EXTRA_STATE)
                    if (state != null) {
                        notifyStateChangeListeners(state)

                        if (state == BluetoothAdapter.STATE_ON) {
                            cbEnabledMap.keys.map { it }.forEach {
                                cbEnabledMap[it]?.invoke()
                                cbEnabledMap.remove(it)
                            }
                        } else if (state == BluetoothAdapter.STATE_OFF) {
                            cbDisabledMap.keys.map { it }.forEach {
                                cbDisabledMap[it]?.invoke()
                                cbDisabledMap.remove(it)
                            }
                        }
                    }
                }
            }
        }
    }

    init {
        context.registerReceiver(broadcastReceiver, IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED))
    }

    private val adapter = BluetoothAdapter.getDefaultAdapter()

    val isEnabled: Boolean
        get() = adapter.isEnabled

    private val cbEnabledMap = mutableMapOf<String, () -> Unit>()
    fun enable(cb: () -> Unit = { }) {
        if (!isEnabled) {
            cbEnabledMap[UUID.randomUUID().toString()] = cb
            adapter.enable()
        } else {
            cb()
        }
    }

    private val cbDisabledMap = mutableMapOf<String, () -> Unit>()
    fun disable(cb: () -> Unit = { }) {
        if (isEnabled) {
            cbDisabledMap[UUID.randomUUID().toString()] = cb
            adapter.disable()
        } else {
            cb()
        }
    }

    private val stateChangeListeners by synchronizedDelegate(mutableMapOf<String, (Int) -> Unit>(), this)
    fun addStateChangeListener(listener: (Int) -> Unit): String {
        val key = UUID.randomUUID().toString()
        stateChangeListeners[key] = listener
        return key
    }

    fun removeStateChangeListener(key: String) {
        stateChangeListeners.remove(key)
    }

    private fun notifyStateChangeListeners(state: Int) {
        stateChangeListeners.forEach { it.value.invoke(state) }
    }
}