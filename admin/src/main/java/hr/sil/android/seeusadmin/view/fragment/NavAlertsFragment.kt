package hr.sil.android.seeusadmin.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import hr.sil.android.mplhuber.core.remote.WSSeeUsAdmin
import hr.sil.android.mplhuber.core.remote.model.RMessageLog
import hr.sil.android.mplhuber.view.ui.home.adapters.NotificationAdapter
import hr.sil.android.seeusadmin.R
import hr.sil.android.seeusadmin.databinding.FragmentAlertsBinding
import hr.sil.android.seeusadmin.databinding.FragmentLoginBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NavAlertsFragment : BaseFragment() {


    val notificationAdapter: NotificationAdapter by lazy {
        NotificationAdapter(singleParcelBoxes, { partItem: RMessageLog -> splItemClicked(partItem) })
    }

    var singleParcelBoxes = mutableListOf<RMessageLog>()

    private lateinit var binding: FragmentAlertsBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        initializeToolbarUIMainActivity(true, resources.getString(R.string.nav_alarms_title), false, false, requireContext())
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)

        binding = FragmentAlertsBinding.inflate(layoutInflater)
        return binding.root
//        return inflater.inflate(
//            R.layout.fragment_alerts, container,
//            false
//        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.clClearAll.setOnClickListener {
            singleParcelBoxes.clear()
            binding.alertRecyclerView.adapter?.notifyDataSetChanged()
            lifecycleScope.launch {
                WSSeeUsAdmin.deleteAll()
            }
        }
    }

    private fun splItemClicked( messageLog: RMessageLog) {
        lifecycleScope.launch {
            WSSeeUsAdmin.deleteMessageItem(messageLog.id)
            withContext(Dispatchers.Main) {
                notificationAdapter.removeNote(messageLog)
                notificationAdapter.notifyDataSetChanged()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        lifecycleScope.launch {
            singleParcelBoxes = WSSeeUsAdmin.getMessageLog()?.toMutableList() ?: mutableListOf()
            withContext(Dispatchers.Main) {
                binding.progressBar.visibility = View.GONE
                binding.alertRecyclerView.adapter = notificationAdapter
            }
        }
    }
}