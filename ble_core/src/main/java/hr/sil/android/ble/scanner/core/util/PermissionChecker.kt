package hr.sil.android.ble.scanner.core.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build

/**
 * @author mfatiga
 */
internal class PermissionChecker(private val context: Context) {
    private fun isPermissionGranted(permission: String): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M
                || context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
    }

    fun isBluetoothPermissionGranted(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!isPermissionGranted(Manifest.permission.BLUETOOTH_SCAN)) return false
            if (!isPermissionGranted(Manifest.permission.BLUETOOTH_CONNECT)) return false
        } else {
            if (!isPermissionGranted(Manifest.permission.BLUETOOTH_ADMIN)) return false
        }
        return true
    }

    fun isLocationPermissionGranted(): Boolean {
        return isPermissionGranted(Manifest.permission.ACCESS_COARSE_LOCATION)
                || isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)
    }
}