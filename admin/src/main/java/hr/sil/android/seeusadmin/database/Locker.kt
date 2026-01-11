package hr.sil.android.seeusadmin.database

import androidx.compose.runtime.Immutable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Immutable
@Entity(tableName = "locker_registration_process")
data class DeviceRegistration(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,
    @ColumnInfo(name = "masterUnitMac")
    val masterUnitMac: String,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "address")
    val address: String,

    @ColumnInfo(name = "lat")
    val lat: Double,
    @ColumnInfo(name = "long")
    val long: Double,

    @ColumnInfo(name = "networkConfigurationId")
    val networkConfigurationId: Int,
    @ColumnInfo(name = "ePaperType")
    val ePaperType: Int,

    @ColumnInfo(name = "isStartedToRegister")
    val isStartedToRegister: Boolean,

    @ColumnInfo(name = "doorBellSupport")
    val doorBellSupport: Boolean,
)