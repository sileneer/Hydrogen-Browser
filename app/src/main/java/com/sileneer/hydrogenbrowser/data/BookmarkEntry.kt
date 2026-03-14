package com.sileneer.hydrogenbrowser.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "bookmarks",
    foreignKeys = [
        ForeignKey(
            entity = BookmarkEntry::class,
            parentColumns = ["id"],
            childColumns = ["parentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("parentId"), Index("url")]
)
data class BookmarkEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val url: String? = null,
    val isFolder: Boolean = false,
    val parentId: Long? = null,
    val position: Int = 0,
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    val favicon: ByteArray? = null,
    val createdAt: Long = 0
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BookmarkEntry) return false
        return id == other.id &&
                title == other.title &&
                url == other.url &&
                isFolder == other.isFolder &&
                parentId == other.parentId &&
                position == other.position &&
                createdAt == other.createdAt &&
                (favicon?.contentEquals(other.favicon) ?: (other.favicon == null))
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + (url?.hashCode() ?: 0)
        result = 31 * result + isFolder.hashCode()
        result = 31 * result + (parentId?.hashCode() ?: 0)
        result = 31 * result + position
        result = 31 * result + createdAt.hashCode()
        result = 31 * result + (favicon?.contentHashCode() ?: 0)
        return result
    }
}
