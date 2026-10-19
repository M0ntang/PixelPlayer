@file:OptIn(
    ExperimentalMaterial3ExpressiveApi::class,
    ExperimentalMaterial3Api::class
)

package com.theveloper.pixelplay.presentation.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.PlaylistAdd
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.CreateNewFolder
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.DriveFileMove
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.theveloper.pixelplay.R
import com.theveloper.pixelplay.data.model.Album
import com.theveloper.pixelplay.data.model.FavoriteAlbumFolder
import com.theveloper.pixelplay.data.preferences.HeaderCoverStyle
import com.theveloper.pixelplay.presentation.components.MiniPlayerHeight
import com.theveloper.pixelplay.presentation.viewmodel.PlayerViewModel
import kotlinx.coroutines.flow.MutableStateFlow

@UnstableApi
@Composable
fun LibraryFavoriteAlbumsTab(
    folders: List<FavoriteAlbumFolder>,
    albums: List<Album>,
    breadcrumbs: List<FavoriteAlbumFolder>,
    currentFolder: FavoriteAlbumFolder?,
    allFolders: List<FavoriteAlbumFolder>,
    isListView: Boolean,
    bottomBarHeight: Dp,
    playerViewModel: PlayerViewModel,
    onAlbumClick: (Long) -> Unit,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    isSelectionMode: Boolean = false,
    selectedAlbumIds: Set<Long> = emptySet(),
    onAlbumLongPress: (Album) -> Unit = {},
    onAlbumSelectionToggle: (Album) -> Unit = {},
    getSelectionIndex: (Long) -> Int? = { null }
) {
    val context = LocalContext.current
    val headerCoverStyle by playerViewModel.headerCoverStyle.collectAsStateWithLifecycle()
    val allLibraryAlbums by playerViewModel.albumsFlow.collectAsStateWithLifecycle()

    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var folderToRename by remember { mutableStateOf<FavoriteAlbumFolder?>(null) }
    var folderToDelete by remember { mutableStateOf<FavoriteAlbumFolder?>(null) }
    var albumToMove by remember { mutableStateOf<Album?>(null) }
    var showAddAlbumsSheet by remember { mutableStateOf(false) }
    var albumForActionMenu by remember { mutableStateOf<Album?>(null) }

    val pullToRefreshState = rememberPullToRefreshState()
    val gridState = rememberLazyGridState()
    val listState = rememberLazyListState()

    val isEmpty = folders.isEmpty() && albums.isEmpty()

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
            // Breadcrumbs Bar (if inside a folder)
            if (currentFolder != null) {
                BreadcrumbsBar(
                    breadcrumbs = breadcrumbs,
                    currentFolderName = currentFolder.name,
                    onNavigateBack = { playerViewModel.navigateBackFavoriteAlbumFolder() },
                    onNavigateToFolder = { folderId -> playerViewModel.navigateToFavoriteAlbumFolder(folderId) },
                    onNavigateToRoot = { playerViewModel.navigateToFavoriteAlbumFolder(null) }
                )
            }

            if (isEmpty) {
                // Empty state
                FavoriteAlbumsEmptyState(
                    isInsideFolder = currentFolder != null,
                    folderName = currentFolder?.name,
                    onAddAlbumsClick = { showAddAlbumsSheet = true },
                    onCreateFolderClick = { showCreateFolderDialog = true },
                    bottomBarHeight = bottomBarHeight
                )
            } else {
                if (isListView) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp, vertical = 4.dp),
                        contentPadding = PaddingValues(bottom = bottomBarHeight + MiniPlayerHeight + ListExtraBottomGap + 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Header actions (Play all, Shuffle, Add, New Folder)
                        item(key = "favorite_albums_header_actions") {
                            FavoriteAlbumsHeaderSection(
                                currentFolder = currentFolder,
                                folderCount = folders.size,
                                albumCount = albums.size,
                                onPlayAll = { playerViewModel.playFavoriteAlbumFolder(currentFolder?.id, shuffle = false) },
                                onShuffleAll = { playerViewModel.playFavoriteAlbumFolder(currentFolder?.id, shuffle = true) },
                                onAddAlbums = { showAddAlbumsSheet = true },
                                onCreateFolder = { showCreateFolderDialog = true },
                                onRenameFolder = { currentFolder?.let { folderToRename = it } },
                                onDeleteFolder = { currentFolder?.let { folderToDelete = it } }
                            )
                        }

                        // Folders list items
                        if (folders.isNotEmpty()) {
                            item(key = "folders_header_title") {
                                SectionHeader(text = stringResource(R.string.library_tab_folders))
                            }
                            items(
                                items = folders,
                                key = { "folder_${it.id}" }
                            ) { folder ->
                                FavoriteFolderListItem(
                                    folder = folder,
                                    onClick = { playerViewModel.navigateToFavoriteAlbumFolder(folder.id) },
                                    onPlay = { playerViewModel.playFavoriteAlbumFolder(folder.id, shuffle = false) },
                                    onShuffle = { playerViewModel.playFavoriteAlbumFolder(folder.id, shuffle = true) },
                                    onRename = { folderToRename = folder },
                                    onDelete = { folderToDelete = folder }
                                )
                            }
                        }

                        // Albums list items
                        if (albums.isNotEmpty()) {
                            item(key = "albums_header_title") {
                                SectionHeader(text = stringResource(R.string.library_tab_albums))
                            }
                            items(
                                items = albums,
                                key = { "album_${it.id}" }
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
                                        onSelectionToggle = { onAlbumSelectionToggle(album) },
                                        headerCoverStyle = headerCoverStyle
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Grid View
                    LazyVerticalGrid(
                        state = gridState,
                        columns = GridCells.Fixed(2),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp, vertical = 4.dp),
                        contentPadding = PaddingValues(bottom = bottomBarHeight + MiniPlayerHeight + ListExtraBottomGap + 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Header actions spanning full width
                        item(span = { GridItemSpan(2) }, key = "favorite_albums_header_actions") {
                            FavoriteAlbumsHeaderSection(
                                currentFolder = currentFolder,
                                folderCount = folders.size,
                                albumCount = albums.size,
                                onPlayAll = { playerViewModel.playFavoriteAlbumFolder(currentFolder?.id, shuffle = false) },
                                onShuffleAll = { playerViewModel.playFavoriteAlbumFolder(currentFolder?.id, shuffle = true) },
                                onAddAlbums = { showAddAlbumsSheet = true },
                                onCreateFolder = { showCreateFolderDialog = true },
                                onRenameFolder = { currentFolder?.let { folderToRename = it } },
                                onDeleteFolder = { currentFolder?.let { folderToDelete = it } }
                            )
                        }

                        // Folders Grid items
                        if (folders.isNotEmpty()) {
                            item(span = { GridItemSpan(2) }, key = "folders_header_title") {
                                SectionHeader(text = stringResource(R.string.library_tab_folders))
                            }
                            items(
                                items = folders,
                                key = { "folder_${it.id}" }
                            ) { folder ->
                                FavoriteFolderGridItem(
                                    folder = folder,
                                    onClick = { playerViewModel.navigateToFavoriteAlbumFolder(folder.id) },
                                    onPlay = { playerViewModel.playFavoriteAlbumFolder(folder.id, shuffle = false) },
                                    onShuffle = { playerViewModel.playFavoriteAlbumFolder(folder.id, shuffle = true) },
                                    onRename = { folderToRename = folder },
                                    onDelete = { folderToDelete = folder }
                                )
                            }
                        }

                        // Albums Grid items
                        if (albums.isNotEmpty()) {
                            item(span = { GridItemSpan(2) }, key = "albums_header_title") {
                                SectionHeader(text = stringResource(R.string.library_tab_albums))
                            }
                            items(
                                items = albums,
                                key = { "album_${it.id}" }
                            ) { album ->
                                val albumSpecificColorSchemeFlow = remember(album.albumArtUriString) {
                                    playerViewModel.themeStateHolder.getAlbumColorSchemeFlow(album.albumArtUriString ?: "")
                                }
                                Box(modifier = Modifier.fillMaxWidth()) {
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
    }

    // Dialog: Create Folder / Subfolder
    if (showCreateFolderDialog) {
        CreateFavoriteFolderDialog(
            isSubfolder = currentFolder != null,
            onDismiss = { showCreateFolderDialog = false },
            onCreate = { name ->
                playerViewModel.createFavoriteAlbumFolder(name, currentFolder?.id)
                showCreateFolderDialog = false
            }
        )
    }

    // Dialog: Rename Folder
    folderToRename?.let { folder ->
        RenameFavoriteFolderDialog(
            folder = folder,
            onDismiss = { folderToRename = null },
            onRename = { id, newName ->
                playerViewModel.renameFavoriteAlbumFolder(id, newName)
                folderToRename = null
            }
        )
    }

    // Dialog: Delete Folder
    folderToDelete?.let { folder ->
        DeleteFavoriteFolderDialog(
            folder = folder,
            onDismiss = { folderToDelete = null },
            onDelete = { id ->
                playerViewModel.deleteFavoriteAlbumFolder(id)
                folderToDelete = null
            }
        )
    }

    // Sheet: Move Album To Folder
    albumToMove?.let { album ->
        MoveAlbumToFolderSheet(
            album = album,
            allFolders = allFolders,
            onDismiss = { albumToMove = null },
            onMoveToFolder = { albumId, targetFolderId, targetFolderName ->
                playerViewModel.moveAlbumToFolder(albumId, targetFolderId)
                albumToMove = null
                val msg = context.getString(R.string.favorite_albums_toast_moved, targetFolderName)
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Sheet: Add Albums To Folder
    if (showAddAlbumsSheet) {
        val currentAlbumIds = remember(albums) { albums.map { it.id }.toSet() }
        AddAlbumsToFolderSheet(
            allAlbums = allLibraryAlbums,
            currentAlbumIds = currentAlbumIds,
            folderName = currentFolder?.name ?: stringResource(R.string.favorite_albums_root_folder),
            onDismiss = { showAddAlbumsSheet = false },
            onAdd = { selectedIds ->
                playerViewModel.addAlbumsToFavorites(selectedIds, currentFolder?.id)
                showAddAlbumsSheet = false
                val msg = context.getString(R.string.favorite_albums_toast_n_added, selectedIds.size)
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Sheet: Album Action Menu
    albumForActionMenu?.let { album ->
        FavoriteAlbumActionSheet(
            album = album,
            onDismiss = { albumForActionMenu = null },
            onPlay = {
                playerViewModel.playAlbum(album)
                albumForActionMenu = null
            },
            onMoveToFolder = {
                albumToMove = album
                albumForActionMenu = null
            },
            onRemoveFromFavorites = {
                playerViewModel.removeAlbumFromFavorites(album.id)
                albumForActionMenu = null
                Toast.makeText(context, context.getString(R.string.favorite_albums_toast_removed), Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 4.dp, horizontal = 2.dp)
    )
}

@Composable
private fun BreadcrumbsBar(
    breadcrumbs: List<FavoriteAlbumFolder>,
    currentFolderName: String,
    onNavigateBack: () -> Unit,
    onNavigateToFolder: (String) -> Unit,
    onNavigateToRoot: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onNavigateBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = stringResource(R.string.common_back)
            )
        }

        Row(
            modifier = Modifier
                .weight(1f)
                .horizontalScroll(rememberScrollState()),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Root
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier.clickable(onClick = onNavigateToRoot)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Home,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = stringResource(R.string.favorite_albums_root_folder),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Parents
            breadcrumbs.dropLast(1).forEach { folder ->
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    modifier = Modifier.clickable { onNavigateToFolder(folder.id) }
                ) {
                    Text(
                        text = folder.name,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            // Current folder (active)
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    text = currentFolderName,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun FavoriteAlbumsHeaderSection(
    currentFolder: FavoriteAlbumFolder?,
    folderCount: Int,
    albumCount: Int,
    onPlayAll: () -> Unit,
    onShuffleAll: () -> Unit,
    onAddAlbums: () -> Unit,
    onCreateFolder: () -> Unit,
    onRenameFolder: () -> Unit,
    onDeleteFolder: () -> Unit
) {
    if (currentFolder != null) {
        // Inside a folder: Folder Card Banner
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    FolderCoverCollage(
                        previewArtworkUris = currentFolder.previewArtUris,
                        modifier = Modifier.size(72.dp),
                        shape = RoundedCornerShape(16.dp)
                    )

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentFolder.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = stringResource(
                                R.string.favorite_albums_folder_subtitle,
                                currentFolder.subfolderCount.toString(),
                                currentFolder.albumCount.toString()
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    var showMenu by remember { mutableStateOf(false) }
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Rounded.MoreVert, contentDescription = null)
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.favorite_albums_rename_folder)) },
                                leadingIcon = { Icon(Icons.Rounded.Edit, null) },
                                onClick = {
                                    showMenu = false
                                    onRenameFolder()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.favorite_albums_delete_folder)) },
                                leadingIcon = { Icon(Icons.Rounded.Delete, null, tint = MaterialTheme.colorScheme.error) },
                                onClick = {
                                    showMenu = false
                                    onDeleteFolder()
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Actions row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (albumCount > 0) {
                        FilledTonalButton(
                            onClick = onPlayAll,
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Rounded.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(stringResource(R.string.favorite_albums_play_folder))
                        }
                        FilledTonalIconButton(onClick = onShuffleAll) {
                            Icon(Icons.Rounded.Shuffle, contentDescription = null, modifier = Modifier.size(18.dp))
                        }
                    }

                    AssistChip(
                        onClick = onAddAlbums,
                        label = { Text(stringResource(R.string.favorite_albums_add_albums)) },
                        leadingIcon = { Icon(Icons.Rounded.Add, null, modifier = Modifier.size(18.dp)) },
                        colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                    )

                    AssistChip(
                        onClick = onCreateFolder,
                        label = { Text(stringResource(R.string.favorite_albums_create_subfolder)) },
                        leadingIcon = { Icon(Icons.Rounded.CreateNewFolder, null, modifier = Modifier.size(18.dp)) },
                        colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                    )
                }
            }
        }
    } else {
        // Root: Clean Horizontal Actions Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (albumCount > 0) {
                FilledTonalButton(
                    onClick = onPlayAll,
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Rounded.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.favorite_albums_play_folder))
                }
                FilledTonalIconButton(onClick = onShuffleAll) {
                    Icon(Icons.Rounded.Shuffle, contentDescription = null, modifier = Modifier.size(18.dp))
                }
            }

            AssistChip(
                onClick = onCreateFolder,
                label = { Text(stringResource(R.string.favorite_albums_create_folder)) },
                leadingIcon = { Icon(Icons.Rounded.CreateNewFolder, null, modifier = Modifier.size(18.dp)) },
                colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
            )

            AssistChip(
                onClick = onAddAlbums,
                label = { Text(stringResource(R.string.favorite_albums_add_albums)) },
                leadingIcon = { Icon(Icons.Rounded.Add, null, modifier = Modifier.size(18.dp)) },
                colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
            )
        }
    }
}

@Composable
fun FolderCoverCollage(
    previewArtworkUris: List<String?>,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp)
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest),
        contentAlignment = Alignment.Center
    ) {
        val validUris = remember(previewArtworkUris) {
            previewArtworkUris.filterNotNull().filter { it.isNotBlank() }.take(4)
        }
        val context = LocalContext.current

        when (validUris.size) {
            0 -> {
                Icon(
                    imageVector = Icons.Rounded.Folder,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            1 -> {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(validUris[0])
                        .crossfade(true)
                        .error(R.drawable.ic_music_placeholder)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            else -> {
                // 2x2 collage
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(validUris.getOrNull(0))
                                    .crossfade(true)
                                    .error(R.drawable.ic_music_placeholder)
                                    .build(),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(validUris.getOrNull(1))
                                    .crossfade(true)
                                    .error(R.drawable.ic_music_placeholder)
                                    .build(),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                    Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(validUris.getOrNull(2))
                                    .crossfade(true)
                                    .error(R.drawable.ic_music_placeholder)
                                    .build(),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(validUris.getOrNull(3) ?: validUris.getOrNull(0))
                                    .crossfade(true)
                                    .error(R.drawable.ic_music_placeholder)
                                    .build(),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }

        // Corner Folder Badge
        Surface(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(4.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
            tonalElevation = 2.dp
        ) {
            Icon(
                imageVector = Icons.Rounded.Folder,
                contentDescription = null,
                modifier = Modifier
                    .padding(3.dp)
                    .size(12.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun FavoriteFolderGridItem(
    folder: FavoriteAlbumFolder,
    onClick: () -> Unit,
    onPlay: () -> Unit,
    onShuffle: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                FolderCoverCollage(
                    previewArtworkUris = folder.previewArtUris,
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(14.dp)
                )

                // Menu button in top right corner
                Box(modifier = Modifier.align(Alignment.TopEnd)) {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.MoreVert,
                                contentDescription = null,
                                modifier = Modifier
                                    .padding(4.dp)
                                    .size(16.dp)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.favorite_albums_play_folder)) },
                            leadingIcon = { Icon(Icons.Rounded.PlayArrow, null) },
                            onClick = {
                                showMenu = false
                                onPlay()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.favorite_albums_shuffle_folder)) },
                            leadingIcon = { Icon(Icons.Rounded.Shuffle, null) },
                            onClick = {
                                showMenu = false
                                onShuffle()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.favorite_albums_rename_folder)) },
                            leadingIcon = { Icon(Icons.Rounded.Edit, null) },
                            onClick = {
                                showMenu = false
                                onRename()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.favorite_albums_delete_folder)) },
                            leadingIcon = { Icon(Icons.Rounded.Delete, null, tint = MaterialTheme.colorScheme.error) },
                            onClick = {
                                showMenu = false
                                onDelete()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = folder.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            val infoText = when {
                folder.subfolderCount > 0 && folder.albumCount > 0 ->
                    stringResource(R.string.favorite_albums_folder_subtitle, folder.subfolderCount.toString(), folder.albumCount.toString())
                folder.subfolderCount > 0 ->
                    stringResource(R.string.favorite_albums_folders_count, folder.subfolderCount)
                else ->
                    stringResource(R.string.favorite_albums_folder_item_desc, folder.albumCount)
            }

            Text(
                text = infoText,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
private fun FavoriteFolderListItem(
    folder: FavoriteAlbumFolder,
    onClick: () -> Unit,
    onPlay: () -> Unit,
    onShuffle: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FolderCoverCollage(
                previewArtworkUris = folder.previewArtUris,
                modifier = Modifier.size(54.dp),
                shape = RoundedCornerShape(12.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = folder.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                val infoText = when {
                    folder.subfolderCount > 0 && folder.albumCount > 0 ->
                        stringResource(R.string.favorite_albums_folder_subtitle, folder.subfolderCount.toString(), folder.albumCount.toString())
                    folder.subfolderCount > 0 ->
                        stringResource(R.string.favorite_albums_folders_count, folder.subfolderCount)
                    else ->
                        stringResource(R.string.favorite_albums_folder_item_desc, folder.albumCount)
                }
                Text(
                    text = infoText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        imageVector = Icons.Rounded.MoreVert,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.favorite_albums_play_folder)) },
                        leadingIcon = { Icon(Icons.Rounded.PlayArrow, null) },
                        onClick = {
                            showMenu = false
                            onPlay()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.favorite_albums_shuffle_folder)) },
                        leadingIcon = { Icon(Icons.Rounded.Shuffle, null) },
                        onClick = {
                            showMenu = false
                            onShuffle()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.favorite_albums_rename_folder)) },
                        leadingIcon = { Icon(Icons.Rounded.Edit, null) },
                        onClick = {
                            showMenu = false
                            onRename()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.favorite_albums_delete_folder)) },
                        leadingIcon = { Icon(Icons.Rounded.Delete, null, tint = MaterialTheme.colorScheme.error) },
                        onClick = {
                            showMenu = false
                            onDelete()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun FavoriteAlbumsEmptyState(
    isInsideFolder: Boolean,
    folderName: String?,
    onAddAlbumsClick: () -> Unit,
    onCreateFolderClick: () -> Unit,
    bottomBarHeight: Dp
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .padding(bottom = bottomBarHeight + MiniPlayerHeight),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier.size(80.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isInsideFolder) Icons.Rounded.FolderOpen else Icons.Rounded.Favorite,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Text(
                text = if (isInsideFolder)
                    stringResource(R.string.favorite_albums_empty_folder_title)
                else
                    stringResource(R.string.favorite_albums_empty_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = if (isInsideFolder)
                    stringResource(R.string.favorite_albums_empty_folder_desc)
                else
                    stringResource(R.string.favorite_albums_empty_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledTonalButton(onClick = onAddAlbumsClick) {
                    Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.favorite_albums_add_albums))
                }

                OutlinedButton(onClick = onCreateFolderClick) {
                    Icon(Icons.Rounded.CreateNewFolder, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        if (isInsideFolder)
                            stringResource(R.string.favorite_albums_create_subfolder)
                        else
                            stringResource(R.string.favorite_albums_create_folder)
                    )
                }
            }
        }
    }
}

@Composable
private fun CreateFavoriteFolderDialog(
    isSubfolder: Boolean,
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit
) {
    var folderName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Rounded.CreateNewFolder, contentDescription = null) },
        title = {
            Text(
                if (isSubfolder)
                    stringResource(R.string.favorite_albums_create_subfolder)
                else
                    stringResource(R.string.favorite_albums_create_folder)
            )
        },
        text = {
            OutlinedTextField(
                value = folderName,
                onValueChange = { folderName = it },
                label = { Text(stringResource(R.string.favorite_albums_folder_name_hint)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onCreate(folderName) },
                enabled = folderName.isNotBlank()
            ) {
                Text(stringResource(R.string.common_create))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_cancel))
            }
        }
    )
}

