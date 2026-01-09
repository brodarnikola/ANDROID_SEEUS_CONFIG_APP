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

package hr.sil.android.blecommunicator.core.characteristics.behaviors

import hr.sil.android.blecommunicator.core.model.BLEWriteResult
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.Flow

/**
 * @author mfatiga
 */
interface BLECharWritable {
    fun isWritable(): Boolean

    fun getMaxWriteSize(): Int

    /**
     * Write [data] to the characteristic. If force large data is larger than returned by
     * [getMaxWriteSize] and [forceLargeWrite] is set to false, the data will be chunked and written
     * using [writeChannel].
     */
    suspend fun write(data: ByteArray, forceLargeWrite: Boolean = false): BLEWriteResult

    suspend fun writeChannel(channel: ReceiveChannel<ByteArray>, concatSmallBlocks: Boolean = false): BLEWriteResult

    suspend fun writeFlow(flow: Flow<ByteArray>): BLEWriteResult
}