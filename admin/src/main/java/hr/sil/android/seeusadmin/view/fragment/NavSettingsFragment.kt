package hr.sil.android.seeusadmin.view.fragment

import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import hr.sil.android.mplhuber.core.model.RUpdateAdminInfo
import hr.sil.android.mplhuber.core.remote.WSSeeUsAdmin
import hr.sil.android.mplhuber.core.remote.model.RLanguage
import hr.sil.android.mplhuber.core.util.logger
import hr.sil.android.seeusadmin.App
import hr.sil.android.seeusadmin.R
import hr.sil.android.seeusadmin.cache.DataCache
import hr.sil.android.seeusadmin.databinding.FragmentSettingsBinding
import hr.sil.android.seeusadmin.util.SettingsHelper
import hr.sil.android.seeusadmin.util.backend.UserUtil
import hr.sil.android.seeusadmin.view.activity.MainActivity
import hr.sil.android.seeusadmin.view.adapter.LanguageAdapter
import hr.sil.android.seeusadmin.view.dialog.LogoutDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.anko.toast

class NavSettingsFragment : BaseFragment() {

    val log = logger()
    lateinit var selectedLanguage: RLanguage
    private val SELECTED_LANGUAGE = "Locale.Helper.Selected.Language"

    private lateinit var binding: FragmentSettingsBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        initializeToolbarUIMainActivity(true, resources.getString(R.string.app_generic_settings), true, false, requireContext())
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)

        binding = FragmentSettingsBinding.inflate(layoutInflater)

        return binding.root
//        return inflater.inflate(
//            R.layout.fragment_settings, container,
//            false
//        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launch {
            val list = DataCache.getLanguages().toList()
            withContext(Dispatchers.Main) {
                binding.spinerLanguageSelection.adapter = LanguageAdapter(list)
                if (context != null) {

                    val languageName = SettingsHelper.languageName
                    val languagesList = DataCache.getLanguages()
                    binding.spinerLanguageSelection.setSelection(languagesList.indexOfFirst { it.code == languageName })


                }
            }
        }

        binding.nameEditText.setText(UserUtil.user?.name)
        binding.settingsEmail.setText(UserUtil.user?.email)
        binding.tvVersion.text = resources.getString(
            R.string.nav_settings_app_version,
            resources.getString(R.string.app_version)
        )
    }

    override fun onActivityCreated(savedInstanceState: Bundle?)  {
        super.onActivityCreated(savedInstanceState)

        binding.spinerLanguageSelection.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {
                }

                override fun onItemSelected(
                    adapterView: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {

                    selectedLanguage = adapterView?.getItemAtPosition(position) as RLanguage
                    log.info("Selected language id is: ${selectedLanguage.id}, code: ${selectedLanguage.code}, name: ${selectedLanguage.name}")
                }
            }

        val ivLogout: ImageView? = this.activity?.findViewById(R.id.ivLogout)
        ivLogout?.setOnClickListener {
            val logoutDialog = LogoutDialog()
            logoutDialog.show(
                (requireContext() as MainActivity).supportFragmentManager, ""
            )
        }

        binding.btnSubmit.setOnClickListener {
            if (validate()) {
                    val updateInfo = RUpdateAdminInfo().apply {
                        this.name = binding.nameEditText.text.toString()
                        this.languageId = selectedLanguage.id
                    }

                    if ((binding.newPasswordEditText.text?.isNotEmpty()
                            ?: false) && binding.newPasswordEditText.text.toString() == binding.retypePasswordEditText.text.toString()
                    ) {
                        updateInfo.password = binding.newPasswordEditText.text.toString()
                    }

                    lifecycleScope.launch {
                        val resultUpdate = UserUtil.userUpdate(updateInfo)
                        withContext(Dispatchers.Main) {
                            if (resultUpdate) {

                                if( binding.retypePasswordEditText.text?.isNotEmpty() ?: false )
                                    SettingsHelper.userPasswordWithoutEncryption = binding.retypePasswordEditText.text.toString()

                                UserUtil.user?.name = binding.nameEditText.text.toString().trim()

                                App.ref.languageCode = selectedLanguage
                                SettingsHelper.languageName = selectedLanguage.code
                                val intent = Intent(context, MainActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                                this@NavSettingsFragment.activity?.finish()
                                this@NavSettingsFragment.activity?.overridePendingTransition(0, 0)
                                this@NavSettingsFragment.activity?.startActivity(intent)
                                val successNotice = this@NavSettingsFragment.getString(
                                    R.string.successfully_saved_data,
                                    UserUtil.user?.id.toString()
                                )
                                App.ref.toast(successNotice)
                            } else {

                            }
                        }
                    }

            }
        }

        binding.tvShowPasswords.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    binding.oldPasswordEditText.inputType = InputType.TYPE_CLASS_TEXT
                    binding.newPasswordEditText.inputType = InputType.TYPE_CLASS_TEXT
                    binding.retypePasswordEditText.inputType = InputType.TYPE_CLASS_TEXT
                }

                MotionEvent.ACTION_UP -> {
                    binding.oldPasswordEditText.inputType =
                        InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                    binding.newPasswordEditText.inputType =
                        InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                    binding.retypePasswordEditText.inputType =
                        InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                }
            }
            true
        }

        binding.tvShowPasswords.paintFlags = Paint.UNDERLINE_TEXT_FLAG
    }

    private fun validate(): Boolean {

        if( !validateName() ) {
            return false
        }

        if( !validateOldPassword() ) {
            return false
        }

        if( !validateNewPassword() ) {
            return false
        }

        if( !validateRetypePassword() ) {
            return false
        }

        return true
    }

    private fun validateOldPassword(): Boolean {
        if ( binding.oldPasswordEditText.text.toString().isBlank() && ( !(binding.retypePasswordEditText.text?.isBlank() ?: false) || !(binding.newPasswordEditText.text?.isBlank() ?: false)) ) {
            binding.tvOldPasswordError.visibility = View.VISIBLE
            binding.tvOldPasswordError.setText(R.string.edit_user_validation_blank_fields_exist)
            return false
        }
        else if( binding.oldPasswordEditText.text.toString() != SettingsHelper.userPasswordWithoutEncryption && binding.oldPasswordEditText.text.toString().isNotBlank() ) {
            binding.tvOldPasswordError.visibility = View.VISIBLE
            binding.tvOldPasswordError.setText(R.string.edit_user_validation_current_password_invalid)
            return false
        }
        binding.tvOldPasswordError.visibility = View.GONE
        return true
    }

    private fun validateNewPassword(): Boolean {
        if ( !binding.retypePasswordEditText.text.toString().isBlank() && binding.newPasswordEditText.text?.isBlank() ?: false ) {
            binding.tvNewPasswordError.visibility = View.VISIBLE
            binding.tvNewPasswordError.setText(R.string.edit_user_validation_blank_fields_exist)
            return false
        }
        binding.tvNewPasswordError.visibility = View.GONE
        return true
    }

    private fun validateRetypePassword(): Boolean {
        if ( binding.retypePasswordEditText.text.toString() != binding.newPasswordEditText.text.toString() ) {
            binding.tvRetypePasswordError.visibility = View.VISIBLE
            binding.tvRetypePasswordError.setText(R.string.reset_password_match_error)
            return false
        }
        binding.tvRetypePasswordError.visibility = View.GONE
        return true
    }

    private fun validateName(): Boolean {
        if ( binding.nameEditText.text.toString().isBlank() || binding.nameEditText.length() > 100 ) {
            binding.tvNameError.visibility = View.VISIBLE
            return false
        }
        binding.tvNameError.visibility = View.GONE
        return true
    }

}