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
enum class MPLModemStatus(val code: Byte?) {
    UNKNOWN(null),
    TURNED_OFF(0x00),
    CONNECTING(0x01),
    CONNECTED(0x02),
    DISCONNECTING(0x03),
    SLEEP(0x04),
    POWER_SAVING(0x05);

    companion object {
        fun parse(code: Byte?) = entries.firstOrNull { it.code == code } ?: UNKNOWN
    }
}