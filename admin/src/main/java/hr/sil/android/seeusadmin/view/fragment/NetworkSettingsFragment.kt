package hr.sil.android.seeusadmin.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.model.LatLng
import hr.sil.android.mplhuber.core.remote.WSSeeUsAdmin
import hr.sil.android.mplhuber.core.remote.model.RNetworkConfiguration
import hr.sil.android.mplhuber.core.remote.model.RPowerType
import hr.sil.android.mplhuber.core.remote.model.RStationUnitRequest
import hr.sil.android.mplhuber.core.util.logger
import hr.sil.android.mplhuber.core.util.macRealToClean
import hr.sil.android.seeusadmin.App
import hr.sil.android.seeusadmin.R
import hr.sil.android.seeusadmin.databinding.FragmentNetworkSettingsBinding
import hr.sil.android.seeusadmin.store.DeviceStore
import hr.sil.android.seeusadmin.store.model.Device
import hr.sil.android.seeusadmin.util.AppUtil
import hr.sil.android.seeusadmin.view.adapter.NetworkConfigurationAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.anko.toast

class NetworkSettingsFragment : BaseFragment() {

    private val log = logger()


    lateinit var macAddress: String

    //private val macAddress: String by lazy { intent.getStringExtra("macAddress") }


    private lateinit var selectedItem: RNetworkConfiguration
    private var device: Device? = null

    private lateinit var binding: FragmentNetworkSettingsBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreate(savedInstanceState)

//        val rootView = inflater.inflate(
//            R.layout.fragment_network_settings, container,
//            false
//        )

        initializeToolbarUIMainActivity(true, getString(R.string.main_locker_manage_network), false, false, requireContext())

        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        (activity as AppCompatActivity).supportActionBar?.setDisplayShowHomeEnabled(true)

        macAddress = arguments?.getString("masterMac", "") ?: ""
        log.info("Received masterMac address is: " + macAddress)
        device = DeviceStore.devices[macAddress]

        log.info("Network configuration id in NetworkSettingFragment is: ${device?.networkConfigurationId}")

        binding = FragmentNetworkSettingsBinding.inflate(layoutInflater)

        return binding.root // rootView

    }

    override fun onResume() {
        super.onResume()

        device = DeviceStore.devices[macAddress]
        if (device?.modemWorkingType == RPowerType.LINE) {
            binding.radioSleep.isChecked = true
            binding.radioOffline.isChecked = false
        } else {
            binding.radioSleep.isChecked = false
            binding.radioOffline.isChecked = true
        }

        binding.spinerapnNetworkSelection.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {
                }

                override fun onItemSelected(
                    adapterView: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    selectedItem = adapterView?.getItemAtPosition(position) as RNetworkConfiguration
                }
            }

        binding.psmMode.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.radioSleep.isEnabled = false
                binding.radioOffline.isEnabled = false
            } else {
                binding.radioSleep.isEnabled = true
                binding.radioOffline.isEnabled = true
            }
        }

        binding.btnSaveChanges.setOnClickListener {
            binding.progressBarNetworkChange.visibility = View.VISIBLE
            binding.btnSaveChanges.visibility = View.GONE
            lifecycleScope.launch {
                var networkConfig = false
                val communicator =
                    device?.createBLECommunicator(requireContext())
                if (communicator?.connect() == true) {
                    log.info("Successfully connected: ${selectedItem.name} , ${selectedItem.apnPass}, ${selectedItem.apnUrl}, ${selectedItem.apnUser}, ${selectedItem.networkMode} , ${selectedItem.customerName}, ${selectedItem.id}")
                    val nrOfSeconds = if (binding.radioOffline.isChecked) 60L else 21600L
                    if (!communicator.writeNetworkConfiguration(
                            selectedItem,
                            null,
                            nrOfSeconds,
                            binding.psmMode.isChecked
                        )
                    ) {
                        log.error("Error in registration!")
                    } else {
                        networkConfig = true
                    }

                    communicator.applyConfiguration()
                    communicator.disconnect()
                    changeNetworkConfigurationInBackend()
                    withContext(Dispatchers.Main) {
                        binding.progressBarNetworkChange.visibility = View.GONE
                        binding.btnSaveChanges.visibility = View.VISIBLE
                        if (networkConfig) {
                            App.ref.toast(R.string.successfull_saved_network_configuration)
                        } else {
                            App.ref.toast(R.string.registration_error)
                        }
                    }
                } else {
                    log.error("Error while connecting!!")
                }
            }
        }
    }

    private suspend fun changeNetworkConfigurationInBackend() {

        val request = RStationUnitRequest()
        request.stationId = device?.stationId
        request.latitude = device?.latitude
        request.longitude = device?.longitude
        request.radiusMeters = device?.radius ?: 0
        request.name = device?.unitName
        request.polygon = device?.polygon as List<LatLng>
        request.stopPoint = device?.stopPoint
        request.epdTypeId = device?.epdId?: 0
        request.networkConfigurationId = selectedItem.id
        request.modemWorkingType = if (binding.radioOffline.isChecked) RPowerType.BATTERY.name else RPowerType.LINE.name
        if (WSSeeUsAdmin.modifyStationUnit(macAddress.macRealToClean(), request) != null)
            AppUtil.refreshCache()
    }

    override fun onStart() {
        super.onStart()
        lifecycleScope.launch {
            val list = WSSeeUsAdmin.getNetworkConfigurations() ?: listOf()
            withContext(Dispatchers.Main) {
                binding.spinerapnNetworkSelection.adapter = NetworkConfigurationAdapter(list)
                if (list.size > 0) {
                    val selectedIndex = list.indexOfFirst { it.id == device?.networkConfigurationId }
                    if( selectedIndex > 0 )
                        binding.spinerapnNetworkSelection.setSelection(selectedIndex)
                    else
                        binding.spinerapnNetworkSelection.setSelection(0)
                }
            }
        }
    }

}