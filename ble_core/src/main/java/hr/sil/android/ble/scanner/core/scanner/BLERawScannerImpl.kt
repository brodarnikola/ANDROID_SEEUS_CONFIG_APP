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

package hr.sil.android.ble.scanner.core.scanner

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.SystemClock
import android.util.Log
import hr.sil.android.ble.scanner.core.filter.BLEScannerPreFilter
import hr.sil.android.ble.scanner.core.util.PermissionChecker
import hr.sil.android.ble.scanner.exception.BLEScanException
import hr.sil.android.ble.scanner.model.scan.BLERawScanResult
import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicBoolean

/**
 * @author mfatiga
 */
internal class BLERawScannerImpl(private val context: Context) : BLERawScanner {
    companion object {
        private const val TAG = "BLERawScannerImpl"
    }

    override var DEBUG_MODE: Boolean = false

    private val isScannerRunning = AtomicBoolean(false)
    private val isWaitingForStart = AtomicBoolean(false)

    private val bluetoothAdapter: BluetoothAdapter = (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter

    private var scanSettings: ScanSettings? = null
    private var filters: List<ScanFilter>? = null

    private val permissionChecker = PermissionChecker(context)
    private fun isBluetoothAdapterEnabled(): Boolean = bluetoothAdapter.isEnabled

    private var externalScanCallback: BLEScanCallback? = null
    private fun notifyRawScanResult(scanResult: BLERawScanResult) {
        externalScanCallback?.onScanResult(scanResult)
    }

    private fun notifyError(error: BLEScanException) {
        debug(error.message ?: "Scan error!", error)
        externalScanCallback?.onError(error)
    }

    private val scanner = bluetoothAdapter.bluetoothLeScanner
    private val nativeScanCallback = object : ScanCallback() {
        private fun handleScanResult(result: ScanResult) {
            // validate
            val bytes = result.scanRecord?.bytes
            if (bytes != null && BLEScannerPreFilter.isValid(bytes)) {
                // create raw scan result
                val realTimeMillis = result.timestampNanos / (1000L * 1000L)
                val timestampMillis = System.currentTimeMillis() - SystemClock.elapsedRealtime() + realTimeMillis
                val rawScanResult = BLERawScanResult(
                        deviceAddress = result.device.address.uppercase(),
                        deviceName = result.scanRecord?.deviceName,
                        rssi = result.rssi,
                        realTimeMillis = realTimeMillis,
                        timestampMillis = timestampMillis,
                        scanRecord = bytes)

                // notify
                notifyRawScanResult(rawScanResult)
            }
        }

        override fun onScanResult(callbackType: Int, result: ScanResult) {
            handleScanResult(result)
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>) {
            for (result in results) {
                handleScanResult(result)
            }
        }

        override fun onScanFailed(errorCode: Int) {
            notifyError(BLEScanException("Scan failed!", errorCode))
        }
    }

    private fun checkStartConditions(): Boolean {
        debug("Checking scan start conditions...")
        if (!permissionChecker.isBluetoothPermissionGranted()) {
            notifyError(BLEScanException("Could not start scan!", BLEScanException.ErrorCode.SCAN_FAILED_BLUETOOTH_PERMISSION_MISSING))
            return false
        }

        if (!permissionChecker.isLocationPermissionGranted()) {
            notifyError(BLEScanException("Could not start scan!", BLEScanException.ErrorCode.SCAN_FAILED_LOCATION_PERMISSION_MISSING))
            return false
        }

        if (!isBluetoothAdapterEnabled()) {
            notifyError(BLEScanException("Could not start scan!", BLEScanException.ErrorCode.SCAN_FAILED_BLUETOOTH_DISABLED))
            return false
        }

        debug("Start conditions all checks PASSED!")
        return true
    }

    private fun buildScanSettings(scanParams: BLEScanParams): ScanSettings {
        val scanSettingsBuilder = ScanSettings.Builder().setScanMode(scanParams.scanMode.code)
        if (scanParams.scanResultsReportDelay > 0L) {
            scanSettingsBuilder.setReportDelay(scanParams.scanResultsReportDelay)
        }
        return scanSettingsBuilder.build()
    }

    private var startScanJob: Job? = null
    override suspend fun startScan(params: BLEScanParams, scanCallback: BLEScanCallback) {
        if (!isScannerRunning.get() && !isWaitingForStart.get()) {
            isWaitingForStart.set(true)

            scanSettings = buildScanSettings(params)
            filters = params.scanFilters

            startScanJob = GlobalScope.launch(Dispatchers.Main) {
                var startConditionsPassed = false
                while (true) {
                    if (checkStartConditions()) {
                        startConditionsPassed = true
                        break
                    } else {
                        try {
                            delay(1000L)
                        } catch (exc: CancellationException) {
                            // ignore
                        }

                        if (!isWaitingForStart.get()) {
                            break
                        }
                    }
                }

                if (startConditionsPassed && isWaitingForStart.compareAndSet(true, false)) {
                    externalScanCallback = scanCallback

                    try {
                        scanner.startScan(filters, scanSettings, nativeScanCallback)
                    } catch (exc: Exception) {
                        Log.e(TAG, "Error while starting scanner!", exc)
                    }

                    isScannerRunning.set(true)
                } else {
                    isWaitingForStart.set(false)
                }
            }
            try {
                startScanJob?.join()
            } catch (exc: CancellationException) {
                //ignore
            }
        }
    }

    override fun stopScan() {
        if (isScannerRunning.get()) {
            scanner.stopScan(nativeScanCallback)

            externalScanCallback = null

            scanSettings = null
            filters = null

            isScannerRunning.set(false)
        } else {
            if (isWaitingForStart.compareAndSet(true, false)) {
                startScanJob?.cancel()
            }
        }
    }

    override fun isScanning() = isScannerRunning.get()

    override fun destroy() {
        stopScan()
    }
}