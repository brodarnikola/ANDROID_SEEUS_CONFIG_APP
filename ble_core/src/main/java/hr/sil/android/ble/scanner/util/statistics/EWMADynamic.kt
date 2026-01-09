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

package hr.sil.android.ble.scanner.util.statistics

import kotlin.math.pow

/**
 * "Exponentially weighted moving average" using the formula:
 * s_t = (alpha * x_t) + ((1 - alpha) * s_t_prev
 * where:
 * alpha is the smoothing factor
 * s_t is the smoothed statistic
 * x_t is the current observed value
 * s_t_prev is the previous smoothed statistic
 *
 * The alpha is calculated based on the time constant "tau" and the delta T between current and
 * previous measurement.
 *
 * https://en.wikipedia.org/wiki/Moving_average
 * https://en.wikipedia.org/wiki/Exponential_smoothing
 *
 * @author mfatiga
 */
class EWMADynamic(private val tau: Long) {
    private var stat: Double? = null
    private var prevTimestamp: Long? = null

    /**
     * @return the last data point timestamp
     */
    fun lastTimestamp(): Long? = prevTimestamp

    /**
     * @return current smoothed value, returns null when no observed values have been added
     */
    fun current(): Double? = stat

    /**
     * Calculates the next smoothed statistic value, sets it as the current statistic value and
     * returns it
     * @return next smoothed statistic value
     */
    fun next(xt: Double, timestamp: Long): Double {
        val statCurrent = stat
        val prevTS = prevTimestamp

        // calculate only if previous stat and timestamp are not null
        val statNext = if (statCurrent != null && prevTS != null) {
            // current timestamp
            val ts = timestamp.coerceAtLeast(prevTS)

            // delta T
            val dt = ts - prevTS

            // alpha using tau and delta T
            val alpha = (1 - Math.E.pow((-1.0 * dt) / tau)).coerceIn(0.0, 1.0)

            // calculate next stat
            alpha * xt + (1.0 - alpha) * statCurrent
        } else {
            // initial observed value
            xt
        }

        // store stat and timestamp
        stat = statNext
        prevTimestamp = timestamp
        return statNext
    }
}