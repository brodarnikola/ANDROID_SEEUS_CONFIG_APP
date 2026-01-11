/* SWISS INNOVATION LAB CONFIDENTIAL
*
* www.swissinnolab.com
* __________________________________________________________________________
*
* [2016] - [2018] Swiss Innovation Lab AG
* All Rights Reserved.
*
* @author mfatiga
*
* NOTICE:  All information contained herein is, and remains
* the property of Swiss Innovation Lab AG and its suppliers,
* if any.  The intellectual and technical concepts contained
* herein are proprietary to Swiss Innovation Lab AG
* and its suppliers and may be covered by E.U. and Foreign Patents,
* patents in process, and are protected by trade secret or copyright law.
* Dissemination of this information or reproduction of this material
* is strictly forbidden unless prior written permission is obtained
* from Swiss Innovation Lab AG.
*/

package hr.sil.android.seeusadmin


import android.app.Application
import android.content.Context
import android.content.res.Configuration
import androidx.room.Room
import com.facebook.stetho.Stetho
import com.facebook.stetho.okhttp3.StethoInterceptor
import com.google.firebase.messaging.FirebaseMessaging

//import com.google.firebase.iid.FirebaseInstanceId

import hr.sil.android.ble.scanner.BLEDeviceScanner
import hr.sil.android.ble.scanner.exception.BLEScanException
import hr.sil.android.ble.scanner.scan_multi.BLEGenericDeviceDataFactory
import hr.sil.android.ble.scanner.scan_multi.model.BLEDeviceType
import hr.sil.android.mplhuber.core.remote.model.RLanguage
import hr.sil.android.mplhuber.core.util.BLEScannerStateHolder
import hr.sil.android.mplhuber.core.util.logger
import hr.sil.android.seeusadmin.beacons.BLEDynamicDefinitionHandler
import hr.sil.android.seeusadmin.cache.status.ActionStatusHandler
import hr.sil.android.seeusadmin.fcm.MPLFireBaseMessagingService
import hr.sil.android.seeusadmin.remote.WSConfig
import hr.sil.android.seeusadmin.store.DeviceStore
import hr.sil.android.seeusadmin.store.MPLDeviceStoreRemoteUpdater
import hr.sil.android.seeusadmin.util.SettingsHelper
//import hr.sil.android.util.bluetooth.BluetoothAdapterMonitor
//import hr.sil.android.util.general.delegates.synchronizedDelegate
//import hr.sil.android.util.general.extensions.format
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import org.greenrobot.eventbus.EventBus
//import org.jetbrains.anko.runOnUiThread
//import org.jetbrains.anko.toast
import java.util.*

import hr.sil.android.rest.core.synchronizedDelegate
import hr.sil.android.rest.core.format
import hr.sil.android.rest.core.BluetoothAdapterMonitor
import hr.sil.android.seeusadmin.database.StationDb
import hr.sil.android.seeusadmin.util.ui.awaitForResult

/**
 * @author mfatiga
 */
class App : Application(), BLEScannerStateHolder {
    private val log = logger()

    companion object {
        @JvmStatic
        lateinit var ref: App
    }

    init {
        ref = this
    }

    //bluetooth adapter monitor
    val btMonitor: BluetoothAdapterMonitor by lazy { BluetoothAdapterMonitor.create(this) }

    //event bus initialization
    val eventBus: EventBus by lazy {
        EventBus.builder()
                .logNoSubscriberMessages(false)
                .sendNoSubscriberEvent(false)
                .build()
    }

    var languageCode: RLanguage = RLanguage()

    override fun attachBaseContext(base: Context) {
        println("Attaching base context in APP!!")

        SettingsHelper.init(base)
        super.attachBaseContext(SettingsHelper.setLocale(base))
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        SettingsHelper.setLocale(this)
    }
    @Volatile
    private var debugMode: Boolean = false

    private fun setDebugMode(enabled: Boolean) {
        log.info("Setting DEBUG_MODE to $enabled")
        debugMode = enabled
        deviceScanner.DEBUG_MODE = enabled
    }

