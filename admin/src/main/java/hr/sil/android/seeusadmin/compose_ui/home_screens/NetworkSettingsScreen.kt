package hr.sil.android.seeusadmin.compose_ui.home_screens


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hr.sil.android.mplhuber.core.remote.model.RNetworkConfiguration
import kotlin.collections.forEach
import kotlin.text.uppercase

import hr.sil.android.seeusadmin.R
import hr.sil.android.seeusadmin.compose_ui.components.ButtonWithFont
import hr.sil.android.seeusadmin.compose_ui.components.ThmButtonLetterSpacing
import hr.sil.android.seeusadmin.compose_ui.components.ThmButtonTextSize
import hr.sil.android.seeusadmin.compose_ui.components.ThmLoginButtonTextColor


import androidx.compose.material.TextFieldDefaults
import androidx.compose.material3.Text
import hr.sil.android.seeusadmin.compose_ui.components.ProgressIndicatorSize
import hr.sil.android.seeusadmin.compose_ui.components.RotatingRingIndicator

@Composable
fun NetworkSettingsScreen(
    viewModel: NetworkSettingsViewModel,
    macAddress: String
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current


    LaunchedEffect(key1 = Unit) {
        viewModel.loadNetworkConfigurations(macAddress)
    }

//    if (uiState.isLoading) {
//        DisableUserActionsDialog()
//    }

    Box(modifier = Modifier.fillMaxSize()) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 30.dp)
                .padding(horizontal = 30.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            //verticalArrangement = Arrangement.SpaceBetween
        ) {

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = stringResource(R.string.main_locker_apn_list),
                fontSize = 17.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                color = colorResource(R.color.colorWhite) //MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(20.dp))

            NetworkConfigurationDropdown(
                items = uiState.networkConfigurations,
                selected = uiState.selectedConfiguration,
                onSelected = viewModel::onNetworkSelected
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(end = 20.dp)
                ) {
                    RadioButton(
                        selected = uiState.isOfflineMode,
                        onClick = { viewModel.onOfflineSelected() },
                        enabled = !uiState.isPsmEnabled
                    )
                    Text(
                        text = stringResource(R.string.offline_mode),
                        fontSize = 13.sp,
                        color = colorResource(R.color.colorWhite) //MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = uiState.isSleepMode,
                        onClick = { viewModel.onSleepSelected() },
                        enabled = !uiState.isPsmEnabled
                    )
                    Text(
                        text = stringResource(R.string.sleep_mode),
                        fontSize = 13.sp,
                        color = colorResource(R.color.colorWhite) //MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = uiState.isPsmEnabled,
                    onCheckedChange = { viewModel.onPsmChanged(it) }
                )

                Text(
                    text = "PSM",
                    fontSize = 13.sp,
                    color = colorResource(R.color.colorWhite) //MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = stringResource(R.string.fur_testing_purpose),
                fontSize = 15.sp,
                color = colorResource(R.color.colorPrimary),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.weight(1f))

            val success = stringResource(R.string.successfull_saved_network_configuration)
            val error = stringResource(R.string.registration_error)
            val connectionError = stringResource(R.string.main_locker_ble_connection_error)

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                if (uiState.isLoading) {
                    RotatingRingIndicator(
                        modifier = Modifier
                            .size(ProgressIndicatorSize) // 40.dp
                            .padding(top = 10.dp)
                    )
                } else {
                    ButtonWithFont(
                        text = stringResource(id = R.string.locker_settings_save_changes).uppercase(),
                        onClick = {
                            viewModel.onSaveClicked(
                                context,
                                success,
                                error,
                                connectionError,
                                uiState.isOfflineMode,
                                uiState.isPsmEnabled
                            )
                        },
                        backgroundColor = colorResource(R.color.colorPrimary),
                        //backgroundColor = ThmMainButtonBackgroundColor,
                        textColor = ThmLoginButtonTextColor,
                        fontSize = ThmButtonTextSize,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = ThmButtonLetterSpacing, // ?attr/thmButtonLetterSpacing (Placeholder)
                        modifier = Modifier
                            .width(210.dp)
                            .height(50.dp)
                            .padding(bottom = 2.dp),
                        enabled = true
                    )
                }
            }
        }
    }
}

@Composable
fun NetworkConfigurationDropdown(
    items: List<RNetworkConfiguration>,
    selected: RNetworkConfiguration?,
    onSelected: (RNetworkConfiguration) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = selected?.name ?: "",
        textStyle = LocalTextStyle.current.copy(color = Color.White),
        placeholder = {
            Text(
                text = selected?.name ?: "", //stringResource(R.string.main_locker_apn_list),
                color = Color.White.copy(alpha = 0.6f)
            )
        },
        onValueChange = {},
        readOnly = true,
        trailingIcon = {
            Icon(
                Icons.Default.ArrowDropDown, null,
                Modifier.clickable { expanded = true })
        },
        colors = TextFieldDefaults.outlinedTextFieldColors(
            textColor = Color.White,
            placeholderColor = Color.White.copy(alpha = 0.6f),
            focusedBorderColor = Color.White,
            unfocusedBorderColor = Color.White,
            cursorColor = Color.White
        )
    )

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false }
    ) {
        items.forEach {
            DropdownMenuItem(
                text = { Text(text = it.name, color = colorResource(R.color.colorBlack)) },
                onClick = {
                    expanded = false
                    onSelected(it)
                }
            )
        }
    }
}