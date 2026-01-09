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
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Test

/**
 * @author mfatiga
 */
class AsyncBLEConnTest : BaseAsyncBLETest("AsyncBLEConnTest") {
    @Test
    fun connectTest(): Unit = runBlocking {
        val comm = getDeviceHandle("DA:BB:83:BF:02:58")
        val connectionParameters = BLEConnectionParameters(
                retryCount = 0,
                retryBackoff = 1000L,
                attemptTimeout = 10000L,
                connectionTimeout = 20000L,
                discoverCharacteristicsTimeout = 10000L
        )

        val totalTestCount = 100
        var connectSucceeded = 0
        var connectFailed = 0
        var connectSuccessTotalTime = 0L
        var lowestConnectSuccessTime = Long.MAX_VALUE
        var highestConnectSuccessTime = Long.MIN_VALUE

        var disconnectSucceeded = 0
        var disconnectFailed = 0
        var disconnectSuccessTotalTime = 0L

        var status133Count = 0
        var status133TotalTime = 0L

        var connectStartTimestamp = 0L
        comm.setOnConnectionStateChangeListener { status, _ ->
            if (status == 133) {
                status133TotalTime += now() - connectStartTimestamp
                status133Count++
            }
        }

        for (i in 1..totalTestCount) {
            delay(1000)

            connectStartTimestamp = now()
            if (comm.connect(connectionParameters)) {
                val connectTime = now() - connectStartTimestamp
                connectSuccessTotalTime += connectTime
                if (connectTime < lowestConnectSuccessTime) {
                    lowestConnectSuccessTime = connectTime
                }
                if (connectTime > highestConnectSuccessTime) {
                    highestConnectSuccessTime = connectTime
                }
                connectSucceeded++

                delay(1000)

                val disconnectStartTimestamp = now()
                if (comm.disconnect()) {
                    disconnectSuccessTotalTime += now() - disconnectStartTimestamp

                    disconnectSucceeded++
                } else {
                    disconnectFailed++
                }
            } else {
                disconnectSucceeded++
                connectFailed++
            }
        }

        comm.removeOnConnectionStateListener()

        //output statistics to log:

        val logResult = mutableListOf<MutableList<String>>()

        val logConnect = mutableListOf<String>()
        logConnect.add("connected $connectSucceeded/$totalTestCount")
        if (connectSucceeded > 0) {
            logConnect.add("avg ${((connectSuccessTotalTime / connectSucceeded) / 1000.0).format(2)}s")
            logConnect.add("fastest ${(lowestConnectSuccessTime / 1000.0).format(2)}s")
            logConnect.add("slowest ${(highestConnectSuccessTime / 1000.0).format(2)}s")
        }
        logResult.add(logConnect)

        val logS133 = mutableListOf<String>()
        logS133.add("status133Error $status133Count/$connectFailed")
        if (status133Count > 0) {
            logS133.add("avg ${((status133TotalTime / status133Count) / 1000.0).format(2)}s")
        }
        logResult.add(logS133)

        val logDisconnect = mutableListOf<String>()
        logDisconnect.add("disconnected $disconnectSucceeded/$totalTestCount")
        if (disconnectSucceeded > 0) {
            logDisconnect.add("avg ${((disconnectSuccessTotalTime / disconnectSucceeded) / 1000.0).format(2)}s")
        }
        logResult.add(logDisconnect)

        val logResultString = logResult.map { it.joinToString(", ") { it } }.joinToString("\n") { it }
        Log.d(TAG, "STATS:\n$logResultString")

        Unit
    }
}