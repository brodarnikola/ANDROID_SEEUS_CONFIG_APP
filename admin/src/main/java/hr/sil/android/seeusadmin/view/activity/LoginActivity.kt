package hr.sil.android.seeusadmin.view.activity

import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.navigation.fragment.NavHostFragment
import hr.sil.android.mplhuber.core.util.logger
import hr.sil.android.seeusadmin.R

class LoginActivity : BaseActivity(noWifiViewId = R.id.no_internet_layout) {

    private val log = logger()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val navHost = NavHostFragment.create(R.navigation.nav_graph)
        supportFragmentManager.beginTransaction()
            .replace(R.id.main_frame, navHost)
            .setPrimaryNavigationFragment(navHost)
            .commit()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

}