package com.theveloper.pixelplay.presentation.library

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.theveloper.pixelplay.R
import com.theveloper.pixelplay.data.model.SortOption
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

/**
 * Stable identifiers for each library tab. The [stableKey] value is persisted so it must not
 * change between app versions.
 */
enum class LibraryTabId(
    val stableKey: String,
    val label: String,
    @StringRes val labelRes: Int,
    @DrawableRes val iconRes: Int,
    val sortOptions: List<SortOption>
) {
    Songs(
        stableKey = "SONGS",
        label = "SONGS",
        labelRes = R.string.library_tab_songs,
        iconRes = R.drawable.rounded_music_note_24,
        sortOptions = listOf(
            SortOption.SongTitleAZ,
            SortOption.SongTitleZA,
            SortOption.SongArtist,
            SortOption.SongArtistDesc,
            SortOption.SongAlbum,
            SortOption.SongAlbumDesc,
            SortOption.SongDateAdded,
            SortOption.SongDateAddedAsc,
            SortOption.SongDuration,
            SortOption.SongDurationAsc
        )
    ),
    Albums(
        stableKey = "ALBUMS",
        label = "ALBUMS",
        labelRes = R.string.library_tab_albums,
        iconRes = R.drawable.rounded_album_24,
        sortOptions = listOf(
            SortOption.AlbumTitleAZ,
            SortOption.AlbumTitleZA,
            SortOption.AlbumArtist,
            SortOption.AlbumArtistDesc,
            SortOption.AlbumReleaseYear,
            SortOption.AlbumReleaseYearAsc,
            SortOption.AlbumDateAdded
        )
    ),
    Singles(
        stableKey = "SINGLES",
        label = "SINGLES",
        labelRes = R.string.library_tab_singles,
        iconRes = R.drawable.rounded_single_24,
        sortOptions = listOf(
            SortOption.AlbumTitleAZ,
            SortOption.AlbumTitleZA,
            SortOption.AlbumArtist,
            SortOption.AlbumArtistDesc,
            SortOption.AlbumReleaseYear,
            SortOption.AlbumReleaseYearAsc,
            SortOption.AlbumDateAdded
        )
    ),
    Artists(
        stableKey = "ARTIST",
        label = "ARTIST",
        labelRes = R.string.library_tab_artists,
        iconRes = R.drawable.rounded_artist_24,
        sortOptions = listOf(
            SortOption.ArtistNameAZ,
            SortOption.ArtistNameZA,
            SortOption.ArtistNumSongsDesc,
            SortOption.ArtistNumSongsAsc
        )
    ),
    Playlists(
        stableKey = "PLAYLISTS",
        label = "PLAYLISTS",
        labelRes = R.string.library_tab_playlists,
        iconRes = R.drawable.rounded_playlist_play_24,
        sortOptions = listOf(
            SortOption.PlaylistNameAZ,
            SortOption.PlaylistNameZA,
            SortOption.PlaylistDateCreated,
            SortOption.PlaylistDateCreatedAsc
        )
    ),
    Folders(
        stableKey = "FOLDERS",
        label = "FOLDERS",
        labelRes = R.string.library_tab_folders,
        iconRes = R.drawable.rounded_folder_24,
        sortOptions = listOf(
            SortOption.FolderNameAZ,
            SortOption.FolderNameZA,
            SortOption.FolderSongCountAsc,
            SortOption.FolderSongCountDesc,
            SortOption.FolderSubdirCountAsc,
            SortOption.FolderSubdirCountDesc
        )
    ),
    Liked(
        stableKey = "LIKED",
        label = "LIKED",
        labelRes = R.string.library_tab_liked,
        iconRes = R.drawable.round_favorite_24,
        sortOptions = listOf(
            SortOption.LikedSongTitleAZ,
            SortOption.LikedSongTitleZA,
            SortOption.LikedSongArtist,
            SortOption.LikedSongArtistDesc,
            SortOption.LikedSongAlbum,
            SortOption.LikedSongAlbumDesc,
            SortOption.LikedSongDateLiked,
            SortOption.LikedSongDateLikedAsc
        )
    );

    companion object {
        val defaultOrder: List<LibraryTabId> = entries.toList()

        fun fromStableKey(key: String): LibraryTabId? = entries.firstOrNull { it.stableKey == key }
    }
}

internal fun decodeLibraryTabOrder(orderJson: String?): List<LibraryTabId> {
    val storedKeys = orderJson?.let {
        runCatching { Json.decodeFromString<List<String>>(it) }.getOrNull()
    } ?: emptyList()

    val ordered = LinkedHashSet<LibraryTabId>()
    storedKeys.mapNotNull { LibraryTabId.fromStableKey(it) }.forEach { ordered.add(it) }
    LibraryTabId.defaultOrder.forEach { ordered.add(it) }
    return ordered.toList()
}

internal fun decodeActiveLibraryTabs(
    orderJson: String?,
    disabledTabs: Set<String> = emptySet()
): List<LibraryTabId> {
    val fullOrder = decodeLibraryTabOrder(orderJson)
    val active = fullOrder.filter { it.stableKey !in disabledTabs }
    return active.ifEmpty { listOf(fullOrder.first()) }
}