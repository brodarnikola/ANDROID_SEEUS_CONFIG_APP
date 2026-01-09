package hr.sil.android.seeusadmin.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import hr.sil.android.mplhuber.core.ble.DeviceStatus
import hr.sil.android.mplhuber.core.util.logger
import hr.sil.android.mplhuber.core.util.macCleanToReal
import hr.sil.android.seeusadmin.App
import hr.sil.android.seeusadmin.R
import hr.sil.android.seeusadmin.cache.status.ActionStatusHandler
import hr.sil.android.seeusadmin.cache.status.ActionStatusType
import hr.sil.android.seeusadmin.data.RButtonDataUiModel
import hr.sil.android.seeusadmin.databinding.FragmentLoginBinding
import hr.sil.android.seeusadmin.databinding.FragmentManageButtonsBinding
import hr.sil.android.seeusadmin.events.DevicesUpdatedEvent
import hr.sil.android.seeusadmin.store.DeviceStore
import hr.sil.android.seeusadmin.store.MPLDeviceStoreRemoteUpdater
import hr.sil.android.seeusadmin.store.model.Device
import hr.sil.android.seeusadmin.view.adapter.ButtonAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class ManageButtonsFragment : BaseFragment() {

    private val log = logger()

    private lateinit var macAddress: String
    private var device: Device? = null

    private lateinit var binding: FragmentManageButtonsBinding

    private val peripheralAdapter: ButtonAdapter by lazy {
        ButtonAdapter(mutableListOf(), macAddress, requireContext())
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreate(savedInstanceState)

//        val rootView = inflater.inflate(
//            R.layout.fragment_manage_buttons, container,
//            false
//        )

        macAddress = arguments?.getString("masterMac", "") ?: ""
        device = DeviceStore.devices[macAddress]

        initializeToolbarUIMainActivity(true, getString(R.string.main_locker_manage_buttons), false, false, requireContext())

        binding = FragmentManageButtonsBinding.inflate(layoutInflater)

        return binding.root // rootView
    }

    override fun onStart() {
        super.onStart()
        binding.manageButtonRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.manageButtonRecyclerView.adapter = peripheralAdapter
        lifecycleScope.launch(Dispatchers.Default) {
            updateDeviceList()
        }
    }

    private suspend fun updateDeviceList() {
        val devices = combineButtons().sortedBy { !it.isInProximity }.sortedBy { it.status }
        lifecycleScope.launch(Dispatchers.Main) {
            peripheralAdapter.updateDevices(devices)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMplDeviceNotify(event: DevicesUpdatedEvent) {
        lifecycleScope.launch {
            updateDeviceList()
        }
    }

    // TODO: Handle this --> combineButtons
    private suspend fun combineButtons(): MutableList<RButtonDataUiModel> {

        return mutableListOf()
        //Non registered slave units
//        val actions = ActionStatusHandler.actionStatusDb.getAll().map { it.keyId }
//        val unregisteredButtonsInProximity = DeviceStore.getNonRegisteredButtonsInProximity(actions)
//        MPLDeviceStoreRemoteUpdater.forceUpdate(false)
//        val currentlyRegisteredButtons = DeviceStore.devices[macAddress]?.buttonUnits
//                ?: listOf()
//        log.debug("Slave units ${currentlyRegisteredButtons.size}, Stored actions keys :" + actions.joinToString(" - ") { it })
//        val registeredAndPendingButtons = currentlyRegisteredButtons.map {
//            val inProximity = DeviceStore.devices[it.mac.macCleanToReal()]?.isInProximity
//                    ?: false
//            if (ActionStatusHandler.actionStatusDb.get(it.mac.macCleanToReal() + ActionStatusType.BUTTON_DEREGISTRATION) != null) {
//                log.debug("Registered - DELETE_PENDING:" + it.mac.macCleanToReal())
//                RButtonDataUiModel(it.id, it.mac.macCleanToReal(), it.deviceStationId, DeviceStatus.DELETE_PENDING,  inProximity)
//            } else {
//                val key = it.mac.macCleanToReal() + ActionStatusType.BUTTON_REGISTRATION
//                if (actions.contains(key)) {
//                    ActionStatusHandler.actionStatusDb.del(key)
//                }
//                log.debug("Registered " + it.mac.macCleanToReal())
//
//                RButtonDataUiModel(it.id, it.mac.macCleanToReal(), it.deviceStationId, DeviceStatus.REGISTERED,   inProximity)
//            }
//        }

//        val unregisteredButtons = unregisteredButtonsInProximity.filter { it.mac !in registeredAndPendingButtons.map { it.mac } }
//        //val filteredRegSlaves = registeredButtons.filter { it.mac !in ActionStatusHandler.actionStatusDb.getAll().filter { v -> v.statusType == ActionStatusType.BUTTON_REGISTRATION }.map { g -> g.macAddress } }
//        return (unregisteredButtons + registeredAndPendingButtons).toMutableList()
    }

    override fun onResume() {
        super.onResume()
        App.ref.eventBus.register(this)
    }

    override fun onPause() {
        super.onPause()
        App.ref.eventBus.unregister(this)
    }

}