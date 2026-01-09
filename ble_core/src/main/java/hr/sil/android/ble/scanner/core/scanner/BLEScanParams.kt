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

package hr.sil.android.ble.scanner.core.scanner

import android.bluetooth.le.ScanFilter
import hr.sil.android.ble.scanner.model.scan.BLEScanMode

/**
 * @author mfatiga
 */
data class BLEScanParams(
        val scanMode: BLEScanMode,
        val scanResultsReportDelay: Long,
        val scanFilters: List<ScanFilter>?
) {
    override fun toString(): String {
        return "ScanMode=$scanMode, " +
                "ReportDelay=$scanResultsReportDelay, " +
                "ScanFilters=${scanFilters?.toString()}"
    }
}
