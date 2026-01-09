package hr.sil.android.seeusadmin.compose_ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme

import androidx.compose.material3.MaterialTheme as Material3
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.systemuicontroller.rememberSystemUiController



// Light color scheme with Zwick Branding
val LightColorScheme = lightColorScheme(
    primary = ZwickColorPrimaryDark,
    onPrimary = White,
    primaryContainer = ZwickColorPrimaryTransparent,
    onPrimaryContainer = ZwickColorPrimaryDark,
    secondary = ZwickColorAccent,
    onSecondary = White,
    secondaryContainer = ZwickColorSettings2,
    onSecondaryContainer = ZwickColorPrimaryDark,
    tertiary = ZwickColorYellow,
    onTertiary = Black,
    background = ZwickColorGradientStart,
    onBackground = ZwickColorBlackText,
    surface = ZwickColorGradientStart,
    onSurface = ZwickColorBlackText,
    surfaceVariant = ZwickColorGradientFinish,
    onSurfaceVariant = ZwickColorBlackText,
    error = ZwickColorError,
    onError = White,
    outline = ZwickColorDarkGray
)

// Dark color scheme
val DarkColorScheme = darkColorScheme(
    primary = ZwickColorPrimaryTransparent,
    onPrimary = ZwickColorPrimaryDark,
    primaryContainer = ZwickColorPrimaryDark,
    onPrimaryContainer = ZwickColorPrimaryTransparent,
    secondary = ZwickColorAccent,
    onSecondary = White,
    tertiary = ZwickColorYellow,
    background = Neutral0,
    onBackground = Neutral99,
    surface = Neutral0,
    onSurface = Neutral100,
    error = ZwickColorError,
    onError = White,
    outline = Neutral60
)

private val LightColorPalette =
    AppColors(
        brandPrimary = ZwickColorPrimaryDark,
        brandSecondary = ZwickColorPrimaryTransparent,
        brandThird = ZwickColorAccent,

        // Bubbles
        sendBubbleColor = ZwickColorSettings5,
        sendBubbleColoriMessage = ZwickColorAccent,
        receiveBubbleColor = White,

        // Backgrounds
        uiBackground = ZwickColorGradientStart,
        uiBackground2 = ZwickColorGradientFinish,
        uiBackground3 = White,
        shadowedBackground = ZwickColorGrayLight,
        shadowedBackground2 = ZwickColorSettings7,
        highlightedBackground = ZwickColorSettings1,

        // Swipe
        uiSwipeBackground1 = ZwickColorAccent,
        uiSwipeBackground2 = ZwickColorPrimaryTransparent,

        // Text
        textPrimary = ZwickColorBlackText,
        textSecondary = ZwickColorGray40Percent,
        textThird = ButtonTextColor,
        textInteractive = ZwickColorPrimaryDark,
        textColorGoldBubble = ZwickColorBlackText,

        dialogText = ZwickColorBlackText,
        introductionBackgroundColor = ZwickColorPrimaryDark,

        // Fields
        textFieldBackground = White,
        textFieldBorder = ZwickColorDarkGray,
        uiBorder = ZwickColorGrayLight,

        // Buttons / Controls
        backButton = ZwickColorPrimaryDark,
        buttonDisconnect = ZwickColorError,
        buttonDisabled = ZwickColorGrayLight,
        radioButtonSelected = ZwickColorPrimaryDark,
        radioButtonUnselected = ZwickColorDarkGray,

        // Dialogs
        dialogBackground = White,
        dialogBackground2 = ZwickColorGradientStart,
        divider = ZwickColorGrayLight,
        uiFloated = ZwickColorPrimaryDark,

        textHelp = ZwickColorDarkGray,
        textLink = ZwickColorAccent,
        iconSecondary = ZwickColorDarkGray,
        iconInteractive = ZwickColorPrimaryDark,
        iconInteractiveInactive = ZwickColorGray40Percent,
        errorDelete = ZwickColorError,

        // Gradients
        gradient6_1 = listOf(ZwickColorPrimaryDark, ZwickColorAccent, ZwickColorPrimaryTransparent),
        gradient3_1 = listOf(ZwickColorPrimaryDark, ZwickColorAccent, ZwickColorPrimaryTransparent),
        gradient2_1 = listOf(ZwickColorPrimaryDark, ZwickColorAccent),
        tornado1 = listOf(ZwickColorPrimaryDark, ZwickColorAccent),

        statusBar = ZwickColorGradientStart,
        unpinBackground = ZwickColorSettings6,
        swipeDelete = ZwickColorError,
        dialogDeleteChat = ZwickColorError,
        dividerDialog = ZwickColorGrayLight,
        isDark = false,
    )

