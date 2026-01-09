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

/**
 * "Exponentially weighted moving average" using the formula:
 * s_t = (alpha * x_t) + ((1 - alpha) * s_t_prev
 * where:
 * alpha is the smoothing factor
 * s_t is the smoothed statistic
 * x_t is the current observed value
 * s_t_prev is the previous smoothed statistic
 *
 * https://en.wikipedia.org/wiki/Moving_average
 * https://en.wikipedia.org/wiki/Exponential_smoothing
 *
 * @author mfatiga
 */
class EWMA(alphaInitial: Double) {
    private var alpha: Double = alphaInitial.coerceIn(0.0, 1.0)
    private var stat: Double? = null

    /**
     * @return current smoothed value, returns null when no observed values have been added
     */
    fun current(): Double? = stat

    /**
     * Calculates the next smoothed statistic value, sets it as the current statistic value and
     * returns it
     * @return next smoothed statistic value
     */
    fun next(xt: Double): Double {
        val statCurrent = stat

        // calculate only if previous stat is not null
        return if (statCurrent != null) {
            val statNext = alpha * xt + (1.0 - alpha) * statCurrent
            stat = statNext
            statNext
        } else {
            stat = xt
            xt
        }
    }

    /**
     * Sets the new alpha value.
     */
    fun configure(alpha: Double) {
        this.alpha = alpha.coerceIn(0.0, 1.0)

    }

    /**
     * @return the current value of alpha
     */
    fun alpha(): Double = alpha
}