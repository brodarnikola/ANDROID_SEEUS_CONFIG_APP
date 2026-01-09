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

package hr.sil.android.blecommunicator.asyncble.base

import android.support.test.InstrumentationRegistry
import hr.sil.android.blecommunicator.core.BLECommDeviceHandle
import hr.sil.android.blecommunicator.base.BaseBLETest

/**
 * @author mfatiga
 */
abstract class BaseAsyncBLETest(tag: String, private val debug: Boolean = true) : BaseBLETest(tag) {
    protected fun getDeviceHandle(deviceAddress: String): BLECommDeviceHandle {
        val handle = BLECommDeviceHandle.create(InstrumentationRegistry.getTargetContext(), deviceAddress)
        handle.setDebugMode(enabled = debug, logThreadName = debug)
        return handle
    }
}