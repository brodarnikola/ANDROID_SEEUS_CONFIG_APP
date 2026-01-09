package hr.sil.android.seeusadmin.compose_ui.home_screens

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hr.sil.android.mplhuber.core.model.RUpdateAdminInfo
import hr.sil.android.mplhuber.core.remote.WSSeeUsAdmin
import hr.sil.android.mplhuber.core.remote.model.RLanguage
import hr.sil.android.seeusadmin.App
import hr.sil.android.seeusadmin.util.SettingsHelper
import hr.sil.android.seeusadmin.util.backend.UserUtil
import kotlinx.coroutines.launch
import kotlin.apply
import kotlin.collections.firstOrNull
import kotlin.text.isBlank
import kotlin.text.isNotBlank
import kotlin.text.orEmpty

data class SettingsUiState(
    val name: String = "",
    val email: String = "",
    val address: String = "",
    val availableLanguages: List<RLanguage> = emptyList(),
    val selectedLanguage: RLanguage? = null,
    val oldPassword: String = "",
    val newPassword: String = "",
    val retypePassword: String = "",
    val oldPasswordError: String? = null,
    val newPasswordError: String? = null,
    val retypePasswordError: String? = null,
    val isLoading: Boolean = false,
    val isSaveEnabled: Boolean = false,
    val appVersion: String = "",
    val isUnauthorized: Boolean = false
)

class SettingsViewModel : ViewModel() {

    val name = mutableStateOf("")
    val email = mutableStateOf("")
    val oldPassword = mutableStateOf("")
    val newPassword = mutableStateOf("")
    val selectedLanguage = mutableStateOf<RLanguage?>(null)

    val languages = mutableStateOf<List<RLanguage>>(emptyList())

    val showProgress = mutableStateOf(false)

    val nameError = mutableStateOf(false)
    val oldPasswordError = mutableStateOf<String?>(null)
    val newPasswordError = mutableStateOf<String?>(null)

    val passwordVisible = mutableStateOf(false)

    fun loadInitialData() {
        name.value = UserUtil.user?.name.orEmpty()
        email.value = UserUtil.user?.email.orEmpty()
    }

    fun loadLanguages() {
        viewModelScope.launch {
            languages.value = WSSeeUsAdmin.getLanguages()?.data ?: listOf()
            selectedLanguage.value = languages.value.firstOrNull {
                it.code == SettingsHelper.languageName
            }
        }
    }

    fun validate(): Boolean {
        nameError.value = name.value.isBlank() || name.value.length > 100

        oldPasswordError.value = when {
            oldPassword.value.isBlank() && newPassword.value.isNotBlank() ->
                "Blank fields exist"
            oldPassword.value.isNotBlank() &&
                    oldPassword.value != SettingsHelper.userPasswordWithoutEncryption ->
                "Invalid current password"
            else -> null
        }

        newPasswordError.value = if (newPassword.value.isBlank() && oldPassword.value.isNotBlank())
            "Blank fields exist" else null

        return !nameError.value && oldPasswordError.value == null && newPasswordError.value == null
    }

    fun submit(onSuccess: () -> Unit) {
        showProgress.value = true
        if (!validate()) {
            showProgress.value = false
            return
        }

        viewModelScope.launch {

            val updateInfo = RUpdateAdminInfo().apply {
                name = this@SettingsViewModel.name.value
                languageId = selectedLanguage.value?.id ?: return@launch
                if (newPassword.value.isNotBlank()) password = newPassword.value
            }

            if (UserUtil.userUpdate(updateInfo)) {
                App.ref.languageCode = selectedLanguage.value ?: RLanguage()

                showProgress.value = false
                onSuccess()
            } else {
                showProgress.value = false
            }
        }
    }
}