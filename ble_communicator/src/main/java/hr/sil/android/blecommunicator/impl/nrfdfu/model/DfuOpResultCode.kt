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
enum class DfuOpResultCode(val code: Byte, val description: String) {
    UNKNOWN(0xFF.toByte(), ""),
    INVALID_CODE(0x00.toByte(), "The provided opcode was missing or malformed."),
    SUCCESS(0x01.toByte(), "The operation completed successfully."),
    OPCODE_NOT_SUPPORTED(0x02.toByte(), "The provided opcode was invalid."),
    INVALID_PARAMETER(0x03.toByte(), "A parameter for the opcode was missing."),
    INSUFFICIENT_RESOURCES(0x04.toByte(), "There was not enough memory for the data object."),
    INVALID_OBJECT(0x05.toByte(), "The data object did not match the firmware and hardware requirements, the signature was missing, or parsing the command failed."),
    UNSUPPORTED_TYPE(0x07.toByte(), "The provided object type was not valid for a Create or Read operation."),
    OPERATION_NOT_PERMITTED(0x08.toByte(), "The state of the DFU process did not allow this operation."),
    OPERATION_FAILED(0x0A.toByte(), "The operation failed."),
    EXTENDED_ERROR(0x0B.toByte(), "The DFU module has encountered the specified error. See nrf_dfu_ext_error_code_t for the possible error codes.");

    companion object {
        fun parse(code: Byte) = DfuOpResultCode.values().firstOrNull { it.code == code } ?: UNKNOWN
    }
}