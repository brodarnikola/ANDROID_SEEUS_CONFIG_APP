package hr.sil.android.seeusadmin.util

import android.util.Patterns
import java.io.File
import kotlin.text.isNullOrEmpty

fun String?.isEmailValid() = !isNullOrEmpty() && Patterns.EMAIL_ADDRESS.matcher(this).matches()

fun String?.isPhoneValid() = !isNullOrEmpty() && Patterns.PHONE.matcher(this).matches()
