package hr.sil.android.seeusadmin.util

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Build
import android.os.Build.VERSION_CODES.JELLY_BEAN_MR1
import java.util.*

object SettingsHelper {

    private const val NAME = "HubberSettingsAdmin"
    private const val MODE = Context.MODE_PRIVATE
    private lateinit var preferences: SharedPreferences


    // list of app specific preferences

    val LANGUAGE_ENGLISH = "DE"
    private val SELECTED_LANGUAGE = Pair("User_settings_Language", LANGUAGE_ENGLISH)
    private val USERNAME_LOGIN = Pair("Username_login", "")

    private val USER_PASSWORD = Pair("User_password_without_encryption", "")

    fun init(context: Context) {
        preferences = context.getSharedPreferences(NAME, MODE)
    }


    fun setLocale(c: Context): Context {
        return updateResources(c, getLanguage())
    }


    fun getLanguage(): String {
        return preferences.getString(SELECTED_LANGUAGE.first, LANGUAGE_ENGLISH).toString()
    }

    private fun updateResources(context: Context, language: String): Context {
        var context = context
        val locale = Locale(language)
        Locale.setDefault(locale)

        val res = context.resources
        val config = Configuration(res.configuration)
        if(  Build.VERSION.SDK_INT >=  JELLY_BEAN_MR1 ) {
            config.setLocale(locale)
            context = context.createConfigurationContext(config)
        } else {
            config.locale = locale
            res.updateConfiguration(config, res.displayMetrics)
        }
        return context
    }


    /**
     * SharedPreferences extension function, so we won't need to call edit() and apply()
     * ourselves on every SharedPreferences operation.
     */
    private inline fun SharedPreferences.edit(operation: (SharedPreferences.Editor) -> Unit) {
        val editor = edit()
        operation(editor)
        editor.apply()
    }

    var languageName: String
        // custom getter to get a preference of a desired type, with a predefined default value
        get() = preferences.getString(SELECTED_LANGUAGE.first, SELECTED_LANGUAGE.second).toString()

        // custom setter to save a preference back to preferences file
        set(value) = preferences.edit {
            it.putString(SELECTED_LANGUAGE.first, value)
        }

    var usernameLogin: String?
        get() = preferences.getString(USERNAME_LOGIN.first, USERNAME_LOGIN.second)

        set(value) = preferences.edit {
            it.putString(USERNAME_LOGIN.first, value)
        }

    var userPasswordWithoutEncryption: String
        get() = preferences.getString(USER_PASSWORD.first, USER_PASSWORD.second) ?: ""

        set(value) = preferences.edit {
            it.putString(USER_PASSWORD.first, value)
        }

}