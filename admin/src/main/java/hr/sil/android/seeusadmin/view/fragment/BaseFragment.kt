package hr.sil.android.seeusadmin.view.fragment

import android.content.Context
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import hr.sil.android.seeusadmin.R
import hr.sil.android.seeusadmin.view.activity.MainActivity
import kotlinx.coroutines.*
import java.lang.Runnable


abstract class BaseFragment : Fragment() {

    private val fragmentLoaderHandler = Handler()

    fun EditText.afterTextChangeDelay(duration: Long, run: (String) -> Unit) {
        var job: Job? = null
        addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                job?.cancel()
                job = GlobalScope.launch(Dispatchers.Main) {
                    try {
                        delay(duration)
                        run.invoke(this@afterTextChangeDelay.text.toString())
                    } catch (e: Exception) {
                        //ignore
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })
    }

    fun setFragment(navFragment: Fragment, forgetHistory: Boolean = false) {

        val pendingRunnable = Runnable {
            val fragmentTransaction = (requireContext() as MainActivity).supportFragmentManager.beginTransaction()
            fragmentTransaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
            fragmentTransaction.replace(R.id.main_frame_layout, navFragment, navFragment.tag).addToBackStack(null)
            if (forgetHistory) {
                fragmentManager?.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
                fragmentTransaction.commitAllowingStateLoss()
            } else fragmentTransaction.commit()
        }
        fragmentLoaderHandler.post(pendingRunnable)
    }

    fun initializeToolbarUIMainActivity(
        displayToolbarArrow: Boolean = false,
        toolbarTitleText: String = "",
        insideSettingsScreen: Boolean,
        insideManageUserDetailsScreen: Boolean,
        context: Context
    ) {

        val ivToolbarLogo: ImageView? = this.activity?.findViewById(R.id.ivToolbarLogo)
        val toolbar: Toolbar? = this.activity?.findViewById(R.id.toolbarMain)
        val toolbarTitle: TextView? = this.activity?.findViewById(R.id.toolbar_title)
        val ivLogout: ImageView? = this.activity?.findViewById(R.id.ivLogout)

        if (insideSettingsScreen || insideManageUserDetailsScreen) {
            ivLogout?.visibility = View.VISIBLE
            val toolbarRightCorneImage = if (insideManageUserDetailsScreen) {
                ContextCompat.getDrawable( context, R.drawable.ic_logout)
                //getDrawableAttrValue(R.attr.thmManageUserDeleteUserImage, context)
            } else {
                ContextCompat.getDrawable( context, R.drawable.ic_logout)
                //getDrawableAttrValue(R.attr.thmToolbarLogoutImage, context)
            }
            if (toolbarRightCorneImage != null)
                ivLogout?.setImageDrawable(toolbarRightCorneImage)
        } else {
            ivLogout?.visibility = View.GONE
        }

        if (displayToolbarArrow) {
            ivToolbarLogo?.visibility = View.GONE

            toolbarTitle?.visibility = View.VISIBLE
            toolbarTitle?.text = toolbarTitleText

            toolbar?.setPadding(0, toolbar.paddingTop, toolbar.paddingRight, toolbar.paddingBottom)
        } else {
            ivToolbarLogo?.visibility = View.VISIBLE
            toolbarTitle?.visibility = View.GONE

            toolbar?.setPadding(
                toolbar.paddingRight,
                toolbar.paddingTop,
                toolbar.paddingRight,
                toolbar.paddingBottom
            )
        }
    }

    fun initializeToolbarUILoginActivity(
        displayToolbarArrow: Boolean = false,
        toolbarTitleText: String = ""
    ) {

        val toolbar: Toolbar? = this.activity?.findViewById(R.id.toolbar)
        val toolbarTitle: TextView? = this.activity?.findViewById(R.id.toolbar_title)

        toolbarTitle?.text = toolbarTitleText

        if( displayToolbarArrow )
            toolbar?.setPadding(0, toolbar.paddingTop, toolbar.paddingRight, toolbar.paddingBottom);
        else
            toolbar?.setPadding(toolbar.paddingRight, toolbar.paddingTop, toolbar.paddingRight, toolbar.paddingBottom)
    }
}