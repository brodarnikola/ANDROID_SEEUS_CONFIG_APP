/* SWISS INNOVATION LAB CONFIDENTIAL
*
* www.swissinnolab.com
* __________________________________________________________________________
*
* [2016] - [2017] Swiss Innovation Lab AG
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

import hr.sil.android.ble.scanner.scan_multi.util.extensions.hexToByteArray
import java.util.*

/**
 * @author mfatiga
 */
enum class BLEManufacturer(val code: ByteArray) {
    //unknown manufacturer
    UNKNOWN(byteArrayOf()),

    SIL_T("BABA".hexToByteArray()), //SIL - transitional protocol (used in SmartLetterbox and PublicBike projects)

    SIL_N("DEDA".hexToByteArray()), //SIL - neo protocol - supported background parsing on iOS
    SIL_N_BOOT("DEDB".hexToByteArray()), //SIL - neo protocol in bootloader mode

    SIL_DYNAMIC("BA11".hexToByteArray()), //SIL - dynamic protocol
    SIL_DYNAMIC_BOOT("BA12".hexToByteArray()), //SIL - dynamic protocol in bootloader mode

    //APPLE manufacturer - used in SIL legacy adv. parsing
    APPLE("4C00".hexToByteArray());

    fun isSIL() = this in listOf(
            SIL_T,
            SIL_N,
            SIL_N_BOOT,
            SIL_DYNAMIC,
            SIL_DYNAMIC_BOOT)

    fun isSILBootloader() = this in listOf(
            SIL_N_BOOT,
            SIL_DYNAMIC_BOOT)

    companion object {
        fun parse(code: ByteArray) = values().firstOrNull { it.code.contentEquals(code) } ?: UNKNOWN
    }
}