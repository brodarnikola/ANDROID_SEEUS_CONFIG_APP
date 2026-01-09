package hr.sil.android.seeusadmin.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import hr.sil.android.mplhuber.core.util.logger
import hr.sil.android.seeusadmin.App
import hr.sil.android.seeusadmin.R
import hr.sil.android.seeusadmin.databinding.FragmentLoginBinding
import hr.sil.android.seeusadmin.databinding.FragmentPasswordRecoveryBinding
import hr.sil.android.seeusadmin.util.backend.UserUtil
import hr.sil.android.seeusadmin.util.connectivity.NetworkChecker
import kotlinx.coroutines.*

class PasswordRecoveryFragment : BaseFragment() {

    val log = logger()

    val parent = Job()

    private lateinit var binding: FragmentPasswordRecoveryBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
//        val rootView = inflater.inflate(
//            R.layout.fragment_password_recovery, container,
//            false
//        )

        initializeToolbarUILoginActivity(true, getString(R.string.forgot_password_title))
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding = FragmentPasswordRecoveryBinding.inflate(layoutInflater)

        return binding.root //rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeUi()
    }

    private fun initializeUi() {

        binding.btnPasswordRecovery.setOnClickListener {
            val bundle = bundleOf("EMAIL" to binding.etEmail.text.toString())
            log.info("Sended email is: " + bundle + " to String: " + bundle.toString())
            if (validate()) {

                binding.progressBar.visibility = View.VISIBLE
                lifecycleScope.launch(parent) {

                    log.info("Sended email is: AAAA proba")
                    if (NetworkChecker.isInternetConnectionAvailable()) {

                        val result = UserUtil.passwordRecovery(binding.etEmail.text.toString())

                        withContext(Dispatchers.Main) {
                            when {
                                result -> {
                                    val bundle = bundleOf("EMAIL" to binding.etEmail.text.toString())
                                    log.info("Sended email is: " + bundle + " to String: " + bundle.toString())
                                    findNavController().navigate(
                                        R.id.password_recovery_fragment_to_password_update_fragment,
                                        bundle
                                    )
                                }
                                else ->  {
                                    log.error("Error while starting to update passsword for user")
                                    Toast.makeText(requireContext(), requireContext().getString(R.string.error_updating_mpl), Toast.LENGTH_SHORT).show()

                                    //App.ref.toast(R.string.error_updating_mpl)
                                }
                            }
                            binding.progressBar.visibility = View.GONE
                        }

                    } else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(requireContext(), requireContext().getString(R.string.app_generic_no_network), Toast.LENGTH_SHORT).show()

                            //App.ref.toast(R.string.app_generic_no_network)
                            binding.progressBar.visibility = View.GONE
                        }
                    }

                }
            }
        }
    }

    override fun onPause() {
        if( parent.isActive ) parent.cancel()
        //log.info("Da li ce tu uci. Is coroutine active: " + parent.isActive + " is canceled: " + parent.isCancelled + " completed: " + parent.isCompleted)
        super.onPause()
    }

    private fun validate(): Boolean {

        if( binding.etEmail.text.toString().isBlank() ) {
            binding.tvEmailError.visibility = View.VISIBLE
            binding.tvEmailError.text = resources.getString(R.string.edit_user_validation_blank_fields_exist)
            return false
        }
        else if( !(".+@.+".toRegex().matches( binding.etEmail.text.toString())) ) {
            binding.tvEmailError.visibility = View.VISIBLE
            binding.tvEmailError.text = resources.getString(R.string.message_email_invalid)
            return false
        }
        binding.tvEmailError.visibility = View.GONE
        return true
    }

}