@Composable
private fun RenameFavoriteFolderDialog(
    folder: FavoriteAlbumFolder,
    onDismiss: () -> Unit,
    onRename: (String, String) -> Unit
) {
    var newName by remember(folder) { mutableStateOf(folder.name) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Rounded.Edit, contentDescription = null) },
        title = { Text(stringResource(R.string.favorite_albums_rename_folder)) },
        text = {
            OutlinedTextField(
                value = newName,
                onValueChange = { newName = it },
                label = { Text(stringResource(R.string.favorite_albums_folder_name_hint)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onRename(folder.id, newName) },
                enabled = newName.isNotBlank()
            ) {
                Text(stringResource(R.string.common_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_cancel))
            }
        }
    )
}

@Composable
private fun DeleteFavoriteFolderDialog(
    folder: FavoriteAlbumFolder,
    onDismiss: () -> Unit,
    onDelete: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Rounded.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
        title = { Text(stringResource(R.string.favorite_albums_delete_folder)) },
        text = { Text(stringResource(R.string.favorite_albums_delete_folder_confirm, folder.name)) },
        confirmButton = {
            TextButton(
                onClick = { onDelete(folder.id) }
            ) {
                Text(stringResource(R.string.common_delete), color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_cancel))
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MoveAlbumToFolderSheet(
    album: Album,
    allFolders: List<FavoriteAlbumFolder>,
    onDismiss: () -> Unit,
    onMoveToFolder: (albumId: Long, targetFolderId: String?, targetFolderName: String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    val rootName = stringResource(R.string.favorite_albums_root_folder)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            Text(
                text = stringResource(R.string.favorite_albums_move_to_folder),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )

            Text(
                text = album.title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 2.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider()

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                // Option: Root / Main Favorites
                item {
                    ListItem(
                        headlineContent = { Text(rootName, fontWeight = FontWeight.SemiBold) },
                        leadingContent = {
                            Icon(Icons.Rounded.Home, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        },
                        modifier = Modifier.clickable {
                            onMoveToFolder(album.id, null, rootName)
                        }
                    )
                }

                // All folders
                items(allFolders, key = { it.id }) { folder ->
                    ListItem(
                        headlineContent = { Text(folder.name) },
                        supportingContent = {
                            Text(stringResource(R.string.favorite_albums_folder_item_desc, folder.albumCount))
                        },
                        leadingContent = {
                            Icon(Icons.Rounded.Folder, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                        },
                        modifier = Modifier.clickable {
                            onMoveToFolder(album.id, folder.id, folder.name)
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddAlbumsToFolderSheet(
    allAlbums: List<Album>,
    currentAlbumIds: Set<Long>,
    folderName: String,
    onDismiss: () -> Unit,
    onAdd: (List<Long>) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var searchQuery by remember { mutableStateOf("") }
    val selectedIds = remember { mutableStateListOf<Long>() }

    // Filter available albums (excluding ones already in this folder)
    val availableAlbums = remember(allAlbums, currentAlbumIds, searchQuery) {
        val filtered = allAlbums.filterNot { currentAlbumIds.contains(it.id) }
        if (searchQuery.isBlank()) {
            filtered
        } else {
            val q = searchQuery.trim().lowercase()
            filtered.filter {
                it.title.lowercase().contains(q) || it.artist.lowercase().contains(q)
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.favorite_albums_add_albums),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = folderName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = { onAdd(selectedIds.toList()) },
                    enabled = selectedIds.isNotEmpty()
                ) {
                    Text(stringResource(R.string.favorite_albums_add_selected, selectedIds.size))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(stringResource(R.string.favorite_albums_search_hint)) },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Rounded.Clear, contentDescription = null)
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Select all / Deselect all row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.favorite_albums_count, availableAlbums.size),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                TextButton(
                    onClick = {
                        if (selectedIds.size == availableAlbums.size) {
                            selectedIds.clear()
                        } else {
                            selectedIds.clear()
                            selectedIds.addAll(availableAlbums.map { it.id })
                        }
                    }
                ) {
                    Text(
                        if (selectedIds.size == availableAlbums.size && availableAlbums.isNotEmpty())
                            stringResource(R.string.queue_save_as_playlist_deselect_all)
                        else
                            stringResource(R.string.common_select_all)
                    )
                }
            }

            HorizontalDivider()

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp),
                contentPadding = PaddingValues(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(availableAlbums, key = { it.id }) { album ->
                    val isChecked = selectedIds.contains(album.id)
                    ListItem(
                        headlineContent = {
                            Text(album.title, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        },
                        supportingContent = {
                            Text(album.artist, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        },
                        leadingContent = {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(album.albumArtUriString)
                                    .crossfade(true)
                                    .error(R.drawable.ic_music_placeholder)
                                    .build(),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                        },
                        trailingContent = {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = { checked ->
                                    if (checked) selectedIds.add(album.id) else selectedIds.remove(album.id)
                                }
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (isChecked) selectedIds.remove(album.id) else selectedIds.add(album.id)
                            }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FavoriteAlbumActionSheet(
    album: Album,
    onDismiss: () -> Unit,
    onPlay: () -> Unit,
    onMoveToFolder: () -> Unit,
    onRemoveFromFavorites: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            ListItem(
                headlineContent = { Text(album.title, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                supportingContent = { Text(album.artist, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                leadingContent = {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(album.albumArtUriString)
                            .crossfade(true)
                            .error(R.drawable.ic_music_placeholder)
                            .build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(10.dp))
                    )
                }
            )

            HorizontalDivider()

            ListItem(
                headlineContent = { Text(stringResource(R.string.common_play_album)) },
                leadingContent = { Icon(Icons.Rounded.PlayArrow, null) },
                modifier = Modifier.clickable(onClick = onPlay)
            )

            ListItem(
                headlineContent = { Text(stringResource(R.string.favorite_albums_move_to_folder)) },
                leadingContent = { Icon(Icons.Rounded.DriveFileMove, null) },
                modifier = Modifier.clickable(onClick = onMoveToFolder)
            )

            ListItem(
                headlineContent = { Text(stringResource(R.string.favorite_albums_remove_from_favorites), color = MaterialTheme.colorScheme.error) },
                leadingContent = { Icon(Icons.Rounded.Delete, null, tint = MaterialTheme.colorScheme.error) },
                modifier = Modifier.clickable(onClick = onRemoveFromFavorites)
            )
        }
    }
}
