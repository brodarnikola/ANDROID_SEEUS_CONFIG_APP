package hr.sil.android.seeusadmin.fcm

import android.widget.Toast
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import hr.sil.android.mplhuber.core.remote.WSSeeUsAdmin
import hr.sil.android.mplhuber.core.util.logger
import hr.sil.android.seeusadmin.App
import hr.sil.android.seeusadmin.R
import hr.sil.android.seeusadmin.util.AppUtil
import hr.sil.android.seeusadmin.util.NotificationHelper
import hr.sil.android.seeusadmin.view.activity.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


open class MPLFireBaseMessagingService : FirebaseMessagingService() {
    val log = logger()

    override fun onMessageReceived(remoteMessage: RemoteMessage) {

        // TODO(developer): Handle FCM messages here.
        log.info("From: " + remoteMessage.from!!)
        // Check if message contains a data payload.
        if (remoteMessage.data.size > 0) {
            log.info("Message data payload: " + remoteMessage.data)

            if (/* Check if data needs to be processed by long running job */ false) {
                // For long-running tasks (10 seconds or more) use Firebase Job Dispatcher.
                scheduleJob()
            } else {
                // Handle message within 10 seconds
                handleNow(remoteMessage.data)
            }

        }
        // Check if message contains a notification payload.
        if (remoteMessage.notification != null) {
            log.info("Message Notification Body: " + remoteMessage.notification!!.body!!)
        }
    }

    override fun onNewToken(token: String) {
        log.info("Refreshed token: $token")
        GlobalScope.launch(Dispatchers.Default) {
            if (!sendRegistrationToServer(token)) {
                withContext(Dispatchers.Main) {
                    log.error("Error in registration to server please check your internet connection")
                    //App.ref.toast(App.ref.getString(R.string.app_generic_no_network))
                    Toast.makeText(App.ref.applicationContext, App.ref.getString(R.string.app_generic_no_network), Toast.LENGTH_SHORT).show()
                }
            }

        }
    }

    /**
     * Schedule a job using FirebaseJobDispatcher.
     */
    private fun scheduleJob() {
        // [START dispatch_job]
//        val dispatcher = FirebaseJobDispatcher(GooglePlayDriver(this))
//        val myJob = dispatcher.newJobBuilder()
//                .setService(NotificationJobService::class.java)
//                .setTag("my-job-tag")
//                .build()
//        dispatcher.schedule(myJob)
        // [END dispatch_job]
    }

    private fun handleNow(result: Map<String, String>) {
        NotificationHelper(App.ref).createNotification(result["subject"], result["body"], MainActivity::class.java)
        GlobalScope.launch {
            AppUtil.refreshCache()
        }
        log.info("Short task when notification is opened is done")
    }



    companion object {

        suspend fun sendRegistrationToServer(token: String): Boolean {
            return WSSeeUsAdmin.registerDevice(token)
        }
    }


}