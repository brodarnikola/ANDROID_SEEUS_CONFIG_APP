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

package hr.sil.android.blecommunicator.base

import android.os.Build
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import org.junit.Before
import org.junit.runner.RunWith

/**
 * @author mfatiga
 */
@RunWith(AndroidJUnit4::class)
abstract class BaseBLETest(protected val TAG: String) {
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
                "android.permission.BLUETOOTH_ADMIN"
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (permission in permissions) {
                InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand(
                        "pm grant ${InstrumentationRegistry.getTargetContext().packageName} $permission"
                )
            }
        }
    }

    protected fun now() = System.currentTimeMillis()
}