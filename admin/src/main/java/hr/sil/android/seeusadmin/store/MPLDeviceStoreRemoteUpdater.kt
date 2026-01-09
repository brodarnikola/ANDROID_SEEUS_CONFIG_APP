/* SWISS INNOVATION LAB CONFIDENTIAL
*
* www.swissinnolab.com
* __________________________________________________________________________
*
* [2016] - [2018] Swiss Innovation Lab AG
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

package hr.sil.android.seeusadmin.store

import hr.sil.android.mplhuber.core.remote.WSSeeUsAdmin
import hr.sil.android.mplhuber.core.util.logger
import hr.sil.android.seeusadmin.util.backend.UserUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

/**
 * @author mfatiga
 */
object MPLDeviceStoreRemoteUpdater {
    private val log = logger()

    private const val UPDATE_PERIOD = 10000L

    private val running = AtomicBoolean(false)

    fun run() {
        if (running.compareAndSet(false, true)) {
            GlobalScope.launch(Dispatchers.Default) {
                while (true) {
                    try {
                        handleUpdate()
                    } catch (ex: Exception) {
                        log.error("Periodic remote-update failed...", ex)
                    }

                    delay(UPDATE_PERIOD)
                }
            }
        }
    }

    suspend fun forceUpdate(propagateEvent: Boolean = true) {
        handleUpdate(propagateEvent)
    }

    private val inHandleUpdate = AtomicBoolean(false)
    private suspend fun handleUpdate(propagateEvent: Boolean = true) {
        if (inHandleUpdate.compareAndSet(false, true)) {
            if (UserUtil.isUserLoggedIn()) {
                doUpdate(propagateEvent)
            }
            inHandleUpdate.set(false)
        }
    }

    private suspend fun doUpdate(propagateEvent: Boolean = true) {
        val stations =  WSSeeUsAdmin.getStationUnits()?: listOf()
        DeviceStore.updateFromRemote(
                stations,
                propagateEvent
        )
    }


}