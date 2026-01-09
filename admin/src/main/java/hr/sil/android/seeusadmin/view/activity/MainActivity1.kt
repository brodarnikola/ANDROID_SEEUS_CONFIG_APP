package hr.sil.android.seeusadmin.view.activity

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.google.firebase.analytics.FirebaseAnalytics
import hr.sil.android.mplhuber.core.util.logger
import hr.sil.android.seeusadmin.App
import hr.sil.android.seeusadmin.BuildConfig
import hr.sil.android.seeusadmin.R
import hr.sil.android.seeusadmin.databinding.ActivityMainBinding
import hr.sil.android.seeusadmin.util.backend.UserUtil
import hr.sil.android.seeusadmin.util.backend.UserUtil.logout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

import hr.sil.android.seeusadmin.util.DroidPermission

class MainActivity1 : BaseActivity(R.id.no_ble_layout, R.id.no_internet_layout, R.id.no_location_gps_layout) {

    private val log = logger()

    private val droidPermission by lazy { DroidPermission.init(this) }
    var navHostFragment: NavHostFragment? = null

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (!UserUtil.isUserLoggedIn()) {
//            setContentView(_RelativeLayout(this).apply {
//                progressBar {
//                    isIndeterminate = true
//                }.lparams {
//                    centerInParent()
//                }
//            })
            GlobalScope.launch(Dispatchers.Main) {
                if (UserUtil.login()) {
                    continueOnCreate(savedInstanceState)
                } else {
                    logout()
                }
            }
        } else {
            continueOnCreate(savedInstanceState)
        }

    }

    private fun continueOnCreate(savedInstanceState: Bundle?) {
        viewLoaded = true
        setNotification()

        val toolbar: Toolbar = findViewById(R.id.toolbarMain)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        if (savedInstanceState == null) {
            navHostFragment =
                supportFragmentManager.findFragmentById(R.id.navigation_host_fragment) as NavHostFragment?
            NavigationUI.setupWithNavController(
                binding.bottomMenu,
                navHostFragment!!.navController
            )

            navHostFragment!!.navController.addOnDestinationChangedListener { _, _, _ ->
                //hideKeyboard()
            }
        }

        val permissions = mutableListOf<String>().apply {
            addAll(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION))
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                addAll(arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT))
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

//                    log.info("Enabling bluetooth...")
//                    App.ref.btMonitor.enable {
//                        log.info("Bluetooth enabled!")
//                        App.ref.permissionCheckDone = true
//                    }
                }
            }
            .execute()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onBluetoothStateUpdated(available: Boolean) {
        super.onBluetoothStateUpdated(available)
        bluetoothAvailable = available
        if( viewLoaded == true )
            updateUI()
    }

    override fun onNetworkStateUpdated(available: Boolean) {
        super.onNetworkStateUpdated(available)
        networkAvailable = available
        if( viewLoaded == true )
            updateUI()
    }

    override fun onLocationGPSStateUpdated(available: Boolean) {
        super.onLocationGPSStateUpdated(available)
        locationGPSAvalilable = available
        if( viewLoaded == true )
            updateUI()
    }

    private fun setNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val bundle = Bundle()
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, UserUtil.user?.id.toString())
            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "NotificationReceived")
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "notification")
            // Create channel to show notifications.
            val channelId = getString(R.string.default_notification_channel_id)
            val channelName = getString(R.string.default_notification_channel_name)
            val notificationManager =
                ContextCompat.getSystemService( baseContext, NotificationManager::class.java)
            notificationManager!!.createNotificationChannel(
                NotificationChannel(
                    channelId,
                    channelName, NotificationManager.IMPORTANCE_HIGH
                )
            )
        }

        // If a notification message is tapped, any data accompanying the notification
        // message is available in the intent extras. In this sample the launcher
        // intent is fired when the notification is tapped, so any accompanying data would
        // be handled here. If you want a different intent fired, set the click_action
        // field of the notification message to the desired intent. The launcher intent
        // is used when no click_action is specified.
        //
        // Handle possible data accompanying notification message.
        // [START handle_data_extras]
        if (intent.extras != null) {
            for (key in intent.extras!!.keySet()) {
                val value = intent.extras!!.get(key)
                log.info("Key: $key Value: $value")
            }
        }
    }

}
