package hr.sil.android.seeusadmin.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import hr.sil.android.mplhuber.core.util.logger
import hr.sil.android.seeusadmin.R
import hr.sil.android.seeusadmin.preferences.PreferenceStore
import hr.sil.android.seeusadmin.util.backend.UserUtil
import hr.sil.android.seeusadmin.view.activity.LoginActivity
import hr.sil.android.seeusadmin.view.activity.MainActivity
import kotlinx.coroutines.*

class SplashActivity : AppCompatActivity() {

    private val log = logger()

    private val SPLASH_DISPLAY_LENGTH = 3000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
//        relativeLayout {
//            imageView (R.drawable.bg_splash
//            ){
//                scaleType = ImageView.ScaleType.CENTER_CROP
//            }.lparams {
//                width = matchParent
//                height = matchParent
//            }
//        }
        preloadAndStartMain()
    }

    private fun preloadAndStartMain() {
        GlobalScope.launch(Dispatchers.Default) {
            val beginTimestamp = System.currentTimeMillis()
            val duration = System.currentTimeMillis() - beginTimestamp
            log.info("App Start length:" + duration)
            if (duration < SPLASH_DISPLAY_LENGTH) {
                delay(SPLASH_DISPLAY_LENGTH - duration)
            }

            withContext(Dispatchers.Main) {
                startApp()
                finish()
            }
        }
    }

    private suspend fun startApp() {
        val startupClass: Class<*>
        startupClass = if (!PreferenceStore.userHash.isNullOrBlank()) {
            if (UserUtil.login()) {
                MainActivity::class.java

            } else {
                LoginActivity::class.java
            }

        } else {
            LoginActivity::class.java
        }
        Log.i("SplashActivity", "This is second start")


        val startIntent = Intent(this@SplashActivity, startupClass)
        startActivity(startIntent)
        finish()

    }
}