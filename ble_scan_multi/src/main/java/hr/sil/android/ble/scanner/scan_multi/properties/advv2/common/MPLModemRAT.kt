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
enum class MPLModemRAT(val code: Byte) {
    GSM(0.toByte()),
    GSM_COMPACT(1.toByte()),
    UTRAN(2.toByte()),
    EGPRS(3.toByte()),
    HSDPA(4.toByte()),
    HSUPA(5.toByte()),
    HSDPA_HSUPA(6.toByte()),
    E_UTRAN(7.toByte()),
    CATM1(8.toByte()),
    NB1(9.toByte()),
    UNKNOWN(10.toByte());

    companion object {
        fun parse(code: Byte?) = entries.firstOrNull { it.code == code }
    }
}