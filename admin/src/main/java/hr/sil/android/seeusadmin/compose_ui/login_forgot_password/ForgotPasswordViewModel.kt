package hr.sil.android.seeusadmin.compose_ui.login_forgot_password


import android.app.Activity
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.viewModelScope
import hr.sil.android.mplhuber.core.util.logger
import hr.sil.android.seeusadmin.R
import hr.sil.android.seeusadmin.util.BaseViewModel
import hr.sil.android.seeusadmin.util.UiEvent
import hr.sil.android.seeusadmin.util.backend.UserUtil
import hr.sil.android.seeusadmin.util.isEmailValid
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ForgotPasswordViewModel  : BaseViewModel<ForgotPasswordUiState, ForgotPasswordEvent>() {

    val log = logger()

    override fun initialState(): ForgotPasswordUiState {
        return ForgotPasswordUiState()
    }

    override fun onEvent(event: ForgotPasswordEvent) {
        when (event) {
            is ForgotPasswordEvent.OnForgotPasswordRequest -> {

                viewModelScope.launch {
                    _state.update { it.copy(loading = true) }
                    val response = UserUtil.passwordRecovery(
                        event.email
                    )
                    _state.update { it.copy(loading = false) }

                    //log.info("Response code: ${response.code()}, is successfully: ${response.isSuccessful}, body is: ${response.body()}")

                    if (response ) {
                        //log.info("Response code 22: ${response.code()}, is successfully: ${response.isSuccessful}, body is: ${response.body()}")
                        log.info("SUCESS FORGOT PASSWORD RECOVERY REQUEST")
                        sendUiEvent(ForgotPasswordUiEvent.NavigateToNextScreen)
                    } else {
                        sendUiEvent(
                            UiEvent.ShowToast(
                                "Email doesn't exist in the system",
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

        }
    }

    fun getEmailError(email: String, context: Context): String {
        var emailError = ""
        if (email.isBlank()) {
            emailError = context.getString(R.string.edit_user_validation_blank_fields_exist)
        } else if (!email.isEmailValid()) {
            emailError = context.getString(R.string.message_email_invalid)
        }

        return emailError
    }

}

data class ForgotPasswordUiState(
    val loading: Boolean = false
)

sealed class ForgotPasswordEvent {
    data class OnForgotPasswordRequest(
        val email: String,
        val context: Context,
        val activity: Activity
    ) : ForgotPasswordEvent()
}

sealed class ForgotPasswordUiEvent : UiEvent {
    object NavigateToNextScreen : ForgotPasswordUiEvent()

    object NavigateBack : ForgotPasswordUiEvent()
}