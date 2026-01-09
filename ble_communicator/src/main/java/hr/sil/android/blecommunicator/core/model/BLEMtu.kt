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

package hr.sil.android.blecommunicator.core.model

import hr.sil.android.blecommunicator.core.communicator.BLEAsyncCommunicator

/**
 * The GATT MTU. The value will always be 23 on pre-lollipop Android devices.
 *
 * Warning: don't use the [value] as a read/write chunk size! To get proper read/write size, use
 * [maxReadSize] and [maxWriteSize] respectively.
 *
 * @author mfatiga
 */
data class BLEMtu(val value: Int) {
    /**
     * Returns the [value] subtracted by [BLEAsyncCommunicator.GATT_READ_MTU_OVERHEAD]
     */
    val maxReadSize: Int
        get() = value - BLEAsyncCommunicator.GATT_READ_MTU_OVERHEAD

    /**
     * Returns the [value] subtracted by [BLEAsyncCommunicator.GATT_WRITE_MTU_OVERHEAD]
     */
    val maxWriteSize: Int
        get() = value - BLEAsyncCommunicator.GATT_WRITE_MTU_OVERHEAD
}