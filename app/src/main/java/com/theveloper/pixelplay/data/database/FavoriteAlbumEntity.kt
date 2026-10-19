package com.theveloper.pixelplay.data.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "favorite_albums",
    indices = [
        Index(value = ["folder_id"]),
        Index(value = ["added_at"])
    ]
)
data class FavoriteAlbumEntity(
    @PrimaryKey
    @ColumnInfo(name = "album_id")
    val albumId: Long,
    @ColumnInfo(name = "folder_id")
    val folderId: String? = null,
    @ColumnInfo(name = "added_at")
    val addedAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "sort_order")
    val sortOrder: Int = 0
)
