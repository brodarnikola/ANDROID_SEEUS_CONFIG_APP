package hr.sil.android.seeusadmin.util

import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlin.collections.forEach
import kotlin.collections.forEachIndexed
import kotlin.collections.isNotEmpty
import kotlin.collections.toTypedArray

//import android.support.v4.app.ActivityCompat
//import android.support.v4.content.ContextCompat

/**
 * @author mfatiga
 */
class DroidPermission private constructor(private val activity: Activity) {
    companion object {
        fun init(activity: Activity) = DroidPermission(activity)
    }

    private var permissions: List<String> = listOf()
    fun request(vararg permissions: String): DroidPermission {
        this.permissions = listOf(*permissions)
        return this
    }

    private var onCheckDone: (granted: List<String>, denied: List<String>) -> Unit = { granted, denied -> }
    fun done(cb: (granted: List<String>, denied: List<String>) -> Unit): DroidPermission {
        onCheckDone = cb
        return this
    }

    private var explainCb: (List<String>) -> Unit = { }
    fun explain(cb: (List<String>) -> Unit): DroidPermission {
        explainCb = cb
        return this
    }

    private val REQ_CODE = 99

    private val grantedPermissions = mutableListOf<String>()
    private val deniedPermissions = mutableListOf<String>()
    private val permissionsToExplain = mutableListOf<String>()

    fun execute() {
        grantedPermissions.clear()
        deniedPermissions.clear()
        permissionsToExplain.clear()

        permissions.forEach { permission ->
            if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                    permissionsToExplain.add(permission)
                }
                deniedPermissions.add(permission)
            } else {
                grantedPermissions.add(permission)
            }
        }
        if (permissionsToExplain.isNotEmpty()) {
            explainCb(permissionsToExplain)
        }

        if (deniedPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(activity, deniedPermissions.toTypedArray(), REQ_CODE)
        } else {
            onCheckDone(grantedPermissions, deniedPermissions)
        }
    }

    fun link(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == REQ_CODE) {
            deniedPermissions.clear()
            permissions.forEachIndexed { index, permission ->
                if (grantResults[index] == PackageManager.PERMISSION_GRANTED) {
                    grantedPermissions.add(permission)
                } else {
                    deniedPermissions.add(permission)
                }
            }

            onCheckDone(grantedPermissions, deniedPermissions)
        }
    }
}