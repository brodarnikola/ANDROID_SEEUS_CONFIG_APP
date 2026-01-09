package hr.sil.android.mplhuber.core.ble.telemetry

import hr.sil.android.mplhuber.core.ble.DeviceStatus


enum class ModemStatus (val type: String?) {
    UNKNOWN (null) ,
    TURNED_OFF  ("MODEM_STATUS_TURNED_OFF"),
    CONNECTING("MODEM_STATUS_CONNECTING"),
    CONNECTED("MODEM_STATUS_CONNECTED"),
    SLEEP("MODEM_STATUS_SLEEP"),
    PSM("MODEM_STATUS_POWER_SAVING"),
    DISCONNECTING ("MODEM_STATUS_DISCONNECTING");

    companion object {
        fun parse( type: String?) = values().firstOrNull{
            it.type == type
        }?: UNKNOWN
    }
}