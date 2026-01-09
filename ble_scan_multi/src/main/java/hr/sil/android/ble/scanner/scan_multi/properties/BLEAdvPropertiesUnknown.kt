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

package hr.sil.android.ble.scanner.scan_multi.properties

import hr.sil.android.ble.scanner.model.scan.BLERawScanResult
import hr.sil.android.ble.scanner.parser.btcore.BTCoreAdvertisement
import hr.sil.android.ble.scanner.scan_multi.model.BLEManufacturer
import hr.sil.android.ble.scanner.scan_multi.properties.base.BLEAdvProperties

/**
 * @author mfatiga
 */
class BLEAdvPropertiesUnknown : BLEAdvProperties() {
    override fun getPacketCode(manufacturer: BLEManufacturer, rawScanResult: BLERawScanResult, btCoreAdvertisement: BTCoreAdvertisement): Byte = 0x00.toByte()
}