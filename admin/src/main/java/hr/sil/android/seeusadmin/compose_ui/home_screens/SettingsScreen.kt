package hr.sil.android.seeusadmin.compose_ui.home_screens

import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll

import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.*

import androidx.compose.material3.MaterialTheme as Material3

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import hr.sil.android.ble.scanner.scan_multi.properties.advv2.common.MPLDeviceStatus
import kotlin.collections.forEach
import kotlin.text.uppercase

import kotlin.collections.count
import kotlin.collections.find
import kotlin.collections.forEach
import kotlin.jvm.java
import kotlin.text.uppercase

import hr.sil.android.seeusadmin.R
import hr.sil.android.seeusadmin.compose_ui.components.ButtonWithFont
import hr.sil.android.seeusadmin.compose_ui.components.ProgressIndicatorSize
import hr.sil.android.seeusadmin.compose_ui.components.RotatingRingIndicator
import hr.sil.android.seeusadmin.compose_ui.components.TextViewWithFont
import hr.sil.android.seeusadmin.compose_ui.components.ThmButtonLetterSpacing
import hr.sil.android.seeusadmin.compose_ui.components.ThmButtonTextSize
import hr.sil.android.seeusadmin.compose_ui.components.ThmErrorTextColor
import hr.sil.android.seeusadmin.compose_ui.components.ThmLoginButtonTextColor
import hr.sil.android.seeusadmin.compose_ui.components.ThmLoginDescriptionTextColor
import hr.sil.android.seeusadmin.compose_ui.login_forgot_password.LoginScreenEvent
import hr.sil.android.seeusadmin.compose_ui.sign_up_onboarding.SignUpOnboardingActivity
import hr.sil.android.seeusadmin.compose_ui.theme.AppTypography
import hr.sil.android.seeusadmin.store.DeviceStore
import hr.sil.android.seeusadmin.util.SettingsHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = viewModel(),
    //onLogout: () -> Unit,
    //onRestartApp: () -> Unit
) {

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.loadInitialData()
        viewModel.loadLanguages()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {

            SettingsNameSection(viewModel)

            SettingsEmailSection(viewModel)

            LanguageSection(viewModel )

            ChangePasswordSection(
                viewModel.passwordVisible,
                viewModel
            )

            ShowPasswordHint(viewModel.passwordVisible )

            SubmitButton(
                isLoading = viewModel.showProgress.value
            ) {
                viewModel.submit(
                    onSuccess = {
                        SettingsHelper.setLocale(context)
                        val selectedLanguage =
                            viewModel.languages.value.find { it.name == viewModel.selectedLanguage.value?.name }
                        println("selected language is: ${selectedLanguage?.name}")
                        println("selected language code is: ${selectedLanguage?.code}")

                        println("selected language SettingsHelper languageName is: ${SettingsHelper.languageName}")
                        if (selectedLanguage?.code != SettingsHelper.languageName) {
                            SettingsHelper.languageName = selectedLanguage?.code ?: "EN"

//                            val intent = Intent()
//                            val packageName = MainActivity::class.java.getPackage()?.name ?: ""
//                            println("package name is: ${packageName}")
//                            val componentName =
//                                ComponentName(packageName, packageName + ".aliasSplashActivity")
//                            intent.component = componentName
//                            intent.flags =
//                                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//
//                            context.startActivity(intent)
//                            (context as? Activity)?.finishAffinity()

               // startActivity(intent)
                //finish()

                            val intent = Intent(context, SignUpOnboardingActivity::class.java)
                            //intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                            intent.flags =
                                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            context.startActivity(intent)
                            (context as? Activity)?.finishAffinity()
                        }
                        //onLogout()
                    }
                )
            }

            SettingsVersionText()
        }
    }
}

