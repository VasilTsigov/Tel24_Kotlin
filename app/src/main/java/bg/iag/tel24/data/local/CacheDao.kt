package bg.iag.tel24.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CacheDao {
    @Query("SELECT * FROM cache WHERE `key` = :key")
    suspend fun get(key: String): CachedData?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun put(data: CachedData)
}
