package hr.sil.android.seeusadmin.compose_ui.home_screens

import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hr.sil.android.mplhuber.core.ble.DeviceStatus
import hr.sil.android.mplhuber.core.remote.WSSeeUsAdmin
import hr.sil.android.mplhuber.core.remote.model.RNetworkConfiguration
import hr.sil.android.mplhuber.core.util.logger
import hr.sil.android.mplhuber.core.util.macRealToClean
import hr.sil.android.seeusadmin.BuildConfig
import hr.sil.android.seeusadmin.R
import hr.sil.android.seeusadmin.store.DeviceStore
import hr.sil.android.seeusadmin.store.model.Device
import hr.sil.android.seeusadmin.util.backend.UserUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date

data class StationItemDetailsState(
    val device: Device? = null,
    val macAddress: String = "",
    val toolbarTitle: String = "",
    val latitude: String = "",
    val longitude: String = "",
    val latitudeError: String? = null,
    val longitudeError: String? = null,
    val networkConfigurations: List<RNetworkConfiguration> = emptyList(),
    val selectedNetworkConfig: RNetworkConfiguration? = null,
    val isRegistrationLoading: Boolean = false,
    val isRegistrationInProgress: Boolean = false,
    val isLedTestLoading: Boolean = false,
    val isRebootLoading: Boolean = false,
    val isDebugLoading: Boolean = false,
    val isResetLoading: Boolean = false,
    val isUnregistered: Boolean = true,
    val showActionsForProximity: Boolean = false,
    val showLedTest: Boolean = false,
    val batteryValue: String = "-",
    val macAddressDisplay: String = "-",
    val modemStatus: String = "-",
    val deviceName: String = "-",
    val queueSize: String = "-",
    val rssiValue: String = "-",
    val ratValue: String = "-",
    val stmVersion: String = "-",
    val numOfButtons: String = "-"
)

sealed class StationItemDetailsUiEvent {
    object NavigateBack : StationItemDetailsUiEvent()
    data class NavigateToStationSettings(val masterMac: String) : StationItemDetailsUiEvent()
    data class NavigateToManageButtons(val masterMac: String) : StationItemDetailsUiEvent()
    data class NavigateToNetworkSettings(val masterMac: String) : StationItemDetailsUiEvent()
    data class NavigateToGoogleMaps(val macAddress: String) : StationItemDetailsUiEvent()
    data class ShowDeleteDialog(val macAddress: String, val device: Device?) : StationItemDetailsUiEvent()
}

sealed class StationItemDetailsEvent {
    data class OnLatitudeChanged(val value: String) : StationItemDetailsEvent()
    data class OnLongitudeChanged(val value: String) : StationItemDetailsEvent()
    data class OnNetworkSelected(val config: RNetworkConfiguration) : StationItemDetailsEvent()
    object OnRegisterClicked : StationItemDetailsEvent()
    object OnLedTestClicked : StationItemDetailsEvent()
    object OnRebootClicked : StationItemDetailsEvent()
    object OnDebugClicked : StationItemDetailsEvent()
    object OnResetClicked : StationItemDetailsEvent()
    object OnStationSettingsClicked : StationItemDetailsEvent()
    object OnManageButtonsClicked : StationItemDetailsEvent()
    object OnNetworkSettingsClicked : StationItemDetailsEvent()
    object OnMapClicked : StationItemDetailsEvent()
    object OnRefreshDevice : StationItemDetailsEvent()
}

class StationItemDetailsViewModel : ViewModel() {

    private val log = logger()

    private val _state = MutableStateFlow(StationItemDetailsState())
    val state: StateFlow<StationItemDetailsState> = _state.asStateFlow()

    private val _uiEvents = MutableSharedFlow<StationItemDetailsUiEvent>()
    val uiEvents = _uiEvents.asSharedFlow()

    private var lastRefreshTimestamp = Date(0)
    private val refreshDebounceMs = 1000L

    fun initialize(macAddress: String, latitude: Double = 0.0, longitude: Double = 0.0) {
        val device = DeviceStore.devices[macAddress]
        val toolbarTitle = if (device?.displayName?.isNotEmpty() == true && device.deviceStatus == DeviceStatus.REGISTERED) {
            device.displayName
        } else {
            ""
        }

        _state.update {
            it.copy(
                macAddress = macAddress,
                device = device,
                toolbarTitle = toolbarTitle,
                latitude = if (latitude != 0.0) latitude.toString() else "",
                longitude = if (longitude != 0.0) longitude.toString() else ""
            )
        }

        updateDeviceDetails()
        loadNetworkConfigurations()
    }

