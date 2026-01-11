package hr.sil.android.seeusadmin.compose_ui.home_screens


import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import hr.sil.android.mplhuber.core.remote.WSSeeUsAdmin
import hr.sil.android.mplhuber.core.remote.model.RNetworkConfiguration
import hr.sil.android.mplhuber.core.remote.model.RPowerType
import hr.sil.android.mplhuber.core.remote.model.RStationUnitRequest
import hr.sil.android.mplhuber.core.util.logger
import hr.sil.android.mplhuber.core.util.macRealToClean
import hr.sil.android.seeusadmin.store.DeviceStore
import hr.sil.android.seeusadmin.store.model.Device
import hr.sil.android.seeusadmin.util.AppUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlin.apply
import kotlin.collections.firstOrNull


class NetworkSettingsViewModel  : ViewModel() {

    private val log = logger()

    private val _uiState = MutableStateFlow(NetworkSettingsUiState())
    val uiState: StateFlow<NetworkSettingsUiState> = _uiState
    private var device: Device? = null //MPLDeviceStore.devices[macAddress]

    fun loadNetworkConfigurations(paramMacAddress: String) {
        viewModelScope.launch {
            device = DeviceStore.devices[paramMacAddress]
            val list = WSSeeUsAdmin.getNetworkConfigurations() ?: emptyList()
            val selected =
                list.firstOrNull { it.id == device?.networkConfigurationId } ?: list.firstOrNull()

            println("Selected network configuration 11: $selected")
            println("Selected network configuration 22: ${selected?.id}")
            println("Selected network configuration 33: ${selected?.name}")
            println("Selected network configuration 44: ${selected?.apnUrl}")
            println("Selected network configuration 55: ${selected?.apnUser}")

            _uiState.update {
                it.copy(
                    networkConfigurations = list,
                    selectedConfiguration = selected,
                    macAddress = paramMacAddress
                )
            }
        }
    }

    fun onNetworkSelected(config: RNetworkConfiguration) {
        _uiState.update {
            it.copy(selectedConfiguration = config)
        }
    }

    fun onOfflineSelected() {
        _uiState.update {
            it.copy(
                isOfflineMode = true,
                isSleepMode = false
            )
        }
    }
    fun onSleepSelected() {
        _uiState.update {
            it.copy(
                isOfflineMode = false,
                isSleepMode = true
            )
        }
    }
    fun onPsmChanged(enabled: Boolean) {
        _uiState.update {
            it.copy(
                isPsmEnabled = enabled
            )
        }
    }

    fun onSaveClicked(
        context: Context,
        success: String,
        error: String,
        connectionError: String,
        offlineMode: Boolean,
        psmEnabled: Boolean
    ) {
        val selectedItem = uiState.value.selectedConfiguration ?: return
        val macAddress = uiState.value.macAddress

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                withTimeout(60_000) {

                    println("Selected network configuration AA: $selectedItem")
                    println("Selected network configuration BB: ${selectedItem?.id}")
                    println("Selected network configuration CC: ${selectedItem?.name}")
                    println("Selected network configuration DD: ${selectedItem?.apnUrl}")
                    println("Selected network configuration EE: ${selectedItem?.apnUser}")

                    val communicator =
                        DeviceStore.devices[macAddress]
                            ?.createBLECommunicator(context)

                    if (communicator?.connect() == true) {
                        val nrOfSeconds = if (offlineMode) 60L else 21600L
                        val result = communicator.writeNetworkConfiguration(
                            selectedItem, null, nrOfSeconds, psmEnabled
                        )

                        communicator.applyConfiguration()
                        communicator.disconnect()

                        val backendResult =
                            if (result) updateMasterUnitOnBackend(selectedItem, macAddress, offlineMode)
                            else false

                        if (result && backendResult) {
                            Toast.makeText(context, success, Toast.LENGTH_SHORT)
                                .show()
                            AppUtil.refreshCache()
                        }
                        else {
                            Toast.makeText(context, error, Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                    else {
                        Toast.makeText(context, connectionError, Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            } catch (e: Exception) {
                log.error("Network update failed", e)
                Toast.makeText(context,  e.message, Toast.LENGTH_SHORT)
                .show()
                _uiState.update { it.copy(isLoading = false) }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private suspend fun updateMasterUnitOnBackend(
        selectedItem: RNetworkConfiguration,
        macAddress: String,
        offlineMode: Boolean
    ): Boolean {

        // TODO: HANDLE this

        //return true

        val request = RStationUnitRequest().apply {

            stationId = device?.stationId
            latitude = device?.latitude
            longitude = device?.longitude
            radiusMeters = device?.radius ?: 0
            name = device?.unitName
            polygon = device?.polygon as List<LatLng>
            stopPoint = device?.stopPoint
            epdTypeId = device?.epdId?: 0
            networkConfigurationId = selectedItem.id
            modemWorkingType = if (offlineMode) RPowerType.BATTERY.name else RPowerType.LINE.name

        }

        return WSSeeUsAdmin.modifyStationUnit(
            macAddress.macRealToClean(),
            request
        ) != null
    }
}

data class NetworkSettingsUiState(
    val isLoading: Boolean = false,
    val macAddress: String = "",
    val networkConfigurations: List<RNetworkConfiguration> = emptyList(),
    val selectedConfiguration: RNetworkConfiguration? = null,
    val isOfflineMode: Boolean = true,
    val isSleepMode: Boolean = false,
    val isPsmEnabled: Boolean = false
)