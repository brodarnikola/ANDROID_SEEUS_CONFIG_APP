/* SWISS INNOVATION LAB CONFIDENTIAL
*
* www.swissinnolab.com
* __________________________________________________________________________
*
* [2016] - [2018] Swiss Innovation Lab AG
* All Rights Reserved.
*
* @author mfatiga
*
* NOTICE:  All information contained herein is, and remains
* the property of Swiss Innovation Lab AG and its suppliers,
* if any.  The intellectual and technical concepts contained
* herein are proprietary to Swiss Innovation Lab AG
* and its suppliers and may be covered by E.U. and Foreign Patents,
* patents in process, and are protected by trade secret or copyright law.
* Dissemination of this information or reproduction of this material
* is strictly forbidden unless prior written permission is obtained
* from Swiss Innovation Lab AG.
*/

package hr.sil.android.mplhuber.core.remote.model

import com.google.gson.annotations.SerializedName
import java.util.*

/**
 * @author mfatiga
 */
class RStationUnit {
    var id: Int = 0
    var mac: String = ""
    var timeCreated: Date = Date()
    var lastSeen: Date = Date()

    @SerializedName("customer___id")
    var customerId = 0

    @SerializedName("station___id")
    var stationId = 0

    @SerializedName("station___name")
    var stationName = ""

    @SerializedName("station___address")
    var stationAddress = ""

    @SerializedName("station___latitude")
    var stationLatitude: Double = 0.0

    @SerializedName("station___longitude")
    var stationLongitude: Double = 0.0

    @SerializedName("station___referenceId")
    var stationReference: String = ""

    @SerializedName("networkConfiguration___id")
    var networkConfigurationId: Int = 0

    var latitude: Double? = null
    var stopPoint: String? = null
    var longitude: Double? = null
    var radiusMeters: Int? = null

    var name = ""

    var lastTelemetry: Date? = null
    var voltage: Double? = null
    var temperature: Double? = null
    var humidity: Double? = null
    var pressure: Double? = null
    var rssi: Int? = null
    var ber: Double? = null
    var uvIndex: Double? = null

    var versionMajor = ""
    var versionMinor = ""
    var versionPatch = ""
    var ninaVersionMajor = ""
    var ninaVersionMinor = ""
    var ninaVersionPatch = ""

    var isDeleted = false
    var isVirtual = false

    var led1Fault = false
    var led2Fault = false
    var led3Fault = false

    var uptime: Int = 0

    var buttons: List<RButtonUnit> = listOf()
    val polygon: List<RLatLng> = listOf()
    val modemWorkingType: RPowerType = RPowerType.BATTERY
    val modemLineSleepTime: Int = 60
    val modemBatterySleepTime: Int = 21000

    val isPlaceholder = false

    @SerializedName("epd___id")
    val  ePdId = 0

}