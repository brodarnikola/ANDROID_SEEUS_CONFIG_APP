package hr.sil.android.seeusadmin.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import hr.sil.android.mplhuber.core.remote.model.RAdminEpdInfo
import hr.sil.android.seeusadmin.R

class EpdAdapter(val listOfEpdUnits: List<RAdminEpdInfo>) : BaseAdapter() {


    override fun getItemId(p0: Int): Long {
        return listOfEpdUnits[p0].id.toLong()
    }

    override fun getItem(p0: Int): RAdminEpdInfo {
        return listOfEpdUnits[p0]
    }

    fun getPositionAt(id: Int): Int {
        return listOfEpdUnits.indexOfFirst { it.id == id }
    }

    override fun getCount(): Int {
        return listOfEpdUnits.size
    }

    override fun getView(position: Int, convertView: View?, viewGroup: ViewGroup): View {
        val view: View
        val itemRowHolder: EpdConfigurationViewHolder
        val itemView =
            LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.list_epdtype, viewGroup, false)

        if (convertView == null) {
            view = itemView
            itemRowHolder = EpdConfigurationViewHolder(view)
            view.tag = itemRowHolder
        } else {
            view = convertView
            itemRowHolder = view.tag as EpdConfigurationViewHolder
        }

        itemRowHolder.textView.text = listOfEpdUnits[position].name
        return view
    }

    inner class EpdConfigurationViewHolder(row: View) {
        val textView: TextView

        init {
            this.textView = row.findViewById(R.id.item_netowrk_config_name)
        }
    }

}