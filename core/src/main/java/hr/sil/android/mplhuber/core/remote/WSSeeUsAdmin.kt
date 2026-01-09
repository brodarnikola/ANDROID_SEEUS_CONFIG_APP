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

package hr.sil.android.mplhuber.core.remote

import android.util.Base64
import hr.sil.android.mplhuber.core.model.RUpdateAdminInfo
import hr.sil.android.mplhuber.core.remote.base.WSBase
import hr.sil.android.mplhuber.core.remote.model.*
import hr.sil.android.mplhuber.core.remote.service.AdminAppService
import hr.sil.android.mplhuber.core.util.macRealToClean
import hr.sil.android.rest.core.configuration.ServiceConfig
import retrofit2.Call

import hr.sil.android.rest.core.util.toHexString

/**
 * @author szuzul
 */
object WSSeeUsAdmin : WSBase() {

    suspend fun registerDevice(pushToken: String?, metaData: String = ""): Boolean {
        if (pushToken != null) {
            log.info("AppKey: " + ServiceConfig.cfg.appKey)
            val request = RUserDeviceInfo().apply {
                this.appKey = ServiceConfig.cfg.appKey
                this.deviceToken = pushToken
                this.deviceType = RUserDeviceType.ANDROID
                this.metadata = metaData
            }
            return wrapAwaitIsSuccessful(
                    call = AdminAppService.service.registerDevice(request),
                    methodName = "registerDevice()"
            )
        } else {
            return false
        }
    }

    suspend fun getAccountInfo(): RAdminUserInfo? {
        return wrapAwaitData(
                call = AdminAppService.service.getAccountInfo(),
                methodName = "getAccountInfo()"
        )
    }

    suspend fun modifyStationUnit(mac: String, request: RStationUnitRequest): RStationUnit? {
        log.info("Request to backend: ${request.stationId} \n ${request.radiusMeters} \n ${request.latitude} \n" +
                " ${request.longitude} \n" +
                " ${request.name} \n" +
                " ${request.polygon.size} \n" +
                " ${request.stopPoint}")
        return wrapAwaitData(
                call = AdminAppService.service.modifyStationUnit(mac, request),
                methodName = "modifyStationUnit()"
        )
    }


    suspend fun getGlobalConfigurationData(): RGlobalConfigurationData? {
        return wrapAwaitData(
                call = AdminAppService.service.getGlobalConfigurationData(),
                methodName = "getGlobalConfigurationData()"
        )
    }

    suspend fun getDeviceApiKey(challenge: ByteArray, masterBleMacAddress: String): ByteArray? {
        val result = wrapAwaitData(
                call = AdminAppService.service.getDeviceApiKey(
                        masterBleMacAddress.macRealToClean(),
                        challenge.toHexString()),
                methodName = "getDeviceApiKey()"
        )

        val b64 = result?.data
        return if (b64 != null) {
            Base64.decode(b64, Base64.DEFAULT)
        } else null
    }

    suspend fun getButtons(masterBleMacAddress: String): List<RButtonUnit>? {
        return WSSeeUsAdmin.wrapAwaitData(
                call = AdminAppService.service.getButtonForMaster(masterBleMacAddress.macRealToClean()),
                methodName = "getLockerForMaster()"
        )
    }

    suspend fun eraseDevice(masterBleMacAddress: String): Void? {
        return WSSeeUsAdmin.wrapAwaitData(
                call = AdminAppService.service.eraseMasterDevice(masterBleMacAddress.macRealToClean()),
                methodName = "eraseDevice()"
        )
    }

    suspend fun getStopPoints(stationReferenceId: String): Map<String,String>? {
        return WSSeeUsAdmin.wrapAwaitData(
                call = AdminAppService.service.getStopPoints(stationReferenceId),
                methodName = "getStopPoints()"
        )
    }
    suspend fun getEpdLists(): List<RAdminEpdInfo>? {
        return WSSeeUsAdmin.wrapAwaitData(
                call = AdminAppService.service.getEpdListTypes(),
                methodName = "getEpdListTypes()"
        )
    }

    suspend fun getStationUnits(): List<RStationUnit>? {
        return WSSeeUsAdmin.wrapAwaitData(
                call = AdminAppService.service.getStationUnits(),
                methodName = "getStationUnits()",
                defaultNullValue = listOf()
        )
    }

    suspend fun getStationUnitsReal(): List<RRealStationLocation>? {
        return WSSeeUsAdmin.wrapAwaitData(
                call = AdminAppService.service.getStationUnitsReal(),
                methodName = "getStationUnits()",
                defaultNullValue = listOf()
        )
    }

