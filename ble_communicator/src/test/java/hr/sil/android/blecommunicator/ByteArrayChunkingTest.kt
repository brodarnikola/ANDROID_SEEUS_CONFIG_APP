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
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * @author mfatiga
 */
class ByteArrayChunkingTest {
    private fun generateByteArray(size: Int): ByteArray = ByteArray(size) { (it and 0xFF).toByte() }

    @Test
    fun testByteArrayChunking_divisible() {
        runByteArrayChunking(
                totalBytes = 20,
                chunkSize = 5,
                expChunkCount = 4,
                expLastChunkSize = 5
        )
    }

    @Test
    fun testByteArrayChunking_nonDivisible() {
        runByteArrayChunking(
                totalBytes = 20,
                chunkSize = 6,
                expChunkCount = 4,
                expLastChunkSize = 2
        )
    }

    @Test
    fun testByteArrayChunking_chunkSmallerThanData() {
        runByteArrayChunking(
                totalBytes = 5,
                chunkSize = 6,
                expChunkCount = 1,
                expLastChunkSize = 5
        )
    }

    @Test
    fun testByteArrayChunking_chunkSizeZero() {
        runByteArrayChunking(
                totalBytes = 5,
                chunkSize = 0,
                expChunkCount = 1,
                expLastChunkSize = 5
        )
    }

    @Test
    fun testByteArrayChunking_dataSizeZero() {
        runByteArrayChunking(
                totalBytes = 0,
                chunkSize = 5,
                expChunkCount = 1,
                expLastChunkSize = 0
        )
    }

    @Test
    fun testPerformance_native() {
        repeat(200) {
            val data = ByteArray(2_000_000).asIterable().chunked(5)
        }
    }

    @Test
    fun testPerformance_impl() {
        repeat(200) {
            val data = ByteArray(2_000_000).chunked(5)
        }
    }

    @Test
    fun testChunkSize() {
        repeat(100) { totalSize ->
            assertEquals(getChunkCountCond(totalSize, 3), getChunkCountMath(totalSize, 3))
        }
    }

    private fun getChunkCountCond(totalSize: Int, chunkSize: Int): Int {
        return (totalSize / chunkSize) + (if (totalSize % chunkSize == 0) 0 else 1)
    }

    private fun getChunkCountMath(totalSize: Int, chunkSize: Int): Int {
        return (totalSize + chunkSize - 1) / chunkSize
    }

    private fun runByteArrayChunking(
            totalBytes: Int,
            chunkSize: Int,
            expChunkCount: Int,
            expLastChunkSize: Int) {

        //in data
        val data = generateByteArray(totalBytes)
        //out data
        val chunked = data.chunked(chunkSize)

        //check total chunk count
        assertEquals("Chunks count", expChunkCount, chunked.size)

        //check each chunk size
        for ((i, chunk) in chunked.withIndex()) {
            if (i < (chunked.size - 1)) assertEquals("Chunk $i size", chunkSize, chunk.size)
            else assertEquals("Chunk $i size", expLastChunkSize, chunk.size)
        }

        //validate data in each chunk
        val assembled = chunked.reduce { acc, bytes -> byteArrayOf(*acc, *bytes) }
        assertEquals("Re-assembled size", data.size, assembled.size)

        assert(assembled.contentEquals(data))
    }
}