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
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.rounded.CreateNewFolder
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.automirrored.rounded.DriveFileMove
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
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
import com.theveloper.pixelplay.presentation.components.ditheredCardScrim
import com.theveloper.pixelplay.presentation.components.ditheredHorizontalCardScrim
import com.theveloper.pixelplay.presentation.viewmodel.PlayerViewModel
import com.theveloper.pixelplay.ui.theme.GoogleSansRounded

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

    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var folderToRename by remember { mutableStateOf<FavoriteAlbumFolder?>(null) }
    var folderToDelete by remember { mutableStateOf<FavoriteAlbumFolder?>(null) }
    var albumToMove by remember { mutableStateOf<Album?>(null) }
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
            // Breadcrumbs Bar (when inside a folder)
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
                FavoriteAlbumsEmptyState(
                    isInsideFolder = currentFolder != null,
                    folderName = currentFolder?.name,
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
                        // When inside a folder: render the folder header banner
                        if (currentFolder != null) {
                            item(key = "favorite_albums_folder_banner") {
                                FavoriteAlbumsFolderHeaderBanner(
                                    currentFolder = currentFolder,
                                    albumCount = albums.size,
                                    onPlayAll = { playerViewModel.playFavoriteAlbumFolder(currentFolder.id, shuffle = false) },
                                    onShuffleAll = { playerViewModel.playFavoriteAlbumFolder(currentFolder.id, shuffle = true) },
                                    onCreateSubfolder = { showCreateFolderDialog = true },
                                    onRenameFolder = { folderToRename = currentFolder },
                                    onDeleteFolder = { folderToDelete = currentFolder }
                                )
                            }
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
                                    headerCoverStyle = headerCoverStyle,
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
                        // When inside a folder: render the folder header banner spanning full width
                        if (currentFolder != null) {
                            item(span = { GridItemSpan(2) }, key = "favorite_albums_folder_banner") {
                                FavoriteAlbumsFolderHeaderBanner(
                                    currentFolder = currentFolder,
                                    albumCount = albums.size,
                                    onPlayAll = { playerViewModel.playFavoriteAlbumFolder(currentFolder.id, shuffle = false) },
                                    onShuffleAll = { playerViewModel.playFavoriteAlbumFolder(currentFolder.id, shuffle = true) },
                                    onCreateSubfolder = { showCreateFolderDialog = true },
                                    onRenameFolder = { folderToRename = currentFolder },
                                    onDeleteFolder = { folderToDelete = currentFolder }
                                )
                            }
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
                                    headerCoverStyle = headerCoverStyle,
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
            onCreateFolder = { showCreateFolderDialog = true },
            onMoveToFolder = { albumId, targetFolderId, targetFolderName ->
                playerViewModel.moveAlbumToFolder(albumId, targetFolderId)
                albumToMove = null
                val msg = context.getString(R.string.favorite_albums_toast_moved, targetFolderName)
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Sheet: Album Action Menu (Screenshot 3 replacement)
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
            onCreateFolder = {
                albumForActionMenu = null
                showCreateFolderDialog = true
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
        style = MaterialTheme.typography.titleMedium.copy(fontFamily = GoogleSansRounded),
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

/**
 * Modern expressive banner shown inside a folder.
 */
@Composable
private fun FavoriteAlbumsFolderHeaderBanner(
    currentFolder: FavoriteAlbumFolder,
    albumCount: Int,
    onPlayAll: () -> Unit,
    onShuffleAll: () -> Unit,
    onCreateSubfolder: () -> Unit,
    onRenameFolder: () -> Unit,
    onDeleteFolder: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FolderCoverCollage(
                    previewArtworkUris = currentFolder.previewArtUris,
                    modifier = Modifier.size(80.dp),
                    shape = RoundedCornerShape(20.dp)
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = currentFolder.name,
                        style = MaterialTheme.typography.titleLarge.copy(fontFamily = GoogleSansRounded),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    val infoText = when {
                        currentFolder.subfolderCount > 0 && currentFolder.albumCount > 0 ->
                            stringResource(
                                R.string.favorite_albums_folder_subtitle,
                                currentFolder.subfolderCount.toString(),
                                currentFolder.albumCount.toString()
                            )
                        currentFolder.subfolderCount > 0 ->
                            stringResource(R.string.favorite_albums_folders_count, currentFolder.subfolderCount)
                        else ->
                            stringResource(R.string.favorite_albums_folder_item_desc, currentFolder.albumCount)
                    }
                    Text(
                        text = infoText,
                        style = MaterialTheme.typography.bodyMedium,
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

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (albumCount > 0) {
                    FilledTonalButton(
                        onClick = onPlayAll,
                        shape = CircleShape,
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Rounded.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(stringResource(R.string.favorite_albums_play_folder), style = MaterialTheme.typography.labelLarge)
                    }

                    FilledTonalIconButton(
                        onClick = onShuffleAll,
                        shape = CircleShape,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(Icons.Rounded.Shuffle, contentDescription = null, modifier = Modifier.size(18.dp))
                    }
                }

                FilledTonalIconButton(
                    onClick = onCreateSubfolder,
                    shape = CircleShape,
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                    ),
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CreateNewFolder,
                        contentDescription = stringResource(R.string.favorite_albums_create_subfolder),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

/**
 * 2x2 collage artwork or stylized folder placeholder for folders.
 */
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
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                                    MaterialTheme.colorScheme.surfaceContainerHighest
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.FolderOpen,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
                    )
                }
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

/**
 * Redesigned Folder Grid Item matching [AlbumGridItemRedesigned] aesthetic.
 */
@Composable
private fun FavoriteFolderGridItem(
    folder: FavoriteAlbumFolder,
    headerCoverStyle: String,
    onClick: () -> Unit,
    onPlay: () -> Unit,
    onShuffle: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit
) {
    val isSquareMode = headerCoverStyle == HeaderCoverStyle.SQUARE
    val currentMaterialColorScheme = MaterialTheme.colorScheme
    val gradientBaseColor = currentMaterialColorScheme.primaryContainer
    val onGradientColor = currentMaterialColorScheme.onPrimaryContainer
    val cardCornerRadius = 20.dp
    val cardShape = RoundedCornerShape(cardCornerRadius)
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(cardShape)
            .clickable(onClick = onClick),
        shape = cardShape,
        colors = CardDefaults.cardColors(
            containerColor = currentMaterialColorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Box {
            Column(
                modifier = Modifier.background(
                    color = gradientBaseColor,
                    shape = cardShape
                )
            ) {
                Box(contentAlignment = Alignment.BottomStart) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(if (isSquareMode) 1f else 3f / 2f)
                    ) {
                        FolderCoverCollage(
                            previewArtworkUris = folder.previewArtUris,
                            modifier = Modifier.fillMaxSize(),
                            shape = RoundedCornerShape(topStart = cardCornerRadius, topEnd = cardCornerRadius)
                        )
                    }

                    if (!isSquareMode) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .ditheredCardScrim(gradientBaseColor)
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(84.dp)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = folder.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontFamily = GoogleSansRounded),
                        fontWeight = FontWeight.Bold,
                        color = onGradientColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = stringResource(R.string.favorite_albums_folder_type_label),
                        style = MaterialTheme.typography.bodySmall,
                        color = onGradientColor.copy(alpha = 0.85f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
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
                        style = MaterialTheme.typography.bodySmall,
                        color = onGradientColor.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Sleek Options Button in TopEnd
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
            ) {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Rounded.MoreVert,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurface
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

/**
 * Redesigned Folder List Item matching [AlbumListItem] aesthetic.
 */
@Composable
private fun FavoriteFolderListItem(
    folder: FavoriteAlbumFolder,
    headerCoverStyle: String,
    onClick: () -> Unit,
    onPlay: () -> Unit,
    onShuffle: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit
) {
    val isSquareMode = headerCoverStyle == HeaderCoverStyle.SQUARE
    val currentMaterialColorScheme = MaterialTheme.colorScheme
    val gradientBaseColor = currentMaterialColorScheme.primaryContainer
    val onGradientColor = currentMaterialColorScheme.onPrimaryContainer
    val cardCornerRadius = 16.dp
    val cardShape = RoundedCornerShape(cardCornerRadius)
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(88.dp)
            .clip(cardShape)
            .clickable(onClick = onClick),
        shape = cardShape,
        colors = CardDefaults.cardColors(
            containerColor = currentMaterialColorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Row(modifier = Modifier.fillMaxSize()) {
                // LEFT: Folder Collage
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .fillMaxHeight()
                ) {
                    FolderCoverCollage(
                        previewArtworkUris = folder.previewArtUris,
                        modifier = Modifier.fillMaxSize(),
                        shape = RoundedCornerShape(topStart = cardCornerRadius, bottomStart = cardCornerRadius)
                    )

                    if (!isSquareMode) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .ditheredHorizontalCardScrim(gradientBaseColor)
                        )
                    }
                }

                // RIGHT: Content
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(gradientBaseColor)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = folder.name,
                                style = MaterialTheme.typography.titleMedium.copy(fontFamily = GoogleSansRounded),
                                fontWeight = FontWeight.Bold,
                                color = onGradientColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = stringResource(R.string.favorite_albums_folder_type_label),
                                style = MaterialTheme.typography.bodySmall,
                                color = onGradientColor.copy(alpha = 0.85f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
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
                                style = MaterialTheme.typography.bodySmall,
                                color = onGradientColor.copy(alpha = 0.7f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                imageVector = Icons.Rounded.MoreVert,
                                contentDescription = null,
                                tint = onGradientColor.copy(alpha = 0.8f)
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
    }
}

@Composable
private fun FavoriteAlbumsEmptyState(
    isInsideFolder: Boolean,
    folderName: String?,
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
                style = MaterialTheme.typography.titleLarge.copy(fontFamily = GoogleSansRounded),
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

            FilledTonalButton(
                onClick = onCreateFolderClick,
                shape = CircleShape
            ) {
                Icon(Icons.Rounded.CreateNewFolder, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isInsideFolder)
                        stringResource(R.string.favorite_albums_create_subfolder)
                    else
                        stringResource(R.string.favorite_albums_create_folder),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
internal fun CreateFavoriteFolderDialog(
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
                    stringResource(R.string.favorite_albums_create_folder),
                fontFamily = GoogleSansRounded,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            OutlinedTextField(
                value = folderName,
                onValueChange = { folderName = it },
                label = { Text(stringResource(R.string.favorite_albums_folder_name_hint)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
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
        title = {
            Text(
                stringResource(R.string.favorite_albums_rename_folder),
                fontFamily = GoogleSansRounded,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            OutlinedTextField(
                value = newName,
                onValueChange = { newName = it },
                label = { Text(stringResource(R.string.favorite_albums_folder_name_hint)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
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
        title = {
            Text(
                stringResource(R.string.favorite_albums_delete_folder),
                fontFamily = GoogleSansRounded,
                fontWeight = FontWeight.SemiBold
            )
        },
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

/**
 * Modern Expressive Bottom Sheet for Album Actions in Favorites (Screenshot 3 replacement).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FavoriteAlbumActionSheet(
    album: Album,
    onDismiss: () -> Unit,
    onPlay: () -> Unit,
    onMoveToFolder: () -> Unit,
    onCreateFolder: () -> Unit,
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
            // Header: Squircle album artwork + title + artist
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(album.albumArtUriString)
                        .crossfade(true)
                        .error(R.drawable.ic_music_placeholder)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(16.dp))
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = album.title,
                        style = MaterialTheme.typography.titleLarge.copy(fontFamily = GoogleSansRounded),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = album.artist,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Items
            ExpressiveSheetActionItem(
                icon = Icons.Rounded.PlayArrow,
                label = stringResource(R.string.common_play_album),
                onClick = onPlay
            )
            ExpressiveSheetActionItem(
                icon = Icons.AutoMirrored.Rounded.DriveFileMove,
                label = stringResource(R.string.favorite_albums_move_to_folder),
                onClick = onMoveToFolder
            )
            ExpressiveSheetActionItem(
                icon = Icons.Rounded.CreateNewFolder,
                label = stringResource(R.string.favorite_albums_create_folder),
                iconTint = MaterialTheme.colorScheme.tertiary,
                onClick = onCreateFolder
            )
            ExpressiveSheetActionItem(
                icon = Icons.Rounded.Delete,
                label = stringResource(R.string.favorite_albums_remove_from_favorites),
                isDestructive = true,
                onClick = onRemoveFromFavorites
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * Modern Expressive Bottom Sheet for Moving an Album to a Folder.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MoveAlbumToFolderSheet(
    album: Album,
    allFolders: List<FavoriteAlbumFolder>,
    onDismiss: () -> Unit,
    onCreateFolder: () -> Unit,
    onMoveToFolder: (albumId: Long, targetFolderId: String?, targetFolderName: String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val rootName = stringResource(R.string.favorite_albums_root_folder)

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
                    .padding(horizontal = 24.dp, vertical = 6.dp)
            ) {
                Text(
                    text = stringResource(R.string.favorite_albums_move_to_folder),
                    style = MaterialTheme.typography.titleLarge.copy(fontFamily = GoogleSansRounded),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = album.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action: Create New Folder right from move sheet
            ExpressiveSheetActionItem(
                icon = Icons.Rounded.CreateNewFolder,
                label = stringResource(R.string.favorite_albums_create_folder),
                iconTint = MaterialTheme.colorScheme.tertiary,
                onClick = {
                    onDismiss()
                    onCreateFolder()
                }
            )

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                // Root / Main Favorites
                item {
                    ExpressiveFolderMoveItem(
                        icon = Icons.Rounded.Home,
                        title = rootName,
                        subtitle = stringResource(R.string.favorite_albums_tab_title),
                        onClick = { onMoveToFolder(album.id, null, rootName) }
                    )
                }

                // All folders
                items(allFolders, key = { it.id }) { folder ->
                    val subtitle = when {
                        folder.subfolderCount > 0 && folder.albumCount > 0 ->
                            stringResource(R.string.favorite_albums_folder_subtitle, folder.subfolderCount.toString(), folder.albumCount.toString())
                        folder.subfolderCount > 0 ->
                            stringResource(R.string.favorite_albums_folders_count, folder.subfolderCount)
                        else ->
                            stringResource(R.string.favorite_albums_folder_item_desc, folder.albumCount)
                    }
                    ExpressiveFolderMoveItem(
                        icon = Icons.Rounded.Folder,
                        title = folder.name,
                        subtitle = subtitle,
                        previewArtUris = folder.previewArtUris,
                        onClick = { onMoveToFolder(album.id, folder.id, folder.name) }
                    )
                }
            }
        }
    }
}

/**
 * Reusable pill-shaped action item for bottom sheets.
 */
@Composable
private fun ExpressiveSheetActionItem(
    icon: ImageVector,
    label: String,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    isDestructive: Boolean = false,
    onClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(
                if (isDestructive) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f)
                else MaterialTheme.colorScheme.surfaceContainerHigh
            )
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(
                    if (isDestructive) MaterialTheme.colorScheme.errorContainer
                    else MaterialTheme.colorScheme.surfaceContainerHighest
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isDestructive) MaterialTheme.colorScheme.error else iconTint,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            color = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun ExpressiveFolderMoveItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    previewArtUris: List<String?> = emptyList(),
    onClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            }
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (previewArtUris.isNotEmpty()) {
            FolderCoverCollage(
                previewArtworkUris = previewArtUris,
                modifier = Modifier.size(44.dp),
                shape = RoundedCornerShape(12.dp)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