    suspend fun getStationUnit(id: Int): RRealStation? {
        return WSSeeUsAdmin.wrapAwaitData(
                call = AdminAppService.service.getStationUnit(id),
                methodName = "getStationUnits()",
                defaultNullValue = RRealStation()
        )
    }


    suspend fun getStationUnitsByLocation(lat: Double, long: Double): List<RRealStation> {


        val locations = WSSeeUsAdmin.wrapAwaitData(
                call = AdminAppService.service.getStationUnitsByLocation(lat, long),
                methodName = "getStationUnitsByLocation()",
                defaultNullValue = listOf()
        ) ?: listOf()
        val locationsDTO = locations.map {
            val item = it
            RRealStation().apply {
                this.address = item.address
                this.referenceId = item.referenceId
                this.id = item.id
                this.name = item.name
                this.latitude = item.latitude
                this.longitude = item.latitude
                this.stationId = item.id ?: 0
            }

        }
        return locationsDTO
    }

    suspend fun getAllButtons(): List<RButtonUnit>? {
        return wrapAwaitData(
                call = AdminAppService.service.getButtonUnits(),
                methodName = "getAllButtons()",
                defaultNullValue = listOf()
        )
    }

    override fun callEncryptService(mac: String, request: REncryptRequest): Call<REncryptResponse> {
        return AdminAppService.service.encrypt(mac, request)
    }

    fun getLanguages(): RWebLanguage? {
        return RWebLanguage()
    }

    suspend fun addButtonToStation(stationMac: String, buttonMac: String): Boolean {
        return WSSeeUsAdmin.wrapAwaitIsSuccessful(
                call = AdminAppService.service.assignButtonToStation(stationMac, buttonMac),
                methodName = "assignButtonToStation()"
        )
    }
    suspend fun simulateStationAction(stationMac: String, action: String): Boolean {
        return WSSeeUsAdmin.wrapAwaitIsSuccessful(
                call = AdminAppService.service.simulateStationAction(stationMac, action),
                methodName = "simulateStopSupportRequest()"
        )
    }


    suspend fun deleteButtonFromStation(buttonMac: String): Boolean {
        return WSSeeUsAdmin.wrapAwaitIsSuccessful(
                call = AdminAppService.service.deleteButtonFromStation( buttonMac),
                methodName = "assignButtonToStation()"
        )
    }
    suspend fun getNetworkConfigurations(): List<RNetworkConfiguration>? {
        return WSSeeUsAdmin.wrapAwaitData(
                call = AdminAppService.service.getNetworkConfigurations(),
                methodName = "getNetworkConfigurations()"
        )
    }

    suspend fun updateUserProfile(
            user: RUpdateAdminInfo
    ): RAdminUserInfo? {


        return WSSeeUsAdmin.wrapAwaitData(
                call = AdminAppService.service.updateUserProfile(user),
                methodName = "updateUserProfile()"
        )
    }

    suspend fun requestPasswordRecovery(email: String): Boolean {
        return WSSeeUsAdmin.wrapAwaitIsSuccessful(
                call = AdminAppService.service.requestPasswordRecovery(email),
                methodName = "requestPasswordRecovery()"
        )
    }

    suspend fun updatePassword(oldPassword: String, newPassword: String): Boolean {
        val request = RUpdatePasswordRequest().apply {
            this.oldPassword = oldPassword
            this.newPassword = newPassword
        }
        return WSSeeUsAdmin.wrapAwaitIsSuccessful(
                call = AdminAppService.service.updatePassword(request),
                methodName = "updatePassword()"
        )
    }

    suspend fun resetPassword(email: String, passwordCode: String, password: String): Boolean {
        val request = RResetPasswordRequest().apply {
            this.email = email
            this.passwordCode = passwordCode
            this.password = password
        }
        return WSSeeUsAdmin.wrapAwaitIsSuccessful(
                call = AdminAppService.service.resetPassword(request),
                methodName = "resetPassword()"
        )
    }

    suspend fun getMessageLog(): List<RMessageLog>? {
        return WSSeeUsAdmin.wrapAwaitData(
                call = AdminAppService.service.getMessageLog(),
                methodName = "getMessageLog()"
        )
    }

    suspend fun deleteMessageItem(itemId: Int): Void? {
        return WSSeeUsAdmin.wrapAwaitData(
                call = AdminAppService.service.deleteMessageItem(itemId),
                methodName = "deleteMessageItem()"
        )
    }

    suspend fun deleteAll(): Void? {
        return WSSeeUsAdmin.wrapAwaitData(
                call = AdminAppService.service.deleteAllMessages(),
                methodName = "deleteAllMessages()"
        )
    }
}