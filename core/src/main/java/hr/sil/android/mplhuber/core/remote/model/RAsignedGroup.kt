package hr.sil.android.mplhuber.core.remote.model

import com.google.gson.annotations.SerializedName

class RAssignedGroup {
    var buttonIndex: Int = 0
    var id: Int = 0

    @SerializedName("group___name")
    var groupName: String = ""

    @SerializedName("group___owner___telephone")
    var telephone: String = ""

    @SerializedName("group___id")
    var groupId: Int = 0

    @SerializedName("group___owner___email")
    var email: String = ""
}