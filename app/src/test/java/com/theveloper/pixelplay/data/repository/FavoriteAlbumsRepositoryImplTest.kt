package com.theveloper.pixelplay.data.repository

import com.google.common.truth.Truth.assertThat
import com.theveloper.pixelplay.data.database.AlbumEntity
import com.theveloper.pixelplay.data.database.FavoriteAlbumEntity
import com.theveloper.pixelplay.data.database.FavoriteAlbumFolderEntity
import com.theveloper.pixelplay.data.database.FavoriteAlbumsDao
import com.theveloper.pixelplay.data.database.SongEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class FavoriteAlbumsRepositoryImplTest {

    private val dao: FavoriteAlbumsDao = mockk(relaxed = true)
    private lateinit var repository: FavoriteAlbumsRepositoryImpl

    @BeforeEach
    fun setUp() {
        repository = FavoriteAlbumsRepositoryImpl(dao)
    }

    @Test
    fun observeFavoriteAlbumIds_mapsListToSet() = runTest {
        every { dao.observeFavoriteAlbumIds() } returns flowOf(listOf(101L, 102L, 101L, 103L))

        val result = repository.observeFavoriteAlbumIds().first()

        assertThat(result).containsExactly(101L, 102L, 103L)
    }

    @Test
    fun toggleFavorite_whenNotFavorite_insertsAndReturnsTrue() = runTest {
        coEvery { dao.isAlbumFavoriteOnce(101L) } returns false

        val inserted = repository.toggleFavorite(101L, "folder-1")

        assertThat(inserted).isTrue()
        val slot = slot<FavoriteAlbumEntity>()
        coVerify { dao.insertFavoriteAlbum(capture(slot)) }
        assertThat(slot.captured.albumId).isEqualTo(101L)
        assertThat(slot.captured.folderId).isEqualTo("folder-1")
    }

    @Test
    fun toggleFavorite_whenAlreadyFavorite_deletesAndReturnsFalse() = runTest {
        coEvery { dao.isAlbumFavoriteOnce(101L) } returns true

        val inserted = repository.toggleFavorite(101L, "folder-1")

        assertThat(inserted).isFalse()
        coVerify { dao.deleteFavoriteAlbum(101L) }
    }

    @Test
    fun addAlbumToFavorites_insertsEntityWithTargetFolder() = runTest {
        repository.addAlbumToFavorites(202L, "target-f")

        val slot = slot<FavoriteAlbumEntity>()
        coVerify { dao.insertFavoriteAlbum(capture(slot)) }
        assertThat(slot.captured.albumId).isEqualTo(202L)
        assertThat(slot.captured.folderId).isEqualTo("target-f")
    }

    @Test
    fun addAlbumsToFavorites_insertsAllEntities() = runTest {
        repository.addAlbumsToFavorites(listOf(1L, 2L, 3L), "f1")

        val slot = slot<List<FavoriteAlbumEntity>>()
        coVerify { dao.insertFavoriteAlbums(capture(slot)) }
        assertThat(slot.captured.map { it.albumId }).containsExactly(1L, 2L, 3L).inOrder()
        assertThat(slot.captured.map { it.folderId }).containsExactly("f1", "f1", "f1")
    }

    @Test
    fun removeAlbumFromFavorites_deletesFromDao() = runTest {
        repository.removeAlbumFromFavorites(303L)
        coVerify { dao.deleteFavoriteAlbum(303L) }
    }

    @Test
    fun moveAlbumToFolder_updatesFolderInDao() = runTest {
        repository.moveAlbumToFolder(404L, "new-folder")
        coVerify { dao.moveAlbumToFolder(404L, "new-folder") }
    }

    @Test
    fun createFolder_insertsFolderWithTrimmedName() = runTest {
        val slot = slot<FavoriteAlbumFolderEntity>()
        coEvery { dao.insertFolder(capture(slot)) } returns Unit

        val folderId = repository.createFolder("  Rock Classics  ", "parent-1")

        assertThat(folderId).isNotEmpty()
        assertThat(slot.captured.name).isEqualTo("Rock Classics")
        assertThat(slot.captured.parentId).isEqualTo("parent-1")
        assertThat(slot.captured.id).isEqualTo(folderId)
    }

    @Test
    fun renameFolder_updatesFolderWithTrimmedName() = runTest {
        repository.renameFolder("folder-1", "  New Name  ")
        coVerify { dao.renameFolder("folder-1", "New Name", any()) }
    }

    @Test
    fun deleteFolder_callsDaoDeleteAndReparent() = runTest {
        repository.deleteFolder("folder-1")
        coVerify { dao.deleteFolderAndReparent("folder-1") }
    }

    @Test
    fun getBreadcrumbs_navigatesUpToRootAndReturnsInCorrectOrder() = runTest {
        val root = FavoriteAlbumFolderEntity("root", "Root", null, 100L, 100L)
        val sub1 = FavoriteAlbumFolderEntity("sub1", "Sub 1", "root", 200L, 200L)
        val sub2 = FavoriteAlbumFolderEntity("sub2", "Sub 2", "sub1", 300L, 300L)

        coEvery { dao.getFolderById("sub2") } returns sub2
        coEvery { dao.getFolderById("sub1") } returns sub1
        coEvery { dao.getFolderById("root") } returns root

        val breadcrumbs = repository.getBreadcrumbs("sub2")

        assertThat(breadcrumbs.map { it.id }).containsExactly("root", "sub1", "sub2").inOrder()
    }

    @Test
    fun observeFolders_computesAlbumCountAndSubfolderCount() = runTest {
        val parentFolder = FavoriteAlbumFolderEntity("p", "Parent", null, 100L, 100L)
        val childFolder = FavoriteAlbumFolderEntity("c", "Child", "p", 200L, 200L)
        val allFolders = listOf(parentFolder, childFolder)

        val favAlbums = listOf(
            FavoriteAlbumEntity(1L, "p", 100L),
            FavoriteAlbumEntity(2L, "c", 101L)
        )

        val albumDetails = listOf(
            AlbumEntity(1L, "Album 1", "Artist 1", 10L, "art1", 5, 100L, 2020),
            AlbumEntity(2L, "Album 2", "Artist 2", 11L, "art2", 6, 101L, 2021)
        )

        every { dao.observeAllFolders() } returns flowOf(allFolders)
        every { dao.observeAllFavoriteAlbums() } returns flowOf(favAlbums)
        every { dao.observeAllFavoriteAlbumsWithDetails() } returns flowOf(albumDetails)

        val rootFolders = repository.observeFolders(null).first()

        assertThat(rootFolders).hasSize(1)
        val p = rootFolders.first()
        assertThat(p.id).isEqualTo("p")
        assertThat(p.subfolderCount).isEqualTo(1)
        // Descendant count includes items in child folder
        assertThat(p.albumCount).isEqualTo(2)
        assertThat(p.previewArtUris).containsExactly("art1", "art2")
    }
}
