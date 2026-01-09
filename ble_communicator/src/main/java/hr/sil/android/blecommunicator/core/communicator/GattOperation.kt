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

package hr.sil.android.blecommunicator.core.communicator

import hr.sil.android.blecommunicator.core.model.BLEConnectionPriority
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.ReceiveChannel
import java.util.*

/**
 * @author mfatiga
 */
internal sealed class GattOperation<GATT, API>(private val name: String, val deviceAddress: String, timeoutMillis: Long) {
    override fun toString(): String = "$name[$deviceAddress]"

    companion object {
        const val DEFAULT_TIMEOUT = 10_000L
    }

    val timeoutMillis: Long = if (timeoutMillis > 0) timeoutMillis else DEFAULT_TIMEOUT

    //GATT deferred
    internal open val gattDeferred = CompletableDeferred<OperationResult<GATT>>()

    //Internal API deferred
    protected val apiDeferred = CompletableDeferred<OperationResult<API>>()

    //Internal GATT cleanup completion
    protected val cleanupJob = Job()

    //API await and completion
    open suspend fun awaitResult(): OperationResult<API> = apiDeferred.await()

    //GATT cleanup completion await
    open suspend fun awaitCleanup() = cleanupJob.join()

    fun complete(result: OperationResult<API>) {
        apiDeferred.complete(result)
        cleanupJob.cancel()
    }

    /**
     * The NOP operation is completed upon creation
     */
    class NOP : GattOperation<Unit, Unit>("NOP", "", 0) {
        override suspend fun awaitResult(): OperationResult<Unit> {
            apiDeferred.complete(OperationResult(true))
            return apiDeferred.await()
        }

        override suspend fun awaitCleanup() {
            cleanupJob.cancel()
            cleanupJob.join()
        }
    }

    class Connect(deviceAddress: String, timeoutMillis: Long)
        : GattOperation<Unit, Unit>("Connect", deviceAddress, timeoutMillis)

    class Disconnect(deviceAddress: String, timeoutMillis: Long)
        : GattOperation<Unit, Unit>("Disconnect", deviceAddress, timeoutMillis)

    class DiscoverCharacteristics(deviceAddress: String, timeoutMillis: Long)
        : GattOperation<Unit, Unit>("DiscoverCharacteristics", deviceAddress, timeoutMillis)

    class CharacteristicWrite(deviceAddress: String, timeoutMillis: Long, val characteristic: UUID, val data: ByteArray)
        : GattOperation<Unit, Long>("CharacteristicWrite", deviceAddress, timeoutMillis)

    class CharacteristicWriteChannel(deviceAddress: String, timeoutMillis: Long, val characteristic: UUID, val channel: ReceiveChannel<ByteArray>)
        : GattOperation<Unit, Long>("CharacteristicWriteChannel", deviceAddress, timeoutMillis) {

        private var _gattDeferred = CompletableDeferred<OperationResult<Unit>>()

        override val gattDeferred: CompletableDeferred<OperationResult<Unit>>
            get() = _gattDeferred

        fun recreateGattDeferred() {
            _gattDeferred = CompletableDeferred()
        }
    }

    class CharacteristicRead(deviceAddress: String, timeoutMillis: Long, val characteristic: UUID)
        : GattOperation<ByteArray, ByteArray>("CharacteristicRead", deviceAddress, timeoutMillis)

    class DescriptorWrite(deviceAddress: String, timeoutMillis: Long, val characteristic: UUID, val descriptor: UUID, val data: ByteArray)
        : GattOperation<Unit, Unit>("DescriptorWrite", deviceAddress, timeoutMillis)

    class DescriptorRead(deviceAddress: String, timeoutMillis: Long, val characteristic: UUID, val descriptor: UUID)
        : GattOperation<ByteArray, ByteArray>("DescriptorRead", deviceAddress, timeoutMillis)

    class SetNotification(deviceAddress: String, timeoutMillis: Long, val characteristic: UUID, val enable: Boolean)
        : GattOperation<Unit, Unit>("SetNotification", deviceAddress, timeoutMillis)

    class RequestMTU(deviceAddress: String, timeoutMillis: Long, val mtu: Int)
        : GattOperation<Int, Int>("RequestMTU", deviceAddress, timeoutMillis)

    class RequestConnectionPriority(deviceAddress: String, timeoutMillis: Long, val connectionPriority: BLEConnectionPriority)
        : GattOperation<Unit, Unit>("RequestConnectionPriority", deviceAddress, timeoutMillis)

    class ReadRemoteRSSI(deviceAddress: String, timeoutMillis: Long)
        : GattOperation<Int, Int>("ReadRemoteRSSI", deviceAddress, timeoutMillis)
}