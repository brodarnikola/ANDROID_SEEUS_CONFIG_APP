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

package hr.sil.android.blecommunicator

import hr.sil.android.blecommunicator.util.extensions.chunked
import kotlinx.coroutines.CommonPool
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * @author mfatiga
 */
class ByteArrayChannelChunkingTest {
    private fun generateByteArray(size: Int): ByteArray = ByteArray(size) { (it and 0xFF).toByte() }

    private fun produceByteArrays(totalSize: Int, chunk: Int) = produce(CommonPool) {
        var remainingSize = totalSize
        while (remainingSize > 0) {
            val chunkSize = if (remainingSize > chunk) chunk else remainingSize
            send(generateByteArray(chunkSize))
            remainingSize -= chunkSize
        }
    }

    @Test
    fun byteArrayChannelChunking1() {
        runChannelChunking(
                inDataSize = 20,
                inChunkSize = 20,
                outChunkSize = 5,
                concatSmallBlocks = false,
                expectedChunkSizes = intArrayOf(5, 5, 5, 5)
        )
    }

    @Test
    fun byteArrayChannelChunking2() {
        runChannelChunking(
                inDataSize = 20,
                inChunkSize = 7,
                outChunkSize = 5,
                concatSmallBlocks = false,
                expectedChunkSizes = intArrayOf(5, 2, 5, 2, 5, 1)
        )
    }

    @Test
    fun byteArrayChannelChunking3() {
        runChannelChunking(
                inDataSize = 20,
                inChunkSize = 7,
                outChunkSize = 5,
                concatSmallBlocks = true,
                expectedChunkSizes = intArrayOf(5, 5, 5, 5)
        )
    }

    @Test
    fun byteArrayChannelChunking4() {
        runChannelChunking(
                inDataSize = 20,
                inChunkSize = 3,
                outChunkSize = 5,
                concatSmallBlocks = true,
                expectedChunkSizes = intArrayOf(5, 5, 5, 5)
        )
    }

    @Test
    fun byteArrayChannelChunking5() {
        runChannelChunking(
                inDataSize = 20,
                inChunkSize = 3,
                outChunkSize = 5,
                concatSmallBlocks = false,
                expectedChunkSizes = intArrayOf(3, 3, 3, 3, 3, 3, 2)
        )
    }

    @Test
    fun byteArrayChannelChunking6() {
        runChannelChunking(
                inDataSize = 13,
                inChunkSize = 7,
                outChunkSize = 5,
                concatSmallBlocks = true,
                expectedChunkSizes = intArrayOf(5, 5, 3)
        )
    }

    @Test
    fun byteArrayChannelChunking7() {
        runChannelChunking(
                inDataSize = 13,
                inChunkSize = 7,
                outChunkSize = 5,
                concatSmallBlocks = false,
                expectedChunkSizes = intArrayOf(5, 2, 5, 1)
        )
    }

    private fun runChannelChunking(
            inDataSize: Int,
            inChunkSize: Int,
            outChunkSize: Int,
            concatSmallBlocks: Boolean,
            expectedChunkSizes: IntArray) = runBlocking {

        val chunked = mutableListOf<ByteArray>()
        val channel = produceByteArrays(inDataSize, inChunkSize).chunked(CommonPool, outChunkSize, concatSmallBlocks)
        try {
            for (data in channel) {
                chunked.add(data)
            }
        } catch (ex: Exception) {
//            ex.printStackTrace()
        }

        //check total chunk count
        assertEquals("Chunks count", expectedChunkSizes.size, chunked.size)

        //check each chunk size
        for ((i, chunk) in chunked.withIndex()) {
            assertEquals("Chunk $i size", expectedChunkSizes[i], chunk.size)
        }

        Unit
    }
}