package hr.sil.android.rest.core

/**
 * Linear interpolation with given input min and max values
 *
 * @author mfatiga
 */
fun Double.lerpInDomain(inMin: Double, inMax: Double, outMin: Double, outMax: Double): Double {
    val coercedVal = this.coerceIn(inMin, inMax)
    val result = (((coercedVal - inMin) * (outMax - outMin)) / (inMax - inMin)) + outMin
    return result.coerceIn(outMin, outMax)
}

/**
 * Linear interpolation where the input value is between 0.0 and 1.0
 *
 * @author mfatiga
 */
fun Double.lerp(a: Double, b: Double): Double {
    return (this.coerceIn(0.0, 1.0) * (b - a)) + a
}

/**
 * Modulo (remainder) that wraps equally in positive and negative
 * example:
 * -7.0.rem(10.0) = -7.0
 * -7.0.remPositive(10.0) = 3.0
 *
 * @author mfatiga
 */
fun Double.remPositive(d: Double): Double {
    return if (this >= 0.0) this % d else d - ((-this) % d)
}

/**
 * Normalizes an angle to be between -180.0 and 180.0 degrees
 */
fun Double.normalizeAngle(): Double {
    return (this + 180.0).remPositive(360.0) - 180.0
}

/**
 * Linear interpolation for an angle that properly interpolates through the shortest path to a
 * target angle wrapping at 360.0 deg
 *
 * @author mfatiga
 */
fun Double.lerpAngle(a: Double, b: Double): Double {
    var normA = a
    var normB = b
    if (Math.abs(a - b) >= 180.0) {
        if (a > b) {
            normA = a.normalizeAngle()
        } else {
            normB = b.normalizeAngle()
        }
    }
    val interpolated = this.lerp(normA, normB)
    return if (interpolated < 0.0) 360.0 + interpolated else interpolated
}