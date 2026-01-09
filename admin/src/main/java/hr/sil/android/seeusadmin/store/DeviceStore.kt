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

package hr.sil.android.seeusadmin.store

import hr.sil.android.ble.scanner.model.device.BLEDevice
import hr.sil.android.ble.scanner.scan_multi.model.BLEDeviceData
import hr.sil.android.ble.scanner.scan_multi.properties.advv3.BLEAdvDynamic
import hr.sil.android.mplhuber.core.ble.DeviceStatus
import hr.sil.android.mplhuber.core.remote.WSSeeUsAdmin
import hr.sil.android.mplhuber.core.remote.model.RStationUnit
import hr.sil.android.mplhuber.core.remote.model.RUnitType
import hr.sil.android.mplhuber.core.util.logger
import hr.sil.android.mplhuber.core.util.macCleanToReal
import hr.sil.android.seeusadmin.App
import hr.sil.android.seeusadmin.cache.status.ActionStatusHandler
import hr.sil.android.seeusadmin.cache.status.ActionStatusType
import hr.sil.android.seeusadmin.data.RButtonDataUiModel
import hr.sil.android.seeusadmin.events.DevicesUpdatedEvent
import hr.sil.android.seeusadmin.store.model.Device

import hr.sil.android.rest.core.synchronizedDelegate

/**
 * @author mfatiga
 */
object DeviceStore {
    private var mDevices by synchronizedDelegate(mapOf<String, Device>())

    val devices: Map<String, Device>
        get() = mDevices.toMap()

    private var bleData by synchronizedDelegate(mapOf<String, BLEDevice<BLEDeviceData>>())
    fun updateFromBLE(bleDevices: List<BLEDevice<BLEDeviceData>>) {
        bleData = bleDevices.associateBy { it.deviceAddress.toUpperCase() }
        mergeData()
        notifyEvents(bleDevices.map { it.deviceAddress.toUpperCase() })
    }

    private var remoteData by synchronizedDelegate(mapOf<String, RStationUnit>())
    fun updateFromRemote(remoteDevices: List<RStationUnit>, propagateEvent: Boolean) {
        remoteData = remoteDevices.associateBy { it.mac.macCleanToReal() }
        mergeData()
        if (propagateEvent)
            notifyEvents(remoteDevices.map { it.mac.toUpperCase() })
    }

    val log = logger()

    // TODO: HANDLE THIS --> getNonRegisteredButtonsInProximity
    suspend fun getNonRegisteredButtonsInProximity(actions: Collection<String>): List<RButtonDataUiModel> {
        return listOf()

        // TODO: HANDLE THIS --> getNonRegisteredButtonsInProximity
//        val buttons = WSSeeUsAdmin.getAllButtons() ?: listOf()
//
//            val allRegisteredButtonsMacs = buttons.map { it.mac.macCleanToReal() }
//            log.debug("Cached buttons registered on backend:" + allRegisteredButtonsMacs.joinToString("-") { it + " " })
//
//            val nonRegisteredButtonsInBleProximity = bleData.values.filter {
//                isMappedCorrectly(it, allRegisteredButtonsMacs)
//            }
//
//            return nonRegisteredButtonsInBleProximity.map {
//                val instanceKey = it.deviceAddress + ActionStatusType.BUTTON_REGISTRATION
//                val cachedAction = ActionStatusHandler.actionStatusDb.get(instanceKey)
//                if (DeviceStore.mDevices.containsKey(it.deviceAddress) && cachedAction != null) {
//                    RButtonDataUiModel(mac = it.deviceAddress, status = DeviceStatus.REGISTRATION_PENDING, isInProximity = true)
//                } else {
//                    val key = it.deviceAddress + ActionStatusType.BUTTON_DEREGISTRATION
//                    if (actions.contains(key)) {
//                        ActionStatusHandler.actionStatusDb.del(key)
//                    }
//                    RButtonDataUiModel(mac = it.deviceAddress, status = DeviceStatus.UNREGISTERED, isInProximity = true)
//                }
//
//            }.toList()
//


    }

    private fun isMappedCorrectly(it: BLEDevice<BLEDeviceData>, allRegisteredButtonsMacs: List<String>): Boolean {

        val bleProps = it.data.properties
        when (bleProps) {
            is BLEAdvDynamic -> {
               return bleProps.definition?.key == RUnitType.SEEUS_BTN.name &&
                        it.deviceAddress !in allRegisteredButtonsMacs
            }
        }
        return false
    }


    private fun mergeData() {
        val allKeys = (remoteData.keys + bleData.keys).distinct()
        mDevices = allKeys
                .associate { it to Device.create(it, bleData[it], remoteData[it]) }
        log.debug("Device list size: ${mDevices.size}")
    }

    private fun notifyEvents(macList: List<String>) {
        App.ref.eventBus.post(DevicesUpdatedEvent(macList))
    }

    fun clear() {
        bleData = mapOf()
        mDevices = mapOf()
        remoteData = mapOf()
    }
}