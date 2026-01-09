package hr.sil.android.ble.scanner.model.scan

import android.bluetooth.le.ScanSettings

/**
 * @author mfatiga
 */
enum class BLEScanMode(val code: Int) {
    /**
     * A special Bluetooth LE scan mode. Applications using this scan mode will passively listen for
     * other scan results without starting BLE scans themselves.
     */
    SCAN_MODE_OPPORTUNISTIC(ScanSettings.SCAN_MODE_OPPORTUNISTIC),

    /**
     * Perform Bluetooth LE scan in low power mode. This is the default scan mode as it consumes the
     * least power.
     */
    SCAN_MODE_LOW_POWER(ScanSettings.SCAN_MODE_LOW_POWER),

    /**
     * Perform Bluetooth LE scan in balanced power mode. Scan results are returned at a rate that
     * provides a good trade-off between scan frequency and power consumption.
     */
    SCAN_MODE_BALANCED(ScanSettings.SCAN_MODE_BALANCED),

    /**
     * Scan using highest duty cycle. It's recommended to only use this mode when the application is
     * running in the foreground.
     */
    SCAN_MODE_LOW_LATENCY(ScanSettings.SCAN_MODE_LOW_LATENCY)
}