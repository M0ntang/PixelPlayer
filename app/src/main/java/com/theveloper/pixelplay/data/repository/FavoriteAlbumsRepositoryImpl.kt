package com.theveloper.pixelplay.data.repository

import com.theveloper.pixelplay.data.database.AlbumEntity
import com.theveloper.pixelplay.data.database.FavoriteAlbumEntity
import com.theveloper.pixelplay.data.database.FavoriteAlbumFolderEntity
import com.theveloper.pixelplay.data.database.FavoriteAlbumsDao
import com.theveloper.pixelplay.data.database.toAlbum
import com.theveloper.pixelplay.data.database.toSong
import com.theveloper.pixelplay.data.model.Album
import com.theveloper.pixelplay.data.model.FavoriteAlbumFolder
import com.theveloper.pixelplay.data.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoriteAlbumsRepositoryImpl @Inject constructor(
    private val favoriteAlbumsDao: FavoriteAlbumsDao
) : FavoriteAlbumsRepository {

    override fun observeFavoriteAlbumIds(): Flow<Set<Long>> {
        return favoriteAlbumsDao.observeFavoriteAlbumIds()
            .map { it.toSet() }
            .distinctUntilChanged()
            .flowOn(Dispatchers.IO)
    }

    override fun isAlbumFavorite(albumId: Long): Flow<Boolean> {
        return favoriteAlbumsDao.isAlbumFavorite(albumId)
            .distinctUntilChanged()
            .flowOn(Dispatchers.IO)
    }

    override suspend fun isAlbumFavoriteOnce(albumId: Long): Boolean = withContext(Dispatchers.IO) {
        favoriteAlbumsDao.isAlbumFavoriteOnce(albumId)
    }

    override suspend fun toggleFavorite(albumId: Long, folderId: String?): Boolean = withContext(Dispatchers.IO) {
        val isFav = favoriteAlbumsDao.isAlbumFavoriteOnce(albumId)
        if (isFav) {
            favoriteAlbumsDao.deleteFavoriteAlbum(albumId)
            false
        } else {
            favoriteAlbumsDao.insertFavoriteAlbum(
                FavoriteAlbumEntity(
                    albumId = albumId,
                    folderId = folderId,
                    addedAt = System.currentTimeMillis()
                )
            )
            true
        }
    }

    override suspend fun addAlbumToFavorites(albumId: Long, folderId: String?) = withContext(Dispatchers.IO) {
        favoriteAlbumsDao.insertFavoriteAlbum(
            FavoriteAlbumEntity(
                albumId = albumId,
                folderId = folderId,
                addedAt = System.currentTimeMillis()
            )
        )
    }

    override suspend fun addAlbumsToFavorites(albumIds: List<Long>, folderId: String?) = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val entities = albumIds.mapIndexed { index, id ->
            FavoriteAlbumEntity(
                albumId = id,
                folderId = folderId,
                addedAt = now + index
            )
        }
        favoriteAlbumsDao.insertFavoriteAlbums(entities)
    }

    override suspend fun removeAlbumFromFavorites(albumId: Long) = withContext(Dispatchers.IO) {
        favoriteAlbumsDao.deleteFavoriteAlbum(albumId)
    }

    override suspend fun removeAlbumsFromFavorites(albumIds: List<Long>) = withContext(Dispatchers.IO) {
        favoriteAlbumsDao.deleteFavoriteAlbums(albumIds)
    }

    override suspend fun moveAlbumToFolder(albumId: Long, targetFolderId: String?) = withContext(Dispatchers.IO) {
        favoriteAlbumsDao.moveAlbumToFolder(albumId, targetFolderId)
    }

    override suspend fun moveAlbumsToFolder(albumIds: List<Long>, targetFolderId: String?) = withContext(Dispatchers.IO) {
        favoriteAlbumsDao.moveAlbumsToFolder(albumIds, targetFolderId)
    }

    private fun findDescendantFolderIds(folderId: String, allFolders: List<FavoriteAlbumFolderEntity>): Set<String> {
        val result = mutableSetOf<String>()
        val queue = ArrayDeque<String>()
        queue.add(folderId)
        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            result.add(current)
            val children = allFolders.filter { it.parentId == current }.map { it.id }
            for (child in children) {
                if (child !in result) {
                    queue.add(child)
                }
            }
        }
        return result
    }

    override fun observeFolders(parentId: String?): Flow<List<FavoriteAlbumFolder>> {
        val foldersFlow = favoriteAlbumsDao.observeAllFolders()
        val favEntitiesFlow = favoriteAlbumsDao.observeAllFavoriteAlbums()
        val albumEntitiesFlow = favoriteAlbumsDao.observeAllFavoriteAlbumsWithDetails()

        return combine(foldersFlow, favEntitiesFlow, albumEntitiesFlow) { allFolders, favEntities, albumEntities ->
            val albumArtByAlbumId = albumEntities.associate { it.id to it.albumArtUriString }
            val directChildren = allFolders.filter { it.parentId == parentId }

            directChildren.map { folder ->
                val descendantIds = findDescendantFolderIds(folder.id, allFolders)
                val matchingFavs = favEntities.filter { it.folderId in descendantIds }
                val subfoldersCount = allFolders.count { it.parentId == folder.id }
                val previewArts = matchingFavs
                    .mapNotNull { fav -> albumArtByAlbumId[fav.albumId] }
                    .filter { it.isNotBlank() }
                    .distinct()
                    .take(4)

                FavoriteAlbumFolder(
                    id = folder.id,
                    name = folder.name,
                    parentId = folder.parentId,
                    createdAt = folder.createdAt,
                    albumCount = matchingFavs.size,
                    subfolderCount = subfoldersCount,
                    previewArtUris = previewArts
                )
            }
        }.flowOn(Dispatchers.IO)
    }

    override fun observeAllFolders(): Flow<List<FavoriteAlbumFolder>> {
        val foldersFlow = favoriteAlbumsDao.observeAllFolders()
        val favEntitiesFlow = favoriteAlbumsDao.observeAllFavoriteAlbums()
        val albumEntitiesFlow = favoriteAlbumsDao.observeAllFavoriteAlbumsWithDetails()

        return combine(foldersFlow, favEntitiesFlow, albumEntitiesFlow) { allFolders, favEntities, albumEntities ->
            val albumArtByAlbumId = albumEntities.associate { it.id to it.albumArtUriString }

            allFolders.map { folder ->
                val descendantIds = findDescendantFolderIds(folder.id, allFolders)
                val matchingFavs = favEntities.filter { it.folderId in descendantIds }
                val subfoldersCount = allFolders.count { it.parentId == folder.id }
                val previewArts = matchingFavs
                    .mapNotNull { fav -> albumArtByAlbumId[fav.albumId] }
                    .filter { it.isNotBlank() }
                    .distinct()
                    .take(4)

                FavoriteAlbumFolder(
                    id = folder.id,
                    name = folder.name,
                    parentId = folder.parentId,
                    createdAt = folder.createdAt,
                    albumCount = matchingFavs.size,
                    subfolderCount = subfoldersCount,
                    previewArtUris = previewArts
                )
            }
        }.flowOn(Dispatchers.IO)
    }

    override fun observeFolder(folderId: String): Flow<FavoriteAlbumFolder?> {
        return observeAllFolders().map { folders ->
            folders.find { it.id == folderId }
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun getFolder(folderId: String): FavoriteAlbumFolder? = withContext(Dispatchers.IO) {
        val entity = favoriteAlbumsDao.getFolderById(folderId) ?: return@withContext null
        FavoriteAlbumFolder(
            id = entity.id,
            name = entity.name,
            parentId = entity.parentId,
            createdAt = entity.createdAt
        )
    }

    override suspend fun createFolder(name: String, parentId: String?): String = withContext(Dispatchers.IO) {
        val id = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        favoriteAlbumsDao.insertFolder(
            FavoriteAlbumFolderEntity(
                id = id,
                name = name.trim().ifBlank { "Folder" },
                parentId = parentId,
                createdAt = now,
                updatedAt = now
            )
        )
        id
    }

    override suspend fun renameFolder(folderId: String, newName: String) = withContext(Dispatchers.IO) {
        favoriteAlbumsDao.renameFolder(folderId, newName.trim().ifBlank { "Folder" })
    }

    override suspend fun deleteFolder(folderId: String) = withContext(Dispatchers.IO) {
        favoriteAlbumsDao.deleteFolderAndReparent(folderId)
    }

    override fun observeAlbumsInFolder(folderId: String?): Flow<List<Album>> {
        val flow = if (folderId == null) {
            favoriteAlbumsDao.observeRootAlbums()
        } else {
            favoriteAlbumsDao.observeAlbumsInFolder(folderId)
        }
        return flow.map { entities -> entities.map { it.toAlbum() } }.flowOn(Dispatchers.IO)
    }

    override suspend fun getBreadcrumbs(folderId: String?): List<FavoriteAlbumFolder> = withContext(Dispatchers.IO) {
        if (folderId == null) return@withContext emptyList()
        val result = mutableListOf<FavoriteAlbumFolder>()
        var currentId: String? = folderId
        while (currentId != null) {
            val entity = favoriteAlbumsDao.getFolderById(currentId) ?: break
            result.add(
                FavoriteAlbumFolder(
                    id = entity.id,
                    name = entity.name,
                    parentId = entity.parentId,
                    createdAt = entity.createdAt
                )
            )
            currentId = entity.parentId
        }
        result.reversed()
    }

    override suspend fun getAllSongsInFolder(folderId: String?): List<Song> = withContext(Dispatchers.IO) {
        val albumIds: List<Long> = if (folderId == null) {
            favoriteAlbumsDao.getAlbumsInRootDirect().map { it.albumId }
        } else {
            val allFolders = favoriteAlbumsDao.getFoldersByParent(null) // or all
            val allFolderEntities = mutableListOf<FavoriteAlbumFolderEntity>()
            // Collect all folders to find descendants
            suspend fun collectFolders(pid: String?) {
                val list = favoriteAlbumsDao.getFoldersByParent(pid)
                allFolderEntities.addAll(list)
                for (item in list) {
                    collectFolders(item.id)
                }
            }
            collectFolders(folderId)
            val descendantIds = setOf(folderId) + allFolderEntities.map { it.id }.toSet()
            val allFavs = mutableListOf<FavoriteAlbumEntity>()
            for (fId in descendantIds) {
                allFavs.addAll(favoriteAlbumsDao.getAlbumsInFolderDirect(fId))
            }
            allFavs.map { it.albumId }
        }

        if (albumIds.isEmpty()) return@withContext emptyList()
        val songEntities = favoriteAlbumsDao.getSongsByAlbumIds(albumIds)
        songEntities.map { it.toSong() }
    }
}