    fun onEvent(event: StationItemDetailsEvent, context: Context? = null) {
        when (event) {
            is StationItemDetailsEvent.OnLatitudeChanged -> {
                _state.update { it.copy(latitude = event.value, latitudeError = null) }
            }
            is StationItemDetailsEvent.OnLongitudeChanged -> {
                _state.update { it.copy(longitude = event.value, longitudeError = null) }
            }
            is StationItemDetailsEvent.OnNetworkSelected -> {
                _state.update { it.copy(selectedNetworkConfig = event.config) }
            }
            is StationItemDetailsEvent.OnRegisterClicked -> {
                context?.let { registerDevice(it) }
            }
            is StationItemDetailsEvent.OnLedTestClicked -> {
                context?.let { performLedTest(it) }
            }
            is StationItemDetailsEvent.OnRebootClicked -> {
                context?.let { performReboot(it) }
            }
            is StationItemDetailsEvent.OnDebugClicked -> {
                context?.let { performDebug(it) }
            }
            is StationItemDetailsEvent.OnResetClicked -> {
                viewModelScope.launch {
                    _uiEvents.emit(StationItemDetailsUiEvent.ShowDeleteDialog(
                        _state.value.macAddress,
                        _state.value.device
                    ))
                }
            }
            is StationItemDetailsEvent.OnStationSettingsClicked -> {
                viewModelScope.launch {
                    _uiEvents.emit(StationItemDetailsUiEvent.NavigateToStationSettings(_state.value.macAddress))
                }
            }
            is StationItemDetailsEvent.OnManageButtonsClicked -> {
                viewModelScope.launch {
                    _uiEvents.emit(StationItemDetailsUiEvent.NavigateToManageButtons(_state.value.macAddress))
                }
            }
            is StationItemDetailsEvent.OnNetworkSettingsClicked -> {
                viewModelScope.launch {
                    _uiEvents.emit(StationItemDetailsUiEvent.NavigateToNetworkSettings(_state.value.macAddress))
                }
            }
            is StationItemDetailsEvent.OnMapClicked -> {
                viewModelScope.launch {
                    _uiEvents.emit(StationItemDetailsUiEvent.NavigateToGoogleMaps(_state.value.macAddress))
                }
            }
            is StationItemDetailsEvent.OnRefreshDevice -> {
                refreshDeviceDebounced()
            }
        }
    }

    private fun refreshDeviceDebounced() {
        val currentTimestamp = Date()
        if (currentTimestamp.time - lastRefreshTimestamp.time > refreshDebounceMs) {
            val device = DeviceStore.devices[_state.value.macAddress]
            _state.update { it.copy(device = device) }
            updateDeviceDetails()
            lastRefreshTimestamp = currentTimestamp
        }
    }

    private fun loadNetworkConfigurations() {
        viewModelScope.launch {
            val list = WSSeeUsAdmin.getNetworkConfigurations() ?: emptyList()
            _state.update {
                it.copy(
                    networkConfigurations = list,
                    selectedNetworkConfig = list.firstOrNull()
                )
            }
        }
    }

    private fun updateDeviceDetails() {
        val device = _state.value.device

        val isUnregistered = device?.deviceStatus == null ||
                device.deviceStatus == DeviceStatus.UNREGISTERED ||
                device.deviceStatus == DeviceStatus.UNKNOWN ||
                device.deviceStatus == DeviceStatus.REGISTRATION_PENDING

        val showActionsForProximity = device?.isInProximity == true
        val showLedTest = BuildConfig.DEBUG || device?.isInProximity == true

        val batteryValue = device?.batteryVoltage ?: "-"
        val macAddressDisplay = device?.macAddress ?: "-"
        val modemStatus = device?.mplMasterModemStatus?.toString() ?: "-"
        val deviceName = when {
            device?.stationName?.isNotEmpty() == true -> device.stationName
            device?.unitName?.isNotEmpty() == true -> device.unitName
            else -> "-"
        }
        val queueSize = device?.mplMasterModemQueueSize?.toString() ?: "-"
        val rssiValue = if (device?.modemRssi == "NaN") "-" else device?.modemRssi ?: "-"
        val ratValue = if (device?.modemRat == "NaN") "-" else device?.modemRat ?: "-"
        val stmVersion = if (device?.stmVersion.isNullOrEmpty()) "-" else device?.stmVersion?.substringBeforeLast(".") ?: "-"
        val numOfButtons = device?.numOfButtons?.toString() ?: "-"

        _state.update {
            it.copy(
                isUnregistered = isUnregistered,
                showActionsForProximity = showActionsForProximity,
                showLedTest = showLedTest,
                batteryValue = batteryValue,
                macAddressDisplay = macAddressDisplay,
                modemStatus = modemStatus,
                deviceName = deviceName,
                queueSize = queueSize,
                rssiValue = rssiValue,
                ratValue = ratValue,
                stmVersion = stmVersion,
                numOfButtons = numOfButtons
            )
        }
    }

