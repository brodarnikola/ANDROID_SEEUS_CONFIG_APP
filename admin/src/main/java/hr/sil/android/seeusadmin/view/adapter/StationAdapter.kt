package hr.sil.android.seeusadmin.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import hr.sil.android.mplhuber.core.remote.model.RRealStationLocation
import hr.sil.android.seeusadmin.R

class StationAdapter(val listOfStations: List<RRealStationLocation>) : BaseAdapter() {


    override fun getItemId(p0: Int): Long {
        return listOfStations[p0].id?.toLong()?: 0
    }

    override fun getItem(p0: Int): RRealStationLocation {
        return listOfStations[p0]
    }

    fun getPositionAt(id: Int): Int {
        return listOfStations.indexOfFirst { it.id == id }
    }

    override fun getCount(): Int {
        return listOfStations.size
    }

    override fun getView(position: Int, convertView: View?, viewGroup: ViewGroup): View {
        val view: View
        val itemRowHolder: StationViewHolder
        val itemView =
            LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.list_station, viewGroup, false)

        if (convertView == null) {
            view = itemView
            itemRowHolder = StationViewHolder(view)
            view.tag = itemRowHolder
        } else {
            view = convertView
            itemRowHolder = view.tag as StationViewHolder
        }

        itemRowHolder.textView.text = listOfStations[position].name
        return view
    }

    inner class StationViewHolder(row: View) {
        val textView: TextView

        init {
            this.textView = row.findViewById(R.id.item_netowrk_config_name)
        }
    }
}