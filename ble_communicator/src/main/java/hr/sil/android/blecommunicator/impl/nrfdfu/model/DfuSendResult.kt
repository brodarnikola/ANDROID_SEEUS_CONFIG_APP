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
sealed class DfuSendResult {
    class Success : DfuSendResult()
    sealed class Error : DfuSendResult() {
        class OperationError(val opCode: DfuOpCode, val resultCode: DfuOpResultCode, val extendedErrorCode: Int? = null) : Error() {
            companion object {
                fun fromOpCallResult(dfuOpCallResult: DfuOpCallResult.Error): OperationError {
                    return OperationError(
                            opCode = dfuOpCallResult.response.requestCode,
                            resultCode = dfuOpCallResult.response.resultCode,
                            extendedErrorCode = dfuOpCallResult.response.extendedErrorCode
                    )
                }
            }

            override fun toString(): String =
                    "OperationError{ opCode=$opCode, resultCode=$resultCode, extendedErrorCode=$extendedErrorCode }"
        }

        class OtherError(val throwable: Throwable?) : Error() {
            override fun toString(): String = throwable.toString()
        }
    }
}