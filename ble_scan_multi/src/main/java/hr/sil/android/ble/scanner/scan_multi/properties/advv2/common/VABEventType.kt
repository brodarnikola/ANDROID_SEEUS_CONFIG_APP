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
enum class VABEventType(private val code: Byte?) {
    UNKNOWN(null),
    EVENT_GPS(0x00.toByte()),
    EVENT_UWB(0x01.toByte()),
    EVENT_GPS_ENTRY(0x02.toByte()),
    EVENT_GPS_EXIT(0x03.toByte()),
    EVENT_WAKEUP(0xFF.toByte());

    companion object {
        fun parse(code: Byte?) = entries.firstOrNull { it.code == code } ?: UNKNOWN
    }
}