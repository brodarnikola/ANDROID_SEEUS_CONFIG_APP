package hr.sil.android.mplhuber.core.remote.model

import com.google.android.gms.maps.model.LatLng
import com.google.gson.annotations.SerializedName

class RStationUnitRequest {
    @SerializedName("station___id")
    var stationId: Int? = null
    var latitude: Double? = 0.0
    var longitude: Double? = 0.0
    var radiusMeters: Int = 0
    var name: String? = ""
    var polygon: List<LatLng> = listOf()
    var stopPoint: String? = ""
    @SerializedName("epd___id")
    var epdTypeId: Int = 0
    @SerializedName("networkConfiguration___id")
    var networkConfigurationId: Int = 0
    var modemWorkingType = RPowerType.BATTERY.name
}