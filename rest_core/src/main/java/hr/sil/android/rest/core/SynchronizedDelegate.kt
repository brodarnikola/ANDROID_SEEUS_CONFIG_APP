package hr.sil.android.rest.core

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * @author mfatiga
 */

fun <R : Any, T> R.synchronizedDelegate(initialValue: T, lock: Any? = null) = object : ReadWriteProperty<R, T> {
    var _value = initialValue
    override fun getValue(thisRef: R, property: KProperty<*>): T = synchronized(lock ?: thisRef) { _value }

    override fun setValue(thisRef: R, property: KProperty<*>, value: T) {
        synchronized(lock ?: thisRef) {
            _value = value
        }
    }
}