package hr.sil.android.seeusadmin.compose_ui.login_forgot_password

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
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
import hr.sil.android.seeusadmin.compose_ui.components.ProgressIndicatorSize
import hr.sil.android.seeusadmin.compose_ui.components.RotatingRingIndicator
import hr.sil.android.seeusadmin.compose_ui.components.TextViewWithFont
import hr.sil.android.seeusadmin.compose_ui.components.ThmButtonTextSize
import hr.sil.android.seeusadmin.compose_ui.components.ThmErrorTextColor
import hr.sil.android.seeusadmin.compose_ui.components.ThmLoginButtonTextColor
import hr.sil.android.seeusadmin.compose_ui.sign_up_onboarding.SignUpOnboardingSections
import hr.sil.android.seeusadmin.compose_ui.theme.AppTypography
import hr.sil.android.seeusadmin.util.UiEvent
import hr.sil.android.seeusadmin.view.activity.MainActivity
import androidx.compose.material3.MaterialTheme as Material3

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel,
    nextScreen: (route: String) -> Unit = {},
    navigateUp: (route: String) -> Unit = {}
) {

    val state = viewModel.state.collectAsStateWithLifecycle().value

    val activity = LocalContext.current as Activity

    val imageCheck = painterResource(id = R.drawable.ic_register_email)
    val imageInfo = painterResource(id = R.drawable.ic_register_email)
    val imageVisibilityOn = painterResource(id = R.drawable.ic_password)
    val imageVisibilityOff = painterResource(id = R.drawable.ic_password)

    var email by remember {
        mutableStateOf("")
    }
    var password by remember {
        mutableStateOf("")
    }
    var passwordVisible by rememberSaveable {
        mutableStateOf(false)
    }
    var isButtonEnabled by remember {
        mutableStateOf(true)
    }
    var errorMessageEmail by remember {
        mutableStateOf<String?>(null)
    }
    var errorMessagePassword by remember {
        mutableStateOf<String?>(null)
    }

    val emailLabelStyle = remember {
        mutableStateOf(AppTypography.labelLarge)
    }

    val passwordLabelStyle = remember {
        mutableStateOf(AppTypography.bodyLarge)
    }

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

    LaunchedEffect(key1 = Unit) {
        val log = logger()
        log.info("collecting events: start ${viewModel.uiEvents}")
        log.info("collecting events: viewModel ${viewModel}")
        viewModel.uiEvents.collect { event ->
            log.info("collecting event: ${event}")
            when (event) {

                is LoginScreenUiEvent.NavigateToMainActivityScreen -> {
                    val startIntent = Intent(context, MainActivity::class.java)
                    context.startActivity(startIntent)
                    activity.finish()
                }

                is LoginScreenUiEvent.NavigateBack -> {
                    logger().info("Back button clicked")
                    //navigateUp(SignUpOnboardingSections.FIRST_ONBOARDING_SCREEN.route)
                }

                is LoginScreenUiEvent.NavigateToForgotPasswordScreen -> {
                    nextScreen(SignUpOnboardingSections.FORGOT_PASSWORD_SCREEN.route)
                }

                is UiEvent.ShowToast -> {
                    isButtonEnabled = true
                    Toast.makeText(context, event.message, event.toastLength).show()
                }
            }
        }
    }

    Image(
        painter = painterResource(R.drawable.bg_home_screen),
        contentDescription = "",
        modifier = Modifier.fillMaxSize()
    )
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Main container is ConstraintLayout to replicate XML's behavior
        ConstraintLayout(
            modifier = Modifier
                .fillMaxSize()
        ) {
            val (appBar, clEmail, rlPassword, tvForgotPassword, progressBar, btnLogin) = createRefs()

            // 1. AppBarLayout (appBarLayout)
            // Replicated using a Column/Box within the ConstraintLayout
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .constrainAs(appBar) {
                        top.linkTo(parent.top)
                    }
            ) {
                // AppBarLayout itself
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        //.background(ThmToolbarBackgroundColor)
                        .windowInsetsPadding(WindowInsets.statusBars) // Simulates fitsSystemWindows="true"
                        .padding(vertical = 0.dp), // Simulates app:elevation="0dp",
                    contentAlignment = Alignment.Center
                ) {
                    // RelativeLayout (rlTopLayout)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Max) // IntrinsicSize.Max to size based on children
                            .padding(top = 4.dp)
                    ) {
                        // Toolbar (toolbar) - Replicated by a fixed height container with inner content alignment
                        Spacer(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                        ) // ?attr/actionBarSize

                        // ImageView (Header Image)
                        TextViewWithFont(
                            text = stringResource(id = R.string.app_generic_sign_in).uppercase(),
                            //color = ThmLoginDescriptionTextColor,
                            fontWeight = FontWeight.Normal, // ?attr/thmMainFontTypeRegular
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, start = 15.dp, end = 15.dp)
                    .constrainAs(clEmail) {
                        top.linkTo(appBar.bottom)
                    }
            ) {
                TextField(
                    value = email,
                    placeholder = {
                        Text(
                            text = stringResource(R.string.app_generic_email).uppercase(),
                            color = Material3.colorScheme.onSurfaceVariant,
                            style = passwordLabelStyle.value
                        )
                    },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        textColor = Material3.colorScheme.onSurface,
                        focusedBorderColor = colorResource(R.color.colorPrimary),
                        unfocusedBorderColor = Material3.colorScheme.outline,
                        cursorColor = colorResource(R.color.colorPrimary),
                        //backgroundColor = DarkModeTransparent
                    ),
                    onValueChange = {
                        email = it
                    },
                    modifier = Modifier
                        .semantics {
                            contentDescription = "emailTextFieldLoginScreen"
                        }
                        .onFocusChanged {
                            if (it.isFocused) {
                                emailLabelStyle.value = AppTypography.bodySmall
                            } else {
                                emailLabelStyle.value = AppTypography.bodyLarge
                            }
                        }
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    maxLines = 1,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    trailingIcon = {
                        if (errorMessageEmail != null && errorMessageEmail !== "") {
                            Icon(
                                painter = imageInfo,
                                contentDescription = null,
                                tint = Material3.colorScheme.error,
                                modifier = Modifier
                                    .width(25.dp)
                                    .semantics {
                                        contentDescription = "loginExclamationMark"
                                    }
                            )
                        } else if (errorMessageEmail != null && email.contains("@")) {
                            Icon(
                                painter = imageCheck,
                                contentDescription = null,
                                modifier = Modifier
                                    .width(25.dp)
                                    .semantics { contentDescription = "loginCheckMark" }
                            )
                        } else {
                            Icon(
                                painter = imageInfo,
                                contentDescription = null,
                                modifier = Modifier
                                    .width(25.dp)
                                    .semantics {
                                        contentDescription = "loginExclamationMark"
                                    }
                            )
                        }
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

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, start = 15.dp, end = 15.dp)
                    .constrainAs(rlPassword) {
                        top.linkTo(clEmail.bottom)
                    }
            ) {
                // 4. Password Input (rlPassword - RelativeLayout)
                TextField(
                    value = password,
                    onValueChange = {
                        password = it
                    },
                    placeholder = {
                        Text(
                            text = stringResource(R.string.app_generic_password).uppercase(),
                            color = Material3.colorScheme.onSurfaceVariant,
                            style = passwordLabelStyle.value
                        )
                    },
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None
                    else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            keyboardController?.hide()
                            focusManager.clearFocus()
                        }
                    ),
                    modifier = Modifier
                        .semantics {
                            contentDescription = "passwordTextFieldLoginScreen"
                        }
                        .onFocusChanged {
                            if (it.isFocused) {
                                passwordLabelStyle.value = AppTypography.bodySmall
                            } else {
                                passwordLabelStyle.value = AppTypography.bodyLarge
                            }
                        }
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(50.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        textColor = Material3.colorScheme.onSurface,
                        focusedBorderColor = colorResource(R.color.colorPrimary),
                        unfocusedBorderColor = Material3.colorScheme.outline,
                        cursorColor = colorResource(R.color.colorPrimary),
                        backgroundColor = colorResource(R.color.transparentColor)
                    ),
                    trailingIcon = {
                        val visibilityImage = if (passwordVisible)
                            imageVisibilityOn else imageVisibilityOff
                        IconButton(onClick = {
                            passwordVisible = !passwordVisible
                        }) {
                            Icon(
                                painter = visibilityImage,
                                contentDescription = null,
                                modifier = Modifier.width(25.dp)
                            )
                        }
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

            // 5. Forgot Password (tvForgotPassword)
            TextViewWithFont(
                text = stringResource(id = R.string.intro_register_show_password),
                //color = ThmLoginDescriptionTextColor,
                fontWeight = FontWeight.Normal, // ?attr/thmMainFontTypeRegular
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp)
                    .constrainAs(tvForgotPassword) {
                        top.linkTo(rlPassword.bottom)
                    }
                    .clickable(
                        onClick = {
                            nextScreen(SignUpOnboardingSections.FORGOT_PASSWORD_SCREEN.route)
                        })
            )

            // 6. Progress Bar (progressBar)
            if (state.loading) {
                RotatingRingIndicator(
                    modifier =   Modifier
                        .size(ProgressIndicatorSize) // 40.dp
                        .padding(top = 10.dp)
                        .constrainAs(progressBar) {
                            top.linkTo(tvForgotPassword.bottom)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                        }
                )
            }

            // 7. Login Button (btnLogin)
            ButtonWithFont(
                text = stringResource(id = R.string.app_generic_sign_in).uppercase(),
                onClick = {
                    val emailValidation = viewModel.getEmailError(email, context)
                    val passwordValidation = viewModel.getPasswordError(password, context)

                    if (emailValidation.isNotBlank() || passwordValidation.isNotBlank()) {
                        errorMessageEmail = emailValidation.ifBlank { "" }
                        errorMessagePassword = passwordValidation.ifBlank { "" }
                    }
                    else {
                        isButtonEnabled = false
                        viewModel.onEvent(
                            LoginScreenEvent.OnLogin(
                                email = email,
                                password = password,
                                context = context,
                                activity = activity
                            )
                        )
                    }
                },
                backgroundColor = colorResource(R.color.colorCyenTransparent),
                //backgroundColor = ThmMainButtonBackgroundColor,
                textColor = ThmLoginButtonTextColor,
                fontSize = ThmButtonTextSize,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.05.sp, // ?attr/thmButtonLetterSpacing (Placeholder)
                modifier = Modifier
                    .width(250.dp)
                    .constrainAs(btnLogin) {
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        bottom.linkTo(parent.bottom , margin = 30.dp)
                        //bottom.linkTo(llRegister.top, margin = 12.dp)
                    },
                enabled = isButtonEnabled
            )
        }
    }
}