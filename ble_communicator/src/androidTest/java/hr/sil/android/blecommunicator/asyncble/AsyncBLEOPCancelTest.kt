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
import kotlinx.coroutines.*
import org.junit.Test

/**
 * @author mfatiga
 */
class AsyncBLEOPCancelTest : BaseAsyncBLETest("AsyncBLEOPCancelTest") {
    @Test
    fun connCancelTest(): Unit = runBlocking {
        //invalid MAC for a non-existent device
        val comm = getDeviceHandle("99:99:99:99:99:99")
        val connectionParameters = BLEConnectionParameters(
                //no retries
                retryCount = 0,
                retryBackoff = 1000L,

                //long connection timeout to be able to cancel it
                connectionTimeout = 20000L
        )

        //begin connecting asynchronously
        Log.d(TAG, "CALL:connect()")
        val connect = GlobalScope.async { comm.connect(connectionParameters) }

        //wait for a second
        delay(1000)

        //cancel the connection
        Log.d(TAG, "CALL:connect.cancel()")
        connect.cancel()

        //try to connect immediately after cancel
        Log.d(TAG, "CALL:reconnect()")
        val reconnect = GlobalScope.asyncó { comm.connect(connectionParameters) }

        //wait for a second
        delay(1000)

        //cancel the connection
        Log.d(TAG, "CALL:reconnect.cancel()")
        reconnect.cancel()

        Unit
    }
}