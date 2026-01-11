package hr.sil.android.seeusadmin.compose_ui.dialogs

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.constraintlayout.compose.ChainStyle
import androidx.constraintlayout.compose.ConstraintLayout
import kotlin.text.uppercase

import hr.sil.android.seeusadmin.R
import hr.sil.android.seeusadmin.compose_ui.components.ButtonWithFont
import hr.sil.android.seeusadmin.compose_ui.components.RoundedDialog
import hr.sil.android.seeusadmin.compose_ui.components.TextViewWithFont
import hr.sil.android.seeusadmin.compose_ui.components.ThmButtonDialogLetterSpacing
import hr.sil.android.seeusadmin.compose_ui.components.ThmButtonDialogTextSize
import hr.sil.android.seeusadmin.compose_ui.components.ThmButtonTextSize
import hr.sil.android.seeusadmin.compose_ui.components.ThmLoginButtonTextColor

@Composable
fun DeleteButtonDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        RoundedDialog {
            ConstraintLayout(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp)
            ) {
                val (titleText, cancelButton, confirmButton, spacer) = createRefs()

                // Title Text - "Logout Question"
                TextViewWithFont(
                    text = stringResource(R.string.key_sharing_delete_key_message),
                    modifier = Modifier
                        .fillMaxWidth()
                        .constrainAs(titleText) {
                            top.linkTo(parent.top, margin = 10.dp)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                        },
                    textAlign = TextAlign.Center,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Normal
                )

                // Cancel Button
                ButtonWithFont(
                    onClick = onCancel,
                    modifier = Modifier
                        .wrapContentWidth()
                        .padding(end = 10.dp)
                        .constrainAs(cancelButton) {
                            top.linkTo(titleText.bottom, margin = 20.dp)
                            start.linkTo(parent.start)
                            end.linkTo(confirmButton.start)
                        },
                    text = stringResource(id = R.string.app_generic_cancel).uppercase(),
                    backgroundColor = colorResource(R.color.colorPrimary),
                    textColor = ThmLoginButtonTextColor, // ?attr/thmLoginButtonTextColor
                    fontSize = ThmButtonDialogTextSize,
                    fontWeight = FontWeight.Medium, // ?attr/thmMainFontTypeMedium
                    letterSpacing = ThmButtonDialogLetterSpacing, // ?attr/thmButtonLetterSpacing (Placeholder)
                    enabled = true,
                    width = 120.dp
                )

                // Confirm Button
                ButtonWithFont(
                    onClick = onConfirm,
                    modifier = Modifier
                        .wrapContentWidth()
                        .padding(start = 10.dp)
                        .constrainAs(confirmButton) {
                            top.linkTo(titleText.bottom, margin = 20.dp)
                            start.linkTo(cancelButton.end)
                            end.linkTo(parent.end)
                        },
                    text = stringResource(id = R.string.app_generic_confirm).uppercase(),
                    backgroundColor = colorResource(R.color.colorPrimary),
                    textColor = ThmLoginButtonTextColor, // ?attr/thmLoginButtonTextColor
                    fontSize = ThmButtonDialogTextSize,
                    fontWeight = FontWeight.Medium, // ?attr/thmMainFontTypeMedium
                    letterSpacing = ThmButtonDialogLetterSpacing, // ?attr/thmButtonLetterSpacing (Placeholder)
                    enabled = true,
                    width = 120.dp
                )

                // Bottom Spacer
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(20.dp)
                        .constrainAs(spacer) {
                            top.linkTo(confirmButton.bottom)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                        }
                )

                // Create horizontal chain for buttons
                createHorizontalChain(
                    cancelButton,
                    confirmButton,
                    chainStyle = ChainStyle.Spread
                )
            }
        }
    }
}