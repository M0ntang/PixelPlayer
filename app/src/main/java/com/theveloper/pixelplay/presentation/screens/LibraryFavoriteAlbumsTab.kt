@file:OptIn(
    ExperimentalMaterial3ExpressiveApi::class,
    ExperimentalMaterial3Api::class
)

package com.theveloper.pixelplay.presentation.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import com.theveloper.pixelplay.R
import com.theveloper.pixelplay.data.model.Album
import com.theveloper.pixelplay.data.model.Playlist
import com.theveloper.pixelplay.data.model.Song
import com.theveloper.pixelplay.data.preferences.HeaderCoverStyle
import com.theveloper.pixelplay.presentation.components.MiniPlayerHeight
import com.theveloper.pixelplay.presentation.components.PlaylistCover
import com.theveloper.pixelplay.presentation.components.PlaylistItem
import com.theveloper.pixelplay.presentation.viewmodel.PlayerViewModel
import com.theveloper.pixelplay.ui.theme.GoogleSansRounded
import com.theveloper.pixelplay.utils.formatSongCount
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private enum class FavoriteFilter {
    ALL,
    ALBUMS,
    PLAYLISTS
}

@UnstableApi
@Composable
fun LibraryFavoriteAlbumsTab(
    albums: List<Album>,
    playlists: List<Playlist>,
    isListView: Boolean,
    bottomBarHeight: Dp,
    playerViewModel: PlayerViewModel,
    onAlbumClick: (Long) -> Unit,
    onPlaylistClick: (String) -> Unit,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    isSelectionMode: Boolean = false,
    selectedAlbumIds: Set<Long> = emptySet(),
    onAlbumLongPress: (Album) -> Unit = {},
    onAlbumSelectionToggle: (Album) -> Unit = {},
    getSelectionIndex: (Long) -> Int? = { null }
) {
    val headerCoverStyle by playerViewModel.headerCoverStyle.collectAsStateWithLifecycle()

    var selectedFilter by remember { mutableStateOf(FavoriteFilter.ALL) }
    var albumForActionMenu by remember { mutableStateOf<Album?>(null) }
    var playlistForActionMenu by remember { mutableStateOf<Playlist?>(null) }

    val pullToRefreshState = rememberPullToRefreshState()
    val gridState = rememberLazyGridState()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val displayedAlbums = remember(albums, selectedFilter) {
        if (selectedFilter == FavoriteFilter.ALL || selectedFilter == FavoriteFilter.ALBUMS) albums else emptyList()
    }
    val displayedPlaylists = remember(playlists, selectedFilter) {
        if (selectedFilter == FavoriteFilter.ALL || selectedFilter == FavoriteFilter.PLAYLISTS) playlists else emptyList()
    }

    val isEntirelyEmpty = albums.isEmpty() && playlists.isEmpty()
    val isFilteredEmpty = displayedAlbums.isEmpty() && displayedPlaylists.isEmpty()

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        state = pullToRefreshState,
        modifier = Modifier.fillMaxSize(),
        indicator = {
            PullToRefreshDefaults.LoadingIndicator(
                state = pullToRefreshState,
                isRefreshing = isRefreshing,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Filter chips row
            if (!isEntirelyEmpty) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FavoriteFilterChip(
                        label = stringResource(R.string.common_all),
                        count = albums.size + playlists.size,
                        selected = selectedFilter == FavoriteFilter.ALL,
                        onClick = { selectedFilter = FavoriteFilter.ALL }
                    )
                    FavoriteFilterChip(
                        label = stringResource(R.string.library_tab_albums),
                        count = albums.size,
                        selected = selectedFilter == FavoriteFilter.ALBUMS,
                        onClick = { selectedFilter = FavoriteFilter.ALBUMS }
                    )
                    FavoriteFilterChip(
                        label = stringResource(R.string.library_tab_playlists),
                        count = playlists.size,
                        selected = selectedFilter == FavoriteFilter.PLAYLISTS,
                        onClick = { selectedFilter = FavoriteFilter.PLAYLISTS }
                    )
                }
            }

            if (isEntirelyEmpty || isFilteredEmpty) {
                FavoriteAlbumsEmptyState(bottomBarHeight = bottomBarHeight)
            } else {
                val showBothSections = selectedFilter == FavoriteFilter.ALL && displayedPlaylists.isNotEmpty() && displayedAlbums.isNotEmpty()

                if (isListView) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp, vertical = 4.dp),
                        contentPadding = PaddingValues(bottom = bottomBarHeight + MiniPlayerHeight + ListExtraBottomGap + 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (displayedPlaylists.isNotEmpty()) {
                            if (showBothSections) {
                                item(key = "fav_playlists_section_header") {
                                    SectionHeader(text = stringResource(R.string.library_tab_playlists))
                                }
                            }
                            items(
                                items = displayedPlaylists,
                                key = { "fav_playlist_${it.id}" }
                            ) { playlist ->
                                PlaylistItem(
                                    playlist = playlist,
                                    playerViewModel = playerViewModel,
                                    onClick = { onPlaylistClick(playlist.id) },
                                    isAddingToPlaylist = false,
                                    isSelectionMode = false,
                                    isSelected = false,
                                    onLongPress = { playlistForActionMenu = playlist }
                                )
                            }
                        }

                        if (displayedAlbums.isNotEmpty()) {
                            if (showBothSections) {
                                item(key = "fav_albums_section_header") {
                                    SectionHeader(text = stringResource(R.string.library_tab_albums))
                                }
                            }
                            items(
                                items = displayedAlbums,
                                key = { "fav_album_${it.id}" }
                            ) { album ->
                                val albumSpecificColorSchemeFlow = remember(album.albumArtUriString) {
                                    playerViewModel.themeStateHolder.getAlbumColorSchemeFlow(album.albumArtUriString ?: "")
                                }
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    AlbumListItem(
                                        album = album,
                                        albumColorSchemePairFlow = albumSpecificColorSchemeFlow,
                                        onClick = {
                                            if (isSelectionMode) {
                                                onAlbumSelectionToggle(album)
                                            } else {
                                                onAlbumClick(album.id)
                                            }
                                        },
                                        isLoading = false,
                                        isSelectionMode = isSelectionMode,
                                        isSelected = selectedAlbumIds.contains(album.id),
                                        selectionIndex = getSelectionIndex(album.id),
                                        onLongPress = {
                                            if (isSelectionMode) {
                                                onAlbumLongPress(album)
                                            } else {
                                                albumForActionMenu = album
                                            }
                                        },
                                        onSelectionToggle = { onAlbumSelectionToggle(album) }
                                    )
                                }
                            }
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        state = gridState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp, vertical = 4.dp),
                        contentPadding = PaddingValues(bottom = bottomBarHeight + MiniPlayerHeight + ListExtraBottomGap + 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (displayedPlaylists.isNotEmpty()) {
                            if (showBothSections) {
                                item(
                                    key = "fav_playlists_grid_header",
                                    span = { GridItemSpan(maxLineSpan) }
                                ) {
                                    SectionHeader(text = stringResource(R.string.library_tab_playlists))
                                }
                            }
                            items(
                                items = displayedPlaylists,
                                key = { "fav_playlist_grid_${it.id}" }
                            ) { playlist ->
                                FavoritePlaylistGridItem(
                                    playlist = playlist,
                                    playerViewModel = playerViewModel,
                                    onClick = { onPlaylistClick(playlist.id) },
                                    onLongClick = { playlistForActionMenu = playlist }
                                )
                            }
                        }

                        if (displayedAlbums.isNotEmpty()) {
                            if (showBothSections) {
                                item(
                                    key = "fav_albums_grid_header",
                                    span = { GridItemSpan(maxLineSpan) }
                                ) {
                                    SectionHeader(text = stringResource(R.string.library_tab_albums))
                                }
                            }
                            items(
                                items = displayedAlbums,
                                key = { "fav_album_grid_${it.id}" }
                            ) { album ->
                                val albumSpecificColorSchemeFlow = remember(album.albumArtUriString) {
                                    playerViewModel.themeStateHolder.getAlbumColorSchemeFlow(album.albumArtUriString ?: "")
                                }
                                AlbumGridItemRedesigned(
                                    album = album,
                                    albumColorSchemePairFlow = albumSpecificColorSchemeFlow,
                                    onClick = {
                                        if (isSelectionMode) {
                                            onAlbumSelectionToggle(album)
                                        } else {
                                            onAlbumClick(album.id)
                                        }
                                    },
                                    isLoading = false,
                                    isSelectionMode = isSelectionMode,
                                    isSelected = selectedAlbumIds.contains(album.id),
                                    selectionIndex = getSelectionIndex(album.id),
                                    onLongPress = {
                                        if (isSelectionMode) {
                                            onAlbumLongPress(album)
                                        } else {
                                            albumForActionMenu = album
                                        }
                                    },
                                    onSelectionToggle = { onAlbumSelectionToggle(album) },
                                    headerCoverStyle = headerCoverStyle
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Context sheet for Album
    albumForActionMenu?.let { album ->
        FavoriteAlbumActionSheet(
            album = album,
            onDismiss = { albumForActionMenu = null },
            onPlay = {
                playerViewModel.playAlbum(album)
                albumForActionMenu = null
            },
            onShuffle = {
                coroutineScope.launch {
                    val songs = playerViewModel.getSongsForAlbums(listOf(album))
                    if (songs.isNotEmpty()) {
                        playerViewModel.playSongsShuffled(songs, album.title, startAtZero = true)
                    }
                }
                albumForActionMenu = null
            },
            onRemoveFromFavorites = {
                playerViewModel.toggleFavoriteAlbum(album.id)
                albumForActionMenu = null
            }
        )
    }

    // Context sheet for Playlist
    playlistForActionMenu?.let { playlist ->
        FavoritePlaylistActionSheet(
            playlist = playlist,
            onDismiss = { playlistForActionMenu = null },
            onPlay = {
                coroutineScope.launch {
                    val songs = playerViewModel.observeSongs(playlist.songIds).first()
                    if (songs.isNotEmpty()) {
                        playerViewModel.playSongs(songs, songs.first(), playlist.name)
                    }
                }
                playlistForActionMenu = null
            },
            onShuffle = {
                coroutineScope.launch {
                    val songs = playerViewModel.observeSongs(playlist.songIds).first()
                    if (songs.isNotEmpty()) {
                        playerViewModel.playSongsShuffled(songs, playlist.name, playlistId = playlist.id, startAtZero = true)
                    }
                }
                playlistForActionMenu = null
            },
            onRemoveFromFavorites = {
                playerViewModel.toggleFavoritePlaylist(playlist.id)
                playlistForActionMenu = null
            }
        )
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium.copy(fontFamily = GoogleSansRounded),
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 6.dp, top = 8.dp, bottom = 4.dp)
    )
}

@Composable
private fun FavoriteFilterChip(
    label: String,
    count: Int? = null,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            val text = if (count != null && count > 0) "$label ($count)" else label
            Text(
                text = text,
                fontFamily = GoogleSansRounded,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
            )
        },
        shape = CircleShape,
        border = BorderStroke(width = 0.dp, color = Color.Transparent),
        colors = FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
        ),
        leadingIcon = if (selected) {
            {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                )
            }
        } else null
    )
}

