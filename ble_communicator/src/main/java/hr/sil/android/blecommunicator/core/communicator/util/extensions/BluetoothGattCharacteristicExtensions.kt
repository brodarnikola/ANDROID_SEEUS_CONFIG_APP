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

package hr.sil.android.blecommunicator.core.communicator.util.extensions

import android.bluetooth.BluetoothGattCharacteristic

/**
 * @author mfatiga
 */
fun BluetoothGattCharacteristic.isWriteNoResponse() =
        this.properties and BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE != 0

fun BluetoothGattCharacteristic.isWriteResponse() =
        this.properties and BluetoothGattCharacteristic.PROPERTY_WRITE != 0

fun BluetoothGattCharacteristic.isWritable() = isWriteNoResponse() || isWriteResponse()

fun BluetoothGattCharacteristic.isReadable()
        = this.properties and BluetoothGattCharacteristic.PROPERTY_READ != 0

fun BluetoothGattCharacteristic.isNotifiable()
        = this.properties and BluetoothGattCharacteristic.PROPERTY_NOTIFY != 0
        || this.properties and BluetoothGattCharacteristic.PROPERTY_INDICATE != 0

fun BluetoothGattCharacteristic.getInternalWriteType(): Int = when {
    this.isWriteResponse() -> BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
    this.isWriteNoResponse() -> BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
    else -> BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
}
