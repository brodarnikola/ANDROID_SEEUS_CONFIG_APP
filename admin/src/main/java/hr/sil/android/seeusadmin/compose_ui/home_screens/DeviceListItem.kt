package hr.sil.android.seeusadmin.compose_ui.home_screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hr.sil.android.mplhuber.core.ble.DeviceStatus
import hr.sil.android.seeusadmin.R
import hr.sil.android.seeusadmin.compose_ui.theme.ColorWhite
import hr.sil.android.seeusadmin.compose_ui.theme.ColorWhite30PercentTransparency
import hr.sil.android.seeusadmin.store.model.Device

@Composable
fun DeviceListItem(
    device: Device,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val deviceName = getDeviceName(device)
    val iconRes = getDeviceIcon(device)
    val showArrow = shouldShowArrow(device)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 5.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(ColorWhite30PercentTransparency)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = "Device status",
                modifier = Modifier
                    .size(48.dp)
                    .padding(end = 8.dp)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 10.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = deviceName,
                    color = ColorWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                DeviceDistanceText(device = device)

                if (device.macAddress.isNotEmpty()) {
                    Text(
                        text = device.macAddress,
                        color = ColorWhite,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            if (showArrow) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_chevron_right),
                    contentDescription = "Navigate",
                    tint = ColorWhite,
                    modifier = Modifier
                        .size(24.dp)
                        .padding(start = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun DeviceDistanceText(device: Device) {
    val distanceText = when {
        device.isPlaceholder -> null
        device.isInProximity && device.deviceStatus == DeviceStatus.REGISTERED && device.masterUnitId != -1 -> {
            stringResource(
                id = R.string.app_generic_distance,
                String.format("%.2f", device.bleDistance ?: 0.0)
            )
        }
        device.deviceStatus == DeviceStatus.UNREGISTERED && device.isInProximity -> {
            stringResource(
                id = R.string.app_generic_distance,
                String.format("%.2f", device.bleDistance ?: 0.0)
            )
        }
        !device.isInProximity -> {
            "${device.stationLatitude} - ${device.stationLongitude}"
        }
        else -> null
    }

    distanceText?.let {
        Text(
            text = it,
            color = ColorWhite,
            fontSize = 12.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

private fun getDeviceName(device: Device): String {
    val baseName = when {
        device.stationName.isNotEmpty() -> "${device.stationName} ${device.stopPoint ?: ""}"
        !device.unitName.isNullOrEmpty() -> device.unitName
        else -> device.macAddress
    }
    return if (device.isVirtual) "V $baseName" else baseName
}

private fun getDeviceIcon(device: Device): Int {
    return when {
        device.isPlaceholder -> R.drawable.ic_station_geofencing
        device.isInProximity && device.deviceStatus == DeviceStatus.REGISTERED && device.masterUnitId != -1 -> {
            R.drawable.ic_station_proximity_registered
        }
        device.deviceStatus == DeviceStatus.UNREGISTERED && device.isInProximity -> {
            R.drawable.ic_station_proximity_unregistered
        }
        !device.isInProximity -> R.drawable.ic_station_not_in_proximity
        else -> R.drawable.ic_station_unavailable
    }
}

private fun shouldShowArrow(device: Device): Boolean {
    return when {
        device.isPlaceholder -> true
        device.isInProximity && device.deviceStatus == DeviceStatus.REGISTERED && device.masterUnitId != -1 -> true
        device.deviceStatus == DeviceStatus.UNREGISTERED && device.isInProximity -> true
        !device.isInProximity -> true
        else -> false
    }
}
