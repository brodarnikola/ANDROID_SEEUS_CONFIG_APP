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

package hr.sil.android.blecommunicator.core.characteristics

import hr.sil.android.blecommunicator.core.BLECommDeviceHandle
import hr.sil.android.blecommunicator.core.characteristics.behaviors.BLECharNotifiable
import hr.sil.android.blecommunicator.core.characteristics.behaviors.BLECharReadable
import hr.sil.android.blecommunicator.core.characteristics.behaviors.BLECharWritable
import hr.sil.android.blecommunicator.core.characteristics.behaviors.impl.notifiable.BLECharIsNotifiable
import hr.sil.android.blecommunicator.core.characteristics.behaviors.impl.notifiable.BLECharNotNotifiable
import hr.sil.android.blecommunicator.core.characteristics.behaviors.impl.readable.BLECharIsReadable
import hr.sil.android.blecommunicator.core.characteristics.behaviors.impl.readable.BLECharNotReadable
import hr.sil.android.blecommunicator.core.characteristics.behaviors.impl.writable.BLECharIsWritable
import hr.sil.android.blecommunicator.core.characteristics.behaviors.impl.writable.BLECharNotWritable
import java.util.*
import kotlin.reflect.KProperty

/**
 * @author mfatiga
 */
class BLECharacteristic internal constructor(w: BLECharWritable, r: BLECharReadable, n: BLECharNotifiable)
    : BLECharWritable by w, BLECharReadable by r, BLECharNotifiable by n {

    fun exists(): Boolean = isWritable() || isReadable() || isNotifiable()

    class Reference(
            private val handle: BLECommDeviceHandle,
            private val uuid: UUID,
            maxWriteBlockSize: Int) {

        constructor(handle: BLECommDeviceHandle, uuid: String, maxWriteBlockSize: Int)
                : this(handle, UUID.fromString(uuid), maxWriteBlockSize)

        private val _writable by lazy(LazyThreadSafetyMode.NONE) { BLECharIsWritable(handle, uuid, maxWriteBlockSize) }
        private val _notWritable by lazy(LazyThreadSafetyMode.NONE) { BLECharNotWritable() }
        private fun isWritable() =
                handle.doesCharacteristicExist(uuid) && handle.isCharacteristicWritable(uuid)

        private fun getWritable(): BLECharWritable =
                if (isWritable()) _writable else _notWritable


        private val _readable by lazy(LazyThreadSafetyMode.NONE) { BLECharIsReadable(handle, uuid) }
        private val _notReadable by lazy(LazyThreadSafetyMode.NONE) { BLECharNotReadable() }
        private fun isReadable() =
                handle.doesCharacteristicExist(uuid) && handle.isCharacteristicReadable(uuid)

        private fun getReadable(): BLECharReadable =
                if (isReadable()) _readable else _notReadable


        private val _notifiable by lazy(LazyThreadSafetyMode.NONE) { BLECharIsNotifiable(handle, uuid) }
        private val _notNotifiable by lazy(LazyThreadSafetyMode.NONE) { BLECharNotNotifiable() }
        private fun isNotifiable() =
                handle.doesCharacteristicExist(uuid) && handle.isCharacteristicNotifiable(uuid)

        private fun getNotifiable(): BLECharNotifiable =
                if (isNotifiable()) _notifiable else _notNotifiable

        fun get(): BLECharacteristic =
                BLECharacteristic(getWritable(), getReadable(), getNotifiable())

        operator fun getValue(thisRef: Any?, property: KProperty<*>): BLECharacteristic = get()
    }
}