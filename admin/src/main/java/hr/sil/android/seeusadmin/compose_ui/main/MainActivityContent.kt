package hr.sil.android.seeusadmin.compose_ui.main


import android.app.Activity
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.currentBackStackEntryAsState

import hr.sil.android.seeusadmin.R
import hr.sil.android.seeusadmin.compose_ui.dialogs.LogoutDialog
import hr.sil.android.seeusadmin.compose_ui.sign_up_onboarding.SignUpOnboardingActivity
import hr.sil.android.seeusadmin.store.DeviceStore
import hr.sil.android.seeusadmin.util.SettingsHelper
import hr.sil.android.seeusadmin.util.backend.UserUtil


data class BottomNavigationBarItem(
    val route: String,
    val icon: Int,
    val badgeAmount: Int? = null
)


fun bottomNavigationItems(): List<BottomNavigationBarItem> {
    // setting up the individual tabs
    val homeTab = BottomNavigationBarItem(
        route = MainDestinations.HOME,
        icon = R.drawable.ic_bottom_home
    )
    val tcTab = BottomNavigationBarItem(
        route = MainDestinations.TERMS_AND_CONDITION_SCREEN,
        icon = R.drawable.ic_bottom_tc
    )
    val settingsTab = BottomNavigationBarItem(
        route = MainDestinations.SETTINGS,
        icon = R.drawable.ic_bottom_settings
    )

    // creating a list of all the tabs
    val tabBarItems = listOf(homeTab, tcTab, settingsTab)
    return tabBarItems
}

// Main Composable with Overlays
@RequiresApi(Build.VERSION_CODES.S)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainActivityContent(
    systemStateViewModel: SystemStateViewModel,
    onNavigateToLogin: () -> Unit
) {
    val systemState by systemStateViewModel.systemState.collectAsState()

    val context = LocalContext.current
    val activity = LocalContext.current as Activity

    val appState = rememberMainAppState()

    val bottomNavigationItems = bottomNavigationItems()

    val showBottomBar = rememberSaveable { mutableStateOf(true) }
    val navBackStackEntry =
        appState.navController.currentBackStackEntryAsState() // navController.currentBackStackEntryAsState()

    val currentRoute = navBackStackEntry.value?.destination?.route

    val deviceMacAddress = rememberSaveable { mutableStateOf("") }
    val showLogoutDialog = remember { mutableStateOf(false) }

    if( showLogoutDialog.value ) {
        LogoutDialog(
            onDismiss = { showLogoutDialog.value = false },
            onConfirm = {
                showLogoutDialog.value = false
                SettingsHelper.userPasswordWithoutEncryption = ""
                UserUtil.logout()
                val intent = Intent(context, SignUpOnboardingActivity::class.java)
                activity.startActivity(intent)
                activity.finish()
            },
            onCancel = { showLogoutDialog.value = false }
        )
    }

    showBottomBar.value = when {
        currentRoute == null -> true
        navBackStackEntry.value?.destination?.route?.contains(MainDestinations.HOME) == true ||
        navBackStackEntry.value?.destination?.route?.contains(MainDestinations.TERMS_AND_CONDITION_SCREEN) == true ||
        navBackStackEntry.value?.destination?.route?.contains(MainDestinations.SETTINGS) == true  -> true
        else -> false
    }


    Scaffold(
        topBar = {
            TopAppBar(
                actions = {
                if (navBackStackEntry.value?.destination?.route == MainDestinations.SETTINGS) {
                    IconButton(
                        onClick = {
                            showLogoutDialog.value = true
                        }
                    ) {
                        Image(
                            painter = painterResource(R.drawable.ic_logout),
                            contentDescription = "Logout",
                            modifier = Modifier.height(28.dp)
                        )
                    }
                }
            },
                title = {
                    if( showBottomBar.value && navBackStackEntry.value?.destination?.route?.contains(MainDestinations.SETTINGS) != true ) {
                        Row(
                            Modifier
                                .fillMaxWidth() ,
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(R.drawable.seeus_black_thin),
                                contentDescription = "Logo",
                                modifier = Modifier.height(40.dp)
                            )
                        }
                    }
                    else {
                        ShowTitleScreen(navBackStackEntry.value?.destination?.route, deviceMacAddress)
                    }
                },
                navigationIcon = {
                    if (!showBottomBar.value) {
                        IconButton(onClick = {
                            appState.upPress()
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = colorResource(R.color.colorBlack)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            if(showBottomBar.value)
                TabView(
                    bottomNavigationItems,
                    navBackStackEntry)
                { route ->
                    Log.d("MENU", "route is: $route")
                    appState.navigateToRoute(route)
                }
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            Image(
                painter = painterResource(id = R.drawable.bg_home_screen),
                contentDescription = "Background",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            MainComposeApp(appState, navBackStackEntry)

            // Overlays - shown in priority order
            when {
                !systemState.bluetoothAvailable -> {
                    SystemOverlay(
                        message = stringResource(R.string.app_generic_no_ble),
                        backgroundDrawable = R.drawable.bg_bluetooth
                    )
                }

                !systemState.networkAvailable -> {
                    SystemOverlay(
                        message = stringResource(R.string.app_generic_no_network),
                        backgroundDrawable = R.drawable.bg_wifi_internet
                    )
                }

                !systemState.locationGPSAvailable -> {
                    LocationGPSOverlay()
                }
            }

        }
    }
}


@Composable
fun ShowTitleScreen(
    route: String?,
    deviceMacAddress: MutableState<String>
) {
    val routeTitle =  if(route?.contains(MainDestinations.DEVICE_DETAILS) == true) {
        val maxLength = if((DeviceStore.devices[deviceMacAddress.value]?.unitName?.length
                ?: 0) > 60
        ) 60 else DeviceStore.devices[deviceMacAddress.value]?.unitName?.length ?: 0
        val masterUnitName = DeviceStore.devices[deviceMacAddress.value]?.unitName?.substring(0, maxLength)
        masterUnitName ?: stringResource(R.string.mpl_locker_details_title)
    } else if(route?.contains(MainDestinations.SETTINGS) == true) {
        stringResource(R.string.app_generic_settings)
    } else if(route?.contains(MainDestinations.SETTINGS) == true) {
        stringResource(R.string.select_locker_location_title)
    } else if(route?.contains(MainDestinations.SETTINGS) == true) {
        stringResource(R.string.main_locker_manage_network)
    }
    else
        stringResource(R.string.app_name).uppercase()

    val maxLines = if( route?.contains(MainDestinations.DEVICE_DETAILS) == true ) 2 else 1

    Text(
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
        text = routeTitle,
        fontSize = 17.sp,
        letterSpacing = 1.sp,
        maxLines = maxLines
    )
}


@Composable
fun SystemOverlay(
    message: String,
    backgroundDrawable: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = backgroundDrawable),
            contentDescription = "Background",
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.8f),
            contentScale = ContentScale.Crop
        )

        Text(
            text = message.uppercase(),
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
fun LocationGPSOverlay(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.rectangle_transparent_dark),
            contentDescription = "Background",
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.8f),
            contentScale = ContentScale.Crop
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_no_location_services),
                contentDescription = "No Location",
                modifier = Modifier
                    .size(120.dp)
                    .padding(bottom = 20.dp)
            )

            Text(
                text = stringResource(R.string.app_generic_no_gps_location_service).uppercase(),
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}
