package hr.sil.android.seeusadmin.view.dialog

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.fragment.app.DialogFragment
import hr.sil.android.mplhuber.core.util.logger
import hr.sil.android.seeusadmin.R
import hr.sil.android.seeusadmin.data.RButtonDataUiModel
import hr.sil.android.seeusadmin.databinding.DialogDeleteButtonBinding
import hr.sil.android.seeusadmin.databinding.DialogLogoutBinding
import hr.sil.android.seeusadmin.util.SettingsHelper
import hr.sil.android.seeusadmin.util.backend.UserUtil
import hr.sil.android.seeusadmin.view.activity.LoginActivity
import hr.sil.android.seeusadmin.view.adapter.ButtonAdapter

class DeleteButtonFromStation(
    val masterMac: String,
    val fragmentContext: Context,
    val buttonModel: RButtonDataUiModel,
    val progressBar: ProgressBar,
    val deleteButton: ImageView,
    val buttonViewHolder: ButtonAdapter.PeripheralItemViewHolder
) : DialogFragment() {

    private lateinit var binding: DialogDeleteButtonBinding
    private val log = logger()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogDeleteButtonBinding.inflate(LayoutInflater.from(context))

        val dialog = activity?.let {
            Dialog(it)
        }

        if(dialog != null) {
            dialog.window?.setBackgroundDrawable( ColorDrawable(Color.TRANSPARENT))
            dialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
            dialog.setCanceledOnTouchOutside(false)
            dialog.setContentView(binding.root)

            binding.btnConfirm.setOnClickListener {
                buttonViewHolder.deleteButtonFromSCU(masterMac, fragmentContext, buttonModel, progressBar, deleteButton)
                dismiss()
            }

            binding.btnCancel.setOnClickListener {
                dismiss()
            }
        }

        return dialog!!
    }

}