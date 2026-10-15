package com.theveloper.pixelplay.presentation.library

import com.theveloper.pixelplay.data.model.SortOption
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertIterableEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class LibraryTabIdTest {

    @Test
    fun `decodeLibraryTabOrder returns default order when stored value is null`() {
        val order = decodeLibraryTabOrder(null)
        assertIterableEquals(LibraryTabId.defaultOrder, order)
    }

    @Test
    fun `decodeLibraryTabOrder preserves known order and restores missing tabs`() {
        val storedKeys = listOf(
            LibraryTabId.Liked.stableKey,
            "UNKNOWN",
            LibraryTabId.Playlists.stableKey,
            LibraryTabId.Liked.stableKey // duplicate should be ignored
        )
        val order = decodeLibraryTabOrder(Json.encodeToString(storedKeys))

        assertEquals(LibraryTabId.Liked, order.first(), "First entry should match stored stable key")
        assertTrue(order.containsAll(LibraryTabId.defaultOrder), "All default tabs should be present exactly once")
        assertEquals(LibraryTabId.defaultOrder.size, order.size)
    }

    @Test
    fun `sort associations remain tied to tab ids after reordering`() {
        val persistedSorts = LibraryTabId.defaultOrder.associateWith { tab ->
            tab.sortOptions.firstOrNull() ?: SortOption.SongTitleAZ
        }

        val shuffledOrder = decodeLibraryTabOrder(
            Json.encodeToString(
                listOf(
                    LibraryTabId.Folders.stableKey,
                    LibraryTabId.Songs.stableKey,
                    LibraryTabId.Playlists.stableKey
                )
            )
        )

        shuffledOrder.forEach { tab ->
            assertEquals(persistedSorts[tab], persistedSorts.getValue(tab))
        }
    }

    @Test
    fun `decodeActiveLibraryTabs filters out disabled tabs`() {
        val activeTabs = decodeActiveLibraryTabs(
            orderJson = null,
            disabledTabs = setOf(LibraryTabId.Folders.stableKey, LibraryTabId.Artists.stableKey)
        )
        assertTrue(activeTabs.none { it == LibraryTabId.Folders || it == LibraryTabId.Artists })
        assertEquals(LibraryTabId.defaultOrder.size - 2, activeTabs.size)
    }

    @Test
    fun `decodeActiveLibraryTabs falls back to at least one tab when all tabs disabled`() {
        val allKeys = LibraryTabId.defaultOrder.map { it.stableKey }.toSet()
        val activeTabs = decodeActiveLibraryTabs(orderJson = null, disabledTabs = allKeys)
        assertEquals(1, activeTabs.size)
        assertEquals(LibraryTabId.defaultOrder.first(), activeTabs.first())
    }

    @Test
    fun `decodeActiveLibraryTabs preserves custom order while filtering disabled tabs`() {
        val customOrder = listOf(
            LibraryTabId.Liked.stableKey,
            LibraryTabId.Songs.stableKey,
            LibraryTabId.Albums.stableKey
        )
        val activeTabs = decodeActiveLibraryTabs(
            orderJson = Json.encodeToString(customOrder),
            disabledTabs = setOf(LibraryTabId.Songs.stableKey)
        )
        assertEquals(LibraryTabId.Liked, activeTabs.first())
        assertTrue(activeTabs.none { it == LibraryTabId.Songs })
    }
}
