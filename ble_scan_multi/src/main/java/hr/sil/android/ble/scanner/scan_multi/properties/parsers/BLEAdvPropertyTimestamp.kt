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

package hr.sil.android.ble.scanner.scan_multi.properties.parsers

import hr.sil.android.ble.scanner.scan_multi.properties.base.BLEAdvProperty
import hr.sil.android.ble.scanner.scan_multi.util.extensions.pad
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*

/**
 * @author mfatiga
 */
class BLEAdvPropertyTimestamp(private val byteOrder: ByteOrder,
                              private val validateBytes: Boolean) : BLEAdvProperty<Long>() {

    override fun parse(bytes: ByteArray): Long? {
        val size = 8

        val isValid = !validateBytes || !(bytes.all { it == 0xFF.toByte() })
        val data = bytes.pad(size, 0x00.toByte(), byteOrder)

        return if (data.size == size && isValid) {
            val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))

            //assign millis in UTC
            calendar.timeInMillis = ByteBuffer.wrap(data).order(byteOrder).getLong(0) * 1000L

            //switch timezone
            calendar.timeZone = TimeZone.getDefault()

            //return local time
            calendar.timeInMillis
        } else null
    }
}