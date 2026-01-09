package hr.sil.android.seeusadmin.compose_ui.main

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import hr.sil.android.seeusadmin.R
import hr.sil.android.seeusadmin.compose_ui.home_screens.AlertsScreen
import hr.sil.android.seeusadmin.compose_ui.home_screens.NavHomeScreen
import hr.sil.android.seeusadmin.compose_ui.home_screens.SettingsScreen
import hr.sil.android.seeusadmin.compose_ui.home_screens.StationItemDetailsScreen
import kotlin.collections.forEachIndexed



@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun MainComposeApp(
    appState: MainAppState,
    navBackStackEntry: State<NavBackStackEntry?>,
    deviceMacAddress: MutableState<String>
) {
    NavHost(
        navController = appState.navController,
        startDestination = MainDestinations.HOME,
        //modifier = Modifier.padding(paddingValues)
    ) {
        mainNavGraph(
            navController = appState.navController,
            deviceMacAddress = deviceMacAddress,
            navBackStackEntry = navBackStackEntry,
            goToPickup = { route, macAddress ->
                appState.goToPickup(route = route, macAddress)
            },
            goToDeviceDetails = { route, macAddress ->
                appState.navigateToDeviceDetails(
                    route = route,
                    macAddress = macAddress
                )
            },
            goToDeviceDetailsCleanUpScreens = { route, deviceId, nameOfDevice ->
                appState.navigateToDeviceDetailsCleanUpScreen(route, deviceId, nameOfDevice)
            },

            navigateUp = {
                appState.upPress()
            }
        )
    }
}

fun NavGraphBuilder.mainNavGraph(
    navBackStackEntry: State<NavBackStackEntry?>,
    goToDeviceDetails: (route: String, macAddress: String) -> Unit,
    goToDeviceDetailsCleanUpScreens: (route: String, deviceId: String, nameOfDevice: String) -> Unit,
    goToPickup: (route: String, macAddress: String) -> Unit,
    navigateUp: () -> Unit,
    navController: NavHostController,
    deviceMacAddress: MutableState<String>
) {
    composable(MainDestinations.HOME) {
        NavHomeScreen(
            viewModel = viewModel(), // viewModel,
            onNavigateToDeviceDetails = { macAddress ->
                if (navBackStackEntry.value?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                    deviceMacAddress.value = macAddress
                    navController.navigate("${MainDestinations.DEVICE_DETAILS}/$macAddress")
                }
            }
        )
    }

    composable(
        "${MainDestinations.DEVICE_DETAILS}/{${NavArguments.MAC_ADDRESS}}",
        arguments = listOf(
            navArgument(NavArguments.MAC_ADDRESS) {
                type = NavType.StringType
            }
        )
    ) {
        StationItemDetailsScreen(
            viewModel = viewModel(), // viewModel,
            macAddress = it.arguments?.getString(NavArguments.MAC_ADDRESS) ?: "",
        )
    }

    composable(MainDestinations.SETTINGS) {
        SettingsScreen(
            viewModel = viewModel()
        )
    }

    composable(MainDestinations.ALERTS) {
        AlertsScreen(  )
    }

}


// ----------------------------------------
// This is a wrapper view that allows us to easily and cleanly
// reuse this component in any future project
// ----------------------------------------
// This is a wrapper view that allows us to easily and cleanly
// reuse this component in any future project
@Composable
fun TabView(
    tabBarItems: List<BottomNavigationBarItem>,
    navBackStackEntry: State<NavBackStackEntry?>,
    goToNextScreen: (route: String) -> Unit
) {

    NavigationBar(
        containerColor = colorResource(R.color.colorWhite)
    ) {
        // looping over each tab to generate the views and navigation for each item
        tabBarItems.forEachIndexed { _, tabBarItem ->
            NavigationBarItem(
                selected = tabBarItem.route == navBackStackEntry.value?.destination?.route, // selectedTabIndex == index,
                onClick = {
                    goToNextScreen(tabBarItem.route)
                },
                icon = {
                    TabBarIconView(
                        isSelected = tabBarItem.route == navBackStackEntry.value?.destination?.route, // selectedTabIndex == index,
                        icon = tabBarItem.icon,
                        title = tabBarItem.route,
                        badgeAmount = tabBarItem.badgeAmount
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = colorResource(R.color.colorPrimary),
                    unselectedIconColor = colorResource(R.color.colorGray),
                    indicatorColor = colorResource(R.color.colorPrimary).copy(alpha = 0.1f)
                )
            )
        }
    }
}

// This component helps to clean up the API call from our TabView above,
// but could just as easily be added inside the TabView without creating this custom component
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabBarIconView(
    isSelected: Boolean,
    icon: Int,
    title: String,
    badgeAmount: Int? = null
) {
    BadgedBox(badge = {
        TabBarBadgeView(badgeAmount)
    }) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = title,
            tint = if (isSelected) {
                colorResource(R.color.colorPrimary)
            } else {
                colorResource(R.color.colorGray)
            }
        )
    }
}

// This component helps to clean up the API call from our TabBarIconView above,
// but could just as easily be added inside the TabBarIconView without creating this custom component
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun TabBarBadgeView(count: Int? = null) {
    if (count != null) {
        Badge {
            Text(count.toString())
        }
    }
}
// end of the reusable components that can be copied over to any new projects
// ----------------------------------------


