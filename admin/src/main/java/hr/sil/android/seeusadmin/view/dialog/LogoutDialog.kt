package hr.sil.android.seeusadmin.view.dialog

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import hr.sil.android.seeusadmin.R
import hr.sil.android.seeusadmin.databinding.DialogLogoutBinding
import hr.sil.android.seeusadmin.util.SettingsHelper
import hr.sil.android.seeusadmin.util.backend.UserUtil
import hr.sil.android.seeusadmin.view.activity.LoginActivity

class LogoutDialog : DialogFragment() {

    private lateinit var binding: DialogLogoutBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogLogoutBinding.inflate(LayoutInflater.from(context))

        val dialog = activity?.let {
            Dialog(it)
        }

        if(dialog != null) {
            dialog.window?.setBackgroundDrawable( ColorDrawable(Color.TRANSPARENT))
            dialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
            dialog.setCanceledOnTouchOutside(false)
            dialog.setContentView(binding.root)

            binding.btnConfirm.setOnClickListener {
                SettingsHelper.userPasswordWithoutEncryption = ""
                UserUtil.logout()
                val intent = Intent(this@LogoutDialog.activity, LoginActivity::class.java)
                startActivity(intent)
                this@LogoutDialog.activity?.finish()
                dismiss()
            }

            binding.btnCancel.setOnClickListener {
                dismiss()
            }
        }

        return dialog!!
    }

}