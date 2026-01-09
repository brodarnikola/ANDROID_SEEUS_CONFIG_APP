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

package hr.sil.android.blecommunicator.core

import android.bluetooth.BluetoothDevice
import android.content.Context
import hr.sil.android.blecommunicator.core.communicator.BLEAsyncCommunicator
import hr.sil.android.blecommunicator.core.model.BLEConnectionParameters
import hr.sil.android.blecommunicator.core.model.BLEConnectionPriority
import hr.sil.android.blecommunicator.core.model.BLEMtu
import kotlinx.coroutines.channels.ReceiveChannel
import java.util.*

/**
 * BLE Device handle that delegates method calls to the [BLEAsyncCommunicator]
 *
 * @author mfatiga
 */
class BLECommDeviceHandle private constructor(private val deviceAddress: String, private val comm: BLEAsyncCommunicator) {
    companion object {
        @JvmStatic
        fun create(context: Context, deviceAddress: String): BLECommDeviceHandle =
                BLECommDeviceHandle(
                        deviceAddress,
                        BLEAsyncCommunicator.getInstanceForApplication(context)
                )
    }

    /**
     * @see BLEAsyncCommunicator.setDebugMode
     */
    fun setDebugMode(enabled: Boolean, logThreadName: Boolean = false) =
            comm.setDebugMode(enabled, logThreadName)

    /**
     * @see BLEAsyncCommunicator.getCharacteristics
     */
    fun getCharacteristics() =
            comm.getCharacteristics(deviceAddress)

    /**
     * @see BLEAsyncCommunicator.doesCharacteristicExist
     */
    fun doesCharacteristicExist(characteristic: UUID) =
            comm.doesCharacteristicExist(deviceAddress, characteristic)

    /**
     * @see BLEAsyncCommunicator.isCharacteristicWritable
     */
    fun isCharacteristicWritable(characteristic: UUID): Boolean =
            comm.isCharacteristicWritable(deviceAddress, characteristic)

    /**
     * @see BLEAsyncCommunicator.isCharacteristicReadable
     */
    fun isCharacteristicReadable(characteristic: UUID): Boolean =
            comm.isCharacteristicReadable(deviceAddress, characteristic)

    /**
     * @see BLEAsyncCommunicator.isCharacteristicNotifiable
     */
    fun isCharacteristicNotifiable(characteristic: UUID): Boolean =
            comm.isCharacteristicNotifiable(deviceAddress, characteristic)

    /**
     * @see BLEAsyncCommunicator.getBluetoothDevice
     */
    fun getBluetoothDevice(): BluetoothDevice? = comm.getBluetoothDevice(deviceAddress)

    /**
     * @see BLEAsyncCommunicator.getMTU
     */
    fun getMTU(): BLEMtu =
            comm.getMTU(deviceAddress)

    /**
     * @see BLEAsyncCommunicator.getConnectionState
     */
    fun getConnectionState() =
            comm.getConnectionState(deviceAddress)

    /**
     * @see BLEAsyncCommunicator.isConnected
     */
    fun isConnected() =
            comm.isConnected(deviceAddress)

    /**
     * @see BLEAsyncCommunicator.setOnConnectionStateChangeListener
     */
    fun setOnConnectionStateChangeListener(listener: (status: Int, newState: Int) -> Unit) =
            comm.setOnConnectionStateChangeListener(deviceAddress, listener)

    /**
     * @see BLEAsyncCommunicator.removeOnConnectionStateListener
     */
    fun removeOnConnectionStateListener() =
            comm.removeOnConnectionStateListener(deviceAddress)

    /**
     * @see BLEAsyncCommunicator.connect
     */
    suspend fun connect(connectionParameters: BLEConnectionParameters = BLEConnectionParameters()) =
            comm.connect(deviceAddress, connectionParameters)

    /**
     * @see BLEAsyncCommunicator.disconnect
     */
    suspend fun disconnect(timeoutMillis: Long = 0L) =
            comm.disconnect(deviceAddress, timeoutMillis)

    /**
     * @see BLEAsyncCommunicator.characteristicWrite
     */
    suspend fun characteristicWrite(characteristic: UUID, data: ByteArray, timeoutMillis: Long = 0L) =
            comm.characteristicWrite(deviceAddress, characteristic, data, timeoutMillis)

    /**
     * @see BLEAsyncCommunicator.characteristicWriteChannel
     */
    suspend fun characteristicWriteChannel(characteristic: UUID, channel: ReceiveChannel<ByteArray>, timeoutMillis: Long = 0L) =
            comm.characteristicWriteChannel(deviceAddress, characteristic, channel, timeoutMillis)

    /**
     * @see BLEAsyncCommunicator.characteristicRead
     */
    suspend fun characteristicRead(characteristic: UUID, timeoutMillis: Long = 0L) =
            comm.characteristicRead(deviceAddress, characteristic, timeoutMillis)

    /**
     * @see BLEAsyncCommunicator.characteristicNotifyEnable
     */
    suspend fun characteristicNotifyEnable(characteristic: UUID, timeoutMillis: Long = 0L, onCharacteristicChanged: (ByteArray) -> Unit) =
            comm.characteristicNotifyEnable(deviceAddress, characteristic, timeoutMillis, onCharacteristicChanged)

    /**
     * @see BLEAsyncCommunicator.characteristicNotifyDisable
     */
    suspend fun characteristicNotifyDisable(characteristic: UUID, timeoutMillis: Long = 0L) =
            comm.characteristicNotifyDisable(deviceAddress, characteristic, timeoutMillis)

    /**
     * @see BLEAsyncCommunicator.requestMTU
     */
    suspend fun requestMTU(targetMTU: Int, timeoutMillis: Long = 0L) =
            comm.requestMTU(deviceAddress, targetMTU, timeoutMillis)

    /**
     * @see BLEAsyncCommunicator.requestConnectionPriority
     */
    suspend fun requestConnectionPriority(connectionPriority: BLEConnectionPriority) =
            comm.requestConnectionPriority(deviceAddress, connectionPriority)

    /**
     * @see BLEAsyncCommunicator.readRemoteRSSI
     */
    suspend fun readRemoteRSSI(timeoutMillis: Long = 0L) =
            comm.readRemoteRSSI(deviceAddress)
}