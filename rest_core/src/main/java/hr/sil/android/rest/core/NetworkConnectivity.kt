package hr.sil.android.rest.core

import android.content.Context
import android.net.ConnectivityManager
import java.net.HttpURLConnection
import java.net.URL

/**
 * @author mfatiga
 */
object NetworkConnectivity {
    @JvmStatic
    fun isNetworkAvailable(context: Context): Boolean {
        return try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            connectivityManager.activeNetworkInfo?.isConnected == true
        } catch (exc: Exception) {
            false
        }
    }

    @JvmStatic
    fun isConnectionToGoogleAvailable(): Boolean {
        return try {
            val urlConnection = URL("https://clients3.google.com/generate_204").openConnection() as HttpURLConnection
            urlConnection.responseCode == 204 && urlConnection.contentLength == 0
        } catch (exc: Exception) {
            false
        }
    }
}