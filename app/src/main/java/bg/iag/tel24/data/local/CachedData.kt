package bg.iag.tel24.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cache")
data class CachedData(
    @PrimaryKey val key: String,
    val json: String,
    val timestamp: Long = System.currentTimeMillis()
)
