package hr.sil.android.seeusadmin.compose_ui.login_forgot_password

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.saveable.rememberSaveable
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
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
import hr.sil.android.seeusadmin.util.UiEvent
import hr.sil.android.seeusadmin.view.activity.MainActivity

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordUpdateScreen(
    modifier: Modifier = Modifier,
    viewModel: ForgotPasswordUpdateViewModel,
    nextScreen: (route: String) -> Unit = {},
    navigateUp: () -> Unit = {}
) {

    val state = viewModel.state.collectAsStateWithLifecycle().value

    val activity = LocalContext.current as Activity
    val context = LocalContext.current

    val imagePassword = painterResource(id = R.drawable.ic_password) // ?attr/thmLoginPassword
    val imagePin = painterResource(id = R.drawable.ic_email) // ?attr/thmLoginEmail
    val imageVisibilityOn = painterResource(id = R.drawable.ic_password)
    val imageVisibilityOff = painterResource(id = R.drawable.ic_password)

    var password by remember {
        mutableStateOf("")
    }
    var repeatPassword = rememberSaveable {
        mutableStateOf("")
    }
    var pin by remember {
        mutableStateOf("")
    }
    var passwordVisible by rememberSaveable {
        mutableStateOf(false)
    }
    var pinVisible by rememberSaveable {
        mutableStateOf(false)
    }
    var showPasswords by remember {
        mutableStateOf(false)
    }
    var isButtonEnabled by remember {
        mutableStateOf(true)
    }
    var errorMessagePassword by remember {
        mutableStateOf<String?>(null)
    }

    val errorMessageRepeatPassword = rememberSaveable { mutableStateOf<String?>(null) }
    var errorMessagePin by remember {
        mutableStateOf<String?>(null)
    }

    val passwordLabelStyle = remember {
        mutableStateOf(AppTypography.labelLarge)
    }

    val pinLabelStyle = remember {
        mutableStateOf(AppTypography.labelLarge)
    }

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    // Update password visibility when showPasswords changes
    LaunchedEffect(showPasswords) {
        passwordVisible = showPasswords
        pinVisible = showPasswords
    }

    LaunchedEffect(key1 = Unit) {
        val log = logger()
        log.info("collecting events: start ${viewModel.uiEvents}")
        viewModel.uiEvents.collect { event ->
            log.info("collecting event: ${event}")
            when (event) {
                is ForgotPasswordUpdateUiEvent.NavigateBack -> {
                    navigateUp()
                }

                is ForgotPasswordUpdateUiEvent.NavigateToNextScreen -> {
                    nextScreen(SignUpOnboardingSections.LOGIN_SCREEN.route)
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
            val (appBar, tvForgotPassword, tvForgotPasswordDescription, clPassword, clRepeatPassword, clPin, tvShowPasswords, progressBar, btnPasswordUpdate) = createRefs()

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
                text = stringResource(id = R.string.reset_password_title).uppercase(), // textAllCaps="true"
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
                text = stringResource(id = R.string.reset_password_description_title),
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

            // 4. Password Input (clPassword - ConstraintLayout)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, start = 15.dp, end = 15.dp)
                    .constrainAs(clPassword) {
                        top.linkTo(tvForgotPasswordDescription.bottom)
                    }
            ) {
                TextField(
                    value = password,
                    placeholder = {
                        Text(
                            text = stringResource(R.string.nav_settings_new_password),
                            color = ThmEdittextHintColor, // ?attr/thmEdittextHintColor
                            style = passwordLabelStyle.value
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
                        password = it
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics {
                            contentDescription = "passwordTextFieldPasswordUpdateScreen"
                        }
                        .onFocusChanged {
                            if (it.isFocused) {
                                passwordLabelStyle.value = AppTypography.bodySmall
                            } else {
                                passwordLabelStyle.value = AppTypography.bodyLarge
                            }
                        },
                    maxLines = 1,
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None
                    else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next
                    ),
                    trailingIcon = {
                        Icon(
                            painter = imagePassword,
                            contentDescription = null,
                            tint = Color.Unspecified,
                            modifier = Modifier
                                .padding(top = 5.dp)
                                .width(25.dp)
                        )
                    },
                    isError = errorMessagePassword != null
                )

                // Display error message if exists
                if (errorMessagePassword != null) {
                    Text(
                        text = errorMessagePassword ?: "",
                        color = ThmErrorTextColor, // ?attr/thmErrorTextColor
                        fontSize = 14.sp,
                        style = AppTypography.bodySmall,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp, start = 15.dp, end = 15.dp)
                    )
                }
            }

            // 5. Password Input (clRepeatPassword - ConstraintLayout)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, start = 15.dp, end = 15.dp)
                    .constrainAs(clRepeatPassword) {
                        top.linkTo(clPassword.bottom)
                    }
            ) {
                TextField(
                    value = repeatPassword.value,
                    placeholder = {
                        Text(
                            text = stringResource(R.string.nav_settings_retype_new),
                            color = ThmEdittextHintColor, // ?attr/thmEdittextHintColor
                            style = passwordLabelStyle.value
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
                        repeatPassword.value = it
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics {
                            contentDescription = "passwordTextFieldPasswordUpdateScreen"
                        }
                        .onFocusChanged {
                            if (it.isFocused) {
                                passwordLabelStyle.value = AppTypography.bodySmall
                            } else {
                                passwordLabelStyle.value = AppTypography.bodyLarge
                            }
                        },
                    maxLines = 1,
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None
                    else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next
                    ),
                    trailingIcon = {
                        Icon(
                            painter = imagePassword,
                            contentDescription = null,
                            tint = Color.Unspecified,
                            modifier = Modifier
                                .padding(top = 5.dp)
                                .width(25.dp)
                        )
                    },
                    isError = errorMessagePassword != null
                )

                // Display error message if exists
                if (errorMessagePassword != null) {
                    Text(
                        text = errorMessagePassword ?: "",
                        color = ThmErrorTextColor, // ?attr/thmErrorTextColor
                        fontSize = 14.sp,
                        style = AppTypography.bodySmall,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp, start = 15.dp, end = 15.dp)
                    )
                }
            }

            // 5. PIN Input (clPin - ConstraintLayout)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, start = 15.dp, end = 15.dp)
                    .constrainAs(clPin) {
                        top.linkTo(clRepeatPassword.bottom)
                    }
            ) {
                TextField(
                    value = pin,
                    placeholder = {
                        Text(
                            text = stringResource(R.string.reset_password_pin),
                            color = ThmEdittextHintColor, // ?attr/thmEdittextHintColor
                            style = pinLabelStyle.value
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
                        pin = it
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics {
                            contentDescription = "pinTextFieldPasswordUpdateScreen"
                        }
                        .onFocusChanged {
                            if (it.isFocused) {
                                pinLabelStyle.value = AppTypography.bodySmall
                            } else {
                                pinLabelStyle.value = AppTypography.bodyLarge
                            }
                        },
                    maxLines = 1,
                    singleLine = true,
                    visualTransformation = if (pinVisible) VisualTransformation.None
                    else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number, // inputType="number"
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
                            painter = imagePin,
                            contentDescription = null,
                            tint = Color.Unspecified,
                            modifier = Modifier
                                .padding(top = 5.dp)
                                .width(25.dp)
                        )
                    },
                    isError = errorMessagePin != null
                )

                // Display error message if exists
                if (errorMessagePin != null) {
                    Text(
                        text = errorMessagePin ?: "",
                        color = ThmErrorTextColor, // ?attr/thmErrorTextColor
                        fontSize = 14.sp,
                        style = AppTypography.bodySmall,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp, start = 15.dp, end = 15.dp)
                    )
                }
            }

            // 6. Show Passwords Text (tvShowPasswords)
            TextViewWithFont(
                text = stringResource(id = R.string.intro_register_show_password),
                color = ThmLoginDescriptionTextColor, // ?attr/thmLoginDescriptionTextColor
                fontWeight = FontWeight.Normal, // ?attr/thmMainFontTypeRegular
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp)
                    .constrainAs(tvShowPasswords) {
                        top.linkTo(clPin.bottom)
                    }
                    .clickable {
                        showPasswords = !showPasswords
                    }
            )

            // 7. Progress Bar (progressBar)
            if (state.loading) {
                RotatingRingIndicator(
                    modifier = Modifier
                        .size(40.dp)
                        .padding(top = 10.dp)
                        .constrainAs(progressBar) {
                            top.linkTo(tvShowPasswords.bottom)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                        }
                )
            }

            // 8. Submit Button (btnPasswordUpdate)
            ButtonWithFont(
                text = stringResource(id = R.string.forgot_password_send).uppercase(),
                onClick = {
                    val passwordValidation = viewModel.getPasswordError(password, context)
                    val pinValidation = viewModel.getPinError(pin, context)

                    if (passwordValidation.isNotBlank() || pinValidation.isNotBlank()) {
                        errorMessagePassword = passwordValidation.ifBlank { null }
                        errorMessagePin = pinValidation.ifBlank { null }
                    } else {
                        isButtonEnabled = false
                        viewModel.onEvent(
                            ForgotPasswordUpdateEvent.OnForgotPasswordUpdateRequest(
                                password = password,
                                pin = pin,
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
                    .constrainAs(btnPasswordUpdate) {
                        bottom.linkTo(parent.bottom, margin = 40.dp)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    },
                enabled = isButtonEnabled
            )
        }
    }
}