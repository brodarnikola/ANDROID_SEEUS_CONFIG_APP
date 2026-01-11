package hr.sil.android.seeusadmin.database

import androidx.room.Database
import androidx.room.RoomDatabase
import javax.inject.Singleton

@Singleton
@Database(entities = [(DeviceRegistration::class), (ButtonKeyStatus::class)], version = 4, exportSchema = false)
abstract class StationDb : RoomDatabase() {
    abstract fun lockerDao(): LockerDao
    abstract fun buttonKeyDao(): ButtonKeyDao

    //abstract  fun keyStatusDao(): ButtonStatusDao

}