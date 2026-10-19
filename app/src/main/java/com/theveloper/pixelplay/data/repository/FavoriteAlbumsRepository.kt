package com.theveloper.pixelplay.data.repository

import com.theveloper.pixelplay.data.model.Album
import com.theveloper.pixelplay.data.model.FavoriteAlbumFolder
import com.theveloper.pixelplay.data.model.Song
import kotlinx.coroutines.flow.Flow

interface FavoriteAlbumsRepository {
    fun observeFavoriteAlbumIds(): Flow<Set<Long>>
    fun isAlbumFavorite(albumId: Long): Flow<Boolean>
    suspend fun isAlbumFavoriteOnce(albumId: Long): Boolean

    suspend fun toggleFavorite(albumId: Long, folderId: String? = null): Boolean
    suspend fun addAlbumToFavorites(albumId: Long, folderId: String? = null)
    suspend fun addAlbumsToFavorites(albumIds: List<Long>, folderId: String? = null)
    suspend fun removeAlbumFromFavorites(albumId: Long)
    suspend fun removeAlbumsFromFavorites(albumIds: List<Long>)

    suspend fun moveAlbumToFolder(albumId: Long, targetFolderId: String?)
    suspend fun moveAlbumsToFolder(albumIds: List<Long>, targetFolderId: String?)

    fun observeFolders(parentId: String?): Flow<List<FavoriteAlbumFolder>>
    fun observeAllFolders(): Flow<List<FavoriteAlbumFolder>>
    fun observeFolder(folderId: String): Flow<FavoriteAlbumFolder?>
    suspend fun getFolder(folderId: String): FavoriteAlbumFolder?

    suspend fun createFolder(name: String, parentId: String? = null): String
    suspend fun renameFolder(folderId: String, newName: String)
    suspend fun deleteFolder(folderId: String)

    fun observeAlbumsInFolder(folderId: String?): Flow<List<Album>>

    suspend fun getBreadcrumbs(folderId: String?): List<FavoriteAlbumFolder>
    suspend fun getAllSongsInFolder(folderId: String?): List<Song>
}
