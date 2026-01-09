package hr.sil.android.seeusadmin.compose_ui.home_screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hr.sil.android.mplhuber.core.model.RUpdateAdminInfo
import hr.sil.android.mplhuber.core.remote.WSSeeUsAdmin
import hr.sil.android.mplhuber.core.remote.model.RLanguage
import hr.sil.android.seeusadmin.App
import hr.sil.android.seeusadmin.util.SettingsHelper
import hr.sil.android.seeusadmin.util.backend.UserUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.apply
import kotlin.collections.firstOrNull
import kotlin.text.isBlank
import kotlin.text.isNotBlank
import kotlin.text.orEmpty

data class SettingsUiState(
    var name: String = "",
    var email: String = "",
    val address: String = "",
    val availableLanguages: List<RLanguage> = emptyList(),
    var selectedLanguage: RLanguage? = null,
    var oldPassword: String = "",
    var newPassword: String = "",
    val retypePassword: String = "",
    val nameError: String? = null,
    val oldPasswordError: String? = null,
    val newPasswordError: String? = null,
    val retypePasswordError: String? = null,
    val isLoading: Boolean = false,
    val isSaveEnabled: Boolean = false,
    val appVersion: String = "",
    val isUnauthorized: Boolean = false,
    val showProgress: Boolean = false
)

class SettingsViewModel : ViewModel() {

//    val name = mutableStateOf("")
//    val email = mutableStateOf("")
//    val oldPassword = mutableStateOf("")
//    val newPassword = mutableStateOf("")
//    val selectedLanguage = mutableStateOf<RLanguage?>(null)
//
//    val languages = mutableStateOf<List<RLanguage>>(emptyList())
//
//    val showProgress = mutableStateOf(false)
//
//    val nameError = mutableStateOf(false)
//    val oldPasswordError = mutableStateOf<String?>(null)
//    val newPasswordError = mutableStateOf<String?>(null)
//
//    val passwordVisible = mutableStateOf(false)

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState

    fun loadInitialData() {
        _uiState.update {
            it.copy(
                name = UserUtil.user?.name.orEmpty(),
                email = UserUtil.user?.email.orEmpty()
            )
        }
    }

    fun updateName(nameParam: String) {
        _uiState.update {
            it.copy(
                name = nameParam
            )
        }
    }

    fun loadLanguages() {
        viewModelScope.launch {
            val languages =  WSSeeUsAdmin.getLanguages() ?: listOf()

            _uiState.update {
                it.copy(
                    availableLanguages = languages,
                    selectedLanguage = languages.firstOrNull {
                        it.code == SettingsHelper.languageName
                    }
                )
            }
            println("seeting values: langauge are: ${languages.joinToString { it.code }}")
            println("seeting values: langauge are 22: ${_uiState.value.availableLanguages.joinToString { it.code }}")
        }
    }

    fun validate(nameParam: String, oldPassword: String, newPassword: String): Boolean {

        val nameError = if( nameParam.isBlank() || nameParam.length > 100 ) "Wrong name input data" else ""

        val oldPasswordError = when {
            oldPassword.isBlank() && newPassword.isNotBlank() ->
                "Blank fields exist"
            oldPassword != SettingsHelper.userPasswordWithoutEncryption ->
                "Invalid current password"
            else -> null
        }

        val newPasswordError = if ( newPassword.isBlank())
            "Please enter all user information" else null

        println("check values: $nameError")
        println("check values: $oldPasswordError")
        println("check values: $newPasswordError")

        _uiState.update {
            it.copy(
                oldPasswordError = oldPasswordError,
                newPasswordError = newPasswordError,
                nameError = nameError
            )
        }

        return nameError.isEmpty() && oldPasswordError == null && newPasswordError == null
    }

    fun submit(nameParam: String, selectedLanguage: RLanguage, oldPassword: String, newPassword: String,
        onSuccess: () -> Unit) {

        println("settings values are : ${nameParam}")
        println("settings values are 22: ${oldPassword}")
        println("settings values are 33: ${newPassword}")
        println("settings values are 44: ${selectedLanguage}")

        _uiState.update {
            it.copy(
                showProgress = true
            )
        }

        if (!validate(nameParam, oldPassword, newPassword)) {
            _uiState.update {
                it.copy(
                    showProgress = false
                )
            }
            return
        }

        viewModelScope.launch {

            val updateInfo = RUpdateAdminInfo().apply {
                name = nameParam
                languageId = selectedLanguage?.id ?: return@launch
                if (newPassword.isNotBlank()) password = newPassword
            }

            if (UserUtil.userUpdate(updateInfo)) {
                App.ref.languageCode = selectedLanguage ?: RLanguage()

                _uiState.update {
                    it.copy(
                        showProgress = false,
                        oldPasswordError = null,
                        newPasswordError = null,
                        nameError = null
                    )
                }
                onSuccess()
            } else {
                _uiState.update {
                    it.copy(
                        showProgress = false
                    )
                }
            }
        }
    }
}