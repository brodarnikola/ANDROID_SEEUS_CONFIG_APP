package hr.sil.android.seeusadmin.view.adapter

import android.view.LayoutInflater
import androidx.core.content.ContextCompat
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import hr.sil.android.mplhuber.core.ble.DeviceStatus
import hr.sil.android.mplhuber.core.util.logger
import hr.sil.android.seeusadmin.R
import hr.sil.android.seeusadmin.store.model.Device
import hr.sil.android.seeusadmin.util.ListDiffer

class ScuAdapter(val mplLocker: List<Device>, val clickListener: (Device) -> Unit) : RecyclerView.Adapter<ScuAdapter.SCUItemViewHolder>() {

    private val devices: MutableList<Device> = mplLocker.toMutableList()


    override fun onBindViewHolder(holder: SCUItemViewHolder, position: Int) {
        holder.bindItem(devices[position], clickListener)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SCUItemViewHolder {
        val itemView =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.list_home_screen_child, parent, false)
        return SCUItemViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return devices.size
    }


    val log = logger()
    fun updateDevices(updatedDevices: List<Device>) {
        val listDiff = ListDiffer.getDiff(
                devices,
                updatedDevices
        ) { old, new ->
            old.isInProximity == new.isInProximity &&
                    old.unitName == new.unitName &&
                    old.latitude == new.latitude &&
                    old.longitude == new.longitude &&
                    old.macAddress == new.macAddress &&
                    old.deviceStatus.value == new.deviceStatus.value
        }

        for (diff in listDiff) {
            when (diff) {
                is ListDiffer.DiffInserted -> {
                    devices.addAll(diff.elements)
                    log.info("notifyItemRangeInserted")
                    notifyItemRangeInserted(diff.position, diff.elements.size)
                }
                is ListDiffer.DiffRemoved -> {
                    //remove devices
                    for (i in (devices.size - 1) downTo diff.position) {
                        devices.removeAt(i)
                    }
                    log.info("notifyItemRangeRemoved")
                    notifyItemRangeRemoved(diff.position, diff.count)
                }
                is ListDiffer.DiffChanged -> {
                    devices[diff.position] = diff.newElement
                    log.info("notifyItemChanged")
                    notifyItemChanged(diff.position)

                }
            }
        }
    }


    inner class SCUItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val log = logger()
        val deviceName: TextView = itemView.findViewById(R.id.tvDeviceName)
        val deviceDistance: TextView = itemView.findViewById(R.id.tvDeviceDistance)
        val tvMacAddress: TextView = itemView.findViewById(R.id.tvMacAddress)
        var arrowNext: ImageView = itemView.findViewById(R.id.ivArrowNext)

        var devicePicture: ImageView = itemView.findViewById(R.id.ivDevicePicture)

        fun bindItem(stationDevice: Device, clickListener: (Device) -> Unit) {

            if( stationDevice.macAddress != null && stationDevice.macAddress != "" ) {
                tvMacAddress.visibility = View.VISIBLE
                tvMacAddress.text = stationDevice.macAddress
            }
            else
                tvMacAddress.visibility = View.GONE


            deviceName.text = if (stationDevice.stationName.isNotEmpty() == true) {
                stationDevice.stationName + " " + stationDevice.stopPoint
            } else if (stationDevice.unitName?.isNotEmpty() == true) {
                stationDevice.unitName
            } else {
                stationDevice.macAddress
            }

            if (stationDevice.isVirtual) {
                deviceName.text = "V ${deviceName.text}"
            }

            log.info("Station info: ${stationDevice.macAddress} ${stationDevice.masterUnitId} ${stationDevice.isInProximity} ${stationDevice.deviceStatus}")
            var unavailable = false
            val icon = if (stationDevice.isPlaceholder) {
                arrowNext.visibility = View.VISIBLE
                R.drawable.ic_station_geofencing
            }
            else if (stationDevice.isInProximity && stationDevice.deviceStatus == DeviceStatus.REGISTERED && stationDevice.masterUnitId != -1) {
                deviceDistance.visibility = View.VISIBLE
                val distanceValue = itemView.context.getString(R.string.app_generic_distance, (String.format("%.2f", stationDevice.bleDistance)
                        ?: 0.0).toString())
                deviceDistance.text = distanceValue
                arrowNext.visibility = View.VISIBLE
                R.drawable.ic_station_proximity_registered
            } else if (stationDevice.deviceStatus == DeviceStatus.UNREGISTERED && stationDevice.isInProximity) {
                deviceDistance.visibility = View.VISIBLE
                val distanceValue = itemView.context.getString(R.string.app_generic_distance, (String.format("%.2f", stationDevice.bleDistance)
                        ?: 0.0).toString())
                deviceDistance.text = distanceValue
                arrowNext.visibility = View.VISIBLE
                R.drawable.ic_station_proximity_unregistered
            } else if (!stationDevice.isInProximity) {
                deviceDistance.visibility = View.VISIBLE
                deviceDistance.text = "${stationDevice.stationLatitude} - ${stationDevice.stationLatitude}"
                arrowNext.visibility = View.VISIBLE
                R.drawable.ic_station_not_in_proximity
            } else {
                //unavailable = true
                arrowNext.visibility = View.INVISIBLE
                deviceDistance.visibility = View.GONE
                R.drawable.ic_station_unavailable
            }

            devicePicture.setImageDrawable(ContextCompat.getDrawable(itemView.context, icon))
            itemView.setOnClickListener {
                if (!unavailable) {
                    clickListener(stationDevice)
                }
            }
        }
    }
}