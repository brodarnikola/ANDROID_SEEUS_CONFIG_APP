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
import hr.sil.android.ble.scanner.core.filter.BLEScannerPreFilter
import hr.sil.android.ble.scanner.core.operations.ChainOperationScan
import hr.sil.android.ble.scanner.core.operations.ScannerOperationChain
import hr.sil.android.ble.scanner.core.queue.BLEScanQueueCycle
import hr.sil.android.ble.scanner.core.scanner.BLERawScanner
import hr.sil.android.ble.scanner.core.scanner.BLERawScannerImpl
import hr.sil.android.ble.scanner.core.scanner.BLEScanCallback
import hr.sil.android.ble.scanner.core.scanner.BLEScanParams
import hr.sil.android.ble.scanner.core.state.BLEDeviceScannerStateHandler
import hr.sil.android.ble.scanner.exception.BLEScanException
import hr.sil.android.ble.scanner.model.device.BLEDevice
import hr.sil.android.ble.scanner.model.device.BLEDeviceStatistics
import hr.sil.android.ble.scanner.model.event.BLEDeviceEvent
import hr.sil.android.ble.scanner.model.event.BLEDeviceEventType
import hr.sil.android.ble.scanner.model.scan.BLERawScanResult
import hr.sil.android.ble.scanner.model.scan.BLEScanMode
import hr.sil.android.ble.scanner.parser.BLEDeviceDataFactory
import hr.sil.android.ble.scanner.util.Debuggable
import hr.sil.android.ble.scanner.util.background.BackgroundDetector
import hr.sil.android.ble.scanner.util.background.BackgroundDetectorBase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

/**
 * @author mfatiga
 */
