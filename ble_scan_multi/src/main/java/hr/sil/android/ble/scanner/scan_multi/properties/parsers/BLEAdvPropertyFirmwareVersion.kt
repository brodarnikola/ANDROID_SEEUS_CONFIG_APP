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

import hr.sil.android.ble.scanner.scan_multi.model.BLENeoFirmwareVersion
import hr.sil.android.ble.scanner.scan_multi.properties.base.BLEAdvProperty

/**
 * @author mfatiga
 */
class BLEAdvPropertyFirmwareVersion : BLEAdvProperty<BLENeoFirmwareVersion>() {
    override fun parse(bytes: ByteArray): BLENeoFirmwareVersion? {
        return if (bytes.size >= 2) {
            BLENeoFirmwareVersion(
                    major = bytes[0].toInt() and 0xFF,
                    minor = bytes[1].toInt() and 0xFF,
                    patch = (if (bytes.size > 2) bytes[2].toInt() and 0xFF else null),
                    build = (if (bytes.size > 3) bytes[3].toInt() and 0xFF else null)
            )
        } else null
    }
}