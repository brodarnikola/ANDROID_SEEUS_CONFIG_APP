package hr.sil.android.seeusadmin.data

import hr.sil.android.mplhuber.core.ble.DeviceStatus


class RButtonDataUiModel(val id: Int = 0, val mac: String, val masterId: Int = 0, var status: DeviceStatus, var isInProximity: Boolean) {


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as RButtonDataUiModel

        return (mac == other.mac &&
                id == other.id &&
                masterId == other.masterId &&
                status == other.status &&
                isInProximity == other.isInProximity
                )
    }

    override fun hashCode(): Int {
        return mac.hashCode()
    }
}