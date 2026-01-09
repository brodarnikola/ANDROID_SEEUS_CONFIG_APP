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

package hr.sil.android.blecommunicator.asyncble

import android.util.Log
import hr.sil.android.blecommunicator.asyncble.base.BaseAsyncBLETest
import hr.sil.android.blecommunicator.core.communicator.BLEAsyncCommunicator
import hr.sil.android.blecommunicator.core.model.BLEConnectionParameters
import hr.sil.android.blecommunicator.core.model.BLEConnectionPriority
import hr.sil.android.util.general.extensions.format
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.runBlocking
import org.junit.Test
import java.util.*

/**
 * @author mfatiga
 */
class AsyncBLEWriteTest : BaseAsyncBLETest(tag = "AsyncBLEWriteTest", debug = false) {
    companion object {
        private const val KILOBYTE = 1024
    }

    private fun generateByteArray(size: Int): ByteArray = ByteArray(size) { 0x0A }

    private fun produceByteArrays(totalSize: Int, chunk: Int) = GlobalScope.produce {
        var remainingSize = totalSize
        while (remainingSize > 0) {
            val chunkSize = if (remainingSize > chunk) chunk else remainingSize
            send(generateByteArray(chunkSize))
            remainingSize -= chunkSize
        }
    }

    @Test
    fun writeTest(): Unit = runBlocking {
        val comm = getDeviceHandle("DA:BB:83:BF:02:58")
//        val characteristic = UUID.fromString("94670032-f7e8-40a0-84e4-6942731072fe")
        val characteristic = UUID.fromString("1a800001-4448-4fbc-8391-41a8137cfbc3")
        val connectionParameters = BLEConnectionParameters(
                retryCount = 4,
                retryBackoff = 1000L,
                connectionTimeout = 10000L,
                discoverCharacteristicsTimeout = 10000L
        )

        if (comm.connect(connectionParameters)) {
            Log.d(TAG, "Connected")

            val writeChunkSize = comm.requestMTU(BLEAsyncCommunicator.GATT_MTU_MAXIMUM).mtu.maxWriteSize
            Log.d(TAG, "Chunk size = $writeChunkSize")

            val connPriority = BLEConnectionPriority.HIGH
            val connPriorityRequestSuccessful = comm.requestConnectionPriority(connPriority)
            Log.d(TAG, "Connection priority $connPriority request ${if (connPriorityRequestSuccessful) "successful" else "failed"}")

            val totalWriteSize = 20 * KILOBYTE

            val writeBeginTimestamp = now()
            val (totalBytesWritten, writeSuccessful) = comm.characteristicWriteChannel(characteristic, produceByteArrays(totalWriteSize, writeChunkSize))
            val durationSeconds = (now() - writeBeginTimestamp) / 1000.0

            Log.d(TAG, "Write ${if (writeSuccessful) "successful" else "failed"} in ${durationSeconds.format(2)}s at ${(totalBytesWritten / durationSeconds).format(2)} bytes/s")

            comm.disconnect()
            Log.d(TAG, "Done")
        } else {
            Log.d(TAG, "Connection failed!")
        }

        Unit
    }
}