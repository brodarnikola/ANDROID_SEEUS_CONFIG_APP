package hr.sil.android.seeusadmin.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface LockerDao {

    @Query("SELECT * FROM locker_registration_process WHERE masterUnitMac = :deviceMacAddress")
    suspend fun getDeviceById(deviceMacAddress: String) : DeviceRegistration?

    @Query("UPDATE locker_registration_process set isStartedToRegister = :isStartedToRegister WHERE masterUnitMac = :macAddress")
    suspend fun updateRegistrationStatus(macAddress: String, isStartedToRegister: Boolean)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNewRegistrationDevice(device: DeviceRegistration)

    @Query("SELECT id FROM locker_registration_process")
    suspend fun fetchAllPendingRegistrationDevices(): List<Int?>

    @Delete()
    suspend fun removeDevice(device: DeviceRegistration)

}