@Composable
fun FavoritePlaylistGridItem(
    playlist: Playlist,
    playerViewModel: PlayerViewModel,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val playlistPreviewSongIds = remember(playlist.songIds) { playlist.songIds.take(4) }
    val playlistSongs by remember(playlistPreviewSongIds, playerViewModel) {
        playerViewModel.observeSongs(playlistPreviewSongIds)
    }.collectAsStateWithLifecycle(initialValue = emptyList())

    val cardCornerRadius = 20.dp
    val cardShape = RoundedCornerShape(cardCornerRadius)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(cardShape)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = cardShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                contentAlignment = Alignment.Center
            ) {
                PlaylistCover(
                    playlist = playlist,
                    playlistSongs = playlistSongs,
                    size = 100.dp
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(84.dp)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = playlist.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontFamily = GoogleSansRounded),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatSongCount(playlist.songIds.size),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun FavoriteAlbumsEmptyState(
    bottomBarHeight: Dp
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                start = 28.dp,
                end = 28.dp,
                bottom = bottomBarHeight + MiniPlayerHeight + 24.dp
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f),
                tonalElevation = 2.dp
            ) {
                Box(
                    modifier = Modifier.size(64.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Favorite,
                        contentDescription = null,
                        modifier = Modifier.size(36.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = stringResource(R.string.favorite_albums_empty_title),
                    style = MaterialTheme.typography.titleLarge.copy(fontFamily = GoogleSansRounded),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.favorite_albums_empty_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }
        }
    }
}

