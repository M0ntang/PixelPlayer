package com.theveloper.pixelplay.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteAlbumsDao {
    @Query("SELECT * FROM favorite_albums ORDER BY added_at DESC")
    fun observeAllFavoriteAlbums(): Flow<List<FavoriteAlbumEntity>>

    @Query("""
        SELECT albums.* FROM albums
        INNER JOIN favorite_albums ON albums.id = favorite_albums.album_id
        WHERE favorite_albums.folder_id = :folderId
        ORDER BY favorite_albums.added_at DESC
    """)
    fun observeAlbumsInFolder(folderId: String): Flow<List<AlbumEntity>>

    @Query("""
        SELECT albums.* FROM albums
        INNER JOIN favorite_albums ON albums.id = favorite_albums.album_id
        WHERE favorite_albums.folder_id IS NULL
        ORDER BY favorite_albums.added_at DESC
    """)
    fun observeRootAlbums(): Flow<List<AlbumEntity>>

    @Query("""
        SELECT albums.* FROM albums
        INNER JOIN favorite_albums ON albums.id = favorite_albums.album_id
        ORDER BY favorite_albums.added_at DESC
    """)
    fun observeAllFavoriteAlbumsWithDetails(): Flow<List<AlbumEntity>>

    @Query("""
        SELECT albums.album_art_uri_string FROM albums
        INNER JOIN favorite_albums ON albums.id = favorite_albums.album_id
        WHERE favorite_albums.folder_id = :folderId AND albums.album_art_uri_string IS NOT NULL AND albums.album_art_uri_string != ''
        LIMIT 4
    """)
    suspend fun getPreviewArtForFolder(folderId: String): List<String>

    @Query("SELECT songs.* FROM songs WHERE songs.album_id IN (:albumIds) ORDER BY songs.album_id ASC, songs.disc_number ASC, songs.track_number ASC")
    suspend fun getSongsByAlbumIds(albumIds: List<Long>): List<SongEntity>

    @Query("SELECT * FROM favorite_albums WHERE folder_id = :folderId ORDER BY added_at DESC")
    fun observeFavoriteAlbumsInFolder(folderId: String): Flow<List<FavoriteAlbumEntity>>

    @Query("SELECT * FROM favorite_albums WHERE folder_id IS NULL ORDER BY added_at DESC")
    fun observeRootFavoriteAlbums(): Flow<List<FavoriteAlbumEntity>>

    @Query("SELECT album_id FROM favorite_albums")
    fun observeFavoriteAlbumIds(): Flow<List<Long>>

    @Query("SELECT album_id FROM favorite_albums")
    suspend fun getAllFavoriteAlbumIdsDirect(): List<Long>

    @Query("SELECT COUNT(*) > 0 FROM favorite_albums WHERE album_id = :albumId")
    fun isAlbumFavorite(albumId: Long): Flow<Boolean>

    @Query("SELECT COUNT(*) > 0 FROM favorite_albums WHERE album_id = :albumId")
    suspend fun isAlbumFavoriteOnce(albumId: Long): Boolean

    @Query("SELECT * FROM favorite_albums WHERE album_id = :albumId LIMIT 1")
    suspend fun getFavoriteAlbumById(albumId: Long): FavoriteAlbumEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavoriteAlbum(entity: FavoriteAlbumEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavoriteAlbums(entities: List<FavoriteAlbumEntity>)

    @Query("DELETE FROM favorite_albums WHERE album_id = :albumId")
    suspend fun deleteFavoriteAlbum(albumId: Long)

    @Query("DELETE FROM favorite_albums WHERE album_id IN (:albumIds)")
    suspend fun deleteFavoriteAlbums(albumIds: List<Long>)

    @Query("UPDATE favorite_albums SET folder_id = :targetFolderId WHERE album_id = :albumId")
    suspend fun moveAlbumToFolder(albumId: Long, targetFolderId: String?)

    @Query("UPDATE favorite_albums SET folder_id = :targetFolderId WHERE album_id IN (:albumIds)")
    suspend fun moveAlbumsToFolder(albumIds: List<Long>, targetFolderId: String?)

    // Folders
    @Query("SELECT * FROM favorite_album_folders ORDER BY name ASC")
    fun observeAllFolders(): Flow<List<FavoriteAlbumFolderEntity>>

    @Query("SELECT * FROM favorite_album_folders WHERE parent_id = :parentId ORDER BY name ASC")
    fun observeSubfolders(parentId: String): Flow<List<FavoriteAlbumFolderEntity>>

    @Query("SELECT * FROM favorite_album_folders WHERE parent_id IS NULL ORDER BY name ASC")
    fun observeRootFolders(): Flow<List<FavoriteAlbumFolderEntity>>

    @Query("SELECT * FROM favorite_album_folders WHERE id = :folderId LIMIT 1")
    suspend fun getFolderById(folderId: String): FavoriteAlbumFolderEntity?

    @Query("SELECT * FROM favorite_album_folders WHERE parent_id = :parentId")
    suspend fun getFoldersByParent(parentId: String?): List<FavoriteAlbumFolderEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolder(entity: FavoriteAlbumFolderEntity)

    @Update
    suspend fun updateFolder(entity: FavoriteAlbumFolderEntity)

    @Query("UPDATE favorite_album_folders SET name = :newName, updated_at = :updatedAt WHERE id = :folderId")
    suspend fun renameFolder(folderId: String, newName: String, updatedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM favorite_album_folders WHERE id = :folderId")
    suspend fun deleteFolderOnly(folderId: String)

    @Query("UPDATE favorite_album_folders SET parent_id = :newParentId WHERE parent_id = :folderId")
    suspend fun reparentSubfolders(folderId: String, newParentId: String?)

    @Query("UPDATE favorite_albums SET folder_id = :newParentId WHERE folder_id = :folderId")
    suspend fun reparentAlbums(folderId: String, newParentId: String?)

    @Transaction
    suspend fun deleteFolderAndReparent(folderId: String) {
        val folder = getFolderById(folderId)
        val parentId = folder?.parentId
        reparentSubfolders(folderId, parentId)
        reparentAlbums(folderId, parentId)
        deleteFolderOnly(folderId)
    }

    @Query("SELECT * FROM favorite_albums WHERE folder_id = :folderId")
    suspend fun getAlbumsInFolderDirect(folderId: String): List<FavoriteAlbumEntity>

    @Query("SELECT * FROM favorite_albums WHERE folder_id IS NULL")
    suspend fun getAlbumsInRootDirect(): List<FavoriteAlbumEntity>
}
