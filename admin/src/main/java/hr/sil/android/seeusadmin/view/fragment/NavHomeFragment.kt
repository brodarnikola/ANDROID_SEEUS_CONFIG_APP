package hr.sil.android.seeusadmin.view.fragment

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import hr.sil.android.mplhuber.core.ble.DeviceStatus
import hr.sil.android.mplhuber.core.remote.model.RUnitType
import hr.sil.android.mplhuber.core.util.logger
import hr.sil.android.seeusadmin.App
import hr.sil.android.seeusadmin.R
import hr.sil.android.seeusadmin.databinding.FragmentLoginBinding
import hr.sil.android.seeusadmin.databinding.FragmentMainScreenBinding
import hr.sil.android.seeusadmin.events.DevicesUpdatedEvent
import hr.sil.android.seeusadmin.store.DeviceStore
import hr.sil.android.seeusadmin.store.model.Device
import hr.sil.android.seeusadmin.view.adapter.ScuAdapter
import hr.sil.android.view_util.extensions.hideKeyboard
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*


class NavHomeFragment : BaseFragment() {

    private val log = logger()
    private lateinit var stationAdapter: ScuAdapter

    private var filterSearchImage: Drawable? = null
    private var filterDeleteTextImage: Drawable? = null

    private var filterText: String = ""
    private var filterTextEdited: Boolean = false

    private lateinit var binding: FragmentMainScreenBinding
    private fun setDeviceItemClickListener(partItem: Device) {
        deviceItemClicked(partItem)
    }

    private fun deviceItemClicked(parcelLocker: Device) {
        clearFilter()
        val bundle = bundleOf("macAddress" to parcelLocker.macAddress)
        log.info("Sended macAddress is: " + bundle + " to String: " + bundle.toString())
        findNavController().navigate(
            R.id.home_screen_fragment_to_device_details_fragment,
            bundle
        )
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
//        val rootView = inflater.inflate(
//            R.layout.fragment_main_screen, container,
//            false
//        )

        initializeToolbarUIMainActivity(false, "", false, false, requireContext())

        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)

        binding = FragmentMainScreenBinding.inflate(layoutInflater)
        return binding.root // rootView
    }

    private fun applyFilter(devices: List<Device>): List<Device> {
        return if (filterText.isEmpty()) {
            filterTextEdited = false
            binding.ivRemoveFilterText.setImageDrawable(filterSearchImage)
            devices
        } else {
            filterTextEdited = true
            binding.ivRemoveFilterText.setImageDrawable(filterDeleteTextImage)
            val filters = filterText
            devices.filter { device ->
                device.macAddress.toLowerCase().contains(filters.toLowerCase())
                        || device.displayName.toLowerCase().contains(filters.toLowerCase())
            }
        }
    }

    var refreshTimestamp = Date()
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMplDeviceNotify(event: DevicesUpdatedEvent) {
        val currentTimeStamp = Date()
        if (currentTimeStamp.time - refreshTimestamp.time > 3000) {
            renderDeviceItems()
            refreshTimestamp = currentTimeStamp
        }
    }

    private fun renderDeviceItems() {
        val devices = getSortedDevices()
        stationAdapter.updateDevices(devices)
        handleParcelVisibility(devices)
    }

    private fun getSortedDevices(): List<Device> {
        var devices = DeviceStore.devices.values.filter { it.unitType == RUnitType.SEEUS_SCU }
        devices = applyFilter(devices)
        return devices.sortedBy { it.displayName }.sortedBy { it.bleDistance }.sortedBy { getSortIndex(it) }

    }

    private fun getSortIndex(stationDevice: Device): Int {
        return if (stationDevice.isInProximity && stationDevice.deviceStatus == DeviceStatus.REGISTERED && stationDevice.masterUnitId != -1) {
            1
        } else if (stationDevice.deviceStatus == DeviceStatus.UNREGISTERED && stationDevice.isInProximity) {
            2
        } else if (!stationDevice.isInProximity) {
            3
        } else {
            4
        }
    }

    private fun handleParcelVisibility( listOfDevices: List<Device>) {
        binding.tvTitle.visibility = if (listOfDevices.isEmpty()) View.GONE else View.VISIBLE
        if (listOfDevices.isEmpty()) {
            binding.noDevicesFound.visibility = View.VISIBLE
            binding.mainMplRecycleView.visibility = View.INVISIBLE
            if (filterTextEdited) {
                binding.noDevicesFound.setText(resources.getString(R.string.wrong_inserted_text_home_screen))
            } else {
                binding.noDevicesFound.setText(resources.getString(R.string.main_no_stations))
            }
        }
        else {
            binding.noDevicesFound.visibility = View.GONE
            binding.mainMplRecycleView.visibility = View.VISIBLE
        }
    }

    override fun onResume() {
        super.onResume()
        stationAdapter = ScuAdapter(getSortedDevices(), { partItem: Device ->
            setDeviceItemClickListener(partItem)
        })
        binding.mainMplRecycleView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.mainMplRecycleView.adapter = stationAdapter
        renderDeviceItems()

        binding.filterEdittext.afterTextChangeDelay(250) { text ->
            filterText = text.trim().toUpperCase(Locale.getDefault())
            renderDeviceItems()
        }
        binding.filterEdittext.setImeOptions(EditorInfo.IME_ACTION_DONE)

        binding.ivRemoveFilterText.setOnClickListener {
            clearFilter()
            requireActivity().hideKeyboard()
        }

        App.ref.eventBus.register(this)
        log.info("Resuming fragment")
    }

    private fun clearFilter() {
        binding.filterEdittext.setText("")
    }

    override fun onPause() {
        super.onPause()
        App.ref.eventBus.unregister(this)
    }

    override fun onStart() {
        super.onStart()

        filterSearchImage = ContextCompat.getDrawable(requireContext(), R.drawable.ic_search)
        filterDeleteTextImage = ContextCompat.getDrawable(requireContext(), R.drawable.ic_clear_search)

        val textView: TextView? = this@NavHomeFragment.activity?.findViewById(hr.sil.android.seeusadmin.R.id.mainactivity_toolbar_huber_text)
        textView?.visibility = View.GONE

        val arrowBackImage: ImageView? = this@NavHomeFragment.activity?.findViewById(hr.sil.android.seeusadmin.R.id.mainactivity_toolbar_arrow)
        arrowBackImage?.visibility = View.GONE

        val huberLogo: ImageView? = this@NavHomeFragment.activity?.findViewById(hr.sil.android.seeusadmin.R.id.mainactivity_toolbar_huber_picture)
        huberLogo?.visibility = View.VISIBLE
    }

}