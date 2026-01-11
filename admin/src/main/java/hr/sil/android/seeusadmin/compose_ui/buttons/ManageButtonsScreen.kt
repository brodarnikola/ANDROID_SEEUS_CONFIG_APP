package hr.sil.android.seeusadmin.compose_ui.buttons

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import hr.sil.android.mplhuber.core.ble.DeviceStatus
import hr.sil.android.seeusadmin.App
import hr.sil.android.seeusadmin.R
import hr.sil.android.seeusadmin.compose_ui.dialogs.DeleteButtonDialog
import hr.sil.android.seeusadmin.data.RButtonDataUiModel
import hr.sil.android.seeusadmin.events.DevicesUpdatedEvent
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

@Composable
fun ManageButtonsScreen(
    viewModel: ManageButtonsViewModel,
    macAddress: String
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    LaunchedEffect(Unit) {
        viewModel.initialize(macAddress)
    }

    DisposableEffect(lifecycleOwner) {
        val eventBusSubscriber = object {
            @Subscribe(threadMode = ThreadMode.MAIN)
            fun onDevicesUpdated(event: DevicesUpdatedEvent) {
                println("manage button refresh 11")
                viewModel.refreshButtonList()
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

    if (uiState.showDeleteDialog) {
        DeleteButtonDialog(
            onConfirm = { viewModel.onDeleteConfirmed(context) },
            onDismiss = { viewModel.onDeleteDialogDismissed() },
            onCancel = { viewModel.onDeleteDialogDismissed() }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp)
                .padding(top = 10.dp)
        ) {
            items(uiState.buttons, key = { it.mac }) { button ->
                ButtonListItem(
                    button = button,
                    isProcessing = uiState.processingButtonMac == button.mac,
                    onAddClick = { viewModel.onAddButtonClicked(button, context) },
                    onDeleteClick = { viewModel.onDeleteButtonClicked(button) }
                )
                Spacer(modifier = Modifier.height(5.dp))
            }
        }
    }
}

@Composable
fun ButtonListItem(
    button: RButtonDataUiModel,
    isProcessing: Boolean,
    onAddClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val context = LocalContext.current

    val buttonImage = when (button.status) {
        DeviceStatus.REGISTERED, DeviceStatus.DELETE_PENDING -> R.drawable.ic_peripheral_registered
        else -> R.drawable.ic_peripheral_unregistered
    }

    val proximityText = if (button.isInProximity) {
        stringResource(R.string.manage_peripherals_inproximity)
    } else {
        stringResource(R.string.manage_peripherals_not_inproximity)
    }

    val registrationText = when (button.status) {
        DeviceStatus.UNREGISTERED -> stringResource(R.string.manage_peripherals_unregistered)
        DeviceStatus.REGISTERED -> stringResource(R.string.manage_peripherals_registered)
        DeviceStatus.DELETE_PENDING -> stringResource(R.string.delete_pending)
        DeviceStatus.REGISTRATION_PENDING -> stringResource(R.string.registration_pending)
        else -> ""
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White.copy(alpha = 0.1f))
            .padding(vertical = 7.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = buttonImage),
            contentDescription = null,
            modifier = Modifier
                .weight(1.8f)
                .size(40.dp)
        )

        Column(
            modifier = Modifier
                .weight(6.7f)
                .padding(start = 2.dp)
        ) {
            Text(
                text = stringResource(R.string.manage_peripherals_title, button.mac),
                color = colorResource(R.color.colorWhite),
                fontSize = 12.sp,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(2.dp))

            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = proximityText,
                    color = colorResource(R.color.colorWhite),
                    fontSize = 12.sp,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = registrationText,
                    color = colorResource(R.color.colorWhite),
                    fontSize = 12.sp,
                    maxLines = 1,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 5.dp)
                )
            }
        }

        Box(
            modifier = Modifier
                .weight(1.5f)
                .padding(end = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isProcessing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(30.dp),
                    color = colorResource(R.color.colorPrimary),
                    strokeWidth = 2.dp
                )
            } else {
                when (button.status) {
                    DeviceStatus.UNREGISTERED -> {
                        Image(
                            painter = painterResource(id = R.drawable.ic_add_white),
                            contentDescription = stringResource(R.string.app_generic_alerts),
                            modifier = Modifier
                                .clickable { onAddClick() }
                        )
                    }
                    DeviceStatus.REGISTERED -> {
                        Image(
                            painter = painterResource(id = R.drawable.ic_cancel_access),
                            contentDescription = stringResource(R.string.app_generic_settings),
                            modifier = Modifier
                                .clickable { onDeleteClick() }
                        )
                    }
                    DeviceStatus.DELETE_PENDING, DeviceStatus.REGISTRATION_PENDING -> {
                    }
                    else -> {}
                }
            }
        }
    }
}
