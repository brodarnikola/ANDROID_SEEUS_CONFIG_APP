package hr.sil.android.seeusadmin.view.fragment

import android.graphics.Paint
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import hr.sil.android.mplhuber.core.util.logger
import hr.sil.android.seeusadmin.App
import hr.sil.android.seeusadmin.R
import hr.sil.android.seeusadmin.databinding.FragmentLoginBinding
import hr.sil.android.seeusadmin.databinding.FragmnetPasswordUpdateBinding
import hr.sil.android.seeusadmin.util.backend.UserUtil
import hr.sil.android.seeusadmin.util.connectivity.NetworkChecker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PasswordUpdateFragment : BaseFragment() {

    val log = logger()

    private lateinit var binding: FragmnetPasswordUpdateBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
//        val rootView = inflater.inflate(
//            R.layout.fragmnet_password_update, container,
//            false
//        )

        initializeToolbarUILoginActivity(true, getString(R.string.reset_password_title))
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding = FragmnetPasswordUpdateBinding.inflate(layoutInflater)

        return binding.root // rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeUi()

    }

    private fun initializeUi() {
        val email = arguments?.getString("EMAIL") ?: ""
        log.info("Received email is: " + email)

        binding.tvShowPasswords.paintFlags = Paint.UNDERLINE_TEXT_FLAG
        binding.etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        binding.etRepeatPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD

        binding.tvShowPasswords.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    binding.etPassword.inputType = InputType.TYPE_CLASS_TEXT
                    binding.etRepeatPassword.inputType = InputType.TYPE_CLASS_TEXT
                }

                MotionEvent.ACTION_UP -> {
                    binding.etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                    binding.etRepeatPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                }
            }
            true
        }

        binding.btnPasswordUpdate.setOnClickListener {
            if (validate()) {

                lifecycleScope.launch {

                    if (NetworkChecker.isInternetConnectionAvailable()) {

                        withContext(Dispatchers.Main) {
                            binding.progressBar.visibility = View.VISIBLE
                        }

                        if (email != null && submitResetPass(email)) {

                            findNavController().navigate(R.id.password_update_fragment_to_login_fragment)
                            binding.progressBar.visibility = View.GONE
                        } else {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, R.string.error_updating_mpl,
                                    Toast.LENGTH_SHORT).show()
                                //App.ref.toast(R.string.error_updating_mpl)
                                binding.progressBar.visibility = View.GONE
                                log.error("Error while updating passsword from the user")
                            }
                        }

                    } else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, R.string.app_common_internet_connection,
                                Toast.LENGTH_SHORT).show()
                            //App.ref.toast(this@PasswordUpdateFragment.getString(R.string.app_common_internet_connection))
                            binding.progressBar.visibility = View.GONE
                        }
                    }
                }
            }
        }
    }


    private fun validate(): Boolean {
        if (!validatePassword()) return false

        if (!validateRepeatPassword()) {
            return false
        }

        if( !validatePin() ) return false

        return true
    }

    private fun validatePassword(): Boolean {

        if( binding.etPassword.text.toString().isBlank() ) {
            binding.tvPasswordError.visibility = View.VISIBLE
            binding.tvPasswordError.text = resources.getString(R.string.edit_user_validation_blank_fields_exist)
            return false
        }
        else if( binding.etPassword.text.toString().length < 6 ) {
            binding.tvPasswordError.visibility = View.VISIBLE
            binding.tvPasswordError.text = resources.getString(R.string.edit_user_validation_password_min_6_characters)
            return false
        }
        binding.tvPasswordError.visibility = View.GONE
        return true
    }

    private fun validateRepeatPassword(): Boolean {
        if( binding.etRepeatPassword.text.toString().isBlank() ) {
            binding.tvRepeatPasswordError.visibility = View.VISIBLE
            binding.tvRepeatPasswordError.text = resources.getString(R.string.edit_user_validation_blank_fields_exist)
            return false
        }
        else if( binding.etRepeatPassword.text.toString().length < 6 ) {
            binding.tvRepeatPasswordError.visibility = View.VISIBLE
            binding.tvRepeatPasswordError.text = resources.getString(R.string.edit_user_validation_password_min_6_characters)
            return false
        }
        binding.tvRepeatPasswordError.visibility = View.GONE
        return true
    }

    private fun validatePin(): Boolean {
        if( binding.etPin.text.toString().isBlank() ) {
            binding.tvPinError.visibility = View.VISIBLE
            binding.tvPinError.text = resources.getString(R.string.edit_user_validation_blank_fields_exist)
            return false
        }
        binding.tvPinError.visibility = View.GONE
        return true
    }

    private suspend fun submitResetPass(email: String): Boolean {
            return UserUtil.passwordReset(
                email = email,
                password = binding.etPassword.text.toString(),
                passwordCode = binding.etPin.text.toString())

    }

}