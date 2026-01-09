/* SWISS INNOVATION LAB CONFIDENTIAL
*
* www.swissinnolab.com
* __________________________________________________________________________
*
* [2016] - [2017] Swiss Innovation Lab AG
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

package hr.sil.android.rest.core.configuration.base

import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

abstract class AbstractConfigContainer internal constructor(private val configFile: File) {
    private val TAG = "AbstractConfigContainer"

    private var map: MutableMap<String, String> = readConfig()
    private fun readConfig(): MutableMap<String, String> {
        if (configFile.exists()) {
            var fileReader: FileReader? = null
            try {
                fileReader = FileReader(configFile)

                val type = object : TypeToken<Map<String, String>>() {}.type
                val result = Gson().fromJson<Map<String, String>>(fileReader, type)
                return if (result != null) mutableMapOf(*result.entries.map { Pair(it.key, it.value) }.toTypedArray()) else mutableMapOf()
            } catch (exc: Exception) {
                Log.e(TAG, "Error while reading service configuration!", exc)
            } finally {
                fileReader?.close()
            }
        }
        return mutableMapOf()
    }

    private fun writeConfig() {
        var fileWriter: FileWriter? = null
        try {
            fileWriter = FileWriter(configFile)
            fileWriter.write("")

            val type = object : TypeToken<Map<String, String>>() {}.type
            Gson().toJson(map, type, fileWriter)
        } catch (exc: Exception) {
            Log.e(TAG, "Error while writing service configuration!", exc)
        } finally {
            fileWriter?.close()
        }
    }

    protected fun get(key: String): String? {
        return map[key]
    }

    protected fun getAll(keyPrefix: String): Map<String, String> {
        return map.filter { it.key.startsWith(keyPrefix) }
    }

    protected fun set(key: String, value: String) {
        map[key] = value
        writeConfig()
    }

    protected fun delegate(defaultValue: String, onWrite: (String) -> String = { it }) = object : ReadWriteProperty<Any?, String> {
        override operator fun getValue(thisRef: Any?, property: KProperty<*>): String {
            return get(property.name) ?: defaultValue
        }

        override operator fun setValue(thisRef: Any?, property: KProperty<*>, value: String) {
            set(property.name, onWrite(value))
        }
    }
}