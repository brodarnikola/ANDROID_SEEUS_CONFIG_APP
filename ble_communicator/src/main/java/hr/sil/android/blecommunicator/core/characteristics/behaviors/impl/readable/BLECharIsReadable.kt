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

package hr.sil.android.blecommunicator.core.characteristics.behaviors.impl.readable

import hr.sil.android.blecommunicator.core.BLECommDeviceHandle
import hr.sil.android.blecommunicator.core.characteristics.behaviors.BLECharReadable
import hr.sil.android.blecommunicator.core.characteristics.behaviors.base.BaseBLECharBehavior
import hr.sil.android.blecommunicator.core.model.BLEReadResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import java.util.*

/**
 * @author mfatiga
 */
internal class BLECharIsReadable(
        private val comm: BLECommDeviceHandle,
        private val uuid: UUID) : BaseBLECharBehavior(), BLECharReadable {

    override fun isReadable() = true

    override suspend fun read(): BLEReadResult = comm.characteristicRead(uuid, timeoutMillis)

    override fun readChannel(dispatcher: CoroutineDispatcher) = GlobalScope.produce(dispatcher) {
        while (isActive) {
            // read data with status and send it through the channel
            // and if the data is not valid or the reading fails, end the reading process
            val (data, status) = read()
            if (status && data.isNotEmpty()) {
                send(data)
            } else {
                break
            }
        }
    }

    override fun readFlow() = flow {
        try {
            while (true) {
                val (data, status) = read()
                if (status && data.isNotEmpty()) {
                    emit(data)
                } else {
                    break
                }
            }
        } finally {
            //on cancelled
        }
    }
}