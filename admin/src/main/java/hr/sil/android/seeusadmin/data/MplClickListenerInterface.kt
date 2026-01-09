package hr.sil.android.seeusadmin.data

import hr.sil.android.mplhuber.core.ble.DeviceStatus

interface MplClickListenerInterface {

    fun onItemSelected(accessId: Int, status: DeviceStatus, index: Int = 0)
}