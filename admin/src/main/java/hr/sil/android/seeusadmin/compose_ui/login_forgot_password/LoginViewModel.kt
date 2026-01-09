package hr.sil.android.seeusadmin.compose_ui.login_forgot_password

import android.app.Activity
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.viewModelScope
import hr.sil.android.mplhuber.core.util.logger
import hr.sil.android.seeusadmin.R
import hr.sil.android.seeusadmin.util.BaseViewModel
import hr.sil.android.seeusadmin.util.SettingsHelper
import hr.sil.android.seeusadmin.util.UiEvent
import hr.sil.android.seeusadmin.util.backend.UserUtil
import hr.sil.android.seeusadmin.util.isEmailValid
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.text.isBlank

class LoginViewModel : BaseViewModel<LoginScreenUiState, LoginScreenEvent>() {

    val log = logger()

    override fun initialState(): LoginScreenUiState {
        return LoginScreenUiState()
    }

    init {
        log.info("collecting event: start new viewmodel")
    }

    override fun onEvent(event: LoginScreenEvent) {
        when (event) {
            is LoginScreenEvent.OnLogin -> {

                viewModelScope.launch {
                    _state.update { it.copy(loading = true) }

                    delay(3000)

                    val userStatus = UserUtil.login(
                        event.email,
                        event.password
                    )
                    log.info("userStatus is: $userStatus")
                    _state.update { it.copy(loading = false) }
                    if (userStatus) {
                        log.info("UserUtil.user?.hasAcceptedTerms is: ${UserUtil.user?.name}")

                            log.info("event.password is: ${event.password}")
                            SettingsHelper.userPasswordWithoutEncryption = event.password

                            SettingsHelper.usernameLogin = event.email
                            sendUiEvent(LoginScreenUiEvent.NavigateToMainActivityScreen)
                    }  else {
                        sendUiEvent(
                            UiEvent.ShowToast(
                                "Email and password don't match, or your account has been disabled.",
                                Toast.LENGTH_SHORT
                            )
                        )
                    }
                }

//                viewModelScope.launch {
//                    _state.update { it.copy(loading = true) }
//                    login(email = event.email, password = event.password, context = event.context)
//                    _state.update { it.copy(loading = false) }
//                }
            }
            LoginScreenEvent.OnForgottenPassword -> {
                sendUiEvent(LoginScreenUiEvent.NavigateToForgotPasswordScreen)
            }
        }
    }

    fun getEmailError(email: String, context: Context): String {
        var emailError = ""
        if (email.isBlank()) {
            emailError = context.getString(R.string.edit_user_validation_blank_fields_exist)
        }

        return emailError
    }

    fun getPasswordError(password: String, context: Context): String {
        var passwordError = ""
        if (password.isBlank()) {
            passwordError = context.getString(R.string.edit_user_validation_blank_fields_exist)
        }

        return passwordError
    }

    //fun getUserEmail(): String = sharedPrefsStorage.getUserEmail()
}

data class LoginScreenUiState(
    val loading: Boolean = false
)

sealed interface LoginScreenEvent {
    data class OnLogin(val email: String, val password: String, val context: Context, val activity: Activity) : LoginScreenEvent
    object OnForgottenPassword : LoginScreenEvent
}

sealed class LoginScreenUiEvent: UiEvent {
    object NavigateToNextScreen : LoginScreenUiEvent()

    object NavigateToMainActivityScreen : LoginScreenUiEvent()

    object NavigateToForgotPasswordScreen : LoginScreenUiEvent()
    object NavigateBack : LoginScreenUiEvent()
}