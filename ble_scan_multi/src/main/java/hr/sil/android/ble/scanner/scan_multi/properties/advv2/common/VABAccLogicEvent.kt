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

package hr.sil.android.ble.scanner.scan_multi.properties.advv2.common

/**
 * @author mfatiga
 */
enum class VABAccLogicEvent(private val code: Byte?) {
    VABACC_UNKNOWN(null),
    VABACC_EVENT_NONE(0x00.toByte()),
    VABACC_EVENT_STOPPED(0x01.toByte()),
    VABACC_EVENT_STARTED(0x02.toByte()),
    VABACC_EVENT_ERROR(0xFF.toByte());

    companion object {
        fun parse(code: Byte?) = entries.firstOrNull { it.code == code } ?: VABACC_UNKNOWN
    }
}