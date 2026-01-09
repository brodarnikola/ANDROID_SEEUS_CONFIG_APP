package hr.sil.android.seeusadmin.compose_ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import hr.sil.android.seeusadmin.R

// --- Placeholder Colors/Styles (You need to define these based on your theme attributes) ---
val ColorPrimaryDark = Color(0xFF4c4372) // Assuming this is the color for @color/colorPrimaryDark
val ThmMainButtonBackgroundColor = ColorPrimaryDark // ?attr/thmMainButtonBackgroundColor now points to the shape
val ThmLoginButtonTextColor = Color(0xFFFFFFFF) // ?attr/thmLoginButtonTextColor
val ThmButtonTextSize = 18.sp // ?attr/thmButtonTextSize
val ThmButtonLetterSpacing = 0.05.sp // ?attr/thmButtonLetterSpacing
val ThmMainFontTypeMedium = FontWeight.Medium

val ThmButtonTextSizeInsideDialog = 16.sp

// <color name="colorPrimary40Percent">#66D3C18C</color>
val ThmCPTelemetryBackgroundColor = Color(0xFFD3C18C)
val ThmShareKeyAdapterTextColor = Color(0xFFFFFFFF)
// 664a4a4a
val ThmCPShareKeyOddBC = Color(0xFF4A4A4A)
val ThmCPShareKeyEvenBC = Color(0xFF4A4A4A)


// -----------------------------------------------------------------------------------------

@Composable
fun ButtonWithFont(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    // XML attributes mapped to Compose parameters
    backgroundColor: Color,      // android:background="?attr/thmMainButtonBackgroundColor" (shape solid color)
    textColor: Color,            // android:textColor="?attr/thmLoginButtonTextColor"
    fontSize: TextUnit,          // android:textSize="?attr/thmButtonTextSize"
    fontWeight: FontWeight,      // app:font_type="?attr/thmMainFontTypeMedium"
    letterSpacing: TextUnit,     // android:letterSpacing="?attr/thmButtonLetterSpacing"
    width: Dp = 250.dp,          // android:layout_width="250dp"
    height: Dp = 40.dp,          // android:layout_height="40dp"
    enabled: Boolean
) {
    // 1. Define the Button Style based on the XML Shape
    val cornerRadius = 5.dp // <corners android:radius="5dp"/>
    val shape = RoundedCornerShape(cornerRadius)

    // <stroke android:width="3dp" android:color="@color/colorPrimaryDark"/>
    val buttonBorder = BorderStroke(width = 3.dp, color = ColorPrimaryDark)

    // <solid android:color="@color/colorPrimaryDark"/> -> containerColor
    val colors = ButtonDefaults.buttonColors(
        containerColor = backgroundColor,
        contentColor = textColor, // Use text color for the content
        disabledContainerColor = textColor.copy(alpha = 0.5f)
    )

    Button(
        onClick = onClick,
        // Apply the passed-in constraints and sizing
        modifier = modifier
            .width(width)
            .height(height),

        // Apply styling from the XML Shape Drawable
        shape = shape,
        border = buttonBorder,
        colors = colors,
        contentPadding = PaddingValues(0.dp), // Minimal padding to match exact height
        enabled = enabled
    ) {
        // Text element with passed-in font/text attributes
        Text(
            text = text,
            fontSize = fontSize,
            letterSpacing = letterSpacing,
            fontWeight = fontWeight
            // Text color is inherited from the Button's contentColor
        )
    }
}