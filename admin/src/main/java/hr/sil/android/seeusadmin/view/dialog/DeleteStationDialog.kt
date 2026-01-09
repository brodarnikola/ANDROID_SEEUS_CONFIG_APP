package hr.sil.android.seeusadmin.view.dialog

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import hr.sil.android.mplhuber.core.remote.WSSeeUsAdmin
import hr.sil.android.mplhuber.core.util.logger
import hr.sil.android.seeusadmin.App
import hr.sil.android.seeusadmin.R
import hr.sil.android.seeusadmin.databinding.DialogDeleteStationBinding
import hr.sil.android.seeusadmin.store.model.Device
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.anko.toast

class DeleteStationDialog(
    val masterMac: String,
    val device: Device?
) : DialogFragment() {

    private lateinit var binding: DialogDeleteStationBinding
    private val log = logger()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogDeleteStationBinding.inflate(LayoutInflater.from(context))

        val dialog = activity?.let {
            Dialog(it)
        }

        if(dialog != null) {
            dialog.window?.setBackgroundDrawable( ColorDrawable(Color.TRANSPARENT))
            dialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
            dialog.setCanceledOnTouchOutside(false)
            dialog.setContentView(binding.root)

            binding.btnConfirm.setOnClickListener {
                binding.progressBarDeleteDevice.visibility = View.VISIBLE
                binding.btnConfirm.visibility = View.GONE
                binding.btnCancel.visibility = View.GONE
                lifecycleScope.launch {
                    val communicator = device?.createBLECommunicator(requireContext())
                    if (communicator?.connect() == true) {
                        log.info("Successfully connected $masterMac")
                        val bleResponse = communicator.resetConfiguration()
                        if( bleResponse ) {
                            WSSeeUsAdmin.eraseDevice(masterMac)
                        }
                        else
                            log.error("Error in reseting device!")
                        withContext(Dispatchers.Main) {
                            if( bleResponse ) {
                                App.ref.toast(requireContext().getString(R.string.success_deleting_device))
                                log.info("Erase of the device successfully started $masterMac")
                                dismiss()
                                findNavController().navigate(
                                    R.id.device_details_fragment_to_home_screen_fragment
                                )
                            }
                            else {
                                App.ref.toast(requireContext().getString(R.string.error_deleting_device))
                                log.error("Error in deleting, reseting device!")
                                binding.progressBarDeleteDevice.visibility = View.GONE
                                binding.btnConfirm.visibility = View.VISIBLE
                                binding.btnCancel.visibility = View.VISIBLE
                            }
                            communicator.disconnect()
                        }

                    } else {
                        withContext(Dispatchers.Main) {
                            App.ref.toast(requireContext().getString(R.string.main_locker_ble_connection_error))
                            log.error("Error while connecting the device")
                            binding.progressBarDeleteDevice.visibility = View.GONE
                            binding.btnConfirm.visibility = View.VISIBLE
                            binding.btnCancel.visibility = View.VISIBLE
                        }
                    }
                    communicator?.disconnect()
                }
            }

            binding.btnCancel.setOnClickListener {
                dismiss()
            }
        }

        return dialog!!
    }

}