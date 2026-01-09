package hr.sil.android.seeusadmin.compose_ui.home_screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import hr.sil.android.mplhuber.core.remote.model.RNetworkConfiguration
import hr.sil.android.seeusadmin.App
import hr.sil.android.seeusadmin.R
import hr.sil.android.seeusadmin.compose_ui.theme.ColorError
import hr.sil.android.seeusadmin.compose_ui.theme.ColorPrimary
import hr.sil.android.seeusadmin.compose_ui.theme.ColorWhite
import hr.sil.android.seeusadmin.events.DevicesUpdatedEvent
import hr.sil.android.seeusadmin.store.model.Device
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

@Composable
fun StationItemDetailsScreen(
    viewModel: StationItemDetailsViewModel,
    macAddress: String,
    latitude: Double = 0.0,
    longitude: Double = 0.0,
    onNavigateBack: () -> Unit = {},
    onNavigateToStationSettings: (String) -> Unit = {},
    onNavigateToManageButtons: (String) -> Unit = {},
    onNavigateToNetworkSettings: (String) -> Unit = {},
    onNavigateToGoogleMaps: (String) -> Unit = {},
    //onShowDeleteDialog: (macAddress: String, device: Device?) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    LaunchedEffect(macAddress) {
        viewModel.initialize(macAddress, latitude, longitude)
    }

    DisposableEffect(lifecycleOwner) {
        val eventBusSubscriber = object {
            @Subscribe(threadMode = ThreadMode.MAIN)
            fun onDevicesUpdated(event: DevicesUpdatedEvent) {
                viewModel.onEvent(StationItemDetailsEvent.OnRefreshDevice)
            }
        }

        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    App.ref.eventBus.register(eventBusSubscriber)
                }
                Lifecycle.Event.ON_PAUSE -> {
                    App.ref.eventBus.unregister(eventBusSubscriber)
                }
                else -> {}
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            try {
                App.ref.eventBus.unregister(eventBusSubscriber)
            } catch (e: Exception) {
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.uiEvents.collect { event ->
            when (event) {
                is StationItemDetailsUiEvent.NavigateBack -> onNavigateBack()
                is StationItemDetailsUiEvent.NavigateToStationSettings -> onNavigateToStationSettings(event.masterMac)
                is StationItemDetailsUiEvent.NavigateToManageButtons -> onNavigateToManageButtons(event.masterMac)
                is StationItemDetailsUiEvent.NavigateToNetworkSettings -> onNavigateToNetworkSettings(event.masterMac)
                is StationItemDetailsUiEvent.NavigateToGoogleMaps -> onNavigateToGoogleMaps(event.macAddress)
                is StationItemDetailsUiEvent.ShowDeleteDialog -> {} // onShowDeleteDialog(event.macAddress, event.device)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(
            text = stringResource(R.string.app_generic_station_unit),
            color = ColorWhite,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 2.dp)
        )

        Spacer(modifier = Modifier.height(5.dp))

        DeviceInfoSection(state = state)

        Divider(
            color = ColorWhite,
            thickness = 2.dp,
            modifier = Modifier.padding(vertical = 7.dp)
        )

        if (state.isUnregistered) {
            UnregisteredSection(
                state = state,
                onLatitudeChanged = { viewModel.onEvent(StationItemDetailsEvent.OnLatitudeChanged(it)) },
                onLongitudeChanged = { viewModel.onEvent(StationItemDetailsEvent.OnLongitudeChanged(it)) },
                onNetworkSelected = { viewModel.onEvent(StationItemDetailsEvent.OnNetworkSelected(it)) },
                onRegisterClicked = { viewModel.onEvent(StationItemDetailsEvent.OnRegisterClicked, context) },
                onMapClicked = { viewModel.onEvent(StationItemDetailsEvent.OnMapClicked) }
            )
        } else {
            RegisteredSection(
                state = state,
                onStationSettingsClicked = { viewModel.onEvent(StationItemDetailsEvent.OnStationSettingsClicked) },
                onManageButtonsClicked = { viewModel.onEvent(StationItemDetailsEvent.OnManageButtonsClicked) },
                onRebootClicked = { viewModel.onEvent(StationItemDetailsEvent.OnRebootClicked, context) },
                onNetworkSettingsClicked = { viewModel.onEvent(StationItemDetailsEvent.OnNetworkSettingsClicked) },
                onLedTestClicked = { viewModel.onEvent(StationItemDetailsEvent.OnLedTestClicked, context) },
                onDebugClicked = { viewModel.onEvent(StationItemDetailsEvent.OnDebugClicked, context) },
                onResetClicked = { viewModel.onEvent(StationItemDetailsEvent.OnResetClicked, context) }
            )
        }
    }
}

