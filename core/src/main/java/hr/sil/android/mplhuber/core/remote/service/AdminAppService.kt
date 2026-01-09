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

package hr.sil.android.mplhuber.core.remote.service

import hr.sil.android.mplhuber.core.model.RUpdateAdminInfo
import hr.sil.android.mplhuber.core.remote.model.*
import hr.sil.android.rest.core.factory.RestServiceAccessor
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * @author mfatiga
 */
interface AdminAppService {
    companion object : RestServiceAccessor<AdminAppService>(AdminAppService::class) {
        //auth: Basic
        private const val ENDPOINT_PREFIX = "service/rest/adminApp/"
    }


    @POST(ENDPOINT_PREFIX + "device/register")
    fun registerDevice(@Body deviceInfo: RUserDeviceInfo): Call<RUserDeviceInfo>

    @GET(ENDPOINT_PREFIX + "login")
    fun login(): Call<RAdminUserInfo>

    @GET(ENDPOINT_PREFIX + "account/info")
    fun getAccountInfo(): Call<RAdminUserInfo>

    @GET(ENDPOINT_PREFIX + "configurationData")
    fun getGlobalConfigurationData(): Call<RGlobalConfigurationData>

    @GET(ENDPOINT_PREFIX + "getApiKey/{mac}/{challenge}")
    fun getDeviceApiKey(@Path("mac") cleanDeviceBleMac: String,
                        @Path("challenge") challenge: String): Call<REncryptResponse>

    @POST(ENDPOINT_PREFIX + "{mac}/encrypt")
    fun encrypt(@Path("mac") mac: String,
                @Body encryptRequest: REncryptRequest): Call<REncryptResponse>

    @GET(ENDPOINT_PREFIX + "/deviceStation/{mac}/buttons")
    fun getButtonForMaster(@Path("mac") cleanDeviceBleMac: String): Call<List<RButtonUnit>>

    @GET(ENDPOINT_PREFIX + "/deviceStation/hardDelete/{deviceStationMac}")
    fun eraseMasterDevice(@Path("deviceStationMac") cleanDeviceBleMac: String): Call<Void>


    @GET(ENDPOINT_PREFIX + "/deviceStation/{mac}/buttons")
    fun getNearByStation(@Path("mac") cleanDeviceBleMac: String): Call<List<RButtonUnit>>

    @GET(ENDPOINT_PREFIX + "/station/stopPoints/{stationReferenceId}")
    fun getStopPoints(@Path("stationReferenceId") referenceId: String): Call<Map<String, String>>


    @GET(ENDPOINT_PREFIX + "deviceStations")
    fun getStationUnits(): Call<List<RStationUnit>>


    @GET(ENDPOINT_PREFIX + "/deviceStation/byId/{id}")
    fun getStationUnit(@Path("id") id: Int): Call<RRealStation>


    @GET(ENDPOINT_PREFIX + "stations/{latitude}/{longitude}/10000/50")
    fun getStationUnitsByLocation(@Path("latitude") latitude: Double,
                                  @Path("longitude") longitude: Double): Call<List<RRealStationLocation>>


    @GET(ENDPOINT_PREFIX + "stations")
    fun getStationUnitsReal(): Call<List<RRealStationLocation>>


    @GET(ENDPOINT_PREFIX + "deviceButtons")
    fun getButtonUnits(): Call<List<RButtonUnit>>


    @POST(ENDPOINT_PREFIX + "deviceStation/{mac}/modify")
    fun modifyStationUnit(@Path("mac") macAddress: String,
                          @Body masterDetails: RStationUnitRequest): Call<RStationUnit>

    @GET(ENDPOINT_PREFIX + "messageLog")
    fun getMessageLog(): Call<List<RMessageLog>>

    @GET(ENDPOINT_PREFIX + "messageLog/delete/{id}")
    fun deleteMessageItem(@Path("id") id: Int): Call<Void>

    @GET(ENDPOINT_PREFIX + "messageLog/delete")
    fun deleteAllMessages(): Call<Void>


    @GET(ENDPOINT_PREFIX + "masterAccess/grant/{accessRequestId}/{buttonIndex}")
    fun grantAccessToMaster(@Path("accessRequestId") accessRequestId: Int,
                            @Path("buttonIndex") buttonIndex: Int): Call<Void>

    @GET(ENDPOINT_PREFIX + "masterAccess/reject/{accessRequestId}")
    fun rejectAccessToMaster(@Path("accessRequestId") accessRequestId: Int): Call<Void>

    @GET(ENDPOINT_PREFIX + "master/{mac}/assignedGroups")
    fun getAssignedGroupsToEpaper(@Path("mac") mac: String): Call<List<RAssignedGroup>>

    @GET(ENDPOINT_PREFIX + "networkConfigurations")
    fun getNetworkConfigurations(): Call<List<RNetworkConfiguration>>

    @GET(ENDPOINT_PREFIX + "master/{mac}/unassign/{buttonIndex}")
    fun unAssignMasterFromEpaper(@Path("mac") mac: String,
                                 @Path("buttonIndex") buttonIndex: Int): Call<Void>


    @GET(ENDPOINT_PREFIX + "deviceStation/{mac}/assignButton/{buttonMac}")
    fun assignButtonToStation(@Path("mac") mac: String,
                              @Path("buttonMac") buttonMac: String): Call<RButtonUnit>

    @GET(ENDPOINT_PREFIX + "deviceStation/{deviceStationMac}/buttonSimulator/{action}")
    fun simulateStationAction(@Path("deviceStationMac") mac: String,
                              @Path("action") buttonMac: String): Call<Void>

    @GET(ENDPOINT_PREFIX + "deviceButton/deactivate/{buttonMac}")
    fun removeButtonFromStation(@Path("buttonMac") buttonMac: String): Call<Void>

    @GET(ENDPOINT_PREFIX + "deviceButton/hardDelete/{buttonMac}")
    fun deleteButtonFromStation(@Path("buttonMac") buttonMac: String): Call<Void>

    @GET(ENDPOINT_PREFIX + "endUser/recoverPassword/{email}")
    fun requestPasswordRecovery(@Path("email") email: String): Call<Void>

    @POST(ENDPOINT_PREFIX + "endUser/resetPassword")
    fun resetPassword(@Body resetPasswordRequest: RResetPasswordRequest): Call<Void>

    @POST(ENDPOINT_PREFIX + "endUser/updatePassword")
    fun updatePassword(@Body updatePasswordRequest: RUpdatePasswordRequest): Call<Void>

    @POST(ENDPOINT_PREFIX + "account/modify")
    fun updateUserProfile(@Body updateUserProfileRequest: RUpdateAdminInfo): Call<RAdminUserInfo>

    @GET(ENDPOINT_PREFIX + "epd/list")
    fun getEpdListTypes(): Call<List<RAdminEpdInfo>>


}


