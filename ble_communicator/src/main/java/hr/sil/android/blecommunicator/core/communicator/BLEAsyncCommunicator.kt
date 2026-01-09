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

import android.bluetooth.*
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import hr.sil.android.blecommunicator.core.communicator.util.BleConnectionCompat
import hr.sil.android.blecommunicator.core.communicator.util.extensions.getInternalWriteType
import hr.sil.android.blecommunicator.core.communicator.util.extensions.isNotifiable
import hr.sil.android.blecommunicator.core.communicator.util.extensions.isReadable
import hr.sil.android.blecommunicator.core.communicator.util.extensions.isWritable
import hr.sil.android.blecommunicator.core.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.android.asCoroutineDispatcher
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.actor
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * @author mfatiga
 */
class BLEAsyncCommunicator private constructor(ctx: Context) {
    companion object {
        //constants
        /**
         * The overhead value that is subtracted from the amount of bytes available when writing to a characteristic.
         * The default MTU value on Android is 23 bytes which gives effectively 23 - GATT_WRITE_MTU_OVERHEAD = 20 bytes
         * available for payload.
         */
        const val GATT_WRITE_MTU_OVERHEAD = 3

        /**
         * The overhead value that is subtracted from the amount of bytes available when reading from a characteristic.
         * The default MTU value on Android is 23 bytes which gives effectively 23 - GATT_READ_MTU_OVERHEAD = 22 bytes
         * available for payload.
         */
        const val GATT_READ_MTU_OVERHEAD = 1

        /**
         * The minimum (default) value for MTU (Maximum Transfer Unit) used by a bluetooth connection.
         */
        const val GATT_MTU_MINIMUM = 23

        /**
         * The maximum supported value for MTU (Maximum Transfer Unit) used by a bluetooth connection on Android OS.
         * https://android.googlesource.com/platform/external/bluetooth/bluedroid/+/android-5.1.0_r1/stack/include/gatt_api.h#119
         */
        const val GATT_MTU_MAXIMUM = 517

        //used to enable notifications/indications
        private const val GATT_DESCRIPTOR_CLIENT_CHARACTERISTIC_CONFIGURATION = "00002902-0000-1000-8000-00805f9b34fb"

        //singleton
        private var instance: BLEAsyncCommunicator? = null

        fun getInstanceForApplication(ctx: Context): BLEAsyncCommunicator {
            if (instance == null) {
                synchronized(this@Companion) {
                    if (instance == null) {
                        instance = BLEAsyncCommunicator(ctx)
                    }
                }
            }
            return instance!!
        }
    }


    //debug mode
    private var debugEnabled: Boolean = false
    private var debugLogThreadName: Boolean = false

    /**
     * Enable or disable debug logging and debug log prefixing with current thread name.
     *
     * @param enabled if set to true, will log complete operation execution workflow
     * @param logThreadName if set to true will prefix all log messages with the current thread name
     */
    fun setDebugMode(enabled: Boolean, logThreadName: Boolean = false) {
        this.debugEnabled = enabled
        this.debugLogThreadName = logThreadName
    }

    private fun debug(msg: String, t: Throwable? = null) {
        if (debugEnabled) {
            val tag = "BLEAsyncCommunicator"
            val thr = if (debugLogThreadName) "[${Thread.currentThread().name}] " else ""

            if (t != null) {
                Log.e(tag, "$thr$msg", t)
            } else {
                Log.d(tag, "$thr$msg")
            }
        }
    }


    //android API interface
    private val bleConn: BleConnectionCompat = BleConnectionCompat(ctx)
    private val btManager = ctx.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager

    //connection state change listeners
    private val connectionStateChangeListeners: ConcurrentHashMap<String, (status: Int, newState: Int) -> Unit> = ConcurrentHashMap()

    //internal state and cache
    private val gattMap: ConcurrentHashMap<String, BluetoothGatt> = ConcurrentHashMap()
    private val charMap: ConcurrentHashMap<String, List<BluetoothGattCharacteristic>> = ConcurrentHashMap()
    private val currentConnectionState: ConcurrentHashMap<String, Int> = ConcurrentHashMap()
    private val charNotifyCallbacks: ConcurrentHashMap<String, ConcurrentHashMap<UUID, (ByteArray) -> Unit>> = ConcurrentHashMap()
    private val gattMtuMap: ConcurrentHashMap<String, Int> = ConcurrentHashMap()
    private fun closeGatt(deviceAddress: String): Boolean {
        debug("GATT: Closing GATT and clearing maps...")

        val cachedGattFound = gattMap.containsKey(deviceAddress)

        //close gatt and clean maps
        gattMap[deviceAddress]?.close()
        gattMap.remove(deviceAddress)
        charMap.remove(deviceAddress)
        currentConnectionState.remove(deviceAddress)
        charNotifyCallbacks.remove(deviceAddress)
        gattMtuMap.remove(deviceAddress)

        return cachedGattFound
    }

    private fun getGattCharacteristics(deviceAddress: String) = charMap[deviceAddress] ?: listOf()

    private fun findGattCharacteristic(deviceAddress: String, characteristic: UUID): BluetoothGattCharacteristic? =
            getGattCharacteristics(deviceAddress).firstOrNull { it.uuid == characteristic }

    //public state utility methods

    /**
     * Returns a list of discovered characteristic UUIDs.
     * The list will be empty if not connected to the specified [deviceAddress]
     */
    fun getCharacteristics(deviceAddress: String): List<UUID> =
            getGattCharacteristics(deviceAddress).map { it.uuid }

    /**
     * Returns true if the specified [characteristic] exists, false otherwise.
     */
    fun doesCharacteristicExist(deviceAddress: String, characteristic: UUID): Boolean =
            findGattCharacteristic(deviceAddress, characteristic) != null

