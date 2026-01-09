package hr.sil.android.seeusadmin.util

import android.content.Context
import android.net.ConnectivityManager
import hr.sil.android.mplhuber.core.util.logger
import hr.sil.android.seeusadmin.App
//import hr.sil.android.seeusadmin.cache.DataCache
import hr.sil.android.seeusadmin.store.DeviceStore
import hr.sil.android.seeusadmin.store.MPLDeviceStoreRemoteUpdater


object AppUtil {
    val log = logger()

    suspend fun refreshCache() {
        //DatabaseHandler.deliveryKeyDb.clear()
        //DataCache.clearCaches()
        //DataCache.preloadCaches()
        DeviceStore.clear()
        //force update device store
        MPLDeviceStoreRemoteUpdater.forceUpdate()
    }


    fun isInternetAvailable(): Boolean {
        try {
            val cm = App.ref.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            return cm.activeNetworkInfo != null
        } catch (e: SecurityException) {
            log.error("Please check if you grant ACCESS_NETWORK_STATE, or put insights = false in App init!")
            return false
        }
    }
}