@Composable
private fun DeviceInfoSection(state: StationItemDetailsState) {
    Column {
        InfoRow(label = "MAC address:", value = state.macAddressDisplay)
        InfoRow(label = stringResource(R.string.name), value = ": ${state.deviceName}")
        InfoRow(label = stringResource(R.string.main_locker_stm_ver), value = " ${state.stmVersion}")
        InfoRow(label = stringResource(R.string.main_gsm_rssi), value = " ${state.rssiValue}")
        InfoRow(label = stringResource(R.string.main_gsm_status), value = " ${state.modemStatus}")
        InfoRow(label = stringResource(R.string.main_gsm_modem_queue), value = " ${state.queueSize}")
        InfoRow(label = "RAT:", value = " ${state.ratValue}")
        InfoRow(label = stringResource(R.string.main_locker_battery), value = " ${state.batteryValue}")
        InfoRow(label = stringResource(R.string.main_number_of_buttons), value = " ${state.numOfButtons}")
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp)
    ) {
        Text(
            text = label,
            color = ColorWhite,
            fontSize = 13.sp
        )
        Text(
            text = value,
            color = ColorWhite,
            fontSize = 13.sp
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UnregisteredSection(
    state: StationItemDetailsState,
    onLatitudeChanged: (String) -> Unit,
    onLongitudeChanged: (String) -> Unit,
    onNetworkSelected: (RNetworkConfiguration) -> Unit,
    onRegisterClicked: () -> Unit,
    onMapClicked: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(0.75f)) {
                Text(
                    text = stringResource(R.string.app_generic_latitude).uppercase(),
                    color = ColorWhite,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 10.dp)
                )

                OutlinedTextField(
                    value = state.latitude,
                    onValueChange = onLatitudeChanged,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = ColorWhite,
                        unfocusedTextColor = ColorWhite,
                        cursorColor = ColorPrimary,
                        focusedBorderColor = ColorWhite,
                        unfocusedBorderColor = ColorWhite.copy(alpha = 0.5f)
                    ),
                    isError = state.latitudeError != null
                )

                state.latitudeError?.let {
                    Text(
                        text = it,
                        color = ColorError,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = stringResource(R.string.app_generic_longitude).uppercase(),
                    color = ColorWhite,
                    fontSize = 13.sp
                )

                OutlinedTextField(
                    value = state.longitude,
                    onValueChange = onLongitudeChanged,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = ColorWhite,
                        unfocusedTextColor = ColorWhite,
                        cursorColor = ColorPrimary,
                        focusedBorderColor = ColorWhite,
                        unfocusedBorderColor = ColorWhite.copy(alpha = 0.5f)
                    ),
                    isError = state.longitudeError != null
                )

                state.longitudeError?.let {
                    Text(
                        text = it,
                        color = ColorError,
                        fontSize = 13.sp
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(0.25f)
                    .padding(start = 5.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.btn_map),
                    contentDescription = "Open Map",
                    modifier = Modifier
                        .size(64.dp)
                        .clickable { onMapClicked() }
                )
            }
        }

        Spacer(modifier = Modifier.height(15.dp))

        Text(
            text = stringResource(R.string.main_locker_apn_list),
            color = ColorWhite,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(3.dp))

        NetworkConfigDropdown(
            configurations = state.networkConfigurations,
            selectedConfig = state.selectedNetworkConfig,
            onConfigSelected = onNetworkSelected
        )

        Spacer(modifier = Modifier.height(10.dp))

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            when {
                state.isRegistrationLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(40.dp),
                        color = ColorPrimary
                    )
                }
                state.isRegistrationInProgress -> {
                    Text(
                        text = stringResource(R.string.register_in_progress),
                        color = ColorWhite,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
                else -> {
                    Button(
                        onClick = onRegisterClicked,
                        modifier = Modifier
                            .width(210.dp)
                            .height(40.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ColorPrimary)
                    ) {
                        Text(
                            text = stringResource(R.string.register_submit_title),
                            fontSize = 16.sp,
                            color = ColorWhite
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NetworkConfigDropdown(
    configurations: List<RNetworkConfiguration>,
    selectedConfig: RNetworkConfiguration?,
    onConfigSelected: (RNetworkConfiguration) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedConfig?.name ?: "",
            onValueChange = {},
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = ColorWhite,
                unfocusedTextColor = ColorWhite,
                focusedBorderColor = ColorWhite,
                unfocusedBorderColor = ColorWhite.copy(alpha = 0.5f)
            )
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            configurations.forEach { config ->
                DropdownMenuItem(
                    text = { Text(config.name) },
                    onClick = {
                        onConfigSelected(config)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun RegisteredSection(
    state: StationItemDetailsState,
    onStationSettingsClicked: () -> Unit,
    onManageButtonsClicked: () -> Unit,
    onRebootClicked: () -> Unit,
    onNetworkSettingsClicked: () -> Unit,
    onLedTestClicked: () -> Unit,
    onDebugClicked: () -> Unit,
    onResetClicked: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        ActionButton(
            iconRes = R.drawable.ic_locker_settings,
            text = stringResource(R.string.main_station_settings),
            onClick = onStationSettingsClicked
        )

        ActionButton(
            iconRes = R.drawable.ic_peripheral,
            text = stringResource(R.string.main_locker_manage_buttons),
            onClick = onManageButtonsClicked
        )

        if (state.showActionsForProximity) {
            ActionButtonWithLoading(
                iconRes = R.drawable.ic_reboot,
                text = stringResource(R.string.peripheral_settings_reboot),
                isLoading = state.isRebootLoading,
                onClick = onRebootClicked
            )

            ActionButton(
                iconRes = R.drawable.ic_network_settings,
                text = stringResource(R.string.main_locker_manage_network),
                onClick = onNetworkSettingsClicked
            )
        }

        if (state.showLedTest) {
            ActionButtonWithLoading(
                iconRes = R.drawable.ic_led_test,
                text = stringResource(R.string.stop_the_bus_simulation),
                isLoading = state.isLedTestLoading,
                onClick = onLedTestClicked
            )
        }

        if (state.showActionsForProximity) {
            ActionButtonWithLoading(
                iconRes = R.drawable.ic_debug,
                text = stringResource(R.string.peripheral_settings_debug_mode),
                isLoading = state.isDebugLoading,
                onClick = onDebugClicked
            )

            ActionButton(
                iconRes = R.drawable.ic_remove,
                text = stringResource(R.string.peripheral_settings_reset),
                onClick = onResetClicked
            )
        }
    }
}

@Composable
private fun ActionButton(
    iconRes: Int,
    text: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp)
            .background(ColorPrimary)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            modifier = Modifier.size(30.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = text,
            color = ColorWhite,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ActionButtonWithLoading(
    iconRes: Int,
    text: String,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(40.dp),
                color = ColorPrimary
            )
        }
    } else {
        ActionButton(iconRes = iconRes, text = text, onClick = onClick)
    }
}
