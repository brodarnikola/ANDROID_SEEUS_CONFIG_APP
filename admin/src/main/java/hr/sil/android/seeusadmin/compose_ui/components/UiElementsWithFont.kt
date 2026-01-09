package hr.sil.android.seeusadmin.compose_ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import hr.sil.android.seeusadmin.compose_ui.theme.ColorWhite


val ThmLoginDescriptionTextColor = ColorWhite
val ThmErrorTextColor = Color(0xFFca0000)
val ThmEdittextHintColor = ColorWhite // ?attr/thmEdittextHintColor
val ThmTitleTextSize = 19.sp // ?attr/thmTitleTextSize
val ThmEdittextTextSize = 12.sp // ?attr/thmEdittextTextSize


val ThmNavigationDrawerMenuTextSize = 17.sp // ?attr/thmTitleTextSize
val ThmDescriptionTextSize = 14.sp
val ThmSubTitleTextSize = 17.sp

val ThmTitleLetterSpacing = 2.sp

// Simplified custom text view replacement
@Composable
fun TextViewWithFont(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: androidx.compose.ui.unit.TextUnit = 14.sp,
    fontWeight: FontWeight = FontWeight.Normal, // ?attr/thmMainFontTypeRegular/Bold/Medium
    textAlign: TextAlign? = null,
    letterSpacing: androidx.compose.ui.unit.TextUnit = 0.sp,
    maxLines: Int = 1,
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,
        fontSize = fontSize,
        fontWeight = fontWeight,
        fontFamily = FontFamily.Default, // ?attr/thmMainFontName placeholder
        textAlign = textAlign,
        letterSpacing = letterSpacing,
        maxLines = maxLines
    )
}

// Simplified custom button replacement
//@Composable
//fun ButtonWithFont(
//    text: String,
//    onClick: () -> Unit,
//    modifier: Modifier = Modifier,
//    backgroundColor: Color,
//    textColor: Color,
//    fontSize: androidx.compose.ui.unit.TextUnit,
//    fontWeight: FontWeight,
//    letterSpacing: androidx.compose.ui.unit.TextUnit
//) {
//    Button(
//        onClick = onClick,
//        modifier = modifier.height(40.dp),
//        colors = ButtonDefaults.buttonColors(containerColor = backgroundColor),
//        contentPadding = PaddingValues(0.dp)
//    ) {
//        TextViewWithFont(
//            text = text,
//            color = textColor,
//            fontSize = fontSize,
//            fontWeight = fontWeight,
//            letterSpacing = letterSpacing
//        )
//    }
//}

// Simplified custom edit text replacement
@Composable
fun EdittextWithFont(
    value: String,
    onValueChange: (String) -> Unit,
    hint: String,
    modifier: Modifier = Modifier,
    textColor: Color,
    textSize: androidx.compose.ui.unit.TextUnit,
    letterSpacing: androidx.compose.ui.unit.TextUnit,
    paddingLeft: Dp // Used to simulate paddingLeft="?attr/thmEdittextLeftPadding"
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .padding(start = paddingLeft, top = 8.dp, bottom = 8.dp),
        textStyle = LocalTextStyle.current.copy(
            color = textColor,
            fontSize = textSize,
            letterSpacing = letterSpacing,
            fontWeight = FontWeight.Normal // ?attr/thmMainFontTypeRegular
        ),
        singleLine = true,
        decorationBox = { innerTextField ->
            if (value.isEmpty()) {
                Text(
                    text = hint,
                    color = ThmEdittextHintColor.copy(alpha = 0.6f),
                    fontSize = textSize,
                    letterSpacing = letterSpacing
                )
            }
            innerTextField()
        }
    )
}

// Placeholder for string resources and icon resources
object R {
    object string {
        const val app_generic_sign_in = "SIGN IN"
        const val app_generic_email = "Email"
        const val app_generic_password = "Password"
        const val intro_register_show_password = "Show Password"
        const val forgot_password_title = "Forgot Password?"
        const val nav_login_missing_account = "Don't have an account?"
        const val register_submit_title = "Register"
    }
    object drawable {
        const val ic_toolbar_header = 1 // ?attr/thmToolbarHeader
        const val ic_toolbar_support = 2 // ?attr/thmToolbarHeaderSupportImage
        const val ic_login_email = 3 // ?attr/thmLoginEmail
        const val ic_login_password = 4 // ?attr/thmLoginPassword
        const val rounded_button = 5
    }
}