@Composable
private fun SettingsNameSection(
    viewModel: SettingsViewModel
) {
    TextViewWithFont(
        text = stringResource(id = R.string.app_generic_name),
        color = colorResource(R.color.colorWhite),
        fontSize = 13.sp,
        fontWeight = FontWeight.Normal, // ?attr/thmMainFontTypeRegular
        textAlign = TextAlign.Start,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp)
    )

    TextField(
        value = viewModel.name.value,
        placeholder = {
            Text(
                text = stringResource(R.string.app_generic_username).uppercase(),
                color = ThmLoginDescriptionTextColor
            )
        },
        colors = TextFieldDefaults.outlinedTextFieldColors(
            textColor = ThmLoginDescriptionTextColor,
            focusedBorderColor = colorResource(R.color.colorPrimary),
            unfocusedBorderColor = Material3.colorScheme.outline,
            cursorColor = colorResource(R.color.colorPrimary),
            //backgroundColor = DarkModeTransparent
        ),
        onValueChange = {
            viewModel.name.value = it
        },
        modifier = Modifier
            .semantics {
                contentDescription = "emailTextFieldLoginScreen"
            }
            .fillMaxWidth(),
        maxLines = 1,
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Next
        ),
        enabled = true
    )

    if (viewModel.nameError.value) {
        ErrorText(stringResource(R.string.edit_user_validation_blank_fields_exist) )
    }
}

@Composable
private fun LanguageSection(
    viewModel: SettingsViewModel
) {
    TextViewWithFont(
        text = stringResource(id = R.string.nav_settings_language),
        color = colorResource(R.color.colorWhite),
        fontSize = 13.sp,
        fontWeight = FontWeight.Normal, // ?attr/thmMainFontTypeRegular
        textAlign = TextAlign.Start,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 15.dp)
    )

    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp)
    ) {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 5.dp)
        ) {

            TextViewWithFont(
                text = viewModel.selectedLanguage?.value?.name ?: "",
                color = colorResource(R.color.colorBlack),
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal, // ?attr/thmMainFontTypeRegular
                textAlign = TextAlign.Start,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 15.dp)
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .width(350.dp)
                .padding(horizontal = 20.dp)
        ) {
            viewModel.languages.value.forEach { language ->
                DropdownMenuItem(
                    text = { Text(language.name, color = colorResource(R.color.colorBlack)) },
                    onClick = {
                        viewModel.selectedLanguage.value = language
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun SettingsVersionText(
) {
    TextViewWithFont(
        text = stringResource(
            R.string.nav_settings_app_version,
            stringResource(R.string.app_version)
        ),
        color = colorResource(R.color.colorWhite),
        fontSize = 13.sp,
        fontWeight = FontWeight.Normal, // ?attr/thmMainFontTypeRegular
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
    )
}

@Composable
fun SettingsEmailSection(
    viewModel: SettingsViewModel
) {

    TextViewWithFont(
        text = stringResource(id = R.string.app_generic_email),
        color = colorResource(R.color.colorWhite),
        fontSize = 13.sp,
        fontWeight = FontWeight.Normal, // ?attr/thmMainFontTypeRegular
        textAlign = TextAlign.Start,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp)
    )

    TextField(
        value = viewModel.email.value,
        placeholder = {
            Text(
                text = stringResource(R.string.app_generic_username).uppercase(),
                color = ThmLoginDescriptionTextColor
            )
        },
        colors = TextFieldDefaults.outlinedTextFieldColors(
            textColor = ThmLoginDescriptionTextColor,
            focusedBorderColor = colorResource(R.color.colorPrimary),
            unfocusedBorderColor = Material3.colorScheme.outline,
            cursorColor = colorResource(R.color.colorPrimary),
            //backgroundColor = DarkModeTransparent
        ),
        onValueChange = {
            viewModel.email.value = it
        },
        modifier = Modifier
            .semantics {
                contentDescription = "emailTextFieldLoginScreen"
            }
            .fillMaxWidth()
           ,
        maxLines = 1,
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next
        ),
        enabled = false
    )
}

@Composable
fun ShowPasswordHint(
    passwordVisible: MutableState<Boolean>
) {

    TextViewWithFont(
        text =  stringResource(R.string.intro_register_show_password),
        color = colorResource(R.color.colorWhite),
        fontSize = 13.sp,
        fontWeight = FontWeight.Normal, // ?attr/thmMainFontTypeRegular
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                passwordVisible.value = !passwordVisible.value
            }
            .padding(top = 10.dp, bottom = 10.dp)
    )
}


@Composable
fun PasswordField(
    passwordVisible: MutableState<Boolean>,
    viewModel: SettingsViewModel,
    oldPassword: Boolean,
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    error: String?
) {
    //var passwordVisible by remember { mutableStateOf(false) }

    TextViewWithFont(
        text = label,
        color = colorResource(R.color.colorWhite),
        fontSize = 13.sp,
        fontWeight = FontWeight.Normal, // ?attr/thmMainFontTypeRegular
        textAlign = TextAlign.Start,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp)
    )

    TextField(
        value = if (oldPassword) viewModel.oldPassword.value else viewModel.newPassword.value,
        placeholder = {

        },
        colors = TextFieldDefaults.outlinedTextFieldColors(
            textColor = ThmLoginDescriptionTextColor,
            focusedBorderColor = colorResource(R.color.colorPrimary),
            unfocusedBorderColor = Material3.colorScheme.outline,
            cursorColor = colorResource(R.color.colorPrimary),
            //backgroundColor = DarkModeTransparent
        ),
        onValueChange = {
            if (oldPassword) viewModel.oldPassword.value = it else viewModel.newPassword.value = it
        },
        modifier = Modifier
            .semantics {
                contentDescription = "emailTextFieldLoginScreen"
            }
            .fillMaxWidth() ,
        maxLines = 1,
        singleLine = true,
        visualTransformation = if (passwordVisible.value) VisualTransformation.None
        else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Next
        ),
        enabled = true
    )

    if (error != null) {
        ErrorText(error)
    }
}

