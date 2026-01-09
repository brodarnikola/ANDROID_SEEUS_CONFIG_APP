package hr.sil.android.seeusadmin.view.fragment


import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import hr.sil.android.mplhuber.core.ble.DeviceStatus
import hr.sil.android.mplhuber.core.remote.WSSeeUsAdmin
import hr.sil.android.mplhuber.core.remote.model.RNetworkConfiguration
import hr.sil.android.mplhuber.core.remote.model.RStationUnitRequest
import hr.sil.android.mplhuber.core.util.logger
import hr.sil.android.mplhuber.core.util.macRealToClean
import hr.sil.android.seeusadmin.App
import hr.sil.android.seeusadmin.BuildConfig
import hr.sil.android.seeusadmin.R
import hr.sil.android.seeusadmin.cache.dto.CRegistration
import hr.sil.android.seeusadmin.cache.status.ActionStatusHandler
import hr.sil.android.seeusadmin.cache.status.ActionStatusKey
import hr.sil.android.seeusadmin.cache.status.ActionStatusType
import hr.sil.android.seeusadmin.databinding.FragmentDeviceDetailsBinding
import hr.sil.android.seeusadmin.events.DevicesUpdatedEvent
import hr.sil.android.seeusadmin.store.DeviceStore
import hr.sil.android.seeusadmin.store.model.Device
import hr.sil.android.seeusadmin.util.AppUtil
import hr.sil.android.seeusadmin.util.backend.UserUtil
import hr.sil.android.seeusadmin.view.activity.MainActivity
import hr.sil.android.seeusadmin.view.adapter.NetworkConfigurationAdapter
import hr.sil.android.seeusadmin.view.dialog.DeleteStationDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean


class StationItemDetailsFragment : BaseFragment() {

    val log = logger()
    lateinit var macAddress: String

    private var connecting: AtomicBoolean = AtomicBoolean(false)
    private lateinit var selectedItem: RNetworkConfiguration
 
    val PREF_LATITUDE = "PREF_LATITUDE"
    val PREF_LONGITUDE = "PREF_LONGITUDE"

    var latitudeFromGoogleMaps: Double = 0.0
    var longitudeFromGoogleMaps: Double = 0.0

    private var device: Device? = null


    private lateinit var binding: FragmentDeviceDetailsBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
//        val rootView = inflater.inflate(
//            R.layout.fragment_device_details, container,
//            false
//        )

        binding = FragmentDeviceDetailsBinding.inflate(layoutInflater)

        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        (activity as AppCompatActivity).supportActionBar?.setDisplayShowHomeEnabled(true)

        macAddress = arguments?.getString("macAddress", "") ?: ""
        latitudeFromGoogleMaps = arguments?.getDouble("latitude", 0.0) ?: 0.0
        longitudeFromGoogleMaps = arguments?.getDouble("longitude", 0.0) ?: 0.0
        log.info("Received mac address is: " + macAddress)
        device = DeviceStore.devices[macAddress]

        log.info("Dispaly name: " + device?.displayName + " device status is: ${ device?.deviceStatus}")
        val toolbarDeviceName =
            if (device?.displayName != "" && device?.deviceStatus == DeviceStatus.REGISTERED ) device?.displayName else getString(R.string.manage_peripherals_unregistered)
        initializeToolbarUIMainActivity(true, toolbarDeviceName ?: "", false, false, requireContext())

        return binding.root // rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val textView: TextView? = this@StationItemDetailsFragment.activity?.findViewById(hr.sil.android.seeusadmin.R.id.mainactivity_toolbar_huber_text)
        textView?.visibility = View.VISIBLE
        textView?.text = device?.unitName

        val arrowBackImage: ImageView? = this@StationItemDetailsFragment.activity?.findViewById(hr.sil.android.seeusadmin.R.id.mainactivity_toolbar_arrow)
        arrowBackImage?.visibility = View.VISIBLE

        arrowBackImage?.setOnClickListener {
            if (fragmentManager?.backStackEntryCount ?: 1 > 0) {
                fragmentManager?.popBackStack()
            }
        }

        val huberLogo: ImageView? = this@StationItemDetailsFragment.activity?.findViewById(hr.sil.android.seeusadmin.R.id.mainactivity_toolbar_huber_picture)
        huberLogo?.visibility = View.GONE
//        binding.spinerapnNetworkSelection.onItemSelectedListener {
//            onItemSelected { adapterView, view, position, id ->
//                selectedItem = adapterView?.getItemAtPosition(position) as RNetworkConfiguration
//            }
//
//            onNothingSelected {
//
//            }
//        }

