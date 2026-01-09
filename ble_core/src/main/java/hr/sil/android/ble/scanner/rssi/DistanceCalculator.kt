package hr.sil.android.ble.scanner.rssi

import kotlin.math.pow

/**
 * @author mfatiga
 */
object DistanceCalculator {
    /**
     * RSSI adjustments should be stored in a remote database with the key:
     * - androidVersion
     * - buildNumber
     * - model
     * - manufacturer
     * for example:
     * version="4.4.2"
     * buildNumber="KOT49H"
     * model="Nexus 4"
     * manufacturer="LGE"
     * */
    var rssiAdjustment = 0

    /**
     * propagation constant (path-loss exponent) is a value between 2.0 and 4.3 depending on the
     * medium through which the radio signal travels - in free space n = 2
     */
    var propagationConstant = 2.7

    /**
     * Distance calculation formula:
     * distance = Math.pow(10.0, ((txPower - (rssi + rssiAdjustment)) / (10.0 * propagationConstant)))
     */
    fun calculate(rssi: Int, txPower: Int): Double {
        val adjustedRssi = rssi + rssiAdjustment
        val ratio = txPower - adjustedRssi
        val adjustedRatio = ratio / (10.0 * propagationConstant)
        val distance = 10.0.pow(adjustedRatio)

        return distance
    }

    fun calibrateCalculator(rssi: Int, txPower: Int): Int {
        /*
        RSSI = -60
        Adjusted RSSI = -69
        TX Power = -69
        */
        val adjustment = txPower - rssi
        rssiAdjustment = adjustment
        return adjustment
    }
}