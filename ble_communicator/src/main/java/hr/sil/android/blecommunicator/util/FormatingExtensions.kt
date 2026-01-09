package hr.sil.android.blecommunicator.util

import java.text.SimpleDateFormat
import java.util.*

/**
 * @author mfatiga
 */
fun Double.format(digits: Int) = java.lang.String.format("%.${digits}f", this)

fun Date.format(format: String) = SimpleDateFormat(format).format(this)