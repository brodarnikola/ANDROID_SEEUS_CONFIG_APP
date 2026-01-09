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

package hr.sil.android.ble.scanner.scan_multi.properties.advv2

import hr.sil.android.ble.scanner.scan_multi.properties.advv2.base.BLEAdvV2Base
import hr.sil.android.ble.scanner.scan_multi.properties.parsers.BLEAdvPropertyNumber
import hr.sil.android.ble.scanner.scan_multi.properties.parsers.BLEAdvPropertyRaw

/**
 * @author mfatiga
 */
class BLEAdvEpaperPoc : BLEAdvV2Base() {
    val epaperModel by registerParser(
            listOf(packet(PACKET_CODE_TLM1) to byte(8)),
            BLEAdvPropertyRaw { it.firstOrNull() })

    val batteryVoltage by registerParser(
            listOf(packet(PACKET_CODE_TLM1) to byte(28)),
            BLEAdvPropertyNumber.UInt8(false) { (it ?: 0) * 0.02 })
}