        binding.btnRegisterDevice.setOnClickListener {
            val ctx = context ?: return@setOnClickListener
            val device = device
            lifecycleScope.launch {
                if (validate() && binding.spinerapnNetworkSelection.selectedItem != null && device?.isInProximity == true) {
                    connecting.set(true)
                    val customerId = UserUtil.user?.customerId

                    if (customerId != null) {
                        binding.progressBarRegisterDevice.visibility = View.VISIBLE
                        binding.btnRegisterDevice.visibility = View.GONE
                        val communicator =
                            device.createBLECommunicator(this@StationItemDetailsFragment.activity as Context)
                        if (communicator.connect()) {
                            log.info("Successfully connected $customerId ,${selectedItem.apnUrl}, $macAddress  ")

                            if (!communicator.registerMaster(
                                    customerId,
                                    selectedItem,
                                    null,
                                    60L,
                                    false
                                )
                            ) {
                                Toast.makeText(
                                    requireContext(),
                                    requireContext().getString(R.string.main_locker_registration_error),
                                    Toast.LENGTH_SHORT
                                ).show()

                                //App.ref.toast(ctx.getString(R.string.main_locker_registration_error))
                                log.error("Error in registration!")
                                handleRegistrationLoadingUI(false)
                            } else {
                                Toast.makeText(
                                    requireContext(),
                                    requireContext().getString(R.string.main_locker_registration_started),
                                    Toast.LENGTH_SHORT
                                ).show()

                                //App.ref.toast(ctx.getString(R.string.main_locker_registration_started))
                                binding.progressBarRegisterDevice.visibility = View.GONE
                                handleRegistrationProcessUI(true)
                                val statusKey = ActionStatusKey()
                                statusKey.macAddress = macAddress
                                statusKey.statusType = ActionStatusType.MASTER_REGISTRATION
                                statusKey.keyId =
                                    macAddress + ActionStatusType.MASTER_REGISTRATION.name
                                //ActionStatusHandler.actionStatusDb.put(statusKey)

                                log.info("Added: ${macAddress + PREF_LATITUDE} ${binding.etLatitude.text}")
                                log.info("Added: ${macAddress + PREF_LONGITUDE} ${binding.etLongitude.text}")
                                val lat = binding.etLatitude.text.toString().toDouble()
                                val long = binding.etLongitude.text.toString().toDouble()
//                                DataCache.setRegistrationStatus(
//                                    CRegistration(
//                                        macAddress,
//                                        macAddress,
//                                        "",
//                                        lat,
//                                        long,
//                                        selectedItem.id
//                                    )
//                                )
                            }
                            communicator.disconnect()
                        } else {
                            handleRegistrationLoadingUI(false)
                            Toast.makeText(
                                requireContext(),
                                requireContext().getString(R.string.main_locker_registration_error),
                                Toast.LENGTH_SHORT
                            ).show()

                            //App.ref.toast(ctx.getString(R.string.main_locker_registration_error))
                            log.error("Error while connecting!!")
                        }
                    }
                    connecting.set(false)
                } else {
                    Toast.makeText(
                        requireContext(),
                        requireContext().getString(R.string.main_locker_registration_error),
                        Toast.LENGTH_SHORT
                    ).show()

                    //App.ref.toast(ctx.getString(R.string.main_locker_registration_error))
                }
            }

        }

