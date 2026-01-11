package hr.sil.android.seeusadmin.compose_ui.buttons

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hr.sil.android.mplhuber.core.ble.DeviceStatus
import hr.sil.android.mplhuber.core.remote.WSSeeUsAdmin
import hr.sil.android.mplhuber.core.util.logger
import hr.sil.android.mplhuber.core.util.macCleanToReal
import hr.sil.android.mplhuber.core.util.macRealToClean
import hr.sil.android.seeusadmin.App
import hr.sil.android.seeusadmin.R
import hr.sil.android.seeusadmin.cache.status.ActionStatusType
import hr.sil.android.seeusadmin.data.RButtonDataUiModel
import hr.sil.android.seeusadmin.store.DeviceStore
import hr.sil.android.seeusadmin.store.MPLDeviceStoreRemoteUpdater
import hr.sil.android.seeusadmin.store.model.Device
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ManageButtonsViewModel : ViewModel() {

    private val log = logger()

    private val _uiState = MutableStateFlow(ManageButtonsUiState())
    val uiState: StateFlow<ManageButtonsUiState> = _uiState.asStateFlow()

    private val _uiEvents = MutableSharedFlow<ManageButtonsUiEvent>()
    val uiEvents: SharedFlow<ManageButtonsUiEvent> = _uiEvents.asSharedFlow()

    private var device: Device? = null

    fun initialize(macAddress: String) {
        _uiState.update { it.copy(masterMac = macAddress) }
        device = DeviceStore.devices[macAddress]
        refreshButtonList()
    }

    fun refreshButtonList() {
        viewModelScope.launch(Dispatchers.Default) {
            val buttons = combineButtons()
            println("manage button refresh 22 ${buttons.size}")
            val sortedButtons = buttons.sortedBy { !it.isInProximity }.sortedBy { it.status }
            println("manage button refresh 33 ${buttons.size}")
            _uiState.update { it.copy(buttons = sortedButtons) }
        }
    }

    // TODO: Handle this --> combineButtons
    private suspend fun combineButtons(): MutableList<RButtonDataUiModel> {

        //return mutableListOf()
        // Non registered slave units
        val actions = App.ref.stationDb.buttonKeyDao().getAllButtons().map { it.keyId } //ActionStatusHandler.actionStatusDb.getAll().map { it.keyId }
        val unregisteredButtonsInProximity = DeviceStore.getNonRegisteredButtonsInProximity(actions)
        MPLDeviceStoreRemoteUpdater.forceUpdate(false)
        val currentlyRegisteredButtons = DeviceStore.devices[_uiState.value.masterMac]?.buttonUnits
                ?: listOf()
        log.debug("Slave units ${currentlyRegisteredButtons.size}, Stored actions keys :" + actions.joinToString(" - ") { it })
        val registeredAndPendingButtons = currentlyRegisteredButtons.map {
            val inProximity = DeviceStore.devices[it.mac.macCleanToReal()]?.isInProximity
                    ?: false
            if (App.ref.stationDb.buttonKeyDao().getButtonByKeyId(it.mac.macCleanToReal() + ActionStatusType.BUTTON_DEREGISTRATION) != null) {
                log.debug("Registered - DELETE_PENDING:" + it.mac.macCleanToReal())
                RButtonDataUiModel(it.id, it.mac.macCleanToReal(), it.deviceStationId, DeviceStatus.DELETE_PENDING, inProximity)
            } else {
                val key = it.mac.macCleanToReal() + ActionStatusType.BUTTON_REGISTRATION
                if (actions.contains(key)) {
                    App.ref.stationDb.buttonKeyDao().removeButtonByKeyId(key)
                }
                log.debug("Registered " + it.mac.macCleanToReal())

                RButtonDataUiModel(it.id, it.mac.macCleanToReal(), it.deviceStationId, DeviceStatus.REGISTERED, inProximity)
            }
        }

        val unregisteredButtons = unregisteredButtonsInProximity.filter { it.mac !in registeredAndPendingButtons.map { it.mac } }
        return (unregisteredButtons + registeredAndPendingButtons).toMutableList()
    }

    fun onAddButtonClicked(button: RButtonDataUiModel, context: Context) {
        if (!button.isInProximity) {
            Toast.makeText(context, context.getString(R.string.main_locker_ble_connection_error), Toast.LENGTH_SHORT).show()
            return
        }

        val buttonIndex = _uiState.value.buttons.indexOfFirst { it.mac == button.mac }
        if (buttonIndex == -1) return

        _uiState.update { state ->
            val updatedButtons = state.buttons.toMutableList()
            updatedButtons[buttonIndex] = RButtonDataUiModel(
                button.id,
                button.mac,
                button.masterId,
                button.status,
                button.isInProximity
            ).apply { this.status = button.status }
            state.copy(
                buttons = updatedButtons,
                processingButtonMac = button.mac
            )
        }

        viewModelScope.launch {
            val masterMac = _uiState.value.masterMac
            val communicator = DeviceStore.devices[masterMac]?.createBLECommunicator(context)

            if (communicator != null && communicator.connect()) {
                if (communicator.registerButton(button.mac)) {
                    withContext(Dispatchers.Main) {
                        modifyStatusChange(button.mac, ActionStatusType.BUTTON_REGISTRATION)
                    }
                    log.info("Successfully send registration request for slave device master $masterMac slave - ${button.mac}")
                }
                communicator.disconnect()

                log.info("Connecting to Backend ${masterMac.macRealToClean()} ${button.mac.macRealToClean()}")
                if (WSSeeUsAdmin.addButtonToStation(masterMac.macRealToClean(), button.mac.macRealToClean())) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, context.getString(R.string.successfully_saved_data, button.mac), Toast.LENGTH_SHORT).show()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, context.getString(R.string.main_locker_ble_connection_error), Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                log.error("Error while connecting the peripheral ${button.mac}")
            }

            _uiState.update { it.copy(processingButtonMac = null) }
            refreshButtonList()
        }
    }

    fun onDeleteButtonClicked(button: RButtonDataUiModel) {
        _uiState.update { it.copy(buttonToDelete = button, showDeleteDialog = true) }
    }

    fun onDeleteDialogDismissed() {
        _uiState.update { it.copy(buttonToDelete = null, showDeleteDialog = false) }
    }

    fun onDeleteConfirmed(context: Context) {
        val buttonToDelete = _uiState.value.buttonToDelete ?: return
        val masterMac = _uiState.value.masterMac

        val masterDevice = DeviceStore.devices[masterMac]
        if (masterDevice?.isInProximity != true) {
            Toast.makeText(context, context.getString(R.string.main_locker_ble_connection_error), Toast.LENGTH_SHORT).show()
            onDeleteDialogDismissed()
            return
        }

        _uiState.update { it.copy(showDeleteDialog = false, processingButtonMac = buttonToDelete.mac) }

        viewModelScope.launch {
            bleDeletePeripheral(masterDevice, context, buttonToDelete)
        }
    }

    private suspend fun bleDeletePeripheral(device: Device, ctx: Context, button: RButtonDataUiModel) {
        val communicator = device.createBLECommunicator(ctx)
        if (communicator != null && communicator.connect()) {
            if (communicator.deregisterSlave(button.mac)) {
                withContext(Dispatchers.Main) {
                    modifyStatusChange(button.mac, ActionStatusType.BUTTON_DEREGISTRATION)
                }
                log.info("Successfully deregister button device ${button.mac}")

                if (WSSeeUsAdmin.deleteButtonFromStation(button.mac.macRealToClean())) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(ctx, ctx.getString(R.string.successfully_saved_data, button.mac), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else {
            log.error("Error while connecting the peripheral")
        }

        communicator.disconnect()
        _uiState.update { it.copy(processingButtonMac = null, buttonToDelete = null) }
        refreshButtonList()
    }

    private fun modifyStatusChange(macAddress: String, status: ActionStatusType) {
        _uiState.update { state ->
            val updatedButtons = state.buttons.map { button ->
                if (button.mac == macAddress) {
                    RButtonDataUiModel(
                        button.id,
                        button.mac,
                        button.masterId,
                        if (status == ActionStatusType.BUTTON_DEREGISTRATION) DeviceStatus.DELETE_PENDING else DeviceStatus.REGISTRATION_PENDING,
                        button.isInProximity
                    )
                } else {
                    button
                }
            }
            state.copy(buttons = updatedButtons)
        }
    }
}

data class ManageButtonsUiState(
    val masterMac: String = "",
    val buttons: List<RButtonDataUiModel> = emptyList(),
    val showDeleteDialog: Boolean = false,
    val buttonToDelete: RButtonDataUiModel? = null,
    val processingButtonMac: String? = null
)

sealed class ManageButtonsUiEvent {
    data class ShowToast(val message: String) : ManageButtonsUiEvent()
}
