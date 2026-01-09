package hr.sil.android.seeusadmin.util.ui


import android.util.Log
import com.google.android.gms.tasks.Task
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


/**
 * @author mfatiga
 */
suspend fun <T> Task<T>.awaitForResult(): T? {
    return suspendCoroutine { c ->
        this@awaitForResult.addOnCompleteListener {
            if (it.isSuccessful) {
                c.resume(it.result)
            } else {
                val exc = it.exception
                if (exc != null) {
                    c.resumeWithException(exc)
                } else {
                    c.resumeWithException(RuntimeException("Task result failed! No exception returned!"))
                }
            }
        }
        this@awaitForResult.addOnFailureListener { c.resumeWithException(it) }
        this@awaitForResult.addOnCanceledListener { c.resumeWithException(RuntimeException("Cancelled!")) }
    }
}

/**
 * @author mfatiga
 */
//suspend fun FirebaseInstanceId.requestToken(): String? {
//    return try {
//        val result = this.instanceId.awaitForResult()
//        val token = result?.token
//        Log.i("FCMToken", "Task result.token=$token")
//        token
//    } catch (exc: Exception) {
//        Log.e("FCMToken", "Error while getting FCM token!", exc)
//        null
//    }
//}
