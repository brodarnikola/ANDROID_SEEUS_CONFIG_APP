
package hr.sil.android.seeusadmin.compose_ui.home_screens

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hr.sil.android.mplhuber.core.ble.DeviceStatus
import hr.sil.android.mplhuber.core.remote.model.RUnitType
import hr.sil.android.mplhuber.core.util.logger
import hr.sil.android.seeusadmin.R
import hr.sil.android.seeusadmin.store.DeviceStore
import hr.sil.android.seeusadmin.store.model.Device
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import java.util.Locale

data class NavHomeState(
    val devices: List<Device> = emptyList(),
    val filterText: String = "",
    val isLoading: Boolean = false,
    val isEmpty: Boolean = true,
    @StringRes val emptyMessageResId: Int = R.string.main_no_stations,
    val isFilterActive: Boolean = false
)

sealed class NavHomeUiEvent {
    data class NavigateToDeviceDetails(val macAddress: String) : NavHomeUiEvent()
}

sealed class NavHomeEvent {
    data class OnFilterChanged(val text: String) : NavHomeEvent()
    data class OnDeviceClicked(val device: Device) : NavHomeEvent()
    object OnRefresh : NavHomeEvent()
    object OnClearFilter : NavHomeEvent()
}

class NavHomeViewModel : ViewModel() {

    private val log = logger()

    private val _state = MutableStateFlow(NavHomeState())
    val state: StateFlow<NavHomeState> = _state.asStateFlow()

    private val _uiEvents = MutableSharedFlow<NavHomeUiEvent>()
    val uiEvents = _uiEvents.asSharedFlow()

    private var lastRefreshTimestamp = Date(0)
    private val refreshDebounceMs = 3000L

    init {
        refreshDevices()
    }

    fun onEvent(event: NavHomeEvent) {
        when (event) {
            is NavHomeEvent.OnFilterChanged -> {
                _state.update { it.copy(filterText = event.text) }
                refreshDevices()
            }
            is NavHomeEvent.OnDeviceClicked -> {
                _state.update { it.copy(filterText = "") }
                viewModelScope.launch {
                    _uiEvents.emit(NavHomeUiEvent.NavigateToDeviceDetails(event.device.macAddress))
                }
            }
            is NavHomeEvent.OnRefresh -> {
                refreshDevicesDebounced()
            }
            is NavHomeEvent.OnClearFilter -> {
                _state.update { it.copy(filterText = "") }
                refreshDevices()
            }
        }
    }

    private fun refreshDevicesDebounced() {
        val currentTimestamp = Date()
        if (currentTimestamp.time - lastRefreshTimestamp.time > refreshDebounceMs) {
            refreshDevices()
            lastRefreshTimestamp = currentTimestamp
        }
    }

    fun refreshDevices() {
        val allDevices = DeviceStore.devices.values
            .filter { it.unitType == RUnitType.SEEUS_SCU }
            .toList()

        val filteredDevices = applyFilter(allDevices)
        val sortedDevices = sortDevices(filteredDevices)

        val isEmpty = sortedDevices.isEmpty()
        val isFilterActive = _state.value.filterText.isNotEmpty()
        val emptyMessageResId = if (isFilterActive) {
            R.string.wrong_inserted_text_home_screen
        } else {
            R.string.main_no_stations
        }

        _state.update {
            it.copy(
                devices = sortedDevices,
                isEmpty = isEmpty,
                emptyMessageResId = emptyMessageResId,
                isFilterActive = isFilterActive
            )
        }
    }

    private fun applyFilter(devices: List<Device>): List<Device> {
        val filterText = _state.value.filterText
        return if (filterText.isEmpty()) {
            devices
        } else {
            val filter = filterText.lowercase(Locale.getDefault())
            devices.filter { device ->
                device.macAddress.lowercase().contains(filter) ||
                        device.displayName.lowercase().contains(filter)
            }
        }
    }

    private fun sortDevices(devices: List<Device>): List<Device> {
        return devices
            .sortedBy { it.displayName }
            .sortedBy { it.bleDistance }
            .sortedBy { getSortIndex(it) }
    }

    private fun getSortIndex(device: Device): Int {
        return when {
            device.isInProximity && device.deviceStatus == DeviceStatus.REGISTERED && device.masterUnitId != -1 -> 1
            device.deviceStatus == DeviceStatus.UNREGISTERED && device.isInProximity -> 2
            !device.isInProximity -> 3
            else -> 4
        }
    }
}
