package hr.sil.android.mplhuber.core.remote.model

import com.google.gson.annotations.SerializedName

class RRealStation {

    var id: Int? = null
    @SerializedName("station___referenceId")
    var referenceId: String = ""
    @SerializedName("station___id")
    var stationId: Int = 0
    @SerializedName("station___name")
    var name: String = "Select Station"
    var address: String? = ""
    var latitude: Double = 0.0
    var longitude: Double = 0.0

}