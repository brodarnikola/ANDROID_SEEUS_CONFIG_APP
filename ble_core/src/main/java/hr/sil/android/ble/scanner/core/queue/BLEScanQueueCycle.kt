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

package hr.sil.android.ble.scanner.core.queue

import hr.sil.android.ble.scanner.util.Debuggable
import java.util.concurrent.LinkedBlockingQueue

/**
 * @author mfatiga
 */
internal class BLEScanQueueCycle<in E>(
        private val threadName: String,
        private val onCycle: (elements: List<E>) -> Boolean
) : Debuggable {

    companion object {
        const val SCAN_RESULTS_QUEUE_PERIOD = 1_000L
        const val INACTIVITY_AUTO_STOP_PERIOD = 30_000L
    }

    override var DEBUG_MODE: Boolean = false

    @Volatile
    private var isRunning = false

    private fun stopSelf() {
        debug("Stopping thread...")
        isRunning = false
    }

    private var lastActiveTimestamp = 0L

    private val queue = LinkedBlockingQueue<E>()
    private val runnable = Runnable {
        debug("Thread started!")
        while (isRunning) {
            val loopBeginTimestamp = System.currentTimeMillis()

            val drainedItems = mutableListOf<E>()
            val drainedItemCount = queue.drainTo(drainedItems)
            debug("Drained $drainedItemCount items from the queue.")

            // notify cycle
            val isActive = onCycle(drainedItems)

            // update last drained timestamp
            if (isActive || drainedItemCount > 0) {
                lastActiveTimestamp = System.currentTimeMillis()
            }
            // check if inactive and stop
            if (lastActiveTimestamp > 0 && System.currentTimeMillis() - lastActiveTimestamp >= INACTIVITY_AUTO_STOP_PERIOD) {
                debug("Detected inactivity of ${(INACTIVITY_AUTO_STOP_PERIOD / 1000.0).toLong()}s!")
                stopSelf()
            }

            // handle wait
            val elapsed = System.currentTimeMillis() - loopBeginTimestamp
            val toSleep = SCAN_RESULTS_QUEUE_PERIOD - elapsed
            if (toSleep > 0L) {
                try {
                    Thread.sleep(toSleep)
                } catch (exc: Exception) {
                    // ignore
                }
            }
            debug("Cycle duration: ${((System.currentTimeMillis() - loopBeginTimestamp) / 1000.0).toLong()}s")
        }
        debug("Thread stopped!")
    }

    private fun createThread() = Thread(runnable, if (threadName.isBlank()) javaClass.simpleName else threadName)
    private var workerThread = createThread()

    private fun startThreadIfNotRunning() {
        synchronized(this) {
            if (!isRunning && !workerThread.isAlive) {
                debug("Starting thread...")

                isRunning = true
                if (workerThread.state == Thread.State.TERMINATED) {
                    workerThread = createThread()
                }
                workerThread.start()
            }
        }
    }

    // will block until there is space in the queue
    fun putData(data: E) {
        startThreadIfNotRunning()
        queue.put(data)
    }

    // will add data to the queue if there is space, otherwise it will skip adding any data
    fun offerData(data: E): Boolean {
        startThreadIfNotRunning()
        return queue.offer(data)
    }
}