@Composable
fun ErrorText(
    text: String
) {

    TextViewWithFont(
        text = text,
        color = colorResource(R.color.colorErrorTransparent),
        fontSize = 13.sp,
        fontWeight = FontWeight.Normal, // ?attr/thmMainFontTypeRegular
        textAlign = TextAlign.Start,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp)
    )
}

@Composable
private fun ChangePasswordSection(
    passwordVisible: MutableState<Boolean>,
    viewModel: SettingsViewModel
) {

    TextViewWithFont(
        text = stringResource(id = R.string.nav_settings_change_password).uppercase(),
        color = colorResource(R.color.colorWhite),
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold, // ?attr/thmMainFontTypeRegular
        textAlign = TextAlign.Start,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp)
    )

    PasswordField(
        passwordVisible = passwordVisible,
        viewModel = viewModel,
        oldPassword = true,
        label = stringResource(R.string.current_password),
        value = viewModel.oldPassword.value,
        onValueChange = { viewModel.oldPassword.value = it },
        error = viewModel.oldPasswordError.value
    )

    PasswordField(
        passwordVisible = passwordVisible,
        viewModel = viewModel,
        oldPassword = false,
        label = stringResource(R.string.reset_password_new),
        value = viewModel.newPassword.value,
        onValueChange = { viewModel.newPassword.value = it },
        error = viewModel.newPasswordError.value
    )
}

@Composable
private fun SubmitButton(
    isLoading: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            RotatingRingIndicator(
                modifier = Modifier
                    .size(ProgressIndicatorSize) // 40.dp
                    .padding(top = 10.dp)
            )
        } else {

            ButtonWithFont(
                text = stringResource(id = R.string.app_generic_submit).uppercase(),
                onClick = onClick,
                backgroundColor = colorResource(R.color.colorPrimary),
                //backgroundColor = ThmMainButtonBackgroundColor,
                textColor = ThmLoginButtonTextColor,
                fontSize = ThmButtonTextSize,
                fontWeight = FontWeight.Medium,
                letterSpacing = ThmButtonLetterSpacing, // ?attr/thmButtonLetterSpacing (Placeholder)
                modifier = Modifier
                    .width(210.dp)
                    .height(50.dp)
                    .padding(vertical = 2.dp),
                enabled = true
            )
        }
    }
}
