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

package hr.sil.android.blecommunicator.impl.nrfdfu.scanner

import android.util.Log
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.withTimeout
import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat
import no.nordicsemi.android.support.v18.scanner.ScanCallback
import no.nordicsemi.android.support.v18.scanner.ScanResult
import no.nordicsemi.android.support.v18.scanner.ScanSettings
import java.util.concurrent.atomic.AtomicBoolean

/**
 * @author mfatiga
 */
internal object BootloaderScanner {
    private const val TAG = "BootloaderScanner"

    //debug mode
    private var debugEnabled: Boolean = false

    fun setDebugMode(enabled: Boolean) {
        this.debugEnabled = enabled
    }

    private fun debug(msg: String, t: Throwable? = null) {
        if (debugEnabled) {
            if (t != null) {
                Log.e(TAG, msg, t)
            } else {
                Log.d(TAG, msg)
            }
        }
    }

    private const val SCAN_SEARCH_TIMEOUT = 5000L
    private const val ADDRESS_DIFF = 1

    private val scanRunning = AtomicBoolean(false)
    private val scanner by lazy { BluetoothLeScannerCompat.getScanner() }
    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            debug("Got scan result: ${result?.device?.address}")
            if (targetAddresses.isNotEmpty()) {
                val address = result?.device?.address
                if (address != null) {
                    if (targetAddresses.contains(address)) {
                        scanResultsDeferred?.complete(address)
                    }
                }
            }
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            debug("Got scan results: ${(results?.map { it.device.address ?: "?" } ?: listOf()).joinToString(", ") { it }}")
            if (targetAddresses.isNotEmpty()) {
                val addresses = results?.map { it.device.address }
                if (addresses != null) {
                    val found = addresses.firstOrNull { targetAddresses.contains(it) }
                    if (found != null) {
                        scanResultsDeferred?.complete(found)
                    }
                }
            }
        }

        override fun onScanFailed(errorCode: Int) {
            debug("Scan failed! Error code = $errorCode")
            scanResultsDeferred?.complete(null)
        }
    }

    private var scanResultsDeferred: CompletableDeferred<String?>? = null

    private var targetAddresses = listOf<String>()
    private fun setTargetAddresses(deviceAddress: String) {
        val firstBytes = deviceAddress.substring(0, 15)
        val lastByte = deviceAddress.substring(15) // assuming that the device address is correct
        val lastByteIncremented = String.format("%02X", Integer.valueOf(lastByte, 16) + ADDRESS_DIFF and 0xFF)
        val deviceAddressIncremented = firstBytes + lastByteIncremented

        targetAddresses = listOf(deviceAddress, deviceAddressIncremented)

    }

    suspend fun searchFor(deviceAddress: String): String? {
        if (!scanRunning.getAndSet(true)) {
            setTargetAddresses(deviceAddress)
            debug("Target addresses = $targetAddresses")

            debug("Starting bootloader scanner...")
            scanResultsDeferred = CompletableDeferred()
            scanner.startScan(null, ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build(), scanCallback)

            val result = try {
                withTimeout(SCAN_SEARCH_TIMEOUT) {
                    scanResultsDeferred?.await()
                }
            } catch (e: CancellationException) {
                null
            }
            scanner.stopScan(scanCallback)
            targetAddresses = listOf()

            scanRunning.set(false)

            debug("Scan complete or timed out, result = $result")
            return result
        } else {
            return null
        }
    }
}