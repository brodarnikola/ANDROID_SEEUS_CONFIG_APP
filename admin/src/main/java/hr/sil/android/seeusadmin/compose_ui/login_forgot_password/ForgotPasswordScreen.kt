package hr.sil.android.seeusadmin.compose_ui.login_forgot_password

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import hr.sil.android.mplhuber.core.util.logger
import hr.sil.android.seeusadmin.R
import hr.sil.android.seeusadmin.compose_ui.components.ButtonWithFont
import hr.sil.android.seeusadmin.compose_ui.components.GradientBackground
import hr.sil.android.seeusadmin.compose_ui.components.RotatingRingIndicator
import hr.sil.android.seeusadmin.compose_ui.components.TextViewWithFont
import hr.sil.android.seeusadmin.compose_ui.theme.AppTypography
import hr.sil.android.seeusadmin.util.UiEvent
import androidx.compose.material3.MaterialTheme as Material3


import hr.sil.android.seeusadmin.compose_ui.components.ButtonWithFont
import hr.sil.android.seeusadmin.compose_ui.components.GradientBackground
import hr.sil.android.seeusadmin.compose_ui.components.ProgressIndicatorSize
import hr.sil.android.seeusadmin.compose_ui.components.RotatingRingIndicator
import hr.sil.android.seeusadmin.compose_ui.components.TextViewWithFont
import hr.sil.android.seeusadmin.compose_ui.components.ThmButtonLetterSpacing
import hr.sil.android.seeusadmin.compose_ui.components.ThmButtonTextSize
import hr.sil.android.seeusadmin.compose_ui.components.ThmDescriptionTextColor
import hr.sil.android.seeusadmin.compose_ui.components.ThmEdittextColor
import hr.sil.android.seeusadmin.compose_ui.components.ThmEdittextHintColor
import hr.sil.android.seeusadmin.compose_ui.components.ThmErrorTextColor
import hr.sil.android.seeusadmin.compose_ui.components.ThmLoginButtonTextColor
import hr.sil.android.seeusadmin.compose_ui.components.ThmLoginDescriptionTextColor
import hr.sil.android.seeusadmin.compose_ui.components.ThmMainButtonBackgroundColor
import hr.sil.android.seeusadmin.compose_ui.components.ThmSubTitleTextSize
import hr.sil.android.seeusadmin.compose_ui.components.ThmTitleLetterSpacing
import hr.sil.android.seeusadmin.compose_ui.components.ThmTitleTextColor
import hr.sil.android.seeusadmin.compose_ui.components.ThmTitleTextSize
import hr.sil.android.seeusadmin.compose_ui.sign_up_onboarding.SignUpOnboardingSections
import hr.sil.android.seeusadmin.compose_ui.theme.AppTypography
import hr.sil.android.seeusadmin.compose_ui.theme.DarkModeTransparent
import hr.sil.android.seeusadmin.view.activity.MainActivity

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    modifier: Modifier = Modifier,
    viewModel: ForgotPasswordViewModel,
    nextScreen: (route: String, email: String) -> Unit,
    navigateUp: () -> Unit = {}
) {

    val state = viewModel.state.collectAsStateWithLifecycle().value

    val activity = LocalContext.current as Activity
    val context = LocalContext.current

    val imageEmail = painterResource(id = R.drawable.ic_email) // ?attr/thmLoginEmail

    var email by remember {
        mutableStateOf("")
    }
    var isButtonEnabled by remember {
        mutableStateOf(true)
    }
    var errorMessageEmail by remember {
        mutableStateOf<String?>(null)
    }

    val emailLabelStyle = remember {
        mutableStateOf(AppTypography.labelLarge)
    }

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    LaunchedEffect(key1 = Unit) {
        val log = logger()
        log.info("collecting events: start ${viewModel.uiEvents}")
        viewModel.uiEvents.collect { event ->
            log.info("collecting event: ${event}")
            when (event) {
                is ForgotPasswordUiEvent.NavigateBack -> {
                    navigateUp()
                }

                is ForgotPasswordUiEvent.NavigateToNextScreen -> {
                    nextScreen(SignUpOnboardingSections.FORGOT_PASSWORD_UPDATE_SCREEN.route, email)
                }

                is UiEvent.ShowToast -> {
                    isButtonEnabled = true
                    Toast.makeText(context, event.message, event.toastLength).show()
                }
            }
        }
    }

    GradientBackground(
        modifier = Modifier.fillMaxSize()
    ) {
        // Main container is ConstraintLayout to replicate XML's behavior
        ConstraintLayout(
            modifier = Modifier
                .fillMaxSize()
        ) {
            val (appBar, tvForgotPassword, tvForgotPasswordDescription, clPassword, progressBar, btnPasswordRecovery) = createRefs()

            // 1. AppBarLayout (appBarLayout)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .constrainAs(appBar) {
                        top.linkTo(parent.top)
                    }
            ) {
                TopAppBar(
                    title = {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(IntrinsicSize.Max)
                                .padding(end = 30.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.seeus_black_thin), // ?attr/thmToolbarHeader
                                contentDescription = null,
                                tint = Color.Unspecified,
                                modifier = Modifier.align(Alignment.Center) // layout_centerInParent="true"
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { navigateUp() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = ""
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        //containerColor = ThmLoginBackground,
                        //containerColor = colorResource(R.color.colorPrimary),
                        //titleContentColor = colorResource(R.color.colorWhite),
                        navigationIconContentColor = colorResource(R.color.colorBlack)
                    )
                )
            }

            // 2. Title Text (tvForgotPassword)
            TextViewWithFont(
                text = stringResource(id = R.string.reset_password_title).uppercase(),
                color = ThmTitleTextColor, // ?attr/thmTitleTextColor
                fontSize = ThmTitleTextSize, // ?attr/thmTitleTextSize
                fontWeight = FontWeight.Medium, // ?attr/thmMainFontTypeMedium
                textAlign = TextAlign.Center,
                letterSpacing = ThmTitleLetterSpacing, // ?attr/thmTitleLetterSpacing
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, start = 15.dp, end = 15.dp)
                    .constrainAs(tvForgotPassword) {
                        top.linkTo(appBar.bottom)
                    }
            )

            // 3. Description Text (tvForgotPasswordDescription)
            TextViewWithFont(
                text = stringResource(id = R.string.forgot_password_description_title),
                color = ThmDescriptionTextColor, // ?attr/thmDescriptionTextColor
                fontSize = ThmSubTitleTextSize, // ?attr/thmSubTitleTextSize
                fontWeight = FontWeight.Normal, // ?attr/thmMainFontTypeRegular
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, start = 15.dp, end = 15.dp)
                    .constrainAs(tvForgotPasswordDescription) {
                        top.linkTo(tvForgotPassword.bottom)
                    }
            )

            // 4. Email Input (clPassword - ConstraintLayout)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, start = 15.dp, end = 15.dp)
                    .constrainAs(clPassword) {
                        top.linkTo(tvForgotPasswordDescription.bottom)
                    }
            ) {
                // TextInputLayout + EditText
                TextField(
                    value = email,
                    placeholder = {
                        Text(
                            text = stringResource(R.string.app_generic_email),
                            color = ThmEdittextHintColor, // ?attr/thmEdittextHintColor
                            style = emailLabelStyle.value
                        )
                    },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        textColor = ThmEdittextColor, // ?attr/thmEdittextColor
                        focusedBorderColor = colorResource(R.color.colorPrimary),
                        unfocusedBorderColor = Material3.colorScheme.outline,
                        cursorColor = colorResource(R.color.colorPrimary),
                        backgroundColor = DarkModeTransparent
                    ),
                    onValueChange = {
                        email = it
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics {
                            contentDescription = "emailTextFieldPasswordRecoveryScreen"
                        }
                        .onFocusChanged {
                            if (it.isFocused) {
                                emailLabelStyle.value = AppTypography.bodySmall
                            } else {
                                emailLabelStyle.value = AppTypography.bodyLarge
                            }
                        },
                    maxLines = 1,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            keyboardController?.hide()
                            focusManager.clearFocus()
                        }
                    ),
                    trailingIcon = {
                        Icon(
                            painter = imageEmail,
                            contentDescription = null,
                            tint = Color.Unspecified,
                            modifier = Modifier
                                .padding(top = 5.dp)
                                .width(25.dp)
                        )
                    },
                    isError = errorMessageEmail != null
                )

                // Display error message if exists
                if (errorMessageEmail != null) {
                    Text(
                        text = errorMessageEmail ?: "",
                        color = ThmErrorTextColor, // ?attr/thmErrorTextColor
                        fontSize = 14.sp,
                        style = AppTypography.bodySmall,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp, start = 15.dp, end = 15.dp)
                    )
                }
            }

            // 5. Progress Bar (progressBar)
            if (state.loading) {
                RotatingRingIndicator(
                    modifier = Modifier
                        .size(40.dp)
                        .padding(top = 10.dp)
                        .constrainAs(progressBar) {
                            top.linkTo(clPassword.bottom)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                        }
                )
            }

            // 6. Submit Button (btnPasswordRecovery)
            ButtonWithFont(
                text = stringResource(id = R.string.forgot_password_send).uppercase(),
                onClick = {
                    val emailValidation = viewModel.getEmailError(email, context)

                    if (emailValidation.isNotBlank()) {
                        errorMessageEmail = emailValidation
                    } else {
                        isButtonEnabled = false
                        viewModel.onEvent(
                            ForgotPasswordEvent.OnForgotPasswordRequest(
                                email = email,
                                context = context,
                                activity = activity
                            )
                        )
                    }
                },
                backgroundColor = ThmMainButtonBackgroundColor, // ?attr/thmMainButtonBackgroundColor
                textColor = ThmLoginButtonTextColor, // ?attr/thmLoginButtonTextColor
                fontSize = ThmButtonTextSize, // ?attr/thmButtonTextSize
                fontWeight = FontWeight.Medium, // ?attr/thmMainFontTypeMedium
                letterSpacing = ThmButtonLetterSpacing, // ?attr/thmButtonLetterSpacing
                modifier = Modifier
                    .width(250.dp)
                    .height(40.dp)
                    .constrainAs(btnPasswordRecovery) {
                        bottom.linkTo(parent.bottom, margin = 40.dp)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    },
                enabled = isButtonEnabled
            )
        }
    }
}
