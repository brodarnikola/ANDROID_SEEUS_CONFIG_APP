package hr.sil.android.seeusadmin.view.adapter

import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import hr.sil.android.seeusadmin.R
import android.view.LayoutInflater


class StopPointAdapter(val listOfPoints: Map<String, String>) : BaseAdapter() {

    val items = mutableMapOf<Int, Pair<String, String>>()
    var maxLength = 1

    init {
        var i = 0
        listOfPoints.forEach {
            val item = it.toPair()
            items[i++] = item
            val length =  item.second.length
            maxLength = if (length in 2..99) {
                100
            } else 20
        }

    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getItem(position: Int): Pair<String, String>? {

        return items[position]
    }

    override fun getCount(): Int {
        return listOfPoints.size
    }

    override fun getView(position: Int, convertView: View?, viewGroup: ViewGroup): View {
        val view: View
        val itemRowHolder: StopPointViewHolder
        val itemView =
            LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.list_stops, viewGroup, false)

        if (convertView == null) {
            view = itemView
            itemRowHolder = StopPointViewHolder(view)
            view.tag = itemRowHolder
        } else {
            view = convertView
            itemRowHolder = view.tag as StopPointViewHolder
        }

        itemRowHolder.textView.text = items[position]?.second
        return view
    }

    inner class StopPointViewHolder(row: View) {
        val textView: TextView

        init {
            this.textView = row.findViewById(R.id.item_netowrk_config_name)
        }
    }
}