package com.sileneer.hydrogenbrowser.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history_entries")
data class HistoryEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val url: String,
    val title: String,
    val timestamp: Long,
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    val favicon: ByteArray? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is HistoryEntry) return false
        return id == other.id &&
                url == other.url &&
                title == other.title &&
                timestamp == other.timestamp &&
                favicon.contentEquals(other.favicon)
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + url.hashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + timestamp.hashCode()
        result = 31 * result + (favicon?.contentHashCode() ?: 0)
        return result
    }
}
