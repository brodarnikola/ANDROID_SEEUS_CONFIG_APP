package hr.sil.android.seeusadmin.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ButtonKeyDao {

    @Query("SELECT * FROM buttons WHERE keyId = :keyId")
    suspend fun getButtonByKeyId(keyId: String) : ButtonKeyStatus?

    @Query("SELECT * FROM buttons")
    suspend fun getAllButtons() : List<ButtonKeyStatus>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNewButton(buttonKeyStatus: ButtonKeyStatus)

    @Delete()
    suspend fun removeButton(buttonKeyStatus: ButtonKeyStatus)


    @Query("DELETE FROM buttons WHERE keyId = :keyId")
    suspend fun removeButtonByKeyId(keyId: String)

}