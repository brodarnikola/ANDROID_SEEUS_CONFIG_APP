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

import hr.sil.android.blecommunicator.core.BLECharacteristicsHolder
import hr.sil.android.blecommunicator.core.BLECommDeviceHandle
import hr.sil.android.blecommunicator.core.model.BLEReadResult
import hr.sil.android.blecommunicator.core.model.BLEWriteResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.reduce
import kotlinx.coroutines.flow.take

/**
 * @author mfatiga
 */
class BLEStreamingCharHolder(handle: BLECommDeviceHandle) : BLECharacteristicsHolder(handle) {
    //characteristics
    val charCommandWrite by characteristic("1a800001-4448-4fbc-8391-41a8137cfbc3", 244)
    val charCommandRead by characteristic("1a800002-4448-4fbc-8391-41a8137cfbc3", 244)

    //----- READ -----
    suspend fun resetRead(): Boolean =
            charCommandRead.write(byteArrayOf(0x00)).status

    suspend fun beginRead(command: StreamingCommand): Boolean =
            if (resetRead()) charCommandRead.write(command.bytes()).status else false

    suspend fun readFlow(command: StreamingCommand) =
            if (beginRead(command)) charCommandRead.readFlow() else null

    suspend fun readChannel(command: StreamingCommand, dispatcher: CoroutineDispatcher): ReceiveChannel<ByteArray>? =
            if (beginRead(command)) charCommandRead.readChannel(dispatcher) else null

    suspend fun readArray(command: StreamingCommand, maxSize: Int = 0): BLEReadResult {
        val flow = readFlow(command)
        return if (flow != null) {
            val data = flow.take(maxSize).reduce { accumulator, value -> accumulator + value }
            BLEReadResult(data, true)
        } else {
            BLEReadResult(byteArrayOf(), false)
        }
    }
    //----------------

    //----- WRITE -----
    suspend fun resetWrite(): Boolean =
            charCommandWrite.write(byteArrayOf(0x00)).status

    suspend fun beginWrite(command: StreamingCommand): Boolean =
            if (resetWrite()) charCommandWrite.write(command.bytes()).status else false

    suspend fun writeArray(command: StreamingCommand, data: ByteArray): BLEWriteResult =
            if (beginWrite(command)) charCommandWrite.write(data) else BLEWriteResult(0L, false)

    suspend fun writeFlow(command: StreamingCommand, flow: Flow<ByteArray>): BLEWriteResult =
            if (beginWrite(command)) charCommandWrite.writeFlow(flow) else BLEWriteResult(0L, false)

    suspend fun writeChannel(command: StreamingCommand, channel: ReceiveChannel<ByteArray>, concatSmallBlocks: Boolean = false): BLEWriteResult =
            if (beginWrite(command)) charCommandWrite.writeChannel(channel, concatSmallBlocks) else BLEWriteResult(0L, false)

    suspend fun writeEmpty(): BLEWriteResult = charCommandWrite.write(byteArrayOf())

    fun getMaxWriteSize(): Int =
            charCommandWrite.getMaxWriteSize()
    //-----------------
}