    /**
     * Returns true if the [characteristic] is writable, false otherwise.
     *
     * @return isWritable
     */
    fun isCharacteristicWritable(deviceAddress: String, characteristic: UUID): Boolean =
            findGattCharacteristic(deviceAddress, characteristic)?.isWritable() ?: false

    /**
     * Returns true if the [characteristic] is readable, false otherwise.
     *
     * @return isReadable
     */
    fun isCharacteristicReadable(deviceAddress: String, characteristic: UUID): Boolean =
            findGattCharacteristic(deviceAddress, characteristic)?.isReadable() ?: false

    /**
     * Returns true if the [characteristic] is notifiable, false otherwise.
     *
     * @return isNotifiable
     */
    fun isCharacteristicNotifiable(deviceAddress: String, characteristic: UUID): Boolean =
            findGattCharacteristic(deviceAddress, characteristic)?.isNotifiable() ?: false

    /**
     * Returns the BluetoothDevice reference for a given [deviceAddress] or null if the connection
     * to the target device does not exist
     *
     * @return BluetoothDevice
     */
    fun getBluetoothDevice(deviceAddress: String): BluetoothDevice? = gattMap[deviceAddress]?.device

    /**
     * Returns the current MTU for a given [deviceAddress]. This method will return
     * 23 ([GATT_MTU_MINIMUM]) if MTU change has not yet been requested.
     *
     * To request a new MTU, use the [requestMTU] method.
     *
     * @return current MTU or [GATT_MTU_MINIMUM] if MTU change has not yet been requested
     */
    fun getMTU(deviceAddress: String): BLEMtu = BLEMtu(gattMtuMap[deviceAddress]
            ?: GATT_MTU_MINIMUM)

    /**
     * Returns the current connection state for a given device MAC address.
     * All connection states are found in the [BluetoothProfile] class.
     * The default state is [BluetoothProfile.STATE_DISCONNECTED].
     *
     * @return current connection state for a given device MAC address
     */
    fun getConnectionState(deviceAddress: String): Int =
            currentConnectionState[deviceAddress] ?: BluetoothProfile.STATE_DISCONNECTED

    /**
     * Returns true if the current connection state is equal to [BluetoothProfile.STATE_CONNECTED] to the specified [deviceAddress], false otherwise
     * @see getConnectionState
     */
    fun isConnected(deviceAddress: String) = getConnectionState(deviceAddress) == BluetoothProfile.STATE_CONNECTED


    /**
     * Sets a callback that will be called when the connection state for the given [deviceAddress]
     * changes.
     *
     * @see getConnectionState
     * @see isConnected
     */
    fun setOnConnectionStateChangeListener(deviceAddress: String, listener: (status: Int, newState: Int) -> Unit) {
        debug("connectionStateChangeListener SET for $deviceAddress")

        connectionStateChangeListeners[deviceAddress] = listener
    }

    /**
     * Removes the connection state change listener for the given [deviceAddress]
     *
     * @see setOnConnectionStateChangeListener
     */
    fun removeOnConnectionStateListener(deviceAddress: String) {
        debug("connectionStateChangeListener REMOVED for $deviceAddress")

        connectionStateChangeListeners.remove(deviceAddress)
    }


    //current operation reference
    private var currentOperation: GattOperation<*, *> = GattOperation.NOP()


    //GATT operation callbacks
    private fun createGattCallback(deviceAddress: String) = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            debug("GATT: onConnectionStateChange(status=$status, newState=$newState)")

            currentConnectionState[deviceAddress] = newState

            connectionStateChangeListeners[deviceAddress]?.invoke(status, newState)

            val op = currentOperation

            //handle bugs
            if (status == 133) {
                debug("GATT: got status 133 bug")
                if (op is GattOperation.Connect) {
                    if (!closeGatt(op.deviceAddress)) {
                        gatt?.close()
                    }

                    debug("GATT: cancelling connect operation")
                    op.gattDeferred.cancel()
                    return
                }
            }

            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    debug("GATT: Connected")

                    //update gatt map
                    if (gatt != null) {
                        debug("GATT: gatt object cached")

                        gattMap[deviceAddress] = gatt
                    }

