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

package hr.sil.android.seeusadmin.util.backend

import com.google.firebase.messaging.FirebaseMessaging
import hr.sil.android.mplhuber.core.model.RUpdateAdminInfo
import hr.sil.android.mplhuber.core.remote.WSSeeUsAdmin
import hr.sil.android.mplhuber.core.remote.model.RAdminUserInfo
import hr.sil.android.mplhuber.core.util.DeviceInfo
import hr.sil.android.mplhuber.core.util.logger
import hr.sil.android.rest.core.util.UserHashUtil
import hr.sil.android.seeusadmin.App
import hr.sil.android.seeusadmin.preferences.PreferenceStore
import hr.sil.android.seeusadmin.remote.WSConfig
import hr.sil.android.seeusadmin.store.DeviceStore
import hr.sil.android.seeusadmin.util.AppUtil
import hr.sil.android.seeusadmin.util.SettingsHelper

/**
 * @author mfatiga
 */
object UserUtil {
    private val log = logger()
    fun isUserLoggedIn() = (user != null)

    var user: RAdminUserInfo? = null
        private set

    fun getUserString(default: String = "--"): String {
        val loggedInUserName = user?.name ?: ""
        val loggedInUserEmail = user?.email ?: ""
        val result = when {
            loggedInUserName.isNotBlank() -> loggedInUserName
            loggedInUserEmail.isNotBlank() -> loggedInUserEmail
            else -> null
        }
        return result ?: default
    }

    private fun updateUserHash(username: String?, password: String?) {
        if (username != null && password != null && username.isNotEmpty() && password.isNotEmpty()) {
            PreferenceStore.userHash = UserHashUtil.createUserHash(username, password)
        } else {
            PreferenceStore.userHash = ""
        }
        WSConfig.updateAuthorizationKeys()
    }

    suspend fun login(username: String, password: String): Boolean {
        updateUserHash(username, password)
        return login()
    }


    suspend fun login(): Boolean {
         if (!PreferenceStore.userHash.isNullOrBlank()) {
            val responseUser = WSSeeUsAdmin.getAccountInfo()
            if (responseUser != null) {
                user = responseUser
                //invalidate caches on login
                AppUtil.refreshCache()

                val languagesList = WSSeeUsAdmin.getLanguages()?.data //DataCache.getLanguages(true)

                val languageData = languagesList?.find { it.id == responseUser.languageId }
                SettingsHelper.languageName = "EN"
                if (languageData != null) {
                    SettingsHelper.languageName = languageData.code
                }

                log.info("User is logged in updating device and token...")
                return true

                //return WSSeeUsAdmin.registerDevice(FirebaseInstanceId.getInstance().requestToken(), DeviceInfo.getJsonInstance())
            } else {
                updateUserHash(null, null)
                user = null
                return false
            }
        } else {
            updateUserHash(null, null)
            user = null
             return false
        }
    }

    fun logout() {
        updateUserHash(null, null)
        user = null

        DeviceStore.clear()
    }

    suspend fun passwordUpdate(newPassword: String, oldPassword: String): Boolean {
        val isPasswordUpdated = WSSeeUsAdmin.updatePassword(oldPassword, newPassword)

        if (!isPasswordUpdated) {
            log.error("Error while updating the user password")
            return false
        } else {
            return true
        }
    }

    suspend fun passwordRecovery(email: String): Boolean {
        return WSSeeUsAdmin.requestPasswordRecovery(email)
    }

    suspend fun passwordReset(email: String, passwordCode: String, password: String): Boolean {
        return WSSeeUsAdmin.resetPassword(email, passwordCode, password)
    }

    suspend fun userUpdate(user: RUpdateAdminInfo): Boolean {
        if (WSSeeUsAdmin.updateUserProfile(user) == null) {
            log.error("Error while updating the user")
            return false
        } else
            return true
    }
}