        binding.clLedTest.setOnClickListener {
            val ctx = requireContext()
            binding.progressBarLedTest.visibility = View.VISIBLE
            binding.clLedTest.visibility = View.GONE
            val device = DeviceStore.devices[macAddress]
            GlobalScope.launch {
                if (device?.isInProximity == true) {
                    val communicator =
                        device.createBLECommunicator(this@StationItemDetailsFragment.context as Context)
                    if (communicator.connect() == true) {
                        log.info("Successfully connected $macAddress")
                        val bleResponseStopTheBusTest = communicator.stopTheBusTest()
                        if (!bleResponseStopTheBusTest) {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(requireContext(), ctx.getString(R.string.main_locker_force_open_update_error), Toast.LENGTH_SHORT).show()

                                //App.ref.toast(ctx.getString(R.string.main_locker_force_open_update_error))
                                log.error("Error in force open!")
                            }
                        } else {
                            log.info("Successfully stop the bus activated $macAddress")
                        }

                        val bleResponseAssistanceTest = communicator.assistanceTest()
                        if (!bleResponseAssistanceTest) {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(requireContext(), "Assistance error", Toast.LENGTH_SHORT).show()

                                //App.ref.toast("Assistance error")
                                log.error("Error in assistance!")
                            }
                        } else {
                            log.info("Successfully stop the bus activated $macAddress")
                        }
                        withContext(Dispatchers.Main) {

                            binding.progressBarLedTest.visibility = View.GONE
                            binding.clLedTest.visibility = View.VISIBLE
                            communicator.disconnect()
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(requireContext(), ctx.getString(R.string.main_locker_ble_connection_error), Toast.LENGTH_SHORT).show()

                            //App.ref.toast(ctx.getString(R.string.main_locker_ble_connection_error))
                            log.error("Error while connecting the device")
                            communicator.disconnect()
                        }
                    }
                } else {
                    log.info("Device not in proximity")
                    if (device?.masterUnitMac != null) {
                        WSSeeUsAdmin.simulateStationAction(
                            device.masterUnitMac.macRealToClean(),
                            "ACTION_STOP"
                        )
                        WSSeeUsAdmin.simulateStationAction(
                            device.masterUnitMac.macRealToClean(),
                            "ACTION_ASSIST"
                        )
                        withContext(Dispatchers.Main) {
                            binding.progressBarLedTest.visibility = View.GONE
                            binding.clLedTest.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }

        binding.clRebootLayout.setOnClickListener {
            binding.progressBarReboot.visibility = View.VISIBLE
            binding.clRebootLayout.visibility = View.GONE
            GlobalScope.launch {
                val ctx = requireContext()
                val communicator =
                    device?.createBLECommunicator(this@StationItemDetailsFragment.context as Context)
                if (communicator?.connect() == true) {
                    log.info("Successfully connected $macAddress")
                    val bleResponse = communicator.rebootSystem()
                    if (!bleResponse) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(requireContext(), requireContext().getString(R.string.main_locker_force_open_update_error), Toast.LENGTH_SHORT).show()

                            //App.ref.toast(ctx.getString(R.string.main_locker_force_open_update_error))
                            log.error("Error in force open!")
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            log.info("Successfully stop the bus activated $macAddress")
                        }
                    }
                    withContext(Dispatchers.Main) {
                        binding.progressBarReboot.visibility = View.GONE
                        binding.clRebootLayout.visibility = View.VISIBLE
                    }

                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), requireContext().getString(R.string.main_locker_ble_connection_error), Toast.LENGTH_SHORT).show()

                        //App.ref.toast(ctx.getString(R.string.main_locker_ble_connection_error))
                        log.error("Error while connecting the device")
                    }
                }
                communicator?.disconnect()
            }
        }

        binding.clDebugLayout.setOnClickListener {
            val ctx =  requireContext()
            binding.progressBarDebug.visibility = View.VISIBLE
            binding.clDebugLayout.visibility = View.GONE
            GlobalScope.launch {
                val communicator =
                    device?.createBLECommunicator(this@StationItemDetailsFragment.context as Context)
                if (communicator?.connect() == true) {
                    log.info("Successfully connected $macAddress")
                    val bleResponse = communicator.setSystemInDebugMode()
                    if (!bleResponse) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(requireContext(), requireContext().getString(R.string.main_locker_debug_set_error), Toast.LENGTH_SHORT).show()

                            //App.ref.toast(ctx.getString(R.string.main_locker_debug_set_error))
                            log.error("Error in seting debug mode open!")
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(requireContext(), requireContext().getString(R.string.main_locker_debug_set_success), Toast.LENGTH_SHORT).show()

                            //App.ref.toast(ctx.getString(R.string.main_locker_debug_set_success))
                            log.info("Debug mode successfully activated $macAddress")
                        }
                    }
                    withContext(Dispatchers.Main) {
                        binding.progressBarDebug.visibility = View.GONE
                        binding.clDebugLayout.visibility = View.VISIBLE
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), requireContext().getString(R.string.main_locker_ble_connection_error), Toast.LENGTH_SHORT).show()

                        //App.ref.toast(ctx.getString(R.string.main_locker_ble_connection_error))
                        log.error("Error while connecting the device")
                    }
                }
                communicator?.disconnect()
            }
        }

        binding.clResetLayout.setOnClickListener {
            val deleteStationDialog = DeleteStationDialog(macAddress, device)
            deleteStationDialog.show(
                (requireContext() as MainActivity).supportFragmentManager, ""
            )
        }

        binding.clStationSettings.setOnClickListener {
            val bundle = bundleOf("masterMac" to macAddress)
            log.info("Sended masterMacAddress to station settings is: " + bundle + " to String: " + bundle.toString())
            findNavController().navigate(
                R.id.device_details_fragment_to_station_settings_fragment,
                bundle
            )
        }

        binding.clManageButtons.setOnClickListener {
            val bundle = bundleOf("masterMac" to macAddress)
            log.info("Sended masterMacAddress to manage button is: " + bundle + " to String: " + bundle.toString())
            findNavController().navigate(
                R.id.device_details_fragment_to_manage_buttons_fragment,
                bundle
            )
        }

        binding.clNetworkLayout.setOnClickListener {
            val bundle = bundleOf("masterMac" to macAddress)
            log.info("Sended masterMacAddress to locker settings is: " + bundle + " to String: " + bundle.toString())
            findNavController().navigate(
                R.id.device_details_fragment_to_network_settings_fragment,
                bundle
            )
        }

        val device = device
        if( device?.deviceStatus == null || device?.deviceStatus == DeviceStatus.UNREGISTERED || device?.deviceStatus == DeviceStatus.UNKNOWN )
            binding.etLatitude.requestFocus()

        if( latitudeFromGoogleMaps != 0.0 && longitudeFromGoogleMaps != 0.0 ) {
            binding.etLatitude.setText(latitudeFromGoogleMaps.toString())
            binding.etLongitude.setText(longitudeFromGoogleMaps.toString())
        }

        setMplViewDetails()
    }

    override fun onStart() {
        super.onStart()

        lifecycleScope.launch {
            val list = WSSeeUsAdmin.getNetworkConfigurations() ?: listOf()
            withContext(Dispatchers.Main) {
                log.info("${list.joinToString { it.name }}")
                if( binding.spinerapnNetworkSelection != null && list.isNotEmpty() )
                    binding.spinerapnNetworkSelection.adapter = NetworkConfigurationAdapter(list)
            }
        }
    }

    private fun handleRegistrationProcessUI(isRegistrationPending: Boolean) {
        if (isRegistrationPending) {
            binding.btnRegisterDevice.visibility = View.GONE
            binding.tvRegisterInProgress.visibility = View.VISIBLE
        } else {
            binding.btnRegisterDevice.visibility = View.VISIBLE
            binding.tvRegisterInProgress.visibility = View.GONE
        }
    }

    private fun handleRegistrationLoadingUI(isSuccessfullyConnected: Boolean) {
        if (isSuccessfullyConnected) {
            binding.btnRegisterDevice.visibility = View.GONE
            binding.progressBarRegisterDevice.visibility = View.GONE
        } else {
            binding.btnRegisterDevice.visibility = View.VISIBLE
            binding.progressBarRegisterDevice.visibility = View.GONE
        }
    }

    private fun setMplViewDetails() {

        binding.clNetworkLayout.visibility = if (device?.isInProximity == true) {
            View.VISIBLE
        } else View.GONE

        binding.clResetLayout.visibility = if (device?.isInProximity == true) {
            View.VISIBLE
        } else View.GONE

        binding.clRebootLayout.visibility = if (device?.isInProximity == true) {
            View.VISIBLE
        } else View.GONE

        binding.clDebugLayout.visibility = if (device?.isInProximity == true) {
            View.VISIBLE
        } else View.GONE

        binding.clLedTest.visibility = if (BuildConfig.DEBUG) View.VISIBLE
        else if (device?.isInProximity == true) {
            View.VISIBLE
        } else {
            View.GONE
        }

        val battery =  if (device == null || device?.batteryVoltage == null) " -" else device?.batteryVoltage
        binding.tvBatteryValue.text =  " " + battery

        val macAddressPlusString = device?.macAddress
        binding.tvMacAddressValue.text =  " " + macAddressPlusString

        binding.tvModemStatusValue.text = device?.mplMasterModemStatus.toString()
        val nameTitle = if (device?.stationName?.isNotEmpty() == true) device?.stationName
        else if (device?.unitName?.isNotEmpty() == true) device?.unitName else "-"

        binding.tvDeviceNameValue.text = ": " + nameTitle

        val queueSize = if (device == null) " -" else device?.mplMasterModemQueueSize
        binding.tvQueueStatus.text = " " + queueSize

        val modemRssi = if (device == null || device?.modemRssi == "NaN") " -" else device?.modemRssi
        binding.tvRSSIvalue.text =  " " + modemRssi

        val modemRat = if (device == null || device?.modemRat == "NaN") " -" else device?.modemRat
        binding.tvRatValue.text =  " " + modemRat

        log.info("Device version is: ${device?.stmVersion.toString()}")
        val stmV = if (device == null || device?.stmVersion == "") " -" else device?.stmVersion.toString()
        binding.tvStmVersionValue.text = " " + stmV.substringBeforeLast(".")

        binding.tvNumberOfLockerValue.text =  " " + device?.numOfButtons

        when (device?.deviceStatus) {
            null, DeviceStatus.UNREGISTERED, DeviceStatus.UNKNOWN, DeviceStatus.REGISTRATION_PENDING -> {
                log.info("UNREGISTERED")
                binding.clUnregistereWrapper.visibility = View.VISIBLE
                binding.clRegisteredWrapper.visibility = View.GONE
                if (device != null) {
                    binding.btnRegisterDevice.isEnabled = true
                    GlobalScope.launch {
                        // TODO: Handle this -> val mplActionStatus = ActionStatusHandler.actionStat
//                        val mplActionStatus = ActionStatusHandler.actionStatusDb.get(device?.macAddress + ActionStatusType.MASTER_REGISTRATION)
//                        withContext(Dispatchers.Main) {
//                            if (!connecting.get())
//                                handleRegistrationProcessUI(mplActionStatus != null)
//                        }
                    }
                    openGoogleMapForRegisteringDevice()
                } else {
                    binding.btnRegisterDevice.isEnabled = false
                }
            }
            else -> {

                binding.clUnregistereWrapper.visibility = View.GONE
                binding.clRegisteredWrapper.visibility = View.VISIBLE

//                val registrationDb = DataCache.getRegistrationStatusDB().find { it.masterUnitMac == macAddress }
//                if (registrationDb != null) {
//                    log.info("REGISTERING PENDING")
//                    GlobalScope.launch {
//                        val request = RStationUnitRequest()
//                        request.latitude = registrationDb.lat
//                        request.longitude = registrationDb.long
//                        request.networkConfigurationId = registrationDb.networkConfigurationId
//
//                        if (WSSeeUsAdmin.modifyStationUnit(macAddress.macRealToClean(), request) != null) {
//                            AppUtil.refreshCache()
//                            //DataCache.removeRegistrationStatus(macAddress)
//                            withContext(Dispatchers.Main) {
//                                //App.ref.toast(R.string.successfully_updated)
//                            }
//                        } else {
//                            withContext(Dispatchers.Main) {
//                                //App.ref.toast(R.string.error_updating_mpl)
//                            }
//                        }
//                    }
//                }


            }
        }
    }

    private fun openGoogleMapForRegisteringDevice() {
        binding.llMap.setOnClickListener {
            val bundle = bundleOf(
                "macAddress" to macAddress
            )
            findNavController().navigate(
                R.id.device_details_fragment_to_google_maps_fragment,
                bundle
            )
        }
    }

    private suspend fun validate(): Boolean {
        return withContext(Dispatchers.Main) {
            var validated = true
            if (!validateLatitude()) validated = false
            if (!validateLongitude()) validated = false

            return@withContext validated
        }
    }

    private fun validateLatitude(): Boolean {
        when {
            binding.etLatitude.text.toString().isBlank() -> {
                binding.tvLatitudeError.visibility = View.VISIBLE
                binding.tvLatitudeError.text = getString(R.string.locker_settings_error_name_empty_warning)
                return false
            }
            binding.etLatitude.text?.get(0)?.toString() ?: "" == "." -> {
                binding.tvLatitudeError.visibility = View.VISIBLE
                binding.tvLatitudeError.text = getString(R.string.scu_latitude_dot_first_place)
                return false
            }
            !binding.etLatitude.text.toString().contains(".") -> {
                binding.tvLatitudeError.visibility = View.VISIBLE
                binding.tvLatitudeError.text = getString(R.string.scu_latitude_dot_does_not_exist)
                return false
            }
            binding.etLatitude.text.toString().contains(".") -> {
                val splitLatitude = binding.etLatitude.text.toString().split(".")
                if( splitLatitude.size < 1 ) {
                    binding.tvLatitudeError.visibility = View.VISIBLE
                    binding.tvLatitudeError.text = getString(R.string.scu_latitude_no_characters_after_dot)
                    return false
                }
                else {
                    if( splitLatitude[0].toFloat() >= 90.0f ) {
                        binding.tvLatitudeError.visibility = View.VISIBLE
                        binding.tvLatitudeError.text = getString(R.string.scu_latitude_value_to_big)
                        return false
                    }
                    else if( splitLatitude[0].toFloat() <= -90.0f  ) {
                        binding.tvLatitudeError.visibility = View.VISIBLE
                        binding.tvLatitudeError.text = getString(R.string.scu_latitude_value_to_small)
                        return false
                    }
                    else if( splitLatitude[1].length < 2 ) {
                        binding.tvLatitudeError.visibility = View.VISIBLE
                        binding.tvLatitudeError.text = getString(R.string.scu_latitude_value_to_small_charachters)
                        return false
                    }
                    else if( splitLatitude[1].length > 8 ) {
                        binding.tvLatitudeError.visibility = View.VISIBLE
                        binding.tvLatitudeError.text = getString(R.string.scu_latitude_value_to_many_charachters)
                        return false
                    }
                    else {
                        binding.tvLatitudeError.visibility = View.GONE
                        return true
                    }
                }
            }
        }
        return true
    }

    private fun validateLongitude(): Boolean {
        when {
            binding.etLongitude.text.toString().isBlank() -> {
                binding.tvLongitudeError.visibility = View.VISIBLE
                binding.tvLongitudeError.text = getString(R.string.locker_settings_error_name_empty_warning)
                return false
            }
            binding.etLongitude.text?.get(0)?.toString() ?: "" == "." -> {
                binding.tvLongitudeError.visibility = View.VISIBLE
                binding.tvLongitudeError.text = getString(R.string.scu_longitude_dot_first_place)
                return false
            }
            !binding.etLongitude.text.toString().contains(".") -> {
                binding.tvLongitudeError.visibility = View.VISIBLE
                binding.tvLongitudeError.text = getString(R.string.scu_longitude_dot_does_not_exist)
                return false
            }
            binding.etLongitude.text.toString().contains(".") -> {
                val splitLatitude = binding.etLongitude.text.toString().split(".")
                if( splitLatitude.size < 1 ) {
                    binding.tvLongitudeError.visibility = View.VISIBLE
                    binding.tvLongitudeError.text = getString(R.string.scu_longitude_no_characters_after_dot)
                    return false
                }
                else {
                    if( splitLatitude[0].toFloat() >= 180.0f ) {
                        binding.tvLongitudeError.visibility = View.VISIBLE
                        binding.tvLongitudeError.text = getString(R.string.scu_longitude_value_to_big)
                        return false
                    }
                    else if( splitLatitude[0].toFloat() <= -180.0f  ) {
                        binding.tvLongitudeError.visibility = View.VISIBLE
                        binding.tvLongitudeError.text = getString(R.string.scu_longitude_value_to_small)
                        return false
                    }
                    else if( splitLatitude[1].length < 2 ) {
                        binding.tvLongitudeError.visibility = View.VISIBLE
                        binding.tvLongitudeError.text = getString(R.string.scu_longitude_value_to_small_charachters)
                        return false
                    }
                    else if( splitLatitude[1].length > 8 ) {
                        binding.tvLongitudeError.visibility = View.VISIBLE
                        binding.tvLongitudeError.text = getString(R.string.scu_longitude_value_to_many_charachters)
                        return false
                    }
                    else {
                        binding.tvLongitudeError.visibility = View.GONE
                        return true
                    }
                }
            }
        }
        return true
    }

    override fun onResume() {
        super.onResume()
        App.ref.eventBus.register(this)

    }

    override fun onPause() {
        super.onPause()
        App.ref.eventBus.unregister(this)
    }

    var refreshTimestamp = Date()

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMplDeviceNotify(event: DevicesUpdatedEvent) {
        val currentTimeStamp = Date()
        if (currentTimeStamp.time - refreshTimestamp.time > 1000) {
            device = DeviceStore.devices[macAddress]
            setMplViewDetails()
            refreshTimestamp = currentTimeStamp
        }
    }

}