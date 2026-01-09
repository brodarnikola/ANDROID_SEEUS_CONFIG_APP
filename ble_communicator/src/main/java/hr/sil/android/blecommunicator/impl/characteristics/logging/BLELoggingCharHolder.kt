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

package hr.sil.android.blecommunicator.impl.characteristics.logging

import hr.sil.android.blecommunicator.core.BLECharacteristicsHolder
import hr.sil.android.blecommunicator.core.BLECommDeviceHandle
import kotlinx.coroutines.channels.ReceiveChannel

/**
 * @author mfatiga
 */
class BLELoggingCharHolder(handle: BLECommDeviceHandle) : BLECharacteristicsHolder(handle) {
    //characteristics
    private val cNotifyLogger by characteristic("1a800003-4448-4fbc-8391-41a8137cfbc3", 244)

    //----- LOGGER -----
    fun listenLoggerNotifications(): ReceiveChannel<ByteArray> = cNotifyLogger.notifyOnChannel()
    //------------------
}