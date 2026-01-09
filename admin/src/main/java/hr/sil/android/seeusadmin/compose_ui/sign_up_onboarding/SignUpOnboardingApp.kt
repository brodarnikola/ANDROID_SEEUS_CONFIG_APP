package hr.sil.android.seeusadmin.compose_ui.sign_up_onboarding

import androidx.annotation.StringRes
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavArgumentBuilder
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestinationBuilder
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import hr.sil.android.seeusadmin.R
import hr.sil.android.seeusadmin.compose_ui.login_forgot_password.ForgotPasswordScreen
import hr.sil.android.seeusadmin.compose_ui.login_forgot_password.ForgotPasswordUpdateScreen
import hr.sil.android.seeusadmin.compose_ui.login_forgot_password.LoginScreen
import hr.sil.android.seeusadmin.compose_ui.theme.AppTheme


@OptIn(ExperimentalAnimationApi::class, ExperimentalComposeUiApi::class)
@Composable
fun SignUpOnboardingApp(
) {
    //val selectedTheme = signUpOnboardingViewModel.selectedTheme.collectAsState(initial = null)

    val appState = rememberSignUpAppState()
    val navBackStackEntry =
        appState.navController.currentBackStackEntryAsState() // navController.currentBackStackEntryAsState()

    AppTheme {
        Scaffold(scaffoldState = appState.scaffoldState, modifier = Modifier.semantics {
            testTagsAsResourceId = true
        }) { innerPaddingModifier ->
            val modifier = Modifier
            // Box required because there is no background in transition moment when changing screens
            Box(
                //modifier = Modifier.background(MaterialTheme.colorScheme.background)
            ) {
                NavigationStack(SignUpOnboardingSections.LOGIN_SCREEN.route, modifier)
            }
        }
    }
}

@Composable
fun NavigationStack(routeFirstScreen: String, modifier: Modifier) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = routeFirstScreen) {

        composable(
            SignUpOnboardingSections.LOGIN_SCREEN.route,
        ) {
            LoginScreen(
                modifier = modifier,
                viewModel = viewModel(),
                navigateUp = {
                    navController.popBackStack()
                },
                nextScreen = { route ->
                    if (route != navController.currentDestination?.route) {
                        navController.navigate(route)
                    }
                }
            )
        }


        composable(
            SignUpOnboardingSections.FORGOT_PASSWORD_SCREEN.route,
        ) {
            ForgotPasswordScreen(
                modifier = modifier,
                viewModel = viewModel(),
                navigateUp = {
                    navController.popBackStack()
                },
                nextScreen = { route, email ->
                    if ( route != navController.currentDestination?.route) {
                        navController.navigate(SignUpOnboardingSections.FORGOT_PASSWORD_UPDATE_SCREEN.route + "/$email")
                    }

                    //nextScreen(route, navBackStackEntry )
                }
            )
        }
//
        composable(
            "${SignUpOnboardingSections.FORGOT_PASSWORD_UPDATE_SCREEN.route}/{${NavArguments.EMAIL}}",
            arguments = listOf(navArgument(NavArguments.EMAIL) {
                type = NavType.StringType
            })
            //SignUpOnboardingSections.FORGOT_PASSWORD_UPDATE_SCREEN.route,
        ) {
            ForgotPasswordUpdateScreen(
                modifier = modifier,
                viewModel = viewModel(),
                navigateUp = {
                    navController.popBackStack()
                },
                nextScreen = { route ->
                    if (route != navController.currentDestination?.route) {
                        navController.navigate(route)
                    }
                }
            )
        }

    }
}

object NavArguments {
    const val EMAIL = "emailAddress"
}



enum class SignUpOnboardingSections(
    @StringRes val title: Int,
    val icon: ImageVector,
    val route: String
) {
    LOGIN_SCREEN(R.string.login_submit_title, Icons.Outlined.Search, "splash/loginScreen"),
    FORGOT_PASSWORD_SCREEN(R.string.forgot_password_title, Icons.Outlined.Search, "splash/forgotPasswordScreen"),
    FORGOT_PASSWORD_UPDATE_SCREEN(R.string.forgot_password_description_title, Icons.Outlined.Search, "splash/forgotPasswordUpdateScreen"),

//    INTRODUCTION(R.string.btn_continue, Icons.Outlined.Search, "splash/introduction"),
//    LOGIN(R.string.btn_continue, Icons.Outlined.Search, "splash/login"),
//    FORGOT_PASSWORD(R.string.btn_continue, Icons.Outlined.Search, "splash/forgotPassword"),
//    FORGOT_PASSWORD_SUCCESS(R.string.btn_continue, Icons.Outlined.Search,
}