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

package hr.sil.android.ble.scanner.scan_multi.model

/**
 * @author mfatiga
 */
enum class BLEAdvProtocolVersion(
        val priority: Int,
        val manufacturers: List<BLEManufacturer>,
        val deviceTypeByteIndex: Int) {

    UNKNOWN(
            priority = -1,
            manufacturers = listOf(),
            deviceTypeByteIndex = -1
    ),

    //iBeacon (SIL legacy beacons)
    V0(
            priority = 0,
            manufacturers = listOf(BLEManufacturer.APPLE),
            deviceTypeByteIndex = 32
    ),

    //SIL custom - transitional (SmartLetterbox and PublicBike)
    V1(
            priority = 1,
            manufacturers = listOf(BLEManufacturer.SIL_T),
            deviceTypeByteIndex = 39
    ),

    //SIL custom - neo (newer SIL devices)
    V2(
            priority = 2,
            manufacturers = listOf(BLEManufacturer.SIL_N, BLEManufacturer.SIL_N_BOOT),
            deviceTypeByteIndex = 29
    ),

    //SIL dynamic
    V3(
            priority = 3,
            manufacturers = listOf(BLEManufacturer.SIL_DYNAMIC, BLEManufacturer.SIL_DYNAMIC_BOOT),
            deviceTypeByteIndex = -1
    );

    companion object {
        fun forManufacturer(manufacturer: BLEManufacturer): BLEAdvProtocolVersion =
                entries.firstOrNull { it.manufacturers.contains(manufacturer) } ?: UNKNOWN
    }
}