@Composable
private fun FavoriteAlbumActionSheet(
    album: Album,
    onDismiss: () -> Unit,
    onPlay: () -> Unit,
    onShuffle: () -> Unit,
    onRemoveFromFavorites: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = album.title,
                    style = MaterialTheme.typography.titleLarge.copy(fontFamily = GoogleSansRounded),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = album.artist,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            FavoriteActionItem(
                icon = Icons.Rounded.PlayArrow,
                label = stringResource(R.string.common_play),
                onClick = onPlay
            )
            FavoriteActionItem(
                icon = Icons.Rounded.Shuffle,
                label = stringResource(R.string.common_shuffle),
                onClick = onShuffle
            )
            FavoriteActionItem(
                icon = Icons.Rounded.Favorite,
                label = stringResource(R.string.favorite_albums_remove),
                onClick = onRemoveFromFavorites,
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun FavoritePlaylistActionSheet(
    playlist: Playlist,
    onDismiss: () -> Unit,
    onPlay: () -> Unit,
    onShuffle: () -> Unit,
    onRemoveFromFavorites: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = playlist.name,
                    style = MaterialTheme.typography.titleLarge.copy(fontFamily = GoogleSansRounded),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = formatSongCount(playlist.songIds.size),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            FavoriteActionItem(
                icon = Icons.Rounded.PlayArrow,
                label = stringResource(R.string.common_play),
                onClick = onPlay
            )
            FavoriteActionItem(
                icon = Icons.Rounded.Shuffle,
                label = stringResource(R.string.common_shuffle),
                onClick = onShuffle
            )
            FavoriteActionItem(
                icon = Icons.Rounded.Favorite,
                label = stringResource(R.string.favorite_albums_remove),
                onClick = onRemoveFromFavorites,
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun FavoriteActionItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    tint: Color = MaterialTheme.colorScheme.primary
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tint,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