    private fun validateLatitude(context: Context): Boolean {
        val latitude = _state.value.latitude

        return when {
            latitude.isBlank() -> {
                _state.update { it.copy(latitudeError = context.getString(R.string.locker_settings_error_name_empty_warning)) }
                false
            }
            latitude.startsWith(".") -> {
                _state.update { it.copy(latitudeError = context.getString(R.string.scu_latitude_dot_first_place)) }
                false
            }
            !latitude.contains(".") -> {
                _state.update { it.copy(latitudeError = context.getString(R.string.scu_latitude_dot_does_not_exist)) }
                false
            }
            else -> {
                val parts = latitude.split(".")
                when {
                    parts.size < 2 || parts[1].isEmpty() -> {
                        _state.update { it.copy(latitudeError = context.getString(R.string.scu_latitude_no_characters_after_dot)) }
                        false
                    }
                    parts[0].toFloatOrNull()?.let { it >= 90f } == true -> {
                        _state.update { it.copy(latitudeError = context.getString(R.string.scu_latitude_value_to_big)) }
                        false
                    }
                    parts[0].toFloatOrNull()?.let { it <= -90f } == true -> {
                        _state.update { it.copy(latitudeError = context.getString(R.string.scu_latitude_value_to_small)) }
                        false
                    }
                    parts[1].length < 2 -> {
                        _state.update { it.copy(latitudeError = context.getString(R.string.scu_latitude_value_to_small_charachters)) }
                        false
                    }
                    parts[1].length > 8 -> {
                        _state.update { it.copy(latitudeError = context.getString(R.string.scu_latitude_value_to_many_charachters)) }
                        false
                    }
                    else -> {
                        _state.update { it.copy(latitudeError = null) }
                        true
                    }
                }
            }
        }
    }

    private fun validateLongitude(context: Context): Boolean {
        val longitude = _state.value.longitude

        return when {
            longitude.isBlank() -> {
                _state.update { it.copy(longitudeError = context.getString(R.string.locker_settings_error_name_empty_warning)) }
                false
            }
            longitude.startsWith(".") -> {
                _state.update { it.copy(longitudeError = context.getString(R.string.scu_longitude_dot_first_place)) }
                false
            }
            !longitude.contains(".") -> {
                _state.update { it.copy(longitudeError = context.getString(R.string.scu_longitude_dot_does_not_exist)) }
                false
            }
            else -> {
                val parts = longitude.split(".")
                when {
                    parts.size < 2 || parts[1].isEmpty() -> {
                        _state.update { it.copy(longitudeError = context.getString(R.string.scu_longitude_no_characters_after_dot)) }
                        false
                    }
                    parts[0].toFloatOrNull()?.let { it >= 180f } == true -> {
                        _state.update { it.copy(longitudeError = context.getString(R.string.scu_longitude_value_to_big)) }
                        false
                    }
                    parts[0].toFloatOrNull()?.let { it <= -180f } == true -> {
                        _state.update { it.copy(longitudeError = context.getString(R.string.scu_longitude_value_to_small)) }
                        false
                    }
                    parts[1].length < 2 -> {
                        _state.update { it.copy(longitudeError = context.getString(R.string.scu_longitude_value_to_small_charachters)) }
                        false
                    }
                    parts[1].length > 8 -> {
                        _state.update { it.copy(longitudeError = context.getString(R.string.scu_longitude_value_to_many_charachters)) }
                        false
                    }
                    else -> {
                        _state.update { it.copy(longitudeError = null) }
                        true
                    }
                }
            }
        }
    }

    private fun registerDevice(context: Context) {
        val state = _state.value
        val device = state.device
        val selectedConfig = state.selectedNetworkConfig

        if (!validateLatitude(context) || !validateLongitude(context)) return
        if (selectedConfig == null || device?.isInProximity != true) {
            Toast.makeText(context, context.getString(R.string.main_locker_registration_error), Toast.LENGTH_SHORT).show()
            return
        }

        val customerId = UserUtil.user?.customerId
        if (customerId == null) {
            Toast.makeText(context, context.getString(R.string.main_locker_registration_error), Toast.LENGTH_SHORT).show()
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isRegistrationLoading = true) }

