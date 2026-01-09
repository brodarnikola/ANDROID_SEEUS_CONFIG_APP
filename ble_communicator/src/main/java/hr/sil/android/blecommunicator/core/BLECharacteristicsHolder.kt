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

import hr.sil.android.blecommunicator.core.characteristics.BLECharacteristic
import java.util.*

/**
 * @author mfatiga
 */
abstract class BLECharacteristicsHolder(val handle: BLECommDeviceHandle) {
    protected fun characteristic(uuid: UUID, maxWriteBlockSize: Int = 0) =
            BLECharacteristic.Reference(handle, uuid, maxWriteBlockSize)

    protected fun characteristic(uuid: String, maxWriteBlockSize: Int = 0) =
            BLECharacteristic.Reference(handle, uuid, maxWriteBlockSize)
}