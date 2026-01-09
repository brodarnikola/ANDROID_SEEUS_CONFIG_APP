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

package hr.sil.android.blecommunicator.core.characteristics.behaviors.impl.writable

import hr.sil.android.blecommunicator.core.BLECommDeviceHandle
import hr.sil.android.blecommunicator.core.characteristics.behaviors.BLECharWritable
import hr.sil.android.blecommunicator.core.characteristics.behaviors.base.BaseBLECharBehavior
import hr.sil.android.blecommunicator.core.model.BLEWriteResult
import hr.sil.android.blecommunicator.util.extensions.chunked
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import java.util.*

/**
 * @author mfatiga
 */
internal class BLECharIsWritable(
        private val comm: BLECommDeviceHandle,
        private val uuid: UUID,
        private val maxBlockSize: Int) : BaseBLECharBehavior(), BLECharWritable {

    override fun isWritable() = true

    override fun getMaxWriteSize(): Int {
        val charSize = if (maxBlockSize == 0) Int.MAX_VALUE else maxBlockSize
        return minOf(comm.getMTU().maxWriteSize, charSize)
    }

    private fun chunkToChannel(dispatcher: CoroutineDispatcher, data: ByteArray) = GlobalScope.produce(dispatcher) {
        val blockSize = getMaxWriteSize()
        for (chunk in data.chunked(blockSize)) {
            if (isClosedForSend) break
            else send(chunk)
        }
    }

    override suspend fun write(data: ByteArray, forceLargeWrite: Boolean): BLEWriteResult {
        val blockSize = getMaxWriteSize()
        return if (forceLargeWrite || (data.size <= blockSize || blockSize == 0)) {
            comm.characteristicWrite(uuid, data, timeoutMillis)
        } else {
//            comm.characteristicWriteChannel(uuid, chunkToChannel(Dispatchers.Default, data), timeoutMillis)
            var totalWritten = 0L
            var isSuccessful = true
            for (block in data.chunked(blockSize)) {
                val (size, status) = comm.characteristicWrite(uuid, block, timeoutMillis)
                if (status) {
                    totalWritten += size
                } else {
                    isSuccessful = false
                    break
                }
            }
            BLEWriteResult(totalWritten, isSuccessful)
        }
    }

    override suspend fun writeChannel(channel: ReceiveChannel<ByteArray>, concatSmallBlocks: Boolean): BLEWriteResult {
        val dispatcher = Dispatchers.Default
        val blockSize = getMaxWriteSize()
        return comm.characteristicWriteChannel(
                uuid,
                channel.chunked(dispatcher, blockSize, concatSmallBlocks),
                timeoutMillis)
    }

    override suspend fun writeFlow(flow: Flow<ByteArray>): BLEWriteResult {
        val blockSize = getMaxWriteSize()
        var totalWritten = 0L
        var isSuccessful = true
        flow.collect {
            for (block in it.chunked(blockSize)) {
                val (size, status) = comm.characteristicWrite(uuid, block, timeoutMillis)
                if (status) {
                    totalWritten += size
                } else {
                    isSuccessful = false
                    break
                }
            }
        }
        return BLEWriteResult(totalWritten, isSuccessful)
    }
}