    //device scanner
    var permissionCheckDone by synchronizedDelegate(false, this)
    private var errorLastShownAt = 0L
    private val deviceScanner by lazy {
        BLEDeviceScanner.create(
            GlobalScope,
                this,
                {
                    if (permissionCheckDone) {
                        log.error("Error during BLE scan!", it)
                    }

                    var showError = true
                    if (!permissionCheckDone && (it.errorCode == BLEScanException.ErrorCode.SCAN_FAILED_BLUETOOTH_DISABLED
                                    || it.errorCode == BLEScanException.ErrorCode.SCAN_FAILED_LOCATION_PERMISSION_MISSING)) {
                        showError = false
                    }

                    if (showError) {
//                        runOnUiThread {
//                            val now = System.currentTimeMillis()
//                            if (now - errorLastShownAt >= 10000L) {
//                                Toast.makeText(App.ref.applicationContext, "Error: ${it.errorCode}, Toast.LENGTH_SHORT).show()
//                                //App.ref.("Error: ${it.errorCode}")
//                                errorLastShownAt = now
//                            }
//                        }
                    }
                },
                BLEGenericDeviceDataFactory()
        )
    }

    private lateinit var stethoClient: OkHttpClient

    lateinit var stationDb: StationDb

    fun setupRoomDatabase(context: Context) {
        stationDb = Room.databaseBuilder(context, StationDb::class.java, "Station_Device_DB")
            //.fallbackToDestructiveMigration()
            .build()
    }

    override fun onCreate() {
        super.onCreate()

        setupRoomDatabase(this.applicationContext)


        Stetho.initializeWithDefaults(this)
        stethoClient = OkHttpClient.Builder().addInterceptor(StethoInterceptor()).build()


        log.info("Starting...")

        ActionStatusHandler.checkClasses(this)
        BLEDynamicDefinitionHandler.checkClasses(this)
        log.info("Initializing web services...")
        WSConfig.initialize(this.applicationContext)

        deviceScanner.setAdvertisementFilters(BLEDeviceType.DYNAMIC.filters())
        deviceScanner.configure {
            deviceLostPeriod = 20000
        }
        deviceScanner.addDeviceEventListener { events ->
            DeviceStore.updateFromBLE(deviceScanner.devices.values.filter { !it.data.manufacturer.isSILBootloader() }.toList())

            if (debugMode) {
                log.info("BLE device events: ${events.joinToString(", ") {
                    val device = it.bleDevice.deviceAddress
                    val deviceType = it.bleDevice.data.deviceType
                    val eventType = it.eventType.toString()
                    val time = Date(it.bleDevice.lastPacketTimestampMillis).format("HH:mm:ss")

                    "$device [$deviceType]->[$eventType]@$time"
                }}")
            }
            BLEDynamicDefinitionHandler.checkUpdateAsync()
        }

        //start periodic remote-data updater
        MPLDeviceStoreRemoteUpdater.run()
        ActionStatusHandler.run()

        //enable or disable debug mode
        setDebugMode(false)

        log.info("Starting BLE scan...")
        startScanner()
        GlobalScope.launch {
            val token = FirebaseMessaging.getInstance().token.awaitForResult() // FirebaseInstanceId.getInstance().requestToken()

            if (token != null) {
                log.info("FCM token: $token")
                MPLFireBaseMessagingService.sendRegistrationToServer(token)
            } else {
                log.error("Error while fetching the FCM token!")
            }
        }
    }

    private val PREFERENCES = "PREFERENCES"
    private val FIRST_TIME_STARTUP = "FIRST_TIME_STARTUP"


    override fun startScanner() {
        deviceScanner.start()
    }

    override suspend fun stopScannerAsync(forceDeviceLost: Boolean) {
        deviceScanner.stop(forceDeviceLost)
    }

    override fun isScannerStarted() = deviceScanner.isStarted()

    override fun onTerminate() {
        GlobalScope.launch(Dispatchers.Main) {
            deviceScanner.stop(false)
            deviceScanner.destroy()
        }
        super.onTerminate()
    }
}

