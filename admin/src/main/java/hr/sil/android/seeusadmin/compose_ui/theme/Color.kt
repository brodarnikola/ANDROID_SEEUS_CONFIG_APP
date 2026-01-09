package hr.sil.android.seeusadmin.compose_ui.theme

import androidx.compose.ui.graphics.Color

// ==========================================
// SEEUS COLORS (Ported from res/values/colors.xml)
// ==========================================

// Primary Colors
val ColorPrimary = Color(0xFFF79101)              // #F79101 - Orange primary
val ColorPrimaryTransparent = Color(0x88F79101)   // #88F79101 - 53% transparent orange
val ColorCyanTransparent = Color(0x8800FFFF)      // #8800FFFF - 53% transparent cyan
val ColorPrimaryDisabled = Color(0xFFC8C4C4)      // #c8c4c4 - Disabled gray
val ColorDarkAccent = Color(0xFFC79C00)           // #c79c00 - Dark yellow accent
val AppPrimaryPressed = Color(0xFFC79C00)         // #c79c00 - Pressed state

// Basic Colors
val ColorBlack = Color(0xFF000000)                // #000000
val ColorWhite = Color(0xFFFFFFFF)                // #FFFFFF
val ColorWhite30PercentTransparency = Color(0x4CFFFFFF)  // #4cffffff
val ColorBlue30PercentTransparency = Color(0x8800FFFF)   // #8800FFFF
val ColorTransparent = Color(0x00000000)          // #00000000

// Error Colors
val ColorError = Color(0xFFFF0000)                // #FF0000 - Red error
val ColorErrorTransparent = Color(0x88FF0000)     // #88FF0000 - 53% transparent red
val ColorCharcoalGray = Color(0xFFFF0000)         // #FF0000 (Note: same as error in XML)

// Gray Colors
val ColorGray = Color(0xFF666666)                 // #666666
val ColorGrayLight = Color(0x33333234)            // #33333234 - Light gray with transparency
val ColorLightGray = Color(0xFF979797)            // #979797
val ColorStrongerGray = Color(0xFF616161)         // #616161
val ColorPinkishGray = Color(0xFFC8C4C4)          // #c8c4c4
val ColorSmallIntensityGray = Color(0xFFD8D8D8)   // #d8d8d8

// Utility Colors
val AppRipple = Color(0x99FFFFFF)                 // #99FFFFFF
val HelpItemTransparent = Color(0x994A4A4A)       // #994a4a4a

// ==========================================
// DERIVED/SEMANTIC COLORS FOR COMPOSE USAGE
// ==========================================

// Backgrounds
val SeeUsBackground = ColorGrayLight
val SeeUsBackgroundLight = ColorWhite
val SeeUsSurface = ColorWhite

// Text Colors
val SeeUsTextPrimary = ColorBlack
val SeeUsTextSecondary = ColorGray
val SeeUsTextOnPrimary = ColorWhite
val SeeUsTextError = ColorError

// Button Colors
val SeeUsButtonPrimary = ColorPrimary
val SeeUsButtonPressed = AppPrimaryPressed
val SeeUsButtonDisabled = ColorPrimaryDisabled

// Dividers and Borders
val SeeUsDivider = ColorSmallIntensityGray
val SeeUsBorder = ColorLightGray

// Status Bar
val SeeUsStatusBar = ColorGrayLight
