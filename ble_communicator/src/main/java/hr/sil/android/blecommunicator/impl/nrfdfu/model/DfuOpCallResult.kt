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

/**
 * @author mfatiga
 */
sealed class DfuOpCallResult<out T : Any>(val response: DfuOpResponse) {
    class Success<out T : Any>(response: DfuOpResponse, val data: T) : DfuOpCallResult<T>(response)
    class Error(response: DfuOpResponse) : DfuOpCallResult<Nothing>(response) {
        fun getLogString(): String {
            val errorDescription = this.response.resultCode.description
            val extendedErrorMsg = if (this.response.resultCode == DfuOpResultCode.EXTENDED_ERROR) {
                " Extended error code ${this.response.extendedErrorCode}"
            } else {
                ""
            }
            return "Operation call error! $errorDescription$extendedErrorMsg"
        }
    }
}