package hr.sil.android.seeusadmin.fcm

import com.firebase.jobdispatcher.JobParameters
import com.firebase.jobdispatcher.JobService
import hr.sil.android.mplhuber.core.util.logger

class NotificationJobService : JobService() {
    val log = logger()

    override fun onStopJob(p0: JobParameters?): Boolean {
        return false
    }

    override fun onStartJob(p0: JobParameters?): Boolean {
        log.info("Performing long running task in scheduled job")

        return true
    }
}