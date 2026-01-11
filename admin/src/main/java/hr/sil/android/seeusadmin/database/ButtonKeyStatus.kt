package hr.sil.android.seeusadmin.database

import androidx.compose.runtime.Immutable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Immutable
@Entity(tableName = "buttons")
data class ButtonKeyStatus(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,
    @ColumnInfo(name = "slaveMacAddress")
    val slaveMacAddress: String,
    @ColumnInfo(name = "statusType")
    val statusType: String,
    @ColumnInfo(name = "keyId")
    val keyId: String,

    @ColumnInfo(name = "timeOfInstance")
    var timeOfInstance: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "isScheduleDelete")
    var isScheduleDelete: Boolean = false,

    @ColumnInfo(name = "scheduleMinutes")
    val scheduleMinutes: Long = 1L,

    @ColumnInfo(name = "scheduledPeriod")
    val scheduledPeriod: Long = 1000L * 60L * scheduleMinutes
)