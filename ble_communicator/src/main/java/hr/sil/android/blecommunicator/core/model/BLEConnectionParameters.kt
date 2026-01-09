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

import hr.sil.android.blecommunicator.core.communicator.GattOperation

/**
 * @author mfatiga
 */
data class BLEConnectionParameters(
        /**
         * Number of retries after the initial connection attempt fails.
         *
         * Example: 1 connection attempt will always be executed, so with 9 retries, total attempt
         * count will be max 10
         */
        val retryCount: Int = DEFAULT_RETRY_COUNT,

        /**
         * Delay between each connection attempt
         */
        val retryBackoff: Long = DEFAULT_RETRY_BACKOFF,

        /**
         * Single connection attempt timeout
         */
        val attemptTimeout: Long = DEFAULT_ATTEMPT_TIMEOUT,

        /**
         * Connection process timeout (including retries)
         */
        val connectionTimeout: Long = DEFAULT_CONNECTION_TIMEOUT,

        /**
         * Characteristic(service) discovery timeout
         */
        val discoverCharacteristicsTimeout: Long = DEFAULT_DISCOVER_CHARACTERISTICS_TIMEOUT
) {
    companion object {
        const val DEFAULT_RETRY_COUNT = 9
        const val DEFAULT_RETRY_BACKOFF = 1000L
        const val DEFAULT_ATTEMPT_TIMEOUT = GattOperation.DEFAULT_TIMEOUT
        const val DEFAULT_CONNECTION_TIMEOUT = 2 * GattOperation.DEFAULT_TIMEOUT
        const val DEFAULT_DISCOVER_CHARACTERISTICS_TIMEOUT = GattOperation.DEFAULT_TIMEOUT
    }
}