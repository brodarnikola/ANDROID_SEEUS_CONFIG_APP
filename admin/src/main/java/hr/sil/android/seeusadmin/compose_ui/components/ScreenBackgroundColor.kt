package hr.sil.android.seeusadmin.compose_ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import hr.sil.android.seeusadmin.R

@Composable
fun GradientBackground(
    modifier: Modifier = Modifier,
    startColor: Color = colorResource(R.color.colorGradientStart),
    endColor: Color = colorResource(R.color.colorGradientFinish),
    angle: Float = 270f,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(startColor, endColor),
                    start = when (angle) {
                        0f -> Offset(0f, 0.5f)
                        90f -> Offset(0f, Float.POSITIVE_INFINITY)
                        180f -> Offset(Float.POSITIVE_INFINITY, 0.5f)
                        270f -> Offset(0.5f, 0f)
                        else -> Offset(0.5f, 0f)
                    },
                    end = when (angle) {
                        0f -> Offset(Float.POSITIVE_INFINITY, 0.5f)
                        90f -> Offset(0f, 0f)
                        180f -> Offset(0f, 0.5f)
                        270f -> Offset(0.5f, Float.POSITIVE_INFINITY)
                        else -> Offset(0.5f, Float.POSITIVE_INFINITY)
                    }
                )
            )
    ) {
        content()
    }
}