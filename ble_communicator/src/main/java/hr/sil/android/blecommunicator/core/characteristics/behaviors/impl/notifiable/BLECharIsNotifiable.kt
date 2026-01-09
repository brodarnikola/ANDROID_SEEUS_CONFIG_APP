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

package hr.sil.android.blecommunicator.core.characteristics.behaviors.impl.notifiable

import hr.sil.android.blecommunicator.core.BLECommDeviceHandle
import hr.sil.android.blecommunicator.core.characteristics.behaviors.BLECharNotifiable
import hr.sil.android.blecommunicator.core.characteristics.behaviors.base.BaseBLECharBehavior
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.produce
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * @author mfatiga
 */
internal class BLECharIsNotifiable(
        private val comm: BLECommDeviceHandle,
        private val uuid: UUID) : BaseBLECharBehavior(), BLECharNotifiable {

    override fun isNotifiable() = true

    override fun notifyOnChannel() = notifyOnChannel(Dispatchers.Default)

    override fun notifyOnChannel(dispatcher: CoroutineDispatcher) = GlobalScope.produce<ByteArray>(dispatcher) {
        var lock = Job()

        val queue = ConcurrentLinkedQueue<ByteArray>()
        val success = withContext(NonCancellable) {
            comm.characteristicNotifyEnable(uuid, timeoutMillis) { data ->
                queue.add(data)
                synchronized(lock) {
                    lock.cancel()
                }
            }
        }
        if (success) {
            try {
                while (isActive) {
                    val data = queue.poll()
                    if (data != null) {
                        send(data)
                    } else {
                        lock.join()
                        synchronized(lock) {
                            lock = Job()
                        }
                    }
                }
            } finally {
                withContext(NonCancellable) {
                    close()
                    comm.characteristicNotifyDisable(uuid, timeoutMillis)
                }
            }
        } else {
            close()
        }
    }
}