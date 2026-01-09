
package hr.sil.android.seeusadmin.compose_ui.home_screens

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.viewmodel.compose.viewModel
import hr.sil.android.ble.scanner.scan_multi.properties.advv2.common.MPLDeviceStatus

import kotlin.collections.filter
import kotlin.collections.joinToString
import kotlin.collections.map
import kotlin.text.isEmpty
import kotlin.text.isNotEmpty
import kotlin.text.uppercase



import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import hr.sil.android.seeusadmin.App
import hr.sil.android.seeusadmin.R
import hr.sil.android.seeusadmin.compose_ui.theme.ColorPrimary
import hr.sil.android.seeusadmin.compose_ui.theme.ColorTransparent
import hr.sil.android.seeusadmin.compose_ui.theme.ColorWhite
import hr.sil.android.seeusadmin.events.DevicesUpdatedEvent
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


@SuppressLint("StateFlowValueCalledInComposition")
@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun NavHomeScreen(
    viewModel: NavHomeViewModel,
    onNavigateToDeviceDetails: (macAddress: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val eventBusSubscriber = object {
            @Subscribe(threadMode = ThreadMode.MAIN)
            fun onDevicesUpdated(event: DevicesUpdatedEvent) {
                viewModel.onEvent(NavHomeEvent.OnRefresh)
            }
        }

        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    App.ref.eventBus.register(eventBusSubscriber)
                    viewModel.refreshDevices()
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
                is NavHomeUiEvent.NavigateToDeviceDetails -> {
                    onNavigateToDeviceDetails(event.macAddress)
                }
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.bg_home_screen),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            SearchBar(
                filterText = state.filterText,
                onFilterChanged = { viewModel.onEvent(NavHomeEvent.OnFilterChanged(it)) },
                onClearFilter = { viewModel.onEvent(NavHomeEvent.OnClearFilter) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            )

            if (!state.isEmpty) {
                Text(
                    text = stringResource(id = R.string.nav_home_mpl_title),
                    color = ColorWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 7.dp)
                )
            }

            if (state.isEmpty) {
                EmptyState(
                    messageResId = state.emptyMessageResId,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                )
            } else {
                DeviceList(
                    devices = state.devices,
                    onDeviceClick = { device ->
                        viewModel.onEvent(NavHomeEvent.OnDeviceClicked(device))
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
private fun SearchBar(
    filterText: String,
    onFilterChanged: (String) -> Unit,
    onClearFilter: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val hasText = filterText.isNotEmpty()

    TextField(
        value = filterText,
        onValueChange = onFilterChanged,
        placeholder = {
            Text(
                text = stringResource(id = R.string.hint_search_text_home_screen),
                color = ColorWhite.copy(alpha = 0.7f),
                fontSize = 13.sp
            )
        },
        colors = TextFieldDefaults.colors(
            focusedTextColor = ColorWhite,
            unfocusedTextColor = ColorWhite,
            cursorColor = ColorPrimary,
            focusedContainerColor = ColorTransparent,
            unfocusedContainerColor = ColorTransparent,
            focusedIndicatorColor = ColorWhite,
            unfocusedIndicatorColor = ColorWhite.copy(alpha = 0.5f)
        ),
        trailingIcon = {
            IconButton(
                onClick = {
                    if (hasText) {
                        onClearFilter()
                    }
                }
            ) {
                Icon(
                    painter = painterResource(
                        id = if (hasText) R.drawable.ic_clear_search else R.drawable.ic_search
                    ),
                    contentDescription = if (hasText) "Clear search" else "Search",
                    tint = ColorWhite
                )
            }
        },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(
            onDone = { focusManager.clearFocus() }
        ),
        singleLine = true,
        maxLines = 1,
        modifier = modifier
    )
}

@Composable
private fun DeviceList(
    devices: List<hr.sil.android.seeusadmin.store.model.Device>,
    onDeviceClick: (hr.sil.android.seeusadmin.store.model.Device) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(vertical = 3.dp)
    ) {
        items(
            items = devices,
            key = { it.macAddress }
        ) { device ->
            DeviceListItem(
                device = device,
                onClick = { onDeviceClick(device) }
            )
        }
    }
}



@Composable
private fun EmptyState(
    messageResId: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(id = messageResId),
            color = ColorWhite,
            fontSize = 16.sp,
            textAlign = TextAlign.Center
        )
    }
}