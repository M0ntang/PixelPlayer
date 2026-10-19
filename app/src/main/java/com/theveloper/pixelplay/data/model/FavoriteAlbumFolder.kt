package com.theveloper.pixelplay.data.model

import androidx.compose.runtime.Immutable

@Immutable
data class FavoriteAlbumFolder(
    val id: String,
    val name: String,
    val parentId: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val albumCount: Int = 0,
    val subfolderCount: Int = 0,
    val previewArtUris: List<String?> = emptyList()
)
