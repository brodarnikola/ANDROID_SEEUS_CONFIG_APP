package hr.sil.android.seeusadmin.view.adapter

import android.content.Context
import androidx.core.content.ContextCompat
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import hr.sil.android.mplhuber.core.ble.DeviceStatus
import hr.sil.android.mplhuber.core.remote.WSSeeUsAdmin
import hr.sil.android.mplhuber.core.util.logger
import hr.sil.android.mplhuber.core.util.macRealToClean
import hr.sil.android.seeusadmin.R
import hr.sil.android.seeusadmin.cache.status.ActionStatusType
import hr.sil.android.seeusadmin.data.DeleteButtonInterface
import hr.sil.android.seeusadmin.data.RButtonDataUiModel
import hr.sil.android.seeusadmin.store.DeviceStore
import hr.sil.android.seeusadmin.store.model.Device
import hr.sil.android.seeusadmin.util.ListDiffer
import hr.sil.android.seeusadmin.view.activity.MainActivity1
import hr.sil.android.seeusadmin.view.dialog.DeleteButtonFromStation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean


class ButtonAdapter(peripherals: List<RButtonDataUiModel>, val masterMac: String, val ctx: Context) : RecyclerView.Adapter<ButtonAdapter.PeripheralItemViewHolder>() {

    private val devices: MutableList<RButtonDataUiModel> = peripherals.toMutableList()

    override fun onBindViewHolder(holder: PeripheralItemViewHolder, position: Int) {
        holder.bindItem(devices[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PeripheralItemViewHolder {
        val itemView =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.list_buttons, parent, false)
        return PeripheralItemViewHolder(itemView)
    }


    override fun getItemCount() = devices.size



    fun updateDevices(updatedDevices: List<RButtonDataUiModel>) {
        val listDiff = ListDiffer.getDiff(
                devices,
                updatedDevices,
                { old, new -> old == new })

        for (diff in listDiff) {
            when (diff) {
                is ListDiffer.DiffInserted -> {
                    devices.addAll(diff.elements)
                    notifyItemRangeInserted(diff.position, diff.elements.size)
                }
                is ListDiffer.DiffRemoved -> {
                    //remove devices
                    for (i in (devices.size - 1) downTo diff.position) {
                        devices.removeAt(i)
                    }
                    notifyItemRangeRemoved(diff.position, diff.count)
                }
                is ListDiffer.DiffChanged -> {
                    devices[diff.position] = diff.newElement
                    notifyItemChanged(diff.position)
                }
            }
        }
    }


    inner class PeripheralItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), DeleteButtonInterface {

        val log = logger()
        val buttonPicture: ImageView = itemView.findViewById(R.id.ivButtonPicture)
        val nameMacAddress: TextView = itemView.findViewById(R.id.tvMacAddress)
        val proximityStatus: TextView = itemView.findViewById(R.id.tvProximityStatus)
        val registrationStatus: TextView = itemView.findViewById(R.id.tvRegistrationStatus)
        val add: ImageView = itemView.findViewById(R.id.ivButtonAdd)
        val delete: ImageView = itemView.findViewById(R.id.ivButtonDelete)
        val progressBar: ProgressBar = itemView.findViewById(R.id.progressBarButtons)

        val isConnecting: AtomicBoolean = AtomicBoolean(false)

        fun bindItem(device: RButtonDataUiModel) {

            log.info("Master mac is ${masterMac}, inside of button adapter")

            nameMacAddress.text = itemView.context.getString(R.string.manage_peripherals_title, device.mac)
            if (!isConnecting.get()) {

                proximityStatus.text = if (device.isInProximity) itemView.context.getString(R.string.manage_peripherals_inproximity) else itemView.context.getString(R.string.manage_peripherals_not_inproximity)
                when (device.status) {
                    DeviceStatus.UNREGISTERED -> {
                        add.visibility = View.VISIBLE
                        delete.visibility = View.GONE
                        registrationStatus.text = itemView.context.getString(R.string.manage_peripherals_unregistered)
                        add.setOnClickListener {
                            addItemPeripheral(itemView.context, device, progressBar)
                        }
                        buttonPicture.setImageDrawable(ContextCompat.getDrawable(itemView.context, R.drawable.ic_peripheral_unregistered))
                    }
                    DeviceStatus.REGISTERED -> {
                        add.visibility = View.GONE
                        registrationStatus.text = itemView.context.getString(R.string.manage_peripherals_registered)
                        delete.visibility = View.VISIBLE
                        delete.setOnClickListener {
                            //deleteItemPeripheral(itemView.context, parcelLocker = device, progressBar = progressBar, deleteButton = delete)
                            val deleteButtonFromStation =
                                DeleteButtonFromStation(
                                    masterMac,
                                    ctx,
                                    device,
                                    progressBar,
                                    delete,
                                    this@PeripheralItemViewHolder
                                )
                            deleteButtonFromStation.show((ctx as MainActivity1).supportFragmentManager, "")
                        }
                        buttonPicture.setImageDrawable(ContextCompat.getDrawable(itemView.context, R.drawable.ic_peripheral_registered))
                    }

                    DeviceStatus.DELETE_PENDING -> {
                        add.visibility = View.GONE
                        delete.visibility = View.GONE
                        registrationStatus.text = itemView.context.getString(R.string.delete_pending)
                        buttonPicture.setImageDrawable(ContextCompat.getDrawable(itemView.context, R.drawable.ic_peripheral_registered))
                    }

                    DeviceStatus.REGISTRATION_PENDING -> {
                        add.visibility = View.GONE
                        delete.visibility = View.GONE
                        registrationStatus.text = itemView.context.getString(R.string.registration_pending)
                        buttonPicture.setImageDrawable(ContextCompat.getDrawable(itemView.context, R.drawable.ic_peripheral_unregistered))
                    }

                    DeviceStatus.UNKNOWN -> {}
                    DeviceStatus.REJECTED -> {}
                    DeviceStatus.NEW -> {}
                }
            }
        }


        override fun deleteButtonFromSCU(masterMac: String, ctx: Context, parcelLocker: RButtonDataUiModel, progressBar: ProgressBar, deleteButton: ImageView) {
            val device = DeviceStore.devices[masterMac]
            if (device?.isInProximity == true) {
                isConnecting.set(true)
                TransitionManager.beginDelayedTransition(itemView as ViewGroup)
                progressBar.visibility = View.VISIBLE
                deleteButton.visibility = View.GONE
                GlobalScope.launch {
                    bleDeletePeripheral(device, ctx, parcelLocker, itemView as ViewGroup)
                }
            } else {
                Toast.makeText(ctx, ctx.getString(R.string.main_locker_ble_connection_error), Toast.LENGTH_SHORT).show()
                //App.ref.toast(ctx.getString(R.string.main_locker_ble_connection_error))
            }
        }

        /*private fun deleteItemPeripheral(ctx: Context, parcelLocker: RButtonDataUiModel, progressBar: ProgressBar, deleteButton: ImageView) {
            ctx.alert {
                val message = ctx.getString(R.string.key_sharing_delete_key_message)
                positiveButton(R.string.app_generic_confirm) {
                    val device = DeviceStore.devices[masterMac]
                    if (device?.isInProximity == true) {
                        isConnecting.set(true)
                        TransitionManager.beginDelayedTransition(itemView as ViewGroup)
                        progressBar.visibility = View.VISIBLE
                        deleteButton.visibility = View.GONE
                        GlobalScope.launch {
                            bleDeletePeripheral(device, ctx, parcelLocker, itemView)

                        }
                    } else {
                        App.ref.toast(ctx.getString(R.string.main_locker_ble_connection_error))
                    }
                    Unit
                }
                negativeButton(android.R.string.cancel) {

                }
                onCancelled {

                }
            }.show()
        }*/

        suspend private fun bleDeletePeripheral(device: Device?, ctx: Context, button: RButtonDataUiModel, itemView: ViewGroup) {
            val communicator = device?.createBLECommunicator(ctx)
            if (communicator != null && communicator.connect()) {
                if (communicator.deregisterSlave(button.mac)) {
                    withContext(Dispatchers.Main) {
                        modifyStatusChange(button.id, button.mac, ActionStatusType.BUTTON_DEREGISTRATION)
                    }
                    log.info("Successfully deregister button device ${button.mac} ")

                    if (WSSeeUsAdmin.deleteButtonFromStation(button.mac.macRealToClean())) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(ctx, ctx.getString(R.string.successfully_saved_data, button.mac), Toast.LENGTH_SHORT).show()

                            //App.ref.toast(ctx.getString(R.string.successfully_saved_data, button.mac))
                        }
                    }
                }
            } else {
                log.error("Error while connecting the peripheral ")
            }
            withContext(Dispatchers.Main) {
                TransitionManager.beginDelayedTransition(itemView)
                progressBar.visibility = View.GONE
                notifyDataSetChanged()

            }
            communicator?.disconnect()
            isConnecting.set(false)
        }

