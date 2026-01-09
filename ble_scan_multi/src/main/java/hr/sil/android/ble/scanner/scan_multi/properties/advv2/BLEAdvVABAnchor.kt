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

/**
 * @author mfatiga
 */
class BLEAdvVABAnchor : BLEAdvV2Base() {
    //TLM0
    val uwbAddress by registerParser(
            listOf(packet(PACKET_CODE_TLM0) to byte(14)),
            BLEAdvPropertyNumber.UInt8(false) { it })

    val batteryVoltage by registerParser(
            listOf(packet(PACKET_CODE_TLM0) to byte(15)),
            BLEAdvPropertyNumber.UInt8(false) { (it ?: 0) * 0.02 })

    val uwbOnHour by registerParser(
            listOf(packet(PACKET_CODE_TLM0) to byte(16)),
            BLEAdvPropertyNumber.UInt8(true) { it })

    val uwbOffHour by registerParser(
            listOf(packet(PACKET_CODE_TLM0) to byte(17)),
            BLEAdvPropertyNumber.UInt8(true) { it })
}