                    if (op is GattOperation.Connect) {
                        debug("GATT: Notifying Connect operation")

                        //complete connect operation
                        op.gattDeferred.complete(OperationResult(true))
                    }
                }

                BluetoothProfile.STATE_DISCONNECTED -> {
                    debug("GATT: Disconnected")
                    if (!closeGatt(op.deviceAddress)) {
                        gatt?.close()
                    }

                    if (op is GattOperation.Disconnect) {
                        debug("GATT: Notifying Disconnect operation")

                        //complete disconnect operation
                        op.gattDeferred.complete(OperationResult(true))
                    } else {
                        debug("GATT: Cancelling current operation")

                        //cancel current operation if the device is disconnected
                        op.gattDeferred.cancel()
                    }
                }

                BluetoothProfile.STATE_CONNECTING -> {
                }

                BluetoothProfile.STATE_DISCONNECTING -> {
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
            debug("GATT: onServicesDiscovered(status=$status)")

            val op = currentOperation
            if (op is GattOperation.DiscoverCharacteristics) {
                val cachedGatt = gattMap[deviceAddress]
                val isSuccessful = if (cachedGatt != null && status == BluetoothGatt.GATT_SUCCESS) {
                    charMap[deviceAddress] = cachedGatt.services.flatMap { it.characteristics }
                    true
                } else {
                    false
                }

                debug("GATT: Service discovery ${if (isSuccessful) "successful" else "failed"}, notifying DiscoverCharacteristics operation")

                op.gattDeferred.complete(OperationResult(isSuccessful))
            }
        }

        override fun onCharacteristicWrite(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
            super.onCharacteristicWrite(gatt, characteristic, status)
            debug("GATT: onCharacteristicWrite(status=$status)")

            val op = currentOperation
            when (op) {
                is GattOperation.CharacteristicWrite -> {
                    if (op.characteristic == characteristic?.uuid) {
                        debug("GATT: Notifying CharacteristicWrite operation")
                        op.gattDeferred.complete(OperationResult(status == BluetoothGatt.GATT_SUCCESS))
                    }
                }
                is GattOperation.CharacteristicWriteChannel -> {
                    if (op.characteristic == characteristic?.uuid) {
                        debug("GATT: Notifying CharacteristicWriteChannel operation")
                        op.gattDeferred.complete(OperationResult(status == BluetoothGatt.GATT_SUCCESS))
                    }
                }

                is GattOperation.CharacteristicRead -> {}
                is GattOperation.Connect -> {}
                is GattOperation.DescriptorRead -> {}
                is GattOperation.DescriptorWrite -> {}
                is GattOperation.Disconnect -> {}
                is GattOperation.DiscoverCharacteristics -> {}
                is GattOperation.NOP ->{}
                is GattOperation.ReadRemoteRSSI -> {}
                is GattOperation.RequestConnectionPriority -> {}
                is GattOperation.RequestMTU -> {}
                is GattOperation.SetNotification -> {}
            }
        }

        override fun onCharacteristicRead(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
            super.onCharacteristicRead(gatt, characteristic, status)
            debug("GATT: onCharacteristicRead(status=$status)")

            val op = currentOperation
            if (op is GattOperation.CharacteristicRead && op.characteristic == characteristic?.uuid) {
                debug("GATT: Notifying CharacteristicRead operation")

                op.gattDeferred.complete(OperationResult(status == BluetoothGatt.GATT_SUCCESS, characteristic.value))
            }
        }

        override fun onDescriptorWrite(gatt: BluetoothGatt?, descriptor: BluetoothGattDescriptor?, status: Int) {
            super.onDescriptorWrite(gatt, descriptor, status)
            debug("GATT: onDescriptorWrite(status=$status)")

            val op = currentOperation
            if (op is GattOperation.DescriptorWrite && op.descriptor == descriptor?.uuid) {
                debug("GATT: Notifying DescriptorWrite operation")

                op.gattDeferred.complete(OperationResult(status == BluetoothGatt.GATT_SUCCESS))
            }
        }

        override fun onDescriptorRead(gatt: BluetoothGatt?, descriptor: BluetoothGattDescriptor?, status: Int) {
            super.onDescriptorRead(gatt, descriptor, status)
            debug("GATT: onDescriptorRead(status=$status)")

            val op = currentOperation
            if (op is GattOperation.DescriptorRead && op.descriptor == descriptor?.uuid) {
                debug("GATT: Notifying DescriptorRead operation")

                op.gattDeferred.complete(OperationResult(status == BluetoothGatt.GATT_SUCCESS, descriptor.value))
            }
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
            super.onCharacteristicChanged(gatt, characteristic)
            debug("GATT: onCharacteristicChanged()")

            val uuid = characteristic?.uuid
            val data = characteristic?.value
            if (uuid != null && data != null) {
                val charNotifyListener = charNotifyCallbacks[deviceAddress]?.get(uuid)
                charNotifyListener?.invoke(data)
            }
        }

        override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
            super.onMtuChanged(gatt, mtu, status)
            debug("GATT: onMtuChanged(status=$status, mtu=$mtu)")

            val op = currentOperation
            if (op is GattOperation.RequestMTU) {
                debug("GATT: Notifying RequestMTU operation")

                op.gattDeferred.complete(OperationResult(status == BluetoothGatt.GATT_SUCCESS, mtu))
            }
        }

        override fun onReadRemoteRssi(gatt: BluetoothGatt?, rssi: Int, status: Int) {
            super.onReadRemoteRssi(gatt, rssi, status)
            debug("GATT: onReadRemoteRssi(status=$status)")

            val op = currentOperation
            if (op is GattOperation.ReadRemoteRSSI) {
                debug("GATT: Notifying ReadRemoteRSSI operation")

                op.gattDeferred.complete(OperationResult(status == BluetoothGatt.GATT_SUCCESS, rssi))
            }
        }
    }


    //await utility
    private suspend fun <T> awaitWithTimeout(timeoutMillis: Long, deferred: CompletableDeferred<T>, onCancel: () -> T): T {
        return try {
            withTimeout(timeoutMillis) {
                deferred.await()
            }
        } catch (ex: CancellationException) {
            debug("LOOP: awaitWithTimeout - operation cancelled or timed out")
            onCancel()
        }
    }


    //GATT utility
    private fun gattWriteCharacteristic(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, writeType: Int, data: ByteArray): Boolean {
        characteristic.writeType = writeType
        characteristic.value = data
        return gatt.writeCharacteristic(characteristic)
    }


    //GATT operation handler actor
    private val mainThreadHandler = Handler(Looper.getMainLooper())

    private fun runOperationActor() = GlobalScope.actor<GattOperation<*, *>>(mainThreadHandler.asCoroutineDispatcher("BLECommContext")) {
        while (true) {
            val op = channel.receiveCatching().getOrNull() // channel.receiveOrNull()
            if (op == null) {
                debug("LOOP: channel closed! Stopping operation actor!")
                break
            }

            currentOperation = op
            debug("LOOP: received $op operation")

            when (op) {
                is GattOperation.Connect -> {
                    val result = if (!isConnected(op.deviceAddress)) {
                        try {
                            btManager.adapter.cancelDiscovery()
                        } catch (e: Exception) {
                            //ignore
                        }
                        val gatt = bleConn.connectGatt(btManager.adapter.getRemoteDevice(op.deviceAddress), false, createGattCallback(op.deviceAddress))

                        debug("LOOP: connectGatt called, awaiting callback")

                        //wait for operation completion with timeout
                        awaitWithTimeout(op.timeoutMillis, op.gattDeferred) {
                            //cancel connection
                            debug("LOOP: connectGatt cancelled or timed out")
                            try {
                                if (btManager.adapter.isEnabled) {
                                    gatt?.disconnect()
                                }
                            } catch (e: Exception) {
                                debug("LOOP: Error while disconnecting during connection timeout cleanup!", e)
                            }
                            try {
                                if (!closeGatt(op.deviceAddress)) {
                                    debug("LOOP: closing GATT during connection timeout cleanup - not found, closing local GATT instance...")
                                    gatt?.close()
                                    debug("LOOP: local GATT instance closed during connection timeout cleanup")
                                }
                            } catch (e: Exception) {
                                debug("LOOP: Error while closing GATT during connection timeout cleanup!", e)
                            }

                            //return failed result
                            OperationResult(false)
                        }
                    } else {
                        debug("LOOP: already connected, returning success")
                        OperationResult(true)
                    }

                    debug("LOOP: completing operation")
                    op.complete(result)
                }

                is GattOperation.Disconnect -> {
                    val gatt = gattMap[op.deviceAddress]
                    val result = if (gatt != null) {
                        gatt.disconnect()

                        debug("LOOP: disconnect called, awaiting callback")

                        awaitWithTimeout(op.timeoutMillis, op.gattDeferred) {
                            //return failed result
                            OperationResult(false)
                        }
                    } else {
                        //return true because gatt is already disconnected
                        closeGatt(op.deviceAddress)
                        OperationResult(true)
                    }

                    debug("LOOP: completing operation")
                    op.complete(result)
                }

                is GattOperation.DiscoverCharacteristics -> {
                    val result = if (!charMap.containsKey(op.deviceAddress)) {
                        val gatt = gattMap[op.deviceAddress]
                        if (gatt != null) {
                            if (gatt.discoverServices()) {
                                debug("LOOP: discoverServices called, awaiting callback")

                                //wait for operation completion with timeout
                                awaitWithTimeout(op.timeoutMillis, op.gattDeferred) {
                                    //return failed result
                                    OperationResult(false)
                                }
                            } else {
                                OperationResult(false)
                            }
                        } else {
                            OperationResult(false)
                        }
                    } else {
                        debug("LOOP: characteristic discovery already done, returning success")
                        OperationResult(true)
                    }

                    debug("LOOP: completing operation")
                    op.complete(result)
                }

                is GattOperation.CharacteristicWrite -> {
                    val gatt = gattMap[op.deviceAddress]
                    val characteristic = findGattCharacteristic(op.deviceAddress, op.characteristic)
                    debug("LOOP: CharacteristicWrite found characteristic = ${characteristic?.uuid?.toString()
                            ?: "NULL"}")
                    val result = if (gatt != null && characteristic != null && characteristic.isWritable()) {
                        val writeType = characteristic.getInternalWriteType()
                        if (gattWriteCharacteristic(gatt, characteristic, writeType, op.data)) {
                            debug("LOOP: writeCharacteristic called, awaiting callback")

                            val writeResult = awaitWithTimeout(op.timeoutMillis, op.gattDeferred) { OperationResult(false) }
                            if (writeResult.isSuccessful) {
                                OperationResult(true, op.data.size.toLong())
                            } else {
                                OperationResult(false)
                            }
                        } else {
                            OperationResult(false)
                        }
                    } else {
                        OperationResult(false)
                    }

                    debug("LOOP: completing operation")
                    op.complete(result)
                }

                is GattOperation.CharacteristicWriteChannel -> {
                    val gatt = gattMap[op.deviceAddress]
                    val characteristic = findGattCharacteristic(op.deviceAddress, op.characteristic)
                    debug("LOOP: CharacteristicWriteChannel found characteristic = ${characteristic?.uuid?.toString()
                            ?: "NULL"}")
                    val result = if (gatt != null && characteristic != null && characteristic.isWritable()) {
                        var allSuccessful = true
                        var totalBytesWritten = 0L
                        val writeType = characteristic.getInternalWriteType()
                        debug("LOOP: CharacteristicWriteChannel begin reading channel")
                        for (data in op.channel) {
                            debug("LOOP: CharacteristicWriteChannel got data")
                            if (gattWriteCharacteristic(gatt, characteristic, writeType, data)) {
                                debug("LOOP: writeCharacteristic called, awaiting callback")
                                val writeResult = awaitWithTimeout(op.timeoutMillis, op.gattDeferred) { OperationResult(false) }
                                if (writeResult.isSuccessful) {
                                    debug("LOOP: CharacteristicWriteChannel success, recreating gattDeferred")
                                    totalBytesWritten += data.size
                                    op.recreateGattDeferred()
                                } else {
                                    debug("LOOP: CharacteristicWriteChannel failed, ending operation")
                                    allSuccessful = false
                                    break
                                }
                            } else {
                                allSuccessful = false
                                break
                            }
                        }

                        OperationResult(allSuccessful, totalBytesWritten)
                    } else {
                        OperationResult(false)
                    }

                    debug("LOOP: completing operation")
                    op.complete(result)
                }

                is GattOperation.CharacteristicRead -> {
                    val gatt = gattMap[op.deviceAddress]
                    val characteristic = findGattCharacteristic(op.deviceAddress, op.characteristic)
                    debug("LOOP: CharacteristicRead found characteristic = ${characteristic?.uuid?.toString()
                            ?: "NULL"}")
                    val result = if (gatt != null && characteristic != null && characteristic.isReadable()) {
                        if (gatt.readCharacteristic(characteristic)) {
                            debug("LOOP: readCharacteristic called, awaiting callback")

                            //wait for operation completion with timeout
                            awaitWithTimeout(op.timeoutMillis, op.gattDeferred) {
                                //return failed result
                                OperationResult(false)
                            }
                        } else {
                            OperationResult(false)
                        }
                    } else {
                        OperationResult(false)
                    }

                    debug("LOOP: completing operation")
                    op.complete(result)
                }

                is GattOperation.DescriptorWrite -> {
                    val gatt = gattMap[op.deviceAddress]
                    val descriptor = findGattCharacteristic(op.deviceAddress, op.characteristic)?.getDescriptor(op.descriptor)
                    debug("LOOP: DescriptorWrite found descriptor = ${descriptor?.uuid?.toString()
                            ?: "NULL"}")
                    val result = if (gatt != null && descriptor != null) {
                        descriptor.value = op.data
                        if (gatt.writeDescriptor(descriptor)) {
                            debug("LOOP: writeDescriptor called, awaiting callback")

                            //wait for operation completion with timeout
                            awaitWithTimeout(op.timeoutMillis, op.gattDeferred) {
                                //return failed result
                                OperationResult(false)
                            }
                        } else {
                            OperationResult(false)
                        }
                    } else {
                        OperationResult(false)
                    }

                    debug("LOOP: completing operation")
                    op.complete(result)
                }

                is GattOperation.DescriptorRead -> {
                    val gatt = gattMap[op.deviceAddress]
                    val descriptor = findGattCharacteristic(op.deviceAddress, op.characteristic)?.getDescriptor(op.descriptor)
                    debug("LOOP: DescriptorRead found descriptor = ${descriptor?.uuid?.toString()
                            ?: "NULL"}")
                    val result = if (gatt != null && descriptor != null) {
                        if (gatt.readDescriptor(descriptor)) {
                            debug("LOOP: readDescriptor called, awaiting callback")

                            //wait for operation completion with timeout
                            awaitWithTimeout(op.timeoutMillis, op.gattDeferred) {
                                //return failed result
                                OperationResult(false)
                            }
                        } else {
                            OperationResult(false)
                        }
                    } else {
                        OperationResult(false)
                    }

                    debug("LOOP: completing operation")
                    op.complete(result)
                }

                is GattOperation.SetNotification -> {
                    val gatt = gattMap[op.deviceAddress]
                    val characteristic = findGattCharacteristic(op.deviceAddress, op.characteristic)
                    debug("LOOP: SetNotification found characteristic = ${characteristic?.uuid?.toString()
                            ?: "NULL"}")
                    val result = if (gatt != null && characteristic != null) {
                        if (gatt.setCharacteristicNotification(characteristic, op.enable)) {
                            debug("LOOP: setCharacteristicNotification called")

                            //return success result
                            OperationResult<Unit>(true)
                        } else {
                            OperationResult(false)
                        }
                    } else {
                        OperationResult(false)
                    }

                    debug("LOOP: completing operation")
                    op.complete(result)
                }

                is GattOperation.RequestMTU -> {
                    val gatt = gattMap[op.deviceAddress]
                    val requestedMTU = op.mtu.coerceIn(GATT_MTU_MINIMUM, GATT_MTU_MAXIMUM)
                    val result = if (gatt != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (gatt.requestMtu(requestedMTU)) {
                            debug("LOOP: requestMtu called, awaiting callback")

                            //wait for operation completion with timeout
                            val requestMtuResult = awaitWithTimeout(op.timeoutMillis, op.gattDeferred) { OperationResult(false) }
                            if (requestMtuResult.isSuccessful && requestMtuResult.data != null) {
                                val resultMtu = requestMtuResult.data
                                gattMtuMap[op.deviceAddress] = resultMtu
                            }
                            requestMtuResult
                        } else {
                            OperationResult(false)
                        }
                    } else {
                        OperationResult(false)
                    }

                    debug("LOOP: completing operation")
                    op.complete(result)
                }

                is GattOperation.RequestConnectionPriority -> {
                    val gatt = gattMap[op.deviceAddress]
                    val priority = op.connectionPriority

                    val result = if (gatt != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (gatt.requestConnectionPriority(priority.code)) {
                            debug("LOOP: requestConnectionPriority called")

                            //return success result
                            OperationResult<Unit>(true)
                        } else {
                            OperationResult(false)
                        }
                    } else {
                        OperationResult(false)
                    }

                    debug("LOOP: completing operation")
                    op.complete(result)
                }

                is GattOperation.ReadRemoteRSSI -> {
                    val gatt = gattMap[op.deviceAddress]

                    val result = if (gatt != null) {
                        if (gatt.readRemoteRssi()) {
                            debug("LOOP: readRemoteRssi called, awaiting callback")

                            //wait for operation completion with timeout
                            awaitWithTimeout(op.timeoutMillis, op.gattDeferred) {
                                //return failed result
                                OperationResult(false)
                            }
                        } else {
                            OperationResult(false)
                        }
                    } else {
                        OperationResult(false)
                    }

                    debug("LOOP: completing operation")
                    op.complete(result)
                }

                is GattOperation.NOP ->{}
            }
        }
    }

    private val operationActor by lazy { runOperationActor() }


    //generic api
    private suspend fun <API> sendWithAwaitResult(operation: GattOperation<*, API>): OperationResult<API> {
        //prevent cancellation when waiting for the previous operation and sending the next
        //operation to the operation actor
        withContext(NonCancellable) {
            debug("IAPI: awaiting previous operation [$currentOperation] cleanup")
            currentOperation.awaitCleanup()

            debug("IAPI: sending next operation [$operation] to the operation actor")
            operationActor.send(operation)
        }

        //when operation awaitResult is cancelled, cancel GATT operation
        return try {
            operation.awaitResult()
        } catch (e: CancellationException) {
            debug("IAPI: await result cancelled, cancelling GATT operation")
            operation.gattDeferred.cancel()
            OperationResult(false, null)
        }
    }

    private suspend fun sendWithAwaitIsSuccessful(operation: GattOperation<*, *>): Boolean =
            sendWithAwaitResult(operation).isSuccessful


    //concrete api
    private suspend fun internalConnect(deviceAddress: String, timeoutMillis: Long): Boolean =
            sendWithAwaitIsSuccessful(GattOperation.Connect(deviceAddress, timeoutMillis))

    private suspend fun internalDiscoverCharacteristics(deviceAddress: String, timeoutMillis: Long): Boolean =
            sendWithAwaitIsSuccessful(GattOperation.DiscoverCharacteristics(deviceAddress, timeoutMillis))

    private suspend fun internalDisconnect(deviceAddress: String, timeoutMillis: Long): Boolean =
            sendWithAwaitIsSuccessful(GattOperation.Disconnect(deviceAddress, timeoutMillis))

    private suspend fun internalCharacteristicWrite(deviceAddress: String, timeoutMillis: Long, characteristic: UUID, data: ByteArray): OperationResult<Long> =
            sendWithAwaitResult(GattOperation.CharacteristicWrite(deviceAddress, timeoutMillis, characteristic, data))

    private suspend fun internalCharacteristicWriteChannel(deviceAddress: String, timeoutMillis: Long, characteristic: UUID, channel: ReceiveChannel<ByteArray>): OperationResult<Long> =
            sendWithAwaitResult(GattOperation.CharacteristicWriteChannel(deviceAddress, timeoutMillis, characteristic, channel))

    private suspend fun internalCharacteristicRead(deviceAddress: String, timeoutMillis: Long, characteristic: UUID): OperationResult<ByteArray> =
            sendWithAwaitResult(GattOperation.CharacteristicRead(deviceAddress, timeoutMillis, characteristic))

    private suspend fun internalDescriptorWrite(deviceAddress: String, timeoutMillis: Long, characteristic: UUID, descriptor: UUID, data: ByteArray): Boolean =
            sendWithAwaitIsSuccessful(GattOperation.DescriptorWrite(deviceAddress, timeoutMillis, characteristic, descriptor, data))

    private suspend fun internalDescriptorRead(deviceAddress: String, timeoutMillis: Long, characteristic: UUID, descriptor: UUID): OperationResult<ByteArray> =
            sendWithAwaitResult(GattOperation.DescriptorRead(deviceAddress, timeoutMillis, characteristic, descriptor))

    private suspend fun internalSetNotification(deviceAddress: String, characteristic: UUID, enable: Boolean): Boolean =
            sendWithAwaitIsSuccessful(GattOperation.SetNotification(deviceAddress, 0L, characteristic, enable))

    private suspend fun internalRequestMTU(deviceAddress: String, timeoutMillis: Long, mtu: Int): OperationResult<Int> =
            sendWithAwaitResult(GattOperation.RequestMTU(deviceAddress, timeoutMillis, mtu))

    private suspend fun internalRequestConnectionPriority(deviceAddress: String, connectionPriority: BLEConnectionPriority): Boolean =
            sendWithAwaitIsSuccessful(GattOperation.RequestConnectionPriority(deviceAddress, 0L, connectionPriority))

    private suspend fun internalReadRemoteRSSI(deviceAddress: String, timeoutMillis: Long): OperationResult<Int> =
            sendWithAwaitResult(GattOperation.ReadRemoteRSSI(deviceAddress, timeoutMillis))

    //compound api
    private suspend fun internalConnectWithCharacteristicDiscovery(deviceAddress: String, connectTimeoutMillis: Long, discoverCharacteristicTimeoutMillis: Long): Boolean {
        return if (internalConnect(deviceAddress, connectTimeoutMillis)) {
            if (internalDiscoverCharacteristics(deviceAddress, discoverCharacteristicTimeoutMillis)) {
                true
            } else {
                internalDisconnect(deviceAddress, connectTimeoutMillis)
                false
            }
        } else {
            false
        }
    }

    private suspend fun internalSetNotificationWithDescriptor(deviceAddress: String, descriptorWriteTimeoutMillis: Long, characteristic: UUID, enable: Boolean): Boolean {
        var isSuccessful = false

        val btCharacteristic = findGattCharacteristic(deviceAddress, characteristic)
        if (btCharacteristic != null) {
            val descriptorData = when {
                !enable -> BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE
                btCharacteristic.properties and BluetoothGattCharacteristic.PROPERTY_NOTIFY != 0 -> BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                btCharacteristic.properties and BluetoothGattCharacteristic.PROPERTY_INDICATE != 0 -> BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
                else -> null
            }

            if (descriptorData != null && descriptorData.isNotEmpty()) {
                if (internalSetNotification(deviceAddress, characteristic, enable)) {
                    val descriptor = UUID.fromString(GATT_DESCRIPTOR_CLIENT_CHARACTERISTIC_CONFIGURATION)
                    if (internalDescriptorWrite(deviceAddress, descriptorWriteTimeoutMillis, characteristic, descriptor, descriptorData)) {
                        isSuccessful = true
                    } else {
                        internalSetNotification(deviceAddress, characteristic, !enable)
                    }
                }
            }
        }

        return isSuccessful
    }

    private suspend fun internalNotificationEnable(deviceAddress: String, descriptorWriteTimeoutMillis: Long, characteristic: UUID, onCharacteristicChanged: (ByteArray) -> Unit): Boolean {
        if (!charNotifyCallbacks.containsKey(deviceAddress)) {
            charNotifyCallbacks[deviceAddress] = ConcurrentHashMap()
        }
        charNotifyCallbacks[deviceAddress]?.put(characteristic, onCharacteristicChanged)

        val isSuccessful = internalSetNotificationWithDescriptor(deviceAddress, descriptorWriteTimeoutMillis, characteristic, true)

        if (!isSuccessful) {
            charNotifyCallbacks[deviceAddress]?.remove(characteristic)
            if (charNotifyCallbacks[deviceAddress]?.isEmpty() == true) {
                charNotifyCallbacks.remove(deviceAddress)
            }
        }

        return isSuccessful
    }

    private suspend fun internalNotificationDisable(deviceAddress: String, descriptorWriteTimeoutMillis: Long, characteristic: UUID): Boolean {
        val isSuccessful = internalSetNotificationWithDescriptor(deviceAddress, descriptorWriteTimeoutMillis, characteristic, false)
        if (isSuccessful) {
            charNotifyCallbacks[deviceAddress]?.remove(characteristic)
            if (charNotifyCallbacks[deviceAddress]?.isEmpty() == true) {
                charNotifyCallbacks.remove(deviceAddress)
            }
        }
        return isSuccessful
    }

    //external api parameter validators
    //pre-compile patterns to improve validation performance
    private val regexDeviceAddress = "^(?:[a-f0-9]{2}:){5}[a-f0-9]{2}\$".toRegex(RegexOption.IGNORE_CASE)

    private fun validateDeviceAddress(deviceAddress: String): Boolean {
        return if (!regexDeviceAddress.matches(deviceAddress)) {
            debug("EAPI: invalid device address ($deviceAddress)!")
            false
        } else true
    }

    //external api
    /**
     * Connects to the target [deviceAddress] with the given [connectionParameters] and discovers characteristics.
     *
     * @param deviceAddress BLE device MAC address
     *
     * @return isSuccessful
     */
    suspend fun connect(deviceAddress: String, connectionParameters: BLEConnectionParameters = BLEConnectionParameters()): Boolean {
        if (!validateDeviceAddress(deviceAddress)) return false

        val attemptTimeoutMillis = connectionParameters.attemptTimeout
        val connectionTimeoutMillis = connectionParameters.connectionTimeout
        val discoverCharacteristicTimeoutMillis = connectionParameters.discoverCharacteristicsTimeout

        val maxConnectAttempts = connectionParameters.retryCount.coerceAtLeast(0) + 1
        val retryBackoff = connectionParameters.retryBackoff.coerceAtLeast(0)

        var connectAttemptCountdown = maxConnectAttempts

        var isSuccessful = false
        try {
            withTimeout(connectionTimeoutMillis) {
                while (connectAttemptCountdown > 0) {
                    connectAttemptCountdown--

                    val attemptNumber = maxConnectAttempts - connectAttemptCountdown

                    debug("EAPI: connect(deviceAddress=$deviceAddress), attempt $attemptNumber of $maxConnectAttempts")
                    val attemptIsSuccessful = internalConnectWithCharacteristicDiscovery(deviceAddress, attemptTimeoutMillis, discoverCharacteristicTimeoutMillis)
                    debug("EAPI: connect attempt $attemptNumber of $maxConnectAttempts ${if (attemptIsSuccessful) "succeeded" else "failed"}")

                    if (attemptIsSuccessful) {
                        isSuccessful = true
                        break
                    } else {
                        if (retryBackoff > 0) {
                            delay(retryBackoff)
                        }
                    }
                }
            }
        } catch (ex: CancellationException) {
            debug("EAPI: connect cancelled or timed out!")
        }
        return isSuccessful
    }

    /**
     * Disconnects from the target [deviceAddress].
     *
     * @param deviceAddress BLE device MAC address
     *
     * @return isSuccessful
     */
    suspend fun disconnect(deviceAddress: String, timeoutMillis: Long = 0L): Boolean {
        if (!validateDeviceAddress(deviceAddress)) return false

        debug("EAPI: disconnect(deviceAddress=$deviceAddress)")
        val result = internalDisconnect(deviceAddress, timeoutMillis)
        debug("EAPI: disconnect ${if (result) "successful" else "failed"}")
        return result
    }

    /**
     * Writes [data] to the [characteristic] on the target [deviceAddress].
     *
     * @param deviceAddress BLE device MAC address
     * @param characteristic target characteristic UUID
     *
     * @return isSuccessful with total written size
     */
    suspend fun characteristicWrite(deviceAddress: String, characteristic: UUID, data: ByteArray, timeoutMillis: Long = 0L): BLEWriteResult {
        if (!validateDeviceAddress(deviceAddress)) return BLEWriteResult(0L, false)

        debug("EAPI: characteristicWrite(deviceAddress=$deviceAddress, char=$characteristic)")
        val result = internalCharacteristicWrite(deviceAddress, timeoutMillis, characteristic, data)
        debug("EAPI: characteristicWrite ${if (result.isSuccessful) "successful, size = ${result.data}" else "failed"}")
        return BLEWriteResult(result.data ?: 0L, result.isSuccessful)
    }

    /**
     * Writes [channel] to the [characteristic] on the target [deviceAddress]
     *
     * @param deviceAddress BLE device MAC address
     * @param characteristic target characteristic UUID
     *
     * @return isSuccessful with total written size
     */
    suspend fun characteristicWriteChannel(deviceAddress: String, characteristic: UUID, channel: ReceiveChannel<ByteArray>, timeoutMillis: Long = 0L): BLEWriteResult {
        if (!validateDeviceAddress(deviceAddress)) return BLEWriteResult(0L, false)

        debug("EAPI: characteristicWriteChannel(deviceAddress=$deviceAddress, char=$characteristic)")
        val result = internalCharacteristicWriteChannel(deviceAddress, timeoutMillis, characteristic, channel)
        debug("EAPI: characteristicWriteChannel ${if (result.isSuccessful) "successful, size=${result.data}" else "failed"}")
        return BLEWriteResult(result.data ?: 0L, result.isSuccessful)
    }

    /**
     * Reads from the [characteristic] on the target [deviceAddress].
     *
     * @param deviceAddress BLE device MAC address
     * @param characteristic target characteristic UUID
     *
     * @return isSuccessful with read byte array
     */
    suspend fun characteristicRead(deviceAddress: String, characteristic: UUID, timeoutMillis: Long = 0L): BLEReadResult {
        if (!validateDeviceAddress(deviceAddress)) return BLEReadResult(byteArrayOf(), false)

        debug("EAPI: characteristicRead(deviceAddress=$deviceAddress, char=$characteristic)")
        val result = internalCharacteristicRead(deviceAddress, timeoutMillis, characteristic)
        debug("EAPI: characteristicRead ${if (result.isSuccessful) "successful" else "failed"}")
        return BLEReadResult(result.data ?: byteArrayOf(), result.isSuccessful)
    }

    /**
     * Enables notify or indicate on the [characteristic] on the target [deviceAddress].
     *
     * @param deviceAddress BLE device MAC address
     * @param characteristic target characteristic UUID
     *
     * @return isSuccessful
     */
    suspend fun characteristicNotifyEnable(deviceAddress: String, characteristic: UUID, timeoutMillis: Long = 0L, onCharacteristicChanged: (ByteArray) -> Unit): Boolean {
        if (!validateDeviceAddress(deviceAddress)) return false

        debug("EAPI: characteristicNotifyEnable(deviceAddress=$deviceAddress, char=$characteristic)")
        val result = internalNotificationEnable(deviceAddress, timeoutMillis, characteristic, onCharacteristicChanged)
        debug("EAPI: characteristicNotifyEnable ${if (result) "successful" else "failed"}")
        return result
    }

    /**
     * Disables notify or indicate on the [characteristic] on the target [deviceAddress].
     *
     * @param deviceAddress BLE device MAC address
     * @param characteristic target characteristic UUID
     *
     * @return isSuccessful
     */
    suspend fun characteristicNotifyDisable(deviceAddress: String, characteristic: UUID, timeoutMillis: Long = 0L): Boolean {
        if (!validateDeviceAddress(deviceAddress)) return false

        debug("EAPI: characteristicNotifyDisable(deviceAddress=$deviceAddress, char=$characteristic)")
        val result = internalNotificationDisable(deviceAddress, timeoutMillis, characteristic)
        debug("EAPI: characteristicNotifyDisable ${if (result) "successful" else "failed"}")
        return result
    }

    /**
     * Performs GATT MTU (Maximum Transfer Unit) request.
     *
     * @param deviceAddress BLE device MAC address
     *
     * @return isSuccessful with new MTU value
     */
    suspend fun requestMTU(deviceAddress: String, targetMTU: Int, timeoutMillis: Long = 0L): BLERequestMTUResult {
        if (!validateDeviceAddress(deviceAddress)) return BLERequestMTUResult(BLEMtu(GATT_MTU_MINIMUM), false)

        debug("EAPI: requestMTU(deviceAddress=$deviceAddress, targetMTU=$targetMTU)")
        val result = internalRequestMTU(deviceAddress, timeoutMillis, targetMTU)
        debug("EAPI: requestMTU ${if (result.isSuccessful) "successful, mtu=${result.data}" else "failed"}")

        return BLERequestMTUResult(
                if (result.data != null) BLEMtu(result.data) else getMTU(deviceAddress),
                result.isSuccessful)
    }

    /**
     * Requests a connection priority update with the target [BLEConnectionPriority].
     *
     * @param deviceAddress BLE device MAC address
     * @param connectionPriority one of [BLEConnectionPriority]
     *
     * @return isSuccessful
     */
    suspend fun requestConnectionPriority(deviceAddress: String, connectionPriority: BLEConnectionPriority): Boolean {
        if (!validateDeviceAddress(deviceAddress)) return false

        debug("EAPI: requestConnectionPriority(deviceAddress=$deviceAddress, connectionPriority=$connectionPriority")
        val result = internalRequestConnectionPriority(deviceAddress, connectionPriority)
        debug("EAPI: requestConnectionPriority ${if (result) "successful" else "failed"}")
        return result
    }

    /**
     * Reads RSSI from a connected remote device.
     *
     * @param deviceAddress BLE device MAC address
     *
     * @return isSuccessful with resulting RSSI value
     */
    suspend fun readRemoteRSSI(deviceAddress: String, timeoutMillis: Long = 0L): BLEReadRemoteRssiResult {
        if (!validateDeviceAddress(deviceAddress)) return BLEReadRemoteRssiResult(null, false)

        debug("EAPI: readRemoteRSSI(deviceAddress=$deviceAddress)")
        val result = internalReadRemoteRSSI(deviceAddress, timeoutMillis)
        debug("EAPI: readRemoteRSSI ${if (result.isSuccessful) "successful, RSSI=${result.data}" else "failed"}")
        return BLEReadRemoteRssiResult(result.data, result.isSuccessful)
    }
}