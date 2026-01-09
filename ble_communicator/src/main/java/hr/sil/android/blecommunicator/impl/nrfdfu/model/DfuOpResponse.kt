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

package hr.sil.android.blecommunicator.impl.nrfdfu.model

import java.util.*

/**
 * @author mfatiga
 */
data class DfuOpResponse(val requestCode: DfuOpCode, val resultCode: DfuOpResultCode, val extendedErrorCode: Int?, val result: ByteArray) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DfuOpResponse

        if (requestCode != other.requestCode) return false
        if (resultCode != other.resultCode) return false
        if (extendedErrorCode != other.extendedErrorCode) return false
        if (!Arrays.equals(result, other.result)) return false

        return true
    }

    override fun hashCode(): Int {
        var result1 = requestCode.hashCode()
        result1 = 31 * result1 + resultCode.hashCode()
        result1 = 31 * result1 + (extendedErrorCode ?: 0)
        result1 = 31 * result1 + Arrays.hashCode(result)
        return result1
    }
}