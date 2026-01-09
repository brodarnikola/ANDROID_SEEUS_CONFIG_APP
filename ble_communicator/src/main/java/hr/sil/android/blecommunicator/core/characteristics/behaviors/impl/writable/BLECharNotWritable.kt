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

package hr.sil.android.blecommunicator.core.characteristics.behaviors.impl.writable

import hr.sil.android.blecommunicator.core.characteristics.behaviors.BLECharWritable
import hr.sil.android.blecommunicator.core.characteristics.behaviors.base.BaseBLECharBehavior
import hr.sil.android.blecommunicator.core.model.BLEWriteResult
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.Flow

/**
 * @author mfatiga
 */
internal class BLECharNotWritable : BaseBLECharBehavior(), BLECharWritable {
    override fun isWritable() = false

    override fun getMaxWriteSize(): Int = 0

    suspend override fun write(data: ByteArray, forceLargeWrite: Boolean): BLEWriteResult =
            BLEWriteResult(0L, false)

    suspend override fun writeChannel(channel: ReceiveChannel<ByteArray>, concatSmallBlocks: Boolean): BLEWriteResult =
            BLEWriteResult(0L, false)

    override suspend fun writeFlow(flow: Flow<ByteArray>): BLEWriteResult =
            BLEWriteResult(0L, false)
}