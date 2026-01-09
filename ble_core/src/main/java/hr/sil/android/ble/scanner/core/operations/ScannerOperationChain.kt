/* SWISS INNOVATION LAB CONFIDENTIAL
*
* www.swissinnolab.com
* __________________________________________________________________________
*
* [2016] - [2018] Swiss Innovation Lab AG
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

package hr.sil.android.ble.scanner.core.operations

import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicBoolean

/**
 * @author mfatiga
 */
internal class ScannerOperationChain(private val operations: List<ChainOperation>) {
    private val isRunning = AtomicBoolean(false)
    private var job: Job? = null
    private var operationIndex = 0

    private fun loop(scope: CoroutineScope) = scope.launch {
        while (isActive) {
            // execute operation
            operations[operationIndex].run()

            // next operation
            operationIndex++
            if (operationIndex >= operations.size) {
                operationIndex = 0
            }
        }
    }

    fun start(scope: CoroutineScope) {
        if (operations.isEmpty()) return
        if (isRunning.compareAndSet(false, true)) {
            operationIndex = 0
            job = loop(scope)
        }
    }

    fun isRunning() = isRunning.get()

    suspend fun stop() {
        if (isRunning.get()) {
            job?.cancel()
            job?.join()

            job = null
            operationIndex = 0

            isRunning.set(false)
        }
    }
}