class BLEDeviceScanner<D> private constructor(
        private val scannerScope: CoroutineScope,
        private val rawScanner: BLERawScanner,
        private val backgroundDetector: BackgroundDetectorBase,
        private val deviceStateHandler: BLEDeviceScannerStateHandler?,
        private val onError: (BLEScanException) -> Unit,
        private val deviceDataFactory: BLEDeviceDataFactory<D>) : Debuggable {

    companion object {
        fun <D> create(scannerScope: CoroutineScope,
                       context: Context,
                       onError: (BLEScanException) -> Unit,
                       deviceDataFactory: BLEDeviceDataFactory<D>) = BLEDeviceScanner(
                scannerScope = scannerScope,
                rawScanner = BLERawScannerImpl(context),
                backgroundDetector = BackgroundDetector.getInstanceForApplication(context),
                deviceStateHandler = BLEDeviceScannerStateHandler.initialize(context),
                onError = onError,
                deviceDataFactory = deviceDataFactory)

        fun <D> create(scannerScope: CoroutineScope,
                       rawScanner: BLERawScanner,
                       backgroundDetector: BackgroundDetectorBase,
                       deviceStateHandler: BLEDeviceScannerStateHandler?,
                       onError: (BLEScanException) -> Unit,
                       deviceDataFactory: BLEDeviceDataFactory<D>) = BLEDeviceScanner(
                scannerScope = scannerScope,
                rawScanner = rawScanner,
                backgroundDetector = backgroundDetector,
                deviceStateHandler = deviceStateHandler,
                onError = onError,
                deviceDataFactory = deviceDataFactory)
    }

    override var DEBUG_MODE: Boolean = false
        set(value) {
            field = value
            rawScanner.DEBUG_MODE = value
            deviceStateHandler?.DEBUG_MODE = value
        }

    // background detection
    private var bgDetectorListenerKey: String = ""

    // statistics
    private val statisticsEWMATau = BLEScanQueueCycle.SCAN_RESULTS_QUEUE_PERIOD * 3L

    private val statistics = ConcurrentHashMap<String, BLEDeviceStatistics>()

    private fun updateStatistics(deviceAddress: String, scanResults: List<BLERawScanResult>): BLEDeviceStatistics {
        val now = System.currentTimeMillis()
        val deviceStatistics = statistics.getOrPut(deviceAddress, { BLEDeviceStatistics(statisticsEWMATau) })

        // update RSSI
        for (scanResult in scanResults) {
            deviceStatistics.rssiEWMA.next(scanResult.rssi.toDouble(), scanResult.timestampMillis)
        }

        // update packets per second
        val startTimestamp = deviceStatistics.ppsEWMA.lastTimestamp() ?: now
        val ppsPeriodInSeconds = (now - startTimestamp) / 1000.0
        val packetsPerSecond = if (ppsPeriodInSeconds > 0.0 && scanResults.isNotEmpty()) {
            scanResults.size / ppsPeriodInSeconds
        } else 0.0
        deviceStatistics.ppsEWMA.next(packetsPerSecond, now)

        return deviceStatistics
    }

    // state handling
    private fun readState(): Map<String, BLEDevice<D>> {
        return if (Config.persistDeviceScannerState) {
            deviceStateHandler?.readState(deviceDataFactory)?.associateBy { it.deviceAddress }
                    ?: mapOf()
        } else mapOf()
    }

    private fun writeState(devices: List<BLEDevice<D>>, forceWrite: Boolean) {
        if (Config.persistDeviceScannerState) {
            deviceStateHandler?.writeState(devices, forceWrite)
        }
    }

    // device data
    val devices = ConcurrentHashMap<String, BLEDevice<D>>().apply {
        putAll(readState())
    }

    private fun onScanQueueCycle(rawScanResults: List<BLERawScanResult>): Boolean {
        if (enabled.get()) {
            // resulting created events
            val deviceEvents = mutableListOf<BLEDeviceEvent<D>>()

            // group scan results
            val groupedScanResults = rawScanResults.groupBy { it.deviceAddress }

            // create device address set from previous devices and new from events
            val deviceAddresses = devices.keys().toList().toSet() + groupedScanResults.keys

            // enumerate and check all devices
            for (deviceAddress in deviceAddresses) {
                // take and sort scan results
                val scanResults = (groupedScanResults[deviceAddress] ?: listOf())
                        .sortedBy { it.realTimeMillis }

                // update statistics
                val deviceStatistics = updateStatistics(deviceAddress, scanResults)
                val packetsPerSecond = deviceStatistics.ppsEWMA.current() ?: 0.0
                val deviceRSSI = deviceStatistics.rssiEWMA.current()?.toInt() ?: 0

                if (scanResults.isNotEmpty()) {
                    // create or update device
                    val previousDevice = devices[deviceAddress]
                    val deviceData = deviceDataFactory.create(deviceRSSI, scanResults, previousDevice?.data)
                            ?: previousDevice?.data
                    if (deviceData != null) {
                        val lastScanResult = scanResults.last()
                        val bleDevice = BLEDevice(
                                deviceAddress = deviceAddress,
                                deviceName = lastScanResult.deviceName,
                                rssi = deviceRSSI,
                                packetsPerSecond = packetsPerSecond,
                                lastSeen = System.currentTimeMillis(), //lastScanResult.timestampMillis,
                                lastPacketRealTimeMillis = lastScanResult.realTimeMillis,
                                lastPacketTimestampMillis = lastScanResult.timestampMillis,
                                lastPacketBytes = lastScanResult.scanRecord,
                                data = deviceData)

                        devices[deviceAddress] = bleDevice

                        //figure out device event type
                        val deviceEventType = if (previousDevice == null) BLEDeviceEventType.NEW else BLEDeviceEventType.UPDATE

                        //construct device event
                        deviceEvents.add(BLEDeviceEvent(bleDevice, deviceEventType, lastScanResult.timestampMillis))
                    }
                } else {
                    // check for LOST conditions
                    val now = System.currentTimeMillis()

                    // skip checking for LOST conditions if the scanner has been started recently
                    val lostSkipPeriod = BLEScanQueueCycle.SCAN_RESULTS_QUEUE_PERIOD * 2
                    if (rawScannerStartTimestamp != 0L && now - rawScannerStartTimestamp >= lostSkipPeriod) {
                        val device = devices[deviceAddress]
                        if (device != null && now - device.lastSeen > Config.deviceLostPeriod) {
                            deviceEvents.add(BLEDeviceEvent(device, BLEDeviceEventType.LOST, now))

                            // remove from maps
                            devices.remove(deviceAddress)
                            statistics.remove(deviceAddress)
                        }
                    }
                }
            }

            //update state - force write only if any of the devices is new
            writeState(
                    devices.values.toList(),
                    deviceEvents.any { it.eventType == BLEDeviceEventType.NEW || it.eventType == BLEDeviceEventType.LOST })

            //notify device events
            if (deviceEvents.isNotEmpty() || Config.notifyEmptyDeviceEvents) {
                notifyDeviceEvents(deviceEvents)
            }
        }

        // notify that we are still active because there are devices in the map
        return devices.isNotEmpty()
    }

    // scan result batching
    private val scanQueueCycle = BLEScanQueueCycle("BLEScanQueueCycle", this::onScanQueueCycle)
    private val scanCallback = object : BLEScanCallback {
        override fun onScanResult(result: BLERawScanResult) {
            scanQueueCycle.offerData(result)
        }

        override fun onError(error: BLEScanException) {
            this@BLEDeviceScanner.onError(error)
        }
    }

    // scan chain operations
    private fun createScanParams(scanMode: BLEScanMode): BLEScanParams {
        return BLEScanParams(
                scanMode = scanMode,
                scanResultsReportDelay = Config.scanResultsReportDelay,
                scanFilters = null)
    }

    private var rawScannerStartTimestamp: Long = 0L
    private suspend fun startRawScanner(scanParams: BLEScanParams) {
        rawScanner.startScan(scanParams, scanCallback)
        rawScannerStartTimestamp = System.currentTimeMillis()
    }

    private fun stopRawScanner() {
        rawScannerStartTimestamp = 0L
        rawScanner.stopScan()
    }

    private val scannerOperationChain = ScannerOperationChain(listOf(
            ChainOperationScan(
                    createScanParams(BLEScanMode.SCAN_MODE_LOW_LATENCY),
                    Config.scanPeriod,
                    { startRawScanner(it) },
                    ::stopRawScanner)
    ))

    private fun onBackgroundStateChanged(wentToBackground: Boolean) {
        debug("Background state changed -> $wentToBackground")
        if (enabled.get()) {
            scannerScope.launch {
                if (wentToBackground) {
                    scannerOperationChain.stop()
                } else {
                    scannerOperationChain.start(scannerScope)
                }
            }
        }
    }

    @Volatile
    private var enabled = AtomicBoolean(false)

    // starting
    fun start() {
        if (!enabled.get()) {
            debug("Starting device scanner")
            if (!backgroundDetector.inBackground) {
                scannerOperationChain.start(scannerScope)
            }
            enabled.set(true)
        }
    }

    fun isStarted(): Boolean = enabled.get()

    // stopping
    suspend fun stop(forceDeviceLost: Boolean = false) {
        if (enabled.get()) {
            debug("Stopping device scanner...")

            scannerOperationChain.stop()

            if (forceDeviceLost) {
                val lostDevices = devices.values.toList()

                devices.clear()
                statistics.clear()
                writeState(listOf(), lostDevices.isNotEmpty())

                val now = System.currentTimeMillis()
                notifyDeviceEvents(lostDevices.map { BLEDeviceEvent(it, BLEDeviceEventType.LOST, now) })
            }

            enabled.set(false)
        }
    }

    // destroying
    suspend fun destroy() {
        stop()
        if (bgDetectorListenerKey.isNotBlank()) {
            backgroundDetector.removeStateChangeListener(bgDetectorListenerKey)
        }
        rawScanner.destroy()
    }

    // initialization
    init {
        backgroundDetector.setReportDelay(2_000L)
        bgDetectorListenerKey = backgroundDetector.addStateChangeListener(::onBackgroundStateChanged)
    }

    // event listeners
    private val deviceEventListeners = ConcurrentHashMap<String, (List<BLEDeviceEvent<D>>) -> Unit>()

    fun addDeviceEventListener(listener: (List<BLEDeviceEvent<D>>) -> Unit): String {
        val key = UUID.randomUUID().toString()
        deviceEventListeners[key] = listener
        return key
    }

    fun removeDeviceEventListener(key: String) {
        deviceEventListeners.remove(key)
    }

    private fun notifyDeviceEvents(events: List<BLEDeviceEvent<D>>) {
        deviceEventListeners.forEach { it.value.invoke(events) }
    }

    // filters
    fun setAdvertisementFilters(filters: List<Pair<IntArray, ByteArray>>) {
        BLEScannerPreFilter.setCustomFilters(filters)
    }

    // configuration
    fun configure(cb: Config.() -> Unit): BLEDeviceScanner<D> {
        cb.invoke(Config)
        return this
    }

    object Config {
        /**
         * When set to true (default) will periodically and on NEW/LOST events store the current
         * device map to a file.
         */
        @Volatile
        var persistDeviceScannerState = true

        /**
         * When set to true (default), will notify event listener with an empty device event list
         * each cycle even when no device events have occurred. When set to false, will notify event
         * listener only when at least one device event has occurred in the last cycle.
         */
        @Volatile
        var notifyEmptyDeviceEvents = true

        /**
         * Set the device lost period. This is the time it takes to mark the device as lost
         * after the last BLE advertisement packet has been received.
         * - value in milliseconds, min value = 5000L
         */
        @Volatile
        var deviceLostPeriod = 20_000L
            set(value) {
                field = value.coerceAtLeast(5000L)
            }

        /**
         * Scan period - this is the time during which the scanner is running before restarting
         * itself. In foreground, the scanner is always scanning in high powered mode
         * - value in milliseconds
         */
        @Volatile
        var scanPeriod = 6_000L
            set(value) {
                field = value.coerceAtLeast(0L)
            }

        /**
         * Delay of report in milliseconds. Set to 0 to be notified of
         * results immediately. Values &gt; 0 causes the scan results to be queued up and
         * delivered after the requested delay or when the internal buffers fill up.
         */
        @Volatile
        var scanResultsReportDelay = 0L
            set(value) {
                field = value.coerceAtLeast(0L)
            }
    }
}