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

package hr.sil.android.ble.scanner

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import hr.sil.android.ble.scanner.model.device.BLEDevice
import hr.sil.android.ble.scanner.model.event.BLEAdvChangeEvent
import hr.sil.android.ble.scanner.model.event.BLEAdvChangeEventType
import hr.sil.android.ble.scanner.model.event.BLEDeviceEvent
import hr.sil.android.ble.scanner.model.event.BLEDeviceEventType
import hr.sil.android.ble.scanner.util.Debuggable
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * @author mfatiga
 */
class BLEAdvChangeMonitor<out D, out K>(
        context: Context?,
        persistenceStateName: String?,
        private val deviceScanner: BLEDeviceScanner<D>,
        private val keyForDevice: (BLEDevice<D>) -> K?,
        private val keyFromString: (String) -> K?,
        private val keyToString: (K) -> String?) : Debuggable {

    override var DEBUG_MODE: Boolean = false

    private val stateFile: File? = if (context != null && !persistenceStateName.isNullOrBlank()) {
        val stateDir = File(context.filesDir.absolutePath + "/ble_adv_change_monitor_state/")
        stateDir.mkdirs()
        val persistenceFileName = persistenceStateName.replace("\\s+".toRegex(), "_") + ".json"
        debug("Adv. change persistence enabled, persistenceStateName=$persistenceStateName, persistenceFileName=$persistenceFileName")
        File(stateDir, persistenceFileName)
    } else {
        debug("Adv. change persistence is disabled, persistenceStateName is not defined")
        null
    }

    // mapping deviceAddress -> lastAdvKeyString
    private val stateMap = ConcurrentHashMap<String, String>()

    //state
    private fun loadState() {
        if (stateFile?.exists() == true) {
            var fileReader: FileReader? = null
            try {
                fileReader = FileReader(stateFile)

                val type = object : TypeToken<Map<String, String>>() {}.type
                val result = Gson().fromJson<Map<String, String>>(fileReader, type)
                debug({ "Reading adv. change state for ${result.size} devices..." })

                fileReader.close()

                // update regions
                stateMap.putAll(result)
            } catch (exc: Exception) {
                debug("Error while reading adv. change state!", exc)
                fileReader?.close()
                stateFile.delete()
            }
        }
    }

    private fun saveState() {
        if (stateFile != null) {
            debug({ "Storing adv. change state for ${stateMap.size} devices..." })

            var fileWriter: FileWriter? = null
            try {
                fileWriter = FileWriter(stateFile)
                fileWriter.write("")

                val type = object : TypeToken<Map<String, String>>() {}.type
                Gson().toJson(stateMap.toMap(), type, fileWriter)
            } catch (exc: Exception) {
                debug("Error while writing adv. change state!", exc)
            } finally {
                fileWriter?.close()
            }
        }
    }

    // device event handler
    private data class BLEAdvChangeKeyWrap<out K>(val advKey: K?, val advKeyString: String?) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as BLEAdvChangeKeyWrap<*>

            if (advKeyString != other.advKeyString) return false

            return true
        }

        override fun hashCode(): Int = advKeyString?.hashCode() ?: 0
    }

    private fun wrapKey(advKeyString: String?): BLEAdvChangeKeyWrap<K> {
        return BLEAdvChangeKeyWrap(
                advKey = if (advKeyString != null) keyFromString(advKeyString) else null,
                advKeyString = advKeyString)
    }

    private fun wrapKey(advKey: K?): BLEAdvChangeKeyWrap<K> {
        return BLEAdvChangeKeyWrap(
                advKey = advKey,
                advKeyString = if (advKey != null) keyToString(advKey) else null
        )
    }

    private fun onDeviceEvents(allDeviceEvents: List<BLEDeviceEvent<D>>) {
        var shouldSaveState = false
        val result = mutableListOf<BLEAdvChangeEvent<K, D>>()

        for (deviceEvent in allDeviceEvents) {
            val deviceAddress = deviceEvent.bleDevice.deviceAddress
            val previous = wrapKey(stateMap[deviceAddress])
            val current = wrapKey(keyForDevice(deviceEvent.bleDevice))

            if (current.advKey != null && current.advKeyString != null) {
                if (previous.advKey != null && previous.advKeyString != null) {
                    if (deviceEvent.eventType == BLEDeviceEventType.LOST) {
                        stateMap.remove(deviceAddress)
                        shouldSaveState = true

                        // on device lost -> exit
                        result.add(BLEAdvChangeEvent(
                                eventType = BLEAdvChangeEventType.EXIT,
                                deviceAddress = deviceAddress,
                                deviceEvent = deviceEvent,
                                advKey = current.advKey))
                    } else {
                        if (current.advKeyString == previous.advKeyString) {
                            // on update
                            result.add(BLEAdvChangeEvent(
                                    eventType = BLEAdvChangeEventType.UPDATE,
                                    deviceAddress = deviceAddress,
                                    deviceEvent = deviceEvent,
                                    advKey = current.advKey))
                        } else {
                            stateMap[deviceAddress] = current.advKeyString
                            shouldSaveState = true

                            // on change
                            result.add(BLEAdvChangeEvent(
                                    eventType = BLEAdvChangeEventType.EXIT,
                                    deviceAddress = deviceAddress,
                                    deviceEvent = deviceEvent,
                                    advKey = previous.advKey))
                            result.add(BLEAdvChangeEvent(
                                    eventType = BLEAdvChangeEventType.ENTRY,
                                    deviceAddress = deviceAddress,
                                    deviceEvent = deviceEvent,
                                    advKey = current.advKey))
                        }
                    }
                } else {
                    stateMap[deviceAddress] = current.advKeyString
                    shouldSaveState = true

                    // on device new -> entry
                    result.add(BLEAdvChangeEvent(
                            eventType = BLEAdvChangeEventType.ENTRY,
                            deviceAddress = deviceAddress,
                            deviceEvent = deviceEvent,
                            advKey = current.advKey))
                }
            }
        }

        if (result.isNotEmpty()) {
            if (shouldSaveState) {
                saveState()
            }
            notifyListeners(result)
        }
    }

    // device event listener
    private var deviceListenerKey: String? = null

    private fun startDeviceListener() {
        synchronized(this) {
            if (deviceListenerKey == null) {
                deviceListenerKey = deviceScanner.addDeviceEventListener(::onDeviceEvents)
            }
        }
    }

    private fun stopDeviceListener() {
        synchronized(this) {
            if (deviceListenerKey != null) {
                deviceScanner.removeDeviceEventListener(deviceListenerKey!!)
                deviceListenerKey = null
            }
        }
    }

    // event listeners
    private val listeners = ConcurrentHashMap<String, (List<BLEAdvChangeEvent<K, D>>) -> Unit>()

    private fun notifyListeners(events: List<BLEAdvChangeEvent<K, D>>) {
        listeners.forEach { it.value.invoke(events) }
    }

    fun addEventListener(listener: (List<BLEAdvChangeEvent<K, D>>) -> Unit): String {
        synchronized(this) {
            val key = UUID.randomUUID().toString()
            listeners[key] = listener
            if (listeners.size == 1) {
                loadState()
                startDeviceListener()
            }
            return key
        }
    }

    fun removeEventListener(key: String) {
        synchronized(this) {
            listeners.remove(key)
            if (listeners.isEmpty()) {
                stopDeviceListener()
                saveState()
            }
        }
    }
}