        private fun addItemPeripheral(ctx: Context, button: RButtonDataUiModel, progressBar: ProgressBar) {
            if (!button.isInProximity) {
                Toast.makeText(ctx, ctx.getString(R.string.main_locker_ble_connection_error), Toast.LENGTH_SHORT).show()

                //itemView.context.toast(R.string.main_locker_ble_connection_error)
                return
            }

            isConnecting.set(true)
            TransitionManager.beginDelayedTransition(itemView as ViewGroup)
            progressBar.visibility = View.VISIBLE
            add.visibility = View.GONE
            GlobalScope.launch {
                val communicator = DeviceStore.devices[masterMac]?.createBLECommunicator(itemView.context)

                if (communicator != null && communicator.connect()) {
                    if (communicator.registerButton(button.mac)) {
                        withContext(Dispatchers.Main) {
                            log.info("")
                            modifyStatusChange(button.id, button.mac, ActionStatusType.BUTTON_REGISTRATION)

                        }
                        log.info("Successfully send registration request for slave device master ${masterMac} slave - ${button.mac} ")
                    }
                } else {
                    log.error("Error while connecting the peripheral ${button.mac}")
                }
                withContext(Dispatchers.Main) {
                    log.info("Finish adding peripheral ${button.mac}")
                    progressBar.visibility = View.GONE
                    notifyDataSetChanged()
                }
                communicator?.disconnect()
                isConnecting.set(false)
                log.info("Connecting to Backend ${masterMac.macRealToClean()} ${button.mac.macRealToClean()}")
                if (WSSeeUsAdmin.addButtonToStation(masterMac.macRealToClean(), button.mac.macRealToClean())) {

                    withContext(Dispatchers.Main) {
                        Toast.makeText(ctx, ctx.getString(R.string.successfully_saved_data, button.mac), Toast.LENGTH_SHORT).show()

                        //App.ref.toast(ctx.getString(R.string.successfully_saved_data, button.mac))
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(ctx, "Error with button communication ", Toast.LENGTH_SHORT).show()

                        //App.ref.toast("Error with button communication ")
                    }
                }
            }
        }

        private fun modifyStatusChange(id: Int, macAddress: String, status: ActionStatusType) {
            val item = devices.find { v -> v.mac == macAddress }
            item?.status = if (status == ActionStatusType.BUTTON_DEREGISTRATION) DeviceStatus.DELETE_PENDING else DeviceStatus.REGISTRATION_PENDING
            notifyDataSetChanged()
        }

    }

}