
package hr.sil.android.seeusadmin.compose_ui.home_screens

import android.app.Activity
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.viewModelScope
import hr.sil.android.mplhuber.core.remote.model.RStationUnit
import hr.sil.android.mplhuber.core.util.logger
import hr.sil.android.seeusadmin.App
import hr.sil.android.seeusadmin.R
import hr.sil.android.seeusadmin.events.DevicesUpdatedEvent
import hr.sil.android.seeusadmin.events.UnauthorizedUserEvent
import hr.sil.android.seeusadmin.util.BaseViewModel
import hr.sil.android.seeusadmin.util.UiEvent
import hr.sil.android.seeusadmin.util.backend.UserUtil
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import kotlin.collections.filter
import kotlin.collections.isNotEmpty
import kotlin.collections.map
import kotlin.collections.partition
import kotlin.text.isBlank

class NavHomeViewModel : BaseViewModel<NavHomeUiState, HomeScreenEvent>() {

    val log = logger()

    private val _uiState = MutableStateFlow(NavHomeUiState())
    val uiState: StateFlow<NavHomeUiState> = _uiState.asStateFlow()

    init {
        App.ref.eventBus.register(this)
        loadUserInfo()
    }

    private fun loadUserInfo() {
        _uiState.value = _uiState.value.copy(
            userName = UserUtil.user?.name ?: "",
            address = UserUtil.user?.email ?: ""
        )
    }

    fun loadDevices(context: Context) {
        viewModelScope.launch {
            //val items = getItemsForRecyclerView(context)
            //_uiState.value = _uiState.value.copy(devices = items)
        }
    }

//    private fun getItemsForRecyclerView(context: Context?): List<ItemHomeScreen> {
//        val items = mutableListOf<ItemHomeScreen>()
//
//        val (splList, mplList) = MPLDeviceStore.devices.values
//            .filter {
//                val isThisDeviceAvailable = when {
//                    UserUtil.user?.testUser == true -> true
//                    else -> it.isProductionReady == true
//                }
//                it.masterUnitType != RMasterUnitType.UNKNOWN && isThisDeviceAvailable
//            }
//            .partition {
//                it.masterUnitType == RMasterUnitType.SPL ||
//                        it.type == MPLDeviceType.SPL ||
//                        it.masterUnitType == RMasterUnitType.SPL_PLUS ||
//                        it.type == MPLDeviceType.SPL_PLUS
//            }
//
//        if (splList.isNotEmpty()) {
//            val header = ItemHomeScreen.Header()
//            header.headerTitle =
//                context?.getString(R.string.nav_home_spl_title) ?: "Single" // Use string resource
//            items.add(header)
//            items.addAll(splList.map { ItemHomeScreen.Child(it) })
//        }
//
//        if (mplList.isNotEmpty()) {
//            val header = ItemHomeScreen.Header()
//            header.headerTitle = context?.getString(R.string.nav_home_mpl_title) ?: "Multiple" // Use string resource
//            items.add(header)
//            items.addAll(mplList.map { ItemHomeScreen.Child(it) })
//        }
//
//        return items
//    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMplDeviceNotify(event: DevicesUpdatedEvent) {
        loadDevices(context = App.ref.applicationContext)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUnauthorizedUser(event: UnauthorizedUserEvent) {
        _uiState.value = _uiState.value.copy(isUnauthorized = true)
    }
    
    override fun initialState(): NavHomeUiState {
        return NavHomeUiState()
    }

    override fun onEvent(event: HomeScreenEvent) {
        when (event) {
            is HomeScreenEvent.OnForgotPasswordRequest -> {

                viewModelScope.launch {
                    _state.update { it.copy(loading = true) }
                    val response = UserUtil.passwordRecovery(
                        event.email
                    )
                    _state.update { it.copy(loading = false) }

                }

//                viewModelScope.launch {
//                    _state.update { it.copy(loading = true) }
//                    login(email = event.email, password = event.password, context = event.context)
//                    _state.update { it.copy(loading = false) }
//                }
            }

        }
    }

    fun getEmailError(email: String, context: Context): String {
        var emailError = ""
        if (email.isBlank()) {
            emailError = context.getString(R.string.edit_user_validation_blank_fields_exist)
        }

        return emailError
    }

}

data class NavHomeUiState(
    val loading: Boolean = false,

    val userName: String = "",
    val address: String = "",
    val devices: List<RStationUnit> = emptyList(),
    val isUnauthorized: Boolean = false
)

sealed class HomeScreenEvent {
    data class OnForgotPasswordRequest(
        val email: String,
        val context: Context,
        val activity: Activity
    ) : HomeScreenEvent()
}

sealed class HomeScreenUiEvent : UiEvent {
    data class NavigateToNextScreen(val route: String) : HomeScreenUiEvent()

    object NavigateBack : HomeScreenUiEvent()
}