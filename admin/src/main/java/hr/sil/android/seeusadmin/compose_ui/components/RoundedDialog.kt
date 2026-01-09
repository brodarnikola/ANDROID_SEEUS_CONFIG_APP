package hr.sil.android.seeusadmin.compose_ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp

import hr.sil.android.seeusadmin.R

@Composable
fun RoundedDialog(
    modifier: Modifier = Modifier,
    cornerRadius: Double = 20.0,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    content: @Composable () -> Unit
) {


    Box(
        modifier = modifier
            .background(
                color = colorResource(R.color.colorWhite),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(cornerRadius.dp)
            )
            .padding(contentPadding)
    ) {
        content()
    }
}