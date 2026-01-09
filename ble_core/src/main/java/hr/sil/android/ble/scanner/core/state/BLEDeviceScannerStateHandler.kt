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

package hr.sil.android.ble.scanner.core.state

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import hr.sil.android.ble.scanner.model.device.BLEDevice
import hr.sil.android.ble.scanner.model.scan.BLERawScanResult
import hr.sil.android.ble.scanner.parser.BLEDeviceDataFactory
import hr.sil.android.ble.scanner.util.Debuggable
import hr.sil.android.ble.scanner.util.hexToByteArray
import hr.sil.android.ble.scanner.util.toHexString
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.util.concurrent.atomic.AtomicBoolean

/**
 * @author mfatiga
 */
object BLEDeviceScannerStateHandler : Debuggable {
    private const val MAX_WRITE_FREQUENCY_MILLIS = 10_000L
    private const val TAG = "BLEDeviceScannerStateHa"

    override var DEBUG_MODE = false

    private val isInitialized = AtomicBoolean(false)
    private lateinit var stateFile: File
    fun initialize(context: Context): BLEDeviceScannerStateHandler {
        if (isInitialized.compareAndSet(false, true)) {
            val stateDir = File(context.filesDir.absolutePath + "/ble_scanner_state/")
            stateDir.mkdirs()
            stateFile = File(stateDir, "state.json")
        }
        return this
    }

    private data class BLEDeviceStateEntry(
            val deviceAddress: String = "",
            val name: String? = null,
            val rssi: Int = 0,
            val lastSeen: Long = 0L,
            val lastPacketRealTimeMillis: Long = 0L,
            val lastPacketTimestampMillis: Long = 0L,
            val scanRecordHex: String = "")

    @Volatile
    private var lastWriteTime: Long = 0L

    private val accessingStateFile = AtomicBoolean(false)
    fun writeState(devices: List<BLEDevice<*>>, forceWrite: Boolean) {
        if (isInitialized.get()) {
            if (forceWrite || System.currentTimeMillis() - lastWriteTime >= MAX_WRITE_FREQUENCY_MILLIS) {
                if (accessingStateFile.compareAndSet(false, true)) {
                    val entries = devices.map {
                        BLEDeviceStateEntry(
                                deviceAddress = it.deviceAddress,
                                name = it.deviceName,
                                rssi = it.rssi,
                                lastSeen = it.lastSeen,
                                lastPacketRealTimeMillis = it.lastPacketRealTimeMillis,
                                lastPacketTimestampMillis = it.lastPacketTimestampMillis,
                                scanRecordHex = it.lastPacketBytes.toHexString())
                    }
                    debug({ "Storing scanner state for ${entries.size} devices: ${entries.joinToString(", ") { it.deviceAddress }}" })

                    var fileWriter: FileWriter? = null
                    try {
                        fileWriter = FileWriter(stateFile)
                        fileWriter.write("")

                        val type = object : TypeToken<List<BLEDeviceStateEntry>>() {}.type
                        Gson().toJson(entries, type, fileWriter)
                        lastWriteTime = System.currentTimeMillis()
                    } catch (exc: Exception) {
                        Log.e(TAG, "Error while writing scanner state!", exc)
                    } finally {
                        fileWriter?.close()
                    }
                    accessingStateFile.set(false)
                } else {
                    debug("Scanner state write skipped, already in progress...")
                }
            }
        }
    }

    private fun readEntries(): List<BLEDeviceStateEntry> {
        if (isInitialized.get()) {
            if (stateFile.exists()) {
                var fileReader: FileReader? = null
                try {
                    fileReader = FileReader(stateFile)

                    val type = object : TypeToken<List<BLEDeviceStateEntry>>() {}.type
                    val result = Gson().fromJson<List<BLEDeviceStateEntry>>(fileReader, type)
                    debug({ "Reading scanner state for ${result.size} devices: ${result.joinToString(", ") { "(deviceAddress=${it.deviceAddress}; scanRecordHex=${it.scanRecordHex});)" }})" })

                    fileReader.close()

                    return result.filter { it.deviceAddress.isNotBlank() }
                } catch (exc: Exception) {
                    Log.e(TAG, "Error while reading scanner state!", exc)
                    fileReader?.close()
                    stateFile.delete()
                }
            }
        }
        return listOf()
    }

    fun <D> readState(deviceDataFactory: BLEDeviceDataFactory<D>): List<BLEDevice<D>> {
        val entries = readEntries()
        return entries.distinctBy { it.deviceAddress }.mapNotNull { entry ->
            val rawBytes = entry.scanRecordHex.hexToByteArray()
            val rawScanResult = BLERawScanResult(
                    deviceAddress = entry.deviceAddress,
                    deviceName = entry.name,
                    rssi = entry.rssi,
                    realTimeMillis = entry.lastPacketRealTimeMillis,
                    timestampMillis = entry.lastPacketTimestampMillis,
                    scanRecord = rawBytes)
            val deviceData = deviceDataFactory.create(entry.rssi, listOf(rawScanResult), null)
            if (deviceData != null) {
                BLEDevice(
                        deviceAddress = entry.deviceAddress,
                        deviceName = entry.name,
                        rssi = entry.rssi,
                        packetsPerSecond = 0.0,
                        lastSeen = entry.lastSeen,
                        lastPacketRealTimeMillis = entry.lastPacketRealTimeMillis,
                        lastPacketTimestampMillis = entry.lastPacketTimestampMillis,
                        lastPacketBytes = rawBytes,
                        data = deviceData
                )
            } else null
        }
    }
}