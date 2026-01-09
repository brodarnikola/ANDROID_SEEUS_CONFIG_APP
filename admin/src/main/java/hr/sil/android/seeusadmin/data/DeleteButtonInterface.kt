package hr.sil.android.seeusadmin.data

import android.content.Context
import android.widget.ImageView
import android.widget.ProgressBar

interface DeleteButtonInterface {

    fun deleteButtonFromSCU( masterMac: String, ctx: Context, parcelLocker: RButtonDataUiModel, progressBar: ProgressBar, deleteButton: ImageView )
}