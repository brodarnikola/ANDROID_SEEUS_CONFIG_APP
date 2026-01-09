package hr.sil.android.mplhuber.view.ui.home.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import hr.sil.android.mplhuber.core.remote.model.RMessageLog
import hr.sil.android.seeusadmin.R

class NotificationAdapter ( var splLockers: MutableList<RMessageLog>, val clickListener: (RMessageLog) -> Unit) : RecyclerView.Adapter<NotificationAdapter.NotesViewHolder>() {
    override fun onBindViewHolder(holder: NotesViewHolder, position: Int) {
        holder.bindItem(splLockers[position], clickListener)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotesViewHolder {
        /*val itemView = NotificationItemViewUI().createView(AnkoContext.Companion.create(parent.context, parent))
        val lp = RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        lp.bottomMargin = 16
        itemView.layoutParams = lp
        return NotesViewHolder(itemView)*/
        val itemView =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.list_alerts, parent, false)

        return NotesViewHolder(itemView)
    }

    override fun getItemCount() = splLockers.size

    fun updateNotes(splLockers: MutableList<RMessageLog>) {
        this.splLockers = splLockers
        notifyDataSetChanged()
    }

    fun addNote(note: RMessageLog) {
        splLockers.add(note)
        notifyItemChanged(splLockers.size - 1)
    }
    fun removeNote(id: RMessageLog) {
        splLockers.remove(id)
        notifyItemChanged(splLockers.size - 1)
    }

    inner class NotesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var date: TextView  = itemView.findViewById(R.id.notification_item_date)
        var masterName: TextView  = itemView.findViewById(R.id.notification_item_master_name)
        val subject: TextView = itemView.findViewById(R.id.notification_item_name)
        val content: TextView = itemView.findViewById(R.id.notification_item_content)


        fun bindItem(parcelLocker: RMessageLog,   clickListener: (RMessageLog) -> Unit) {
            date.text = parcelLocker.dateCreated.toString()
            masterName.text = parcelLocker.master___name
            subject.text = parcelLocker.subject
            content.text = parcelLocker.body
            itemView.setOnClickListener { clickListener(parcelLocker)}
        }


    }

}