            try {
                val communicator = device.createBLECommunicator(context)
                if (communicator.connect()) {
                    log.info("Successfully connected $customerId, ${selectedConfig.apnUrl}, ${state.macAddress}")

                    val result = communicator.registerMaster(
                        customerId,
                        selectedConfig,
                        null,
                        60L,
                        false
                    )

                    withContext(Dispatchers.Main) {
                        if (!result) {
                            Toast.makeText(context, context.getString(R.string.main_locker_registration_error), Toast.LENGTH_SHORT).show()
                            _state.update { it.copy(isRegistrationLoading = false) }
                        } else {
                            Toast.makeText(context, context.getString(R.string.main_locker_registration_started), Toast.LENGTH_SHORT).show()
                            _state.update { it.copy(isRegistrationLoading = false, isRegistrationInProgress = true) }
                        }
                    }
                    communicator.disconnect()
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, context.getString(R.string.main_locker_registration_error), Toast.LENGTH_SHORT).show()
                        _state.update { it.copy(isRegistrationLoading = false) }
                    }
                }
            } catch (e: Exception) {
                log.error("Registration failed", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, context.getString(R.string.main_locker_registration_error), Toast.LENGTH_SHORT).show()
                    _state.update { it.copy(isRegistrationLoading = false) }
                }
            }
        }
    }

    private fun performLedTest(context: Context) {
        val device = DeviceStore.devices[_state.value.macAddress]

        viewModelScope.launch {
            _state.update { it.copy(isLedTestLoading = true) }

            try {
                if (device?.isInProximity == true) {
                    val communicator = device.createBLECommunicator(context)
                    if (communicator.connect()) {
                        log.info("Successfully connected ${_state.value.macAddress}")
                        val stopBusResult = communicator.stopTheBusTest()
                        if (!stopBusResult) {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, context.getString(R.string.main_locker_force_open_update_error), Toast.LENGTH_SHORT).show()
                            }
                        }
                        val assistanceResult = communicator.assistanceTest()
                        if (!assistanceResult) {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "Assistance error", Toast.LENGTH_SHORT).show()
                            }
                        }
                        communicator.disconnect()
                    } else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, context.getString(R.string.main_locker_ble_connection_error), Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    device?.masterUnitMac?.let { masterMac ->
                        WSSeeUsAdmin.simulateStationAction(masterMac.macRealToClean(), "ACTION_STOP")
                        WSSeeUsAdmin.simulateStationAction(masterMac.macRealToClean(), "ACTION_ASSIST")
                    }
                }
            } catch (e: Exception) {
                log.error("LED test failed", e)
            } finally {
                withContext(Dispatchers.Main) {
                    _state.update { it.copy(isLedTestLoading = false) }
                }
            }
        }
    }

    private fun performReboot(context: Context) {
        val device = _state.value.device

        viewModelScope.launch {
            _state.update { it.copy(isRebootLoading = true) }

            try {
                val communicator = device?.createBLECommunicator(context)
                if (communicator?.connect() == true) {
                    log.info("Successfully connected ${_state.value.macAddress}")
                    val result = communicator.rebootSystem()
                    if (!result) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, context.getString(R.string.main_locker_force_open_update_error), Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, context.getString(R.string.main_locker_ble_connection_error), Toast.LENGTH_SHORT).show()
                    }
                }
                communicator?.disconnect()
            } catch (e: Exception) {
                log.error("Reboot failed", e)
            } finally {
                withContext(Dispatchers.Main) {
                    _state.update { it.copy(isRebootLoading = false) }
                }
            }
        }
    }

    private fun performDebug(context: Context) {
        val device = _state.value.device

        viewModelScope.launch {
            _state.update { it.copy(isDebugLoading = true) }

            try {
                val communicator = device?.createBLECommunicator(context)
                if (communicator?.connect() == true) {
                    log.info("Successfully connected ${_state.value.macAddress}")
                    val result = communicator.setSystemInDebugMode()
                    withContext(Dispatchers.Main) {
                        if (!result) {
                            Toast.makeText(context, context.getString(R.string.main_locker_debug_set_error), Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, context.getString(R.string.main_locker_debug_set_success), Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, context.getString(R.string.main_locker_ble_connection_error), Toast.LENGTH_SHORT).show()
                    }
                }
                communicator?.disconnect()
            } catch (e: Exception) {
                log.error("Debug mode failed", e)
            } finally {
                withContext(Dispatchers.Main) {
                    _state.update { it.copy(isDebugLoading = false) }
                }
            }
        }
    }
}
