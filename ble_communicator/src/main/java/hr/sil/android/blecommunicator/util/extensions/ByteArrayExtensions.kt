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

package hr.sil.android.blecommunicator.util.extensions

/**
 * @author mfatiga
 */
fun ByteArray.chunked(chunkSize: Int): Array<ByteArray> {
    return if (this.isNotEmpty() && chunkSize > 0) {
        val totalSize = this.size
        val extraChunk = if (totalSize % chunkSize == 0) 0 else 1
        val chunkCount = (totalSize / chunkSize) + extraChunk
        Array(chunkCount) { chunkIndex ->
            val startIndex = chunkIndex * chunkSize
            val endIndex = (startIndex + chunkSize).coerceAtMost(totalSize)
            this.copyOfRange(startIndex, endIndex)
        }
    } else {
        arrayOf(this)
    }
}