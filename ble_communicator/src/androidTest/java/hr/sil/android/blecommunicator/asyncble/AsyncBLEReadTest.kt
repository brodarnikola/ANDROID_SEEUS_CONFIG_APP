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
import hr.sil.android.blecommunicator.core.model.BLEConnectionParameters
import hr.sil.android.util.general.extensions.format
import kotlinx.coroutines.runBlocking
import org.junit.Test
import java.util.*

/**
 * @author mfatiga
 */
class AsyncBLEReadTest : BaseAsyncBLETest("AsyncBLEReadTest") {
    companion object {
        private const val KILOBYTE = 1024
    }

    @Test
    fun readTest(): Unit = runBlocking {
        val comm = getDeviceHandle("DA:BB:83:BF:02:58")
        val characteristic = UUID.fromString("94670032-f7e8-40a0-84e4-6942731072fe")
        val connectionParameters = BLEConnectionParameters(
                retryCount = 9,
                retryBackoff = 1000L,
                connectionTimeout = 10000L,
                discoverCharacteristicsTimeout = 10000L
        )

        if (comm.connect(connectionParameters)) {
            Log.d(TAG, "Connected")

            val maxReadSize = 1 * KILOBYTE
            var totalBytesRead = 0

            val readBeginTimestamp = now()
            var allSuccessful = true
            while (true) {
                val readResult = comm.characteristicRead(characteristic)
                if (!readResult.status) {
                    allSuccessful = false
                    break
                } else {
                    val chunk = readResult.data.size
                    totalBytesRead += chunk
                    if (totalBytesRead > maxReadSize) {
                        break
                    }
                }
            }
            val durationSeconds = (now() - readBeginTimestamp) / 1000.0
            val speedBytesPerSecond = totalBytesRead / durationSeconds
            Log.d(TAG, "Read ${if (allSuccessful) "successful" else "failed"} in ${durationSeconds.format(2)}s at ${speedBytesPerSecond.format(2)} bytes/s")

            comm.disconnect()
            Log.d(TAG, "Done")
        } else {
            Log.d(TAG, "Connection failed!")
        }

        Unit
    }
}