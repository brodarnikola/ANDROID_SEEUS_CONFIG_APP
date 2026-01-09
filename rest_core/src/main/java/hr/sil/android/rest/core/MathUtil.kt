package hr.sil.android.rest.core

/**
 * @author mfatiga
 */
object MathUtil {
    tailrec fun greatestCommonDenominator(a: Long, b: Long): Long {
        if (a == 0L || b == 0L) return a + b
        return greatestCommonDenominator(b, a % b)
    }
}