private val DarkColorPalette =
    AppColors(
        brandPrimary = ZwickColorPrimaryDark,
        brandSecondary = ZwickColorPrimaryTransparent,
        brandThird = ZwickColorAccent,

        // Bubbles
        sendBubbleColor = ZwickColorSettings5,
        sendBubbleColoriMessage = ZwickColorAccent,
        receiveBubbleColor = White,

        // Backgrounds
        uiBackground = ZwickColorGradientStart,
        uiBackground2 = ZwickColorGradientFinish,
        uiBackground3 = White,
        shadowedBackground = ZwickColorGrayLight,
        shadowedBackground2 = ZwickColorSettings7,
        highlightedBackground = ZwickColorSettings1,

        // Swipe
        uiSwipeBackground1 = ZwickColorAccent,
        uiSwipeBackground2 = ZwickColorPrimaryTransparent,

        // Text
        textPrimary = ZwickColorBlackText,
        textSecondary = ZwickColorGray40Percent,
        textThird = ButtonTextColor,
        textInteractive = ZwickColorPrimaryDark,
        textColorGoldBubble = ZwickColorBlackText,

        dialogText = ZwickColorBlackText,
        introductionBackgroundColor = ZwickColorPrimaryDark,

        // Fields
        textFieldBackground = White,
        textFieldBorder = ZwickColorDarkGray,
        uiBorder = ZwickColorGrayLight,

        // Buttons / Controls
        backButton = ZwickColorPrimaryDark,
        buttonDisconnect = ZwickColorError,
        buttonDisabled = ZwickColorGrayLight,
        radioButtonSelected = ZwickColorPrimaryDark,
        radioButtonUnselected = ZwickColorDarkGray,

        // Dialogs
        dialogBackground = White,
        dialogBackground2 = ZwickColorGradientStart,
        divider = ZwickColorGrayLight,
        uiFloated = ZwickColorPrimaryDark,

        textHelp = ZwickColorDarkGray,
        textLink = ZwickColorAccent,
        iconSecondary = ZwickColorDarkGray,
        iconInteractive = ZwickColorPrimaryDark,
        iconInteractiveInactive = ZwickColorGray40Percent,
        errorDelete = ZwickColorError,

        // Gradients
        gradient6_1 = listOf(ZwickColorPrimaryDark, ZwickColorAccent, ZwickColorPrimaryTransparent),
        gradient3_1 = listOf(ZwickColorPrimaryDark, ZwickColorAccent, ZwickColorPrimaryTransparent),
        gradient2_1 = listOf(ZwickColorPrimaryDark, ZwickColorAccent),
        tornado1 = listOf(ZwickColorPrimaryDark, ZwickColorAccent),

        statusBar = ZwickColorGradientStart,
        unpinBackground = ZwickColorSettings6,
        swipeDelete = ZwickColorError,
        dialogDeleteChat = ZwickColorError,
        dividerDialog = ZwickColorGrayLight,
        isDark = true,
    )


object AppTheme {
    val colors: AppColors
        @Composable get() = LocalAppColors.current
}

@Stable
class AppColors(
    brandPrimary: Color,
    brandSecondary: Color,
    brandThird: Color,
    sendBubbleColor: Color,
    sendBubbleColoriMessage: Color,
    receiveBubbleColor: Color,
    uiBackground: Color,
    uiBackground2: Color,
    uiBackground3: Color,
    shadowedBackground: Color,
    shadowedBackground2: Color,
    highlightedBackground: Color,
    uiSwipeBackground1: Color,
    uiSwipeBackground2: Color,
    textPrimary: Color,
    textSecondary: Color,
    textThird: Color,
    textInteractive: Color,
    textColorGoldBubble: Color,
    dialogText: Color,
    introductionBackgroundColor: Color,
    textFieldBackground: Color,
    textFieldBorder: Color,
    dialogDeleteChat: Color,
    uiBorder: Color,
    backButton: Color,
    buttonDisconnect: Color,
    buttonDisabled: Color,
    radioButtonSelected: Color,
    radioButtonUnselected: Color,
    divider: Color,
    dividerDialog: Color,
    dialogBackground: Color,
    dialogBackground2: Color,
    unpinBackground: Color,
    swipeDelete: Color,
    gradient6_1: List<Color>,
    gradient3_1: List<Color>,
    gradient2_1: List<Color>,
    uiFloated: Color,
    interactivePrimary: List<Color> = gradient2_1,
    interactiveMask: List<Color> = gradient6_1,
    textHelp: Color,
    textLink: Color,
    tornado1: List<Color>,
    statusBar: Color,
    iconPrimary: Color = brandPrimary,
    iconSecondary: Color,
    iconInteractive: Color,
    iconInteractiveInactive: Color,
    errorDelete: Color,
    notificationBadge: Color = errorDelete,
    isDark: Boolean,
) {
    var gradient6_1 by mutableStateOf(gradient6_1)
        private set
    var gradient3_1 by mutableStateOf(gradient3_1)
        private set
    var gradient2_1 by mutableStateOf(gradient2_1)
        private set
    var brandPrimary by mutableStateOf(brandPrimary)
        private set
    var brandSecondary by mutableStateOf(brandSecondary)
        private set
    var brandThird by mutableStateOf(brandThird)
        private set
    var sendBubbleColor by mutableStateOf(sendBubbleColor)
        private set
    var sendBubbleColoriMessage by mutableStateOf(sendBubbleColoriMessage)
        private set
    var receiveBubbleColor by mutableStateOf(receiveBubbleColor)
        private set
    var uiBackground by mutableStateOf(uiBackground)
        private set
    var uiBackground2 by mutableStateOf(uiBackground2)
        private set
    var uiBackground3 by mutableStateOf(uiBackground3)
        private set
    var uiBorder by mutableStateOf(uiBorder)
        private set
    var backButton by mutableStateOf(backButton)
        private set
    var buttonDisabled by mutableStateOf(buttonDisabled)
        private set
    var buttonDisconnect by mutableStateOf(buttonDisconnect)
        private set
    var radioButtonSelected by mutableStateOf(radioButtonSelected)
        private set
    var radioButtonUnselected by mutableStateOf(radioButtonUnselected)
        private set
    var dialogBackground by mutableStateOf(dialogBackground)
        private set
    var dialogBackground2 by mutableStateOf(dialogBackground2)
        private set
    var uiSwipeBackground1 by mutableStateOf(uiSwipeBackground1)
        private set
    var uiSwipeBackground2 by mutableStateOf(uiSwipeBackground2)
        private set
    var uiFloated by mutableStateOf(uiFloated)
        private set
    var interactivePrimary by mutableStateOf(interactivePrimary)
        private set
    var interactiveMask by mutableStateOf(interactiveMask)
        private set
    var textPrimary by mutableStateOf(textPrimary)
        private set
    var textSecondary by mutableStateOf(textSecondary)
        private set
    var textThird by mutableStateOf(textThird)
        private set
    var textColorGoldBubble by mutableStateOf(textColorGoldBubble)
        private set
    var textHelp by mutableStateOf(textHelp)
        private set
    var textInteractive by mutableStateOf(textInteractive)
        private set
    var tornado1 by mutableStateOf(tornado1)
        private set
    var textLink by mutableStateOf(textLink)
        private set
    var statusBar by mutableStateOf(statusBar)
        private set
    var iconPrimary by mutableStateOf(iconPrimary)
        private set
    var iconSecondary by mutableStateOf(iconSecondary)
        private set
    var iconInteractive by mutableStateOf(iconInteractive)
        private set
    var iconInteractiveInactive by mutableStateOf(iconInteractiveInactive)
        private set
    var errorDelete by mutableStateOf(errorDelete)
        private set
    var notificationBadge by mutableStateOf(notificationBadge)
        private set
    var isDark by mutableStateOf(isDark)
        private set
    var highlightedBackground by mutableStateOf(highlightedBackground)
        private set
    var shadowedBackground by mutableStateOf(shadowedBackground)
        private set
    var shadowedBackground2 by mutableStateOf(shadowedBackground2)
        private set
    var textFieldBackground by mutableStateOf(textFieldBackground)
        private set
    var textFieldBorder by mutableStateOf(textFieldBorder)
        private set
    var divider by mutableStateOf(divider)
        private set
    var unpinBackground by mutableStateOf(unpinBackground)
        private set
    var swipeDelete by mutableStateOf(swipeDelete)
        private set
    var dialogDeleteChat by mutableStateOf(dialogDeleteChat)
        private set
    var dialogText by mutableStateOf(dialogText)
        private set
    var introductionBackgroundColor by mutableStateOf(introductionBackgroundColor)
        private set
    var dividerDialog by mutableStateOf(dividerDialog)
        private set

    fun update(other: AppColors) {
        gradient6_1 = other.gradient6_1
        gradient3_1 = other.gradient3_1
        gradient2_1 = other.gradient2_1
        brandPrimary = other.brandPrimary
        brandSecondary = other.brandSecondary
        brandThird = other.brandThird
        sendBubbleColor= other.sendBubbleColor
        sendBubbleColoriMessage = other.sendBubbleColoriMessage
        receiveBubbleColor = other.receiveBubbleColor
        uiBackground = other.uiBackground
        uiBackground2 = other.uiBackground2
        uiBackground3 = other.uiBackground3
        buttonDisabled = other.buttonDisabled
        buttonDisconnect = other.buttonDisconnect
        radioButtonSelected = other.radioButtonSelected
        radioButtonUnselected = other.radioButtonUnselected
        dialogBackground = other.dialogBackground
        dialogBackground2 = other.dialogBackground2
        uiBorder = other.uiBorder
        uiSwipeBackground1 = other.uiSwipeBackground1
        uiSwipeBackground2 = other.uiSwipeBackground2
        uiFloated = other.uiFloated
        interactivePrimary = other.interactivePrimary
        interactiveMask = other.interactiveMask
        textPrimary = other.textPrimary
        textSecondary = other.textSecondary
        textThird = other.textThird
        textColorGoldBubble = other.textColorGoldBubble
        textHelp = other.textHelp
        textInteractive = other.textInteractive
        textLink = other.textLink
        tornado1 = other.tornado1
        iconPrimary = other.iconPrimary
        iconSecondary = other.iconSecondary
        iconInteractive = other.iconInteractive
        iconInteractiveInactive = other.iconInteractiveInactive
        errorDelete = other.errorDelete
        notificationBadge = other.notificationBadge
        isDark = other.isDark
        highlightedBackground = other.highlightedBackground
        shadowedBackground = other.shadowedBackground
        shadowedBackground2 = other.shadowedBackground2
        unpinBackground = other.unpinBackground
        textFieldBackground = other.textFieldBackground
        textFieldBorder = other.textFieldBorder
        swipeDelete = other.swipeDelete
        dialogDeleteChat = other.dialogDeleteChat
        dialogText = other.dialogText
        introductionBackgroundColor = other.introductionBackgroundColor
        dividerDialog = other.dividerDialog
    }

    fun copy(): AppColors =
        AppColors(
            gradient6_1 = gradient6_1,
            gradient3_1 = gradient3_1,
            gradient2_1 = gradient2_1,
            brandPrimary = brandPrimary,
            brandSecondary = brandSecondary,
            brandThird = brandThird,
            sendBubbleColor = sendBubbleColor,
            sendBubbleColoriMessage = sendBubbleColoriMessage,
            receiveBubbleColor = receiveBubbleColor,
            uiBackground = uiBackground,
            uiBackground2 = uiBackground2,
            uiBackground3 = uiBackground3,
            uiBorder = uiBorder,
            backButton = backButton,
            buttonDisconnect = buttonDisconnect,
            buttonDisabled = buttonDisabled,
            radioButtonSelected = radioButtonSelected,
            radioButtonUnselected = radioButtonUnselected,
            dialogBackground = dialogBackground,
            dialogBackground2 = dialogBackground2,
            uiSwipeBackground1 = uiSwipeBackground1,
            uiSwipeBackground2 = uiSwipeBackground2,
            uiFloated = uiFloated,
            interactivePrimary = interactivePrimary,
            interactiveMask = interactiveMask,
            textPrimary = textPrimary,
            textSecondary = textSecondary,
            textThird = textThird,
            textColorGoldBubble = textColorGoldBubble,
            textHelp = textHelp,
            textInteractive = textInteractive,
            textLink = textLink,
            tornado1 = tornado1,
            iconPrimary = iconPrimary,
            iconSecondary = iconSecondary,
            iconInteractive = iconInteractive,
            iconInteractiveInactive = iconInteractiveInactive,
            errorDelete = errorDelete,
            notificationBadge = notificationBadge,
            statusBar = statusBar,
            isDark = isDark,
            highlightedBackground = highlightedBackground,
            shadowedBackground = shadowedBackground,
            shadowedBackground2 = shadowedBackground2,
            textFieldBackground = textFieldBackground,
            textFieldBorder = textFieldBorder,
            divider = divider,
            unpinBackground = unpinBackground,
            swipeDelete = swipeDelete,
            dialogDeleteChat = dialogDeleteChat,
            dialogText = dialogText,
            introductionBackgroundColor = introductionBackgroundColor,
            dividerDialog = dividerDialog
        )
}

@Composable
fun ProvideAppColors(colors: AppColors, content: @Composable () -> Unit) {
    val colorPalette = remember {
        // Explicitly creating a new object here so we don't mutate the initial [colors]
        // provided, and overwrite the values set in it.
        colors.copy()
    }
    colorPalette.update(colors)
    CompositionLocalProvider(LocalAppColors provides colorPalette, content = content)
}

private val LocalAppColors =
    staticCompositionLocalOf<AppColors> { error("No LocalColorsPalette provided") }


val IsAppInDarkTheme = compositionLocalOf<Boolean> { error("No IsAppInDarkTheme provided") }


@Composable
fun AppTheme(
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    isDynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {

    val colors =
        if (isDarkTheme) {
            LightColorPalette
        } else {
            LightColorPalette
        }

    val dynamicColor = isDynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val colorScheme = when {
        dynamicColor && isDarkTheme -> {
            dynamicDarkColorScheme(LocalContext.current)
        }
        dynamicColor && !isDarkTheme -> {
            dynamicLightColorScheme(LocalContext.current)
        }
        isDarkTheme -> LightColorScheme
        else -> LightColorScheme
    }

    val sysUiController = rememberSystemUiController()

    SideEffect {
        sysUiController.setSystemBarsColor(color = colorScheme.background)
    }

//    MaterialTheme(
//        colorScheme = colorScheme,
//        typography = Typography,
//        content = content
//    )

    ProvideAppColors(colors) {
        Material3(
            colorScheme = colorScheme,
            typography = AppTypography,
            shapes = AppShapes,
            content = content
        )
    }

}
