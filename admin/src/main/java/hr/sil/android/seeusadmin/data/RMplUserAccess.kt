package hr.sil.android.seeusadmin.data

import hr.sil.android.mplhuber.core.ble.DeviceStatus
import hr.sil.android.mplhuber.core.remote.model.RAssignedGroup
import hr.sil.android.mplhuber.core.remote.model.RMasterUnitAccessRequests

class RMplUserAccess() {
    var status: DeviceStatus = DeviceStatus.UNREGISTERED
    var name: String = ""
    var email: String = ""
    var phoneNumber: String = ""
    var index: Int = 0
    var accessId: Int = 0
    var groupId: Int = 0

    var isSelected: Boolean = false

    var userInsertedIndex: Int = 0

    constructor(masterUnitRequest: RMasterUnitAccessRequests) : this() {
        name = masterUnitRequest.groupOwnerName
        email = masterUnitRequest.groupOwnerEmail
        phoneNumber = masterUnitRequest.groupOwnerPhone
        accessId = masterUnitRequest.id
        status = DeviceStatus.NEW
    }

    constructor(assignedGroup: RAssignedGroup) : this() {
        index = assignedGroup.buttonIndex
        name = assignedGroup.groupName
        email = assignedGroup.email
        phoneNumber = assignedGroup.telephone
        status = DeviceStatus.REGISTERED
        groupId = assignedGroup.groupId
    }

}