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

import android.os.Build
import android.os.Environment
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import android.util.Log
import hr.sil.android.blecommunicator.core.model.BLEConnectionParameters
import hr.sil.android.blecommunicator.impl.nrfdfu.DFUHandler
import hr.sil.android.blecommunicator.impl.nrfdfu.model.DfuSendResult
import hr.sil.android.blecommunicator.impl.nrfdfu.progress.DFUProgressListener
import hr.sil.android.blecommunicator.impl.nrfdfu.progress.DFUProgressState
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.FileInputStream
import java.io.InputStream

/**
 * @author mfatiga
 */
@RunWith(AndroidJUnit4::class)
class DFUTest {
    private val TAG = "DFUTest"

    @Before
    fun enableVerboseLog() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand("setprop log.tag.$TAG VERBOSE")
            InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand("setprop log.tag.$TAG DEBUG")
        }
    }

    @Before
    fun grantPermissions() {
        val permissions = listOf(
                "android.permission.BLUETOOTH",
                "android.permission.BLUETOOTH_ADMIN",
                "android.permission.WRITE_EXTERNAL_STORAGE",
                "android.permission.READ_EXTERNAL_STORAGE",
                "android.permission.ACCESS_COARSE_LOCATION"
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (permission in permissions) {
                InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand(
                        "pm grant ${InstrumentationRegistry.getTargetContext().packageName} $permission"
                )
            }
        }
    }

    private fun debug(msg: String, throwable: Throwable? = null) {
        if (throwable != null) {
            Log.e(TAG, msg, throwable)
        } else {
            Log.d(TAG, msg)
        }
    }

    private val zipPathSdk14App = "/Download/app_dfu_package_141.zip"
    private val zipPathSdk13App = "/Download/app_dfu_package_sdk131.zip"
    private val zipPathSdk14SB = "/Download/sb_dfu_package_141.zip"
    private val zipPathSdk14All = "/Download/all_dfu_package_141.zip"
    private val zipPath = zipPathSdk13App

    private val deviceAddressDefaultSdk14 = "F5:83:38:D4:DC:05"
    private val deviceAddressInDfuModeSdk14 = "F5:83:38:D4:DC:06"

    private val deviceAddressDefaultSdk13 = "FD:F4:C0:81:BB:D8"
    private val deviceAddressInDfuModeSdk13 = "FD:F4:C0:81:BB:D9"
    private val invalidMacAddress = "AA:BB:CC:DD:WAT:FF"

    private val deviceAddress = invalidMacAddress

    private fun getFirmwareZipStream(): InputStream? {
        val path = Environment.getExternalStorageDirectory().path
        val filePath = "$path$zipPath"
        debug("Opening firmware ZIP: $filePath...")
        return try {
            FileInputStream(filePath)
        } catch (e: Exception) {
            null
        }
    }

    //log filter:
    //BLEAsyncCommunicator|BootloaderScanner|DFUHandler|DFUTest|SecureDFU|error|exception

    private val dfuProgressListener = object : DFUProgressListener {
        override fun onStateChange(state: DFUProgressState) {
            debug("dfu.onStateChange(state=$state)")
        }

        override fun onProgressChange(current: Int, max: Int) {
            debug("dfu.onProgressChange(current=$current, max=$max)")
        }

        override fun onComplete() {
            debug("dfu.onComplete()")
        }

        override fun onError(error: DfuSendResult.Error) {
            debug("dfu.onError(error=$error)")
        }
    }

    @Test
    fun testDfu() = runBlocking {
        val appContext = InstrumentationRegistry.getTargetContext()

        val connectionParameters = BLEConnectionParameters(
                retryCount = 4,
                retryBackoff = 1000L,
                attemptTimeout = 10000L,
                connectionTimeout = 20000L,
                discoverCharacteristicsTimeout = 10000L
        )

        val inputStream = getFirmwareZipStream()
        if (inputStream != null) {
            debug("Running DFU...")
            val dfuHandler = DFUHandler(appContext, dfuProgressListener)
            dfuHandler.setDebugMode(enabled = true, bleDebugEnabled = true)
            val result = dfuHandler.run(deviceAddress, connectionParameters, inputStream)

            if (result is DfuSendResult.Success) {
                debug("Success!")
            } else {
                if (result is DfuSendResult.Error.OperationError) {
                    debug("Fail! Operation error! Error=$result")
                } else {
                    debug("Fail! Other error!", (result as? DfuSendResult.Error.OtherError?)?.throwable)
                }
            }

        } else {
            debug("Failed to open firmware ZIP!")
        }
        Unit
    }

}