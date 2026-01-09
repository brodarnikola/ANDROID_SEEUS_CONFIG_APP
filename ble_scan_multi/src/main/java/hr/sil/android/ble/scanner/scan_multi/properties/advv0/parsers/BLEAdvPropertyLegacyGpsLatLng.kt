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

package hr.sil.android.ble.scanner.scan_multi.properties.advv0.parsers

import hr.sil.android.ble.scanner.scan_multi.properties.base.BLEAdvProperty
import hr.sil.android.ble.scanner.scan_multi.util.extensions.pad
import hr.sil.android.ble.scanner.scan_multi.util.extensions.toLong
import java.nio.ByteOrder

/**
 * @author mfatiga
 */
class BLEAdvPropertyLegacyGpsLatLng : BLEAdvProperty<Double>() {
    private fun uIntToInt(longValue: Long): Long {
        val uIntMax = 4294967296L
        val uIntMid = 2147483647L
        return if (longValue > uIntMid) longValue - uIntMax else longValue
    }

    override fun parse(bytes: ByteArray): Double? {
        val size = 4

        val data = bytes.pad(size, 0x00.toByte(), ByteOrder.BIG_ENDIAN)
        return if (data.size == size) {
            if (data.all { it.toInt() == 0 }) {
                0.0
            } else {
                val cleanBytes = data.reversed()
                val unsignedValue = cleanBytes.toLong()
                val str = uIntToInt(unsignedValue).toString()
                if (str.length > 6) {
                    val degreesDecimal = str.take(str.length - 6).toDouble()
                    var minutesDecimal = (str.takeLast(6).toDouble() / 10000.0) / 60.0
                    if (degreesDecimal < 0) minutesDecimal *= -1
                    degreesDecimal + minutesDecimal
                } else {
                    null
                }
            }
        } else null
    }
}