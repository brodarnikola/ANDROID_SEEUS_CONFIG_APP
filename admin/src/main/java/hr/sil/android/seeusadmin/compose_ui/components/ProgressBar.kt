package hr.sil.android.seeusadmin.compose_ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp


// --- Placeholder Colors/Constants (You need to define these) ---
val ColorBlackText = Color.Black // Assuming @color/colorBlackText is black
val ProgressIndicatorSize = 50.dp // Matches your CircularProgressIndicator size
// Ring thickness and size are calculated from the XML ratios:
// ThicknessRatio="8" on a 76dip size -> 76/8 = 9.5dip
// InnerRadiusRatio="3" on a 76dip size -> Inner radius = 76 * (1/2 - 1/8) = 28.5dip
// We'll calculate the stroke width based on the final 40dp size.
// For a 40dp size, a thickness of 5dp looks appropriate (40/8 = 5dp).
val StrokeThickness = 3.dp

@Composable
fun RotatingRingIndicator(modifier: Modifier = Modifier) {
    // 1. Setup Infinite Rotation Animation
    val infiniteTransition = rememberInfiniteTransition(label = "RotationTransition")

    // Equivalent to: android:fromDegrees="0" android:toDegrees="1080" android:duration="1"
    // We choose a duration that looks smooth (e.g., 1000ms for 360 degrees)
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1000, // Speed of rotation
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    // 2. Draw the Custom Ring Shape with Sweep Gradient
    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasSize = size.minDimension
            val center = Offset(x = size.width / 2, y = size.height / 2)

            // Calculate the radius for the ring stroke
            val radius = (canvasSize - StrokeThickness.toPx()) / 2f

            // The gradient is drawn starting from the top (-90 degrees)
            // Sweep gradient equivalent to XML gradient tag
            val brush = Brush.sweepGradient(
                // android:startColor="@color/colorBlackText" android:endColor="#00ffffff"
                colorStops = arrayOf(
                    0.0f to ColorBlackText,
                    0.7f to ColorBlackText, // Keep black for a large portion
                    1.0f to Color.Transparent // Fade to transparent
                ),
                center = center
            )

            // Apply the infinite rotation (pivotX="50%", pivotY="50%" is the default for Canvas rotate)
            rotate(rotation) {
                // Draw the ring (shape="ring" with thickness)
                drawArc(
                    brush = brush,
                    startAngle = 270f, // Start at the top (-90 degrees)
                    sweepAngle = 360f, // Draw a full circle
                    useCenter = false,
                    topLeft = Offset(
                        (size.width - canvasSize) / 2f + StrokeThickness.toPx() / 2,
                        (size.height - canvasSize) / 2f + StrokeThickness.toPx() / 2
                    ),
                    style = Stroke(
                        width = StrokeThickness.toPx(),
                        cap = StrokeCap.Butt // Use Butt cap to avoid rounding the ends
                    ),
                    size = Size(canvasSize - StrokeThickness.toPx(), canvasSize - StrokeThickness.toPx())
                )
            }
        }
    }
}