package hr.sil.android.ble.scanner.parser

import hr.sil.android.ble.scanner.model.scan.BLERawScanResult

/**
 * @author mfatiga
 */
interface BLEDeviceDataFactory<D> {
    fun create(aggregatedRssi: Int, rawScanResults: List<BLERawScanResult>, previousDeviceData: D?): D?
}