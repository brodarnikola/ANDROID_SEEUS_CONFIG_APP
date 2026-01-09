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

package hr.sil.android.blecommunicator.util.extensions

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce

/**
 * @author mfatiga
 */
fun ReceiveChannel<ByteArray>.chunked(dispatcher: CoroutineDispatcher, chunkSize: Int, concatSmallBlocks: Boolean) = GlobalScope.produce(dispatcher) {
    if (chunkSize > 0) {
        var remainingData = byteArrayOf()
        var canReceive = true
        while (true) {
            if (remainingData.size < chunkSize) {
                if (!concatSmallBlocks && remainingData.isNotEmpty()) {
                    send(remainingData)
                    remainingData = byteArrayOf()
                }

                if (canReceive) {
                    val block = receiveCatching().getOrNull() // this@chunked.receiveOrNull()
                    if (block != null) {
                        remainingData += block
                    } else {
                        canReceive = false
                    }
                } else {
                    if (remainingData.isNotEmpty()) {
                        send(remainingData)
                    }
                    break
                }
            } else if (remainingData.size == chunkSize) {
                send(remainingData)
                remainingData = byteArrayOf()
            } else {
                val chunkedRemainingData = remainingData.chunked(chunkSize)
                remainingData = byteArrayOf()
                for (remainingDataChunk in chunkedRemainingData) {
                    if (remainingDataChunk.size == chunkSize) {
                        send(remainingDataChunk)
                    } else {
                        remainingData = remainingDataChunk
                    }
                }
            }
        }
    } else {
        for (block in this@chunked) {
            send(block)
        }
    }
}