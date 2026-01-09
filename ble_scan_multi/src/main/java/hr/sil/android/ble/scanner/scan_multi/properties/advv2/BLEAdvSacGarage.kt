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
import hr.sil.android.ble.scanner.scan_multi.properties.parsers.BLEAdvPropertyBool
import hr.sil.android.ble.scanner.scan_multi.properties.parsers.BLEAdvPropertyNumber
import hr.sil.android.ble.scanner.scan_multi.properties.parsers.BLEAdvPropertyRaw
import java.nio.ByteOrder

/**
 * @author mfatiga
 */
class BLEAdvSacGarage : BLEAdvV2Base() {
    val busCount by registerParser(
            listOf(packet(PACKET_CODE_TLM0) to byte(14)),
            BLEAdvPropertyNumber.UInt8(false) { it })

    val busDistance by registerParser(
            listOf(packet(PACKET_CODE_TLM0) to bytes(15, 18)),
            BLEAdvPropertyNumber.FloatingSingle(ByteOrder.LITTLE_ENDIAN, false) { it })

    val doorStatus by registerParser(
            listOf(packet(PACKET_CODE_TLM0) to byte(19)),
            BLEAdvPropertyBool())

    val uwbStatusBitmask by registerParser(
            listOf(packet(PACKET_CODE_TLM0) to byte(20)),
            BLEAdvPropertyRaw { it })
}