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
import hr.sil.android.ble.scanner.scan_multi.util.extensions.padStart
import hr.sil.android.ble.scanner.scan_multi.util.extensions.toHexString
import java.util.*

/**
 * @author mfatiga
 */
class BLEAdvPropertyUUID : BLEAdvProperty<String>() {
    override fun parse(bytes: ByteArray): String? {
        val size = 16
        val data = bytes.padStart(size, 0x00.toByte())
        val isValid = data.size == size

        return if (isValid) {
            data.toHexString()
                .uppercase(Locale.getDefault())
                .foldIndexed("") { idx, acc, chr ->
                    if (idx in listOf(8, 12, 16, 20)) "$acc-$chr"
                    else acc + chr
                }
        } else null
    }
}