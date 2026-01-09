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

package hr.sil.android.blecommunicator.impl.characteristics.streaming

import java.util.*

/**
 * @author mfatiga
 */
data class StreamingCommand(private val commandGroup: Int, private val commandAction: Int, private val parameters: ByteArray = byteArrayOf()) {
    fun bytes() = byteArrayOf(commandGroup.toByte(), commandAction.toByte()) + parameters.take(18)
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as StreamingCommand

        if (commandGroup != other.commandGroup) return false
        if (commandAction != other.commandAction) return false
        if (!Arrays.equals(parameters, other.parameters)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = commandGroup
        result = 31 * result + commandAction
        result = 31 * result + Arrays.hashCode(parameters)
        return result
    }
}