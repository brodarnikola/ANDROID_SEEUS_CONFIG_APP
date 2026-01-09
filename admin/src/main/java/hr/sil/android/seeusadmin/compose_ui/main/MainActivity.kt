package hr.sil.android.seeusadmin.compose_ui.main


import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import hr.sil.android.mplhuber.core.util.logger
import hr.sil.android.seeusadmin.App
import hr.sil.android.seeusadmin.BuildConfig
import hr.sil.android.seeusadmin.R
import hr.sil.android.seeusadmin.compose_ui.sign_up_onboarding.SignUpOnboardingActivity
import hr.sil.android.seeusadmin.compose_ui.theme.AppTheme
import hr.sil.android.seeusadmin.events.UnauthorizedUserEvent
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

import kotlin.apply
import kotlin.collections.addAll
import kotlin.collections.isNotEmpty
import kotlin.collections.toTypedArray
import kotlin.jvm.java
import kotlin.let

import hr.sil.android.seeusadmin.util.DroidPermission
import hr.sil.android.seeusadmin.util.SettingsHelper

class MainActivity : ComponentActivity() {

    private val log = logger()
    private val droidPermission by lazy { DroidPermission.init(this) }

    // State holders for overlays
    private val systemStateViewModel = SystemStateViewModel()

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(SettingsHelper.setLocale(newBase))
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setNotification()
        requestPermissions()

        setContent {
            AppTheme {
                MainActivityContent(
                    systemStateViewModel = systemStateViewModel,
                    onNavigateToLogin = {
                        val intent = Intent(this, SignUpOnboardingActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                )
            }
        }
    }

    private fun requestPermissions() {
        val permissions = mutableListOf<String>().apply {
            addAll(
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                addAll(
                    arrayOf(
                        Manifest.permission.BLUETOOTH_SCAN,
                        Manifest.permission.BLUETOOTH_ADVERTISE,
                        Manifest.permission.BLUETOOTH_CONNECT
                    )
                )
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.POST_NOTIFICATIONS)
            }
            if (BuildConfig.DEBUG) {
                add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }.toTypedArray()

        droidPermission
            .request(*permissions)
            .done { _, deniedPermissions ->
                if (deniedPermissions.isNotEmpty()) {
                    log.info("Some permissions were denied!")
                    App.ref.permissionCheckDone = true
                } else {
                    log.info("Permissions accepted...")
                    App.ref.permissionCheckDone = true
                }
            }
            .execute()
    }

    override fun onResume() {
        super.onResume()
        App.ref.eventBus.register(this)
        systemStateViewModel.startMonitoring(this)
    }

    override fun onPause() {
        super.onPause()
        App.ref.eventBus.unregister(this)
        systemStateViewModel.stopMonitoring()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMplDeviceNotify(event: UnauthorizedUserEvent) {
        log.info("Received unauthorized event, user will now be logged out")
        val intent = Intent(this, SignUpOnboardingActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun setNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = getString(R.string.default_notification_channel_id)
            val channelName = getString(R.string.default_notification_channel_name)
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(
                NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW)
            )
        }

        intent.extras?.let { extras ->
            for (key in extras.keySet()) {
                val value = extras.get(key)
                log.info("Key: $key Value: $value")
            }
        }
    }
}

//@Preview(showBackground = true)
//@Composable
//fun GreetingPreview() {
//    MVI_ComposeTheme {
//    }
//}