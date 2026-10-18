@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.theveloper.pixelplay.presentation.screens

import com.theveloper.pixelplay.presentation.navigation.navigateSafely
import com.theveloper.pixelplay.presentation.navigation.navigateSafelyReplacing

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.LargeExtendedFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.theveloper.pixelplay.presentation.components.ditheredHeaderScrim
import com.theveloper.pixelplay.presentation.components.ditheredStatusBarScrim
import com.theveloper.pixelplay.presentation.components.subcomps.SwipeToQueueBox
import com.theveloper.pixelplay.utils.formatTotalDuration
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.zIndex
import com.theveloper.pixelplay.data.preferences.HeaderCoverStyle
import com.theveloper.pixelplay.data.preferences.HeaderActionsLayout
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp as lerpColor
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextGeometricTransform
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import com.theveloper.pixelplay.ui.theme.LocalPixelPlayDarkTheme
import com.theveloper.pixelplay.ui.theme.GoogleSansRounded
import com.theveloper.pixelplay.ui.theme.PixelPlayStatusBarStyle
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import coil.compose.AsyncImagePainter
import coil.size.Size
import com.theveloper.pixelplay.R
import com.theveloper.pixelplay.data.model.Album
import com.theveloper.pixelplay.presentation.components.CollapsibleCommonTopBar
import com.theveloper.pixelplay.presentation.components.EditAlbumSheet
import com.theveloper.pixelplay.presentation.components.ExpressiveScrollBar
import com.theveloper.pixelplay.ui.theme.LocalShowScrollbar
import com.theveloper.pixelplay.presentation.components.MiniPlayerHeight
import com.theveloper.pixelplay.presentation.components.PlaylistBottomSheet
import com.theveloper.pixelplay.presentation.components.SmartImage
import com.theveloper.pixelplay.presentation.components.SongInfoBottomSheet
import com.theveloper.pixelplay.presentation.components.resolveNavBarOccupiedHeight
import com.theveloper.pixelplay.presentation.components.subcomps.EnhancedSongListItem
import com.theveloper.pixelplay.presentation.navigation.Screen
import com.theveloper.pixelplay.presentation.viewmodel.AlbumDetailViewModel
import com.theveloper.pixelplay.presentation.viewmodel.PlayerViewModel
import com.theveloper.pixelplay.presentation.viewmodel.PlaylistViewModel
import com.theveloper.pixelplay.utils.formatSongCount
import com.theveloper.pixelplay.utils.shapes.RoundedStarShape
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import androidx.compose.ui.res.stringResource

private const val UseSharedCollapsibleTopBarProbe = true

@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AlbumDetailScreen(
    albumId: String,
    navController: NavController,
    playerViewModel: PlayerViewModel,
    viewModel: AlbumDetailViewModel = hiltViewModel(),
    playlistViewModel: PlaylistViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val stablePlayerState by playerViewModel.stablePlayerState.collectAsStateWithLifecycle()
    val favoriteIds by playerViewModel.favoriteSongIds.collectAsStateWithLifecycle()
    val navBarCompactMode by playerViewModel.navBarCompactMode.collectAsStateWithLifecycle()
    val headerCoverStyle by playerViewModel.headerCoverStyle.collectAsStateWithLifecycle()
    val headerActionsLayout by playerViewModel.headerActionsLayout.collectAsStateWithLifecycle()
    val hiddenAlbumSongKeys by playerViewModel.hiddenAlbumSongKeys.collectAsStateWithLifecycle()

    var showSongInfoBottomSheet by remember { mutableStateOf(false) }
    var showEditAlbumSheet by remember { mutableStateOf(false) }
    val selectedSongForInfo by playerViewModel.selectedSongForInfo.collectAsStateWithLifecycle()
    val systemNavBarInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val bottomBarHeightDp = resolveNavBarOccupiedHeight(systemNavBarInset, navBarCompactMode)
    val toastSongHidden = stringResource(R.string.album_toast_song_hidden)
    val toastSongUnhidden = stringResource(R.string.album_toast_song_unhidden)
    val toastSongIsHidden = stringResource(R.string.album_toast_song_is_hidden)
    val toastAllSongsHidden = stringResource(R.string.album_toast_all_songs_hidden)
    var showPlaylistBottomSheet by remember { mutableStateOf(false) }
    val isDarkTheme = LocalPixelPlayDarkTheme.current
    val baseColorScheme = MaterialTheme.colorScheme
    val albumArtUri = uiState.album?.albumArtUriString?.takeIf { it.isNotBlank() }
    val albumColorSchemeFlow = remember(albumArtUri) {
        albumArtUri?.let { playerViewModel.themeStateHolder.getAlbumColorSchemeFlow(it, eager = false) }
    }
    val albumColorSchemePair = albumColorSchemeFlow?.collectAsStateWithLifecycle()?.value
    val albumColorScheme = remember(albumColorSchemePair, isDarkTheme, baseColorScheme) {
        albumColorSchemePair?.let { pair -> if (isDarkTheme) pair.dark else pair.light }
            ?: baseColorScheme
    }
    var headerArtworkLoaded by remember(albumArtUri) { mutableStateOf(albumArtUri == null) }
    var themeRequestIssued by remember(albumArtUri) { mutableStateOf(albumArtUri == null) }
    LaunchedEffect(albumArtUri, headerArtworkLoaded, themeRequestIssued) {
        if (!themeRequestIssued && headerArtworkLoaded && albumArtUri != null) {
            themeRequestIssued = true
            playerViewModel.themeStateHolder.ensureAlbumColorScheme(albumArtUri)
        }
    }
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()

    MaterialTheme(
        colorScheme = albumColorScheme,
        typography = MaterialTheme.typography,
        shapes = MaterialTheme.shapes
    ) {

        val isMiniPlayerVisible = stablePlayerState.currentSong != null
        val fabBottomPadding by animateDpAsState(
            targetValue = if (isMiniPlayerVisible) MiniPlayerHeight + 16.dp else 16.dp,
            label = "fabPadding"
        )

        when {
            uiState.isLoading && uiState.album == null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    ContainedLoadingIndicator()
                }
            }

            uiState.error != null && uiState.album == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = uiState.error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            uiState.album != null -> {
                val album = uiState.album!!
                val songs = uiState.songs
                val songsByDisc = remember(songs) {
                    songs.groupBy { it.discNumber ?: 1 }
                }
                val totalDurationText = remember(songs) { formatTotalDuration(songs) }
                val context = LocalContext.current
                val currentAlbumId = album.id.toString()
                val playableSongs = remember(songs, hiddenAlbumSongKeys, currentAlbumId) {
                    songs.filterNot { "${currentAlbumId}_${it.id}" in hiddenAlbumSongKeys }
                }
                val toastAddedToQueue = stringResource(R.string.library_toast_added_to_queue)
                val lazyListState = rememberLazyListState()

                val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
                val minTopBarHeight = 64.dp + statusBarHeight
                val isSquareHeader = headerCoverStyle == HeaderCoverStyle.SQUARE
                val maxTopBarHeight = if (isSquareHeader) {
                    statusBarHeight + (if (headerActionsLayout == HeaderActionsLayout.SPLIT) 395.dp else 425.dp)
                } else 300.dp

                val minTopBarHeightPx = with(density) { minTopBarHeight.toPx() }
                val maxTopBarHeightPx = with(density) { maxTopBarHeight.toPx() }
                val headerImageRequestSize = remember(
                    configuration.screenWidthDp,
                    density.density,
                    maxTopBarHeightPx
                ) {
                    Size(
                        width = with(density) { configuration.screenWidthDp.dp.roundToPx() },
                        height = maxTopBarHeightPx.roundToInt()
                    )
                }

                val topBarHeight = remember(maxTopBarHeightPx) { Animatable(maxTopBarHeightPx) }
                LaunchedEffect(maxTopBarHeightPx) {
                    if (topBarHeight.value > minTopBarHeightPx) {
                        topBarHeight.snapTo(maxTopBarHeightPx)
                    }
                }
                val collapseFraction by remember(minTopBarHeightPx, maxTopBarHeightPx) {
                    derivedStateOf {
                        1f - ((topBarHeight.value - minTopBarHeightPx) / (maxTopBarHeightPx - minTopBarHeightPx)).coerceIn(
                            0f,
                            1f
                        )
                    }
                }

                val nestedScrollConnection = remember(minTopBarHeightPx, maxTopBarHeightPx) {
                    object : NestedScrollConnection {
                        override fun onPreScroll(
                            available: Offset,
                            source: NestedScrollSource
                        ): Offset {
                            val delta = available.y
                            val isScrollingDown = delta < 0

                            if (!isScrollingDown && (lazyListState.firstVisibleItemIndex > 0 || lazyListState.firstVisibleItemScrollOffset > 0)) {
                                return Offset.Zero
                            }

                            val previousHeight = topBarHeight.value
                            val newHeight =
                                (previousHeight + delta).coerceIn(minTopBarHeightPx, maxTopBarHeightPx)
                            val consumed = newHeight - previousHeight

                            if (consumed.roundToInt() != 0) {
                                coroutineScope.launch {
                                    topBarHeight.snapTo(newHeight)
                                }
                            }

                            val canConsumeScroll = !(isScrollingDown && newHeight == minTopBarHeightPx)
                            return if (canConsumeScroll) Offset(0f, consumed) else Offset.Zero
                        }

                        override suspend fun onPostFling(
                            consumed: Velocity,
                            available: Velocity
                        ): Velocity {
                            return super.onPostFling(consumed, available)
                        }
                    }
                }

                LaunchedEffect(lazyListState.isScrollInProgress) {
                    if (!lazyListState.isScrollInProgress) {
                        val shouldExpand =
                            topBarHeight.value > (minTopBarHeightPx + maxTopBarHeightPx) / 2
                        val canExpand =
                            lazyListState.firstVisibleItemIndex == 0 && lazyListState.firstVisibleItemScrollOffset == 0

                        val targetValue = if (shouldExpand && canExpand) {
                            maxTopBarHeightPx
                        } else {
                            minTopBarHeightPx
                        }

                        if (topBarHeight.value != targetValue) {
                            coroutineScope.launch {
                                topBarHeight.animateTo(
                                    targetValue,
                                    spring(stiffness = Spring.StiffnessMedium)
                                )
                            }
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            color = MaterialTheme.colorScheme.surface
                        )
                        .nestedScroll(nestedScrollConnection)
                ) {
                    val currentTopBarHeightDp = with(density) { topBarHeight.value.toDp() }
                    val showScrollBar =
                        LocalShowScrollbar.current &&
                        collapseFraction > 0.95f &&
                            (lazyListState.canScrollForward || lazyListState.canScrollBackward)

                    LazyColumn(
                        state = lazyListState,
                        modifier = Modifier
                            .fillMaxSize()
                            .offset {
                                val extraHeight =
                                    (topBarHeight.value - minTopBarHeightPx).roundToInt()
                                IntOffset(0, extraHeight)
                            },
                        contentPadding = PaddingValues(
                            top = minTopBarHeight + 8.dp,
                            start = 16.dp,
                            end = if (showScrollBar) 24.dp else 16.dp,
                            bottom = fabBottomPadding + 80.dp // To account for FAB
                        ),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        songsByDisc.forEach { (discNumber, discSongs) ->
                            if (songsByDisc.size > 1) {
                                item(key = "disc_header_$discNumber") {
                                    val discDurationText = remember(discSongs) { formatTotalDuration(discSongs) }
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 16.dp, bottom = 8.dp, start = 8.dp, end = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = stringResource(R.string.album_disc_number_header, discNumber),
                                            style = MaterialTheme.typography.labelLarge,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${formatSongCount(discSongs.size)} • $discDurationText",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                            items(
                                items = discSongs,
                                key = { song -> "album_song_${song.id}" },
                                contentType = { "album_song" }
                            ) { song ->
                                val isHidden = "${currentAlbumId}_${song.id}" in hiddenAlbumSongKeys
                                val animatedAlpha by animateFloatAsState(
                                    targetValue = if (isHidden) 0.38f else 1f,
                                    animationSpec = tween(durationMillis = 250),
                                    label = "albumSongAlpha_${song.id}"
                                )

                                SwipeToQueueBox(
                                    enabled = !isHidden,
                                    swipeStateIdentity = song.id,
                                    coverArtUri = song.albumArtUriString ?: album.albumArtUriString,
                                    themeStateHolder = playerViewModel.themeStateHolder,
                                    backgroundHorizontalPadding = 0.dp,
                                    onSwipeToQueue = {
                                        playerViewModel.addSongToQueue(song)
                                        playerViewModel.sendToast(toastAddedToQueue)
                                    }
                                ) {
                                    EnhancedSongListItem(
                                        modifier = Modifier.graphicsLayer { alpha = animatedAlpha },
                                        song = song,
                                        isCurrentSong = !isHidden && stablePlayerState.currentSong?.id == song.id,
                                        isPlaying = !isHidden && stablePlayerState.isPlaying,
                                        showAlbumArt = false,
                                        onMoreOptionsClick = {
                                            playerViewModel.selectSongForInfo(song)
                                            showSongInfoBottomSheet = true
                                        },
                                        onClick = {
                                            if (!isHidden) {
                                                playerViewModel.showAndPlaySong(song, playableSongs, album.title)
                                            } else {
                                                playerViewModel.sendToast(toastSongIsHidden)
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }

                    if (showScrollBar) {
                        ExpressiveScrollBar(
                            listState = lazyListState,
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(
                                    top = minTopBarHeight + 12.dp,
                                    bottom = fabBottomPadding + 80.dp
                                )
                        )
                    }

                    val currentSong = stablePlayerState.currentSong
                    val isCurrentAlbumSong = remember(currentSong?.id, album.id, songs) {
                        currentSong != null && (
                            currentSong.albumId == album.id ||
                            songs.any { it.id == currentSong.id }
                        )
                    }
                    val isPlayingThisAlbum = isCurrentAlbumSong && stablePlayerState.isPlaying

                    val handlePlayClick = {
                        if (isPlayingThisAlbum) {
                            playerViewModel.playPause()
                        } else if (isCurrentAlbumSong) {
                            playerViewModel.playPause()
                        } else {
                            if (playableSongs.isNotEmpty()) {
                                playerViewModel.showAndPlaySong(playableSongs.first(), playableSongs, album.title)
                            } else if (songs.isNotEmpty()) {
                                playerViewModel.sendToast(toastAllSongsHidden)
                            }
                        }
                    }

                    if (UseSharedCollapsibleTopBarProbe) {
                        SharedAlbumTopBarProbe(
                            album = album,
                            songsCount = songs.size,
                            totalDuration = totalDurationText,
                            collapseFraction = collapseFraction,
                            headerHeight = currentTopBarHeightDp,
                            headerImageRequestSize = headerImageRequestSize,
                            headerCoverStyle = headerCoverStyle,
                            headerActionsLayout = headerActionsLayout,
                            isPlaying = isPlayingThisAlbum,
                            onHeaderArtworkState = { state ->
                                if (state is AsyncImagePainter.State.Success) {
                                    headerArtworkLoaded = true
                                }
                            },
                            onBackPressed = { navController.popBackStack() },
                            onPlayClick = handlePlayClick,
                            onEditClick = { showEditAlbumSheet = true }
                        )
                    } else {
                        CollapsingAlbumTopBar(
                            album = album,
                            songsCount = songs.size,
                            totalDuration = totalDurationText,
                            collapseFraction = collapseFraction,
                            headerHeight = currentTopBarHeightDp,
                            headerImageRequestSize = headerImageRequestSize,
                            headerCoverStyle = headerCoverStyle,
                            headerActionsLayout = headerActionsLayout,
                            isPlaying = isPlayingThisAlbum,
                            onHeaderArtworkState = { state ->
                                if (state is AsyncImagePainter.State.Success) {
                                    headerArtworkLoaded = true
                                }
                            },
                            onBackPressed = { navController.popBackStack() },
                            onPlayClick = handlePlayClick,
                            onEditClick = { showEditAlbumSheet = true }
                        )
                    }
                }
            }
        }
        if (showSongInfoBottomSheet && selectedSongForInfo != null) {
            val currentSong = selectedSongForInfo
            val isFavorite = remember(currentSong?.id, favoriteIds) {
                currentSong?.let { favoriteIds.contains(it.id) } ?: false
            }

            if (currentSong != null) {
                val currentAlbumId = uiState.album?.id?.toString() ?: albumId
                val isSongHiddenInAlbum = remember(currentAlbumId, currentSong.id, hiddenAlbumSongKeys) {
                    "${currentAlbumId}_${currentSong.id}" in hiddenAlbumSongKeys
                }

                val removeFromListTrigger = remember(uiState.songs) {
                    {
                        viewModel.update(uiState.songs.filterNot { it.id == currentSong.id })
                    }
                }
                SongInfoBottomSheet(
                    song = currentSong,
                    isFavorite = isFavorite,
                    onToggleFavorite = {
                        playerViewModel.toggleFavoriteSpecificSong(currentSong)
                    },
                    onDismiss = { showSongInfoBottomSheet = false },
                    isSongHiddenInAlbum = isSongHiddenInAlbum,
                    onToggleHideFromAlbum = {
                        val wasHidden = isSongHiddenInAlbum
                        playerViewModel.toggleHideSongFromAlbum(currentAlbumId, currentSong.id)
                        val toastMsg = if (wasHidden) toastSongUnhidden else toastSongHidden
                        playerViewModel.sendToast(toastMsg)
                        if (!wasHidden && stablePlayerState.currentSong?.id == currentSong.id) {
                            playerViewModel.nextSong()
                        }
                    },
                    onPlaySong = {
                        playerViewModel.showAndPlaySong(currentSong)
                    },
                    onAddToQueue = {
                        playerViewModel.addSongToQueue(currentSong)
                    },
                    onAddNextToQueue = {
                        playerViewModel.addSongNextToQueue(currentSong)
                    },
                    onAddToPlayList = {
                        showPlaylistBottomSheet = true;
                    },
                    onDeleteFromDevice = playerViewModel::deleteFromDevice,
                    onNavigateToAlbum = {
                        navController.navigateSafelyReplacing(
                            route = Screen.AlbumDetail.createRoute(currentSong.albumId),
                            patternToPop = Screen.AlbumDetail.route
                        )
                        showSongInfoBottomSheet = false
                    },
                    onNavigateToArtist = {
                        navController.navigateSafelyReplacing(
                            route = Screen.ArtistDetail.createRoute(currentSong.artistId),
                            patternToPop = Screen.ArtistDetail.route
                        )
                        showSongInfoBottomSheet = false
                    },
                    onNavigateToArtistById = { artistId ->
                        navController.navigateSafelyReplacing(
                            route = Screen.ArtistDetail.createRoute(artistId),
                            patternToPop = Screen.ArtistDetail.route
                        )
                        showSongInfoBottomSheet = false
                    },
                    onNavigateToGenre = {
                        currentSong.genre?.let {
                            navController.navigateSafelyReplacing(
                                route = Screen.GenreDetail.createRoute(java.net.URLEncoder.encode(it, "UTF-8")),
                                patternToPop = Screen.GenreDetail.route
                            )
                        }
                        showSongInfoBottomSheet = false
                    },
                    onEditSong = { newTitle, newArtist, newAlbum, newAlbumArtist, newComposer, newGenre, newLyrics, newTrackNumber, newDiscNumber, replayGainTrackGainDb, replayGainAlbumGainDb, coverArtUpdate ->
                        playerViewModel.editSongMetadata(
                            currentSong,
                            newTitle,
                            newArtist,
                            newAlbum,
                            newAlbumArtist,
                            newComposer,
                            newGenre,
                            newLyrics,
                            newTrackNumber,
                            newDiscNumber,
                            replayGainTrackGainDb,
                            replayGainAlbumGainDb,
                            coverArtUpdate
                        )
                    },
                    removeFromListTrigger = removeFromListTrigger
                )
                if (showPlaylistBottomSheet) {
                    val playlistUiState by playlistViewModel.uiState.collectAsStateWithLifecycle()

                    PlaylistBottomSheet(
                        playlistUiState = playlistUiState,
                        songs = listOf(currentSong),
                        onDismiss = { showPlaylistBottomSheet = false },
                        bottomBarHeight = bottomBarHeightDp,
                        playerViewModel = playerViewModel
                    )
                }
            }
        }

        if (showEditAlbumSheet && uiState.album != null) {
            EditAlbumSheet(
                visible = showEditAlbumSheet,
                album = uiState.album!!,
                songs = uiState.songs,
                onDismiss = { showEditAlbumSheet = false },
                onSave = { newTitle, newAlbumArtist, newArtist, newGenre, newReplayGainAlbumGainDb, coverArtUpdate, trackEdits ->
                    playerViewModel.saveAlbumMetadata(
                        album = uiState.album!!,
                        trackEdits = trackEdits,
                        newAlbumTitle = newTitle,
                        newAlbumArtist = newAlbumArtist.takeIf { it.isNotBlank() },
                        newArtist = newArtist.takeIf { it.isNotBlank() },
                        newGenre = newGenre.takeIf { it.isNotBlank() },
                        newReplayGainAlbumGainDb = newReplayGainAlbumGainDb.takeIf { it.isNotBlank() },
                        coverArtUpdate = coverArtUpdate
                    )
                    showEditAlbumSheet = false
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun AlbumPlayPauseFab(
    isPlaying: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentAlpha: Float = 1f
) {
    if (contentAlpha <= 0.01f) return

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val hapticFeedback = LocalHapticFeedback.current

    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "albumFabPressScale"
    )

    val shapeRotation by animateFloatAsState(
        targetValue = if (isPlaying) 45f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "albumFabShapeRotation"
    )

    val shapeCurve by animateFloatAsState(
        targetValue = if (isPlaying) 0.08f else 0.05f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "albumFabShapeCurve"
    )

    val animatedShape = remember(shapeRotation, shapeCurve) {
        RoundedStarShape(sides = 8, curve = shapeCurve.toDouble(), rotation = shapeRotation)
    }

    val containerColor by animateColorAsState(
        targetValue = if (isPlaying) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.primaryContainer
        },
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "albumFabContainerColor"
    )

    val contentColor by animateColorAsState(
        targetValue = if (isPlaying) {
            MaterialTheme.colorScheme.onPrimary
        } else {
            MaterialTheme.colorScheme.onPrimaryContainer
        },
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "albumFabContentColor"
    )

    val effectiveScale = contentAlpha * pressScale

    LargeExtendedFloatingActionButton(
        onClick = {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onClick()
        },
        shape = animatedShape,
        containerColor = containerColor,
        contentColor = contentColor,
        elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp, 0.dp, 0.dp),
        interactionSource = interactionSource,
        modifier = modifier.graphicsLayer {
            scaleX = effectiveScale
            scaleY = effectiveScale
            alpha = contentAlpha
        }
    ) {
        AnimatedContent(
            targetState = isPlaying,
            transitionSpec = {
                (fadeIn(animationSpec = tween(180, delayMillis = 35)) +
                    scaleIn(
                        initialScale = 0.5f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    )).togetherWith(
                    fadeOut(animationSpec = tween(90)) +
                        scaleOut(
                            targetScale = 0.5f,
                            animationSpec = tween(90)
                        )
                )
            },
            label = "albumFabIconCrossfade"
        ) { playing ->
            Icon(
                imageVector = if (playing) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                contentDescription = stringResource(
                    if (playing) R.string.common_pause else R.string.common_play_album
                ),
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
private fun SharedAlbumTopBarProbe(
    album: Album,
    songsCount: Int,
    totalDuration: String = "",
    collapseFraction: Float,
    headerHeight: Dp,
    headerImageRequestSize: Size,
    headerCoverStyle: String = HeaderCoverStyle.GRADIENT_BANNER,
    headerActionsLayout: String = HeaderActionsLayout.SPLIT,
    isPlaying: Boolean = false,
    onHeaderArtworkState: ((AsyncImagePainter.State) -> Unit)? = null,
    onBackPressed: () -> Unit,
    onPlayClick: () -> Unit,
    onEditClick: () -> Unit
) {
    val isSquareMode = headerCoverStyle == HeaderCoverStyle.SQUARE
    val surfaceColor = MaterialTheme.colorScheme.surface
    val isDarkTheme = LocalPixelPlayDarkTheme.current
    val statusBarColor =
        if (isDarkTheme) Color.Black.copy(alpha = 0.6f)
        else Color.White.copy(alpha = 0.4f)
    val solidAlpha = (collapseFraction * 2f).coerceIn(0f, 1f)
    val expandedContentAlpha = 1f - solidAlpha
    val expandedStatusBarFallback = remember(statusBarColor, surfaceColor) {
        statusBarColor.compositeOver(surfaceColor)
    }

    val vibrantColor = MaterialTheme.colorScheme.primary
    val topAlpha = if (isDarkTheme) 0.55f else 0.35f
    val vibrantBackgroundBrush = remember(vibrantColor, surfaceColor, topAlpha, expandedContentAlpha) {
        Brush.verticalGradient(
            0.0f to vibrantColor.copy(alpha = topAlpha * expandedContentAlpha).compositeOver(surfaceColor),
            0.50f to vibrantColor.copy(alpha = (topAlpha * 0.45f) * expandedContentAlpha).compositeOver(surfaceColor),
            0.78f to surfaceColor,
            1.0f to surfaceColor
        )
    }
    val vibrantTopColor = remember(vibrantColor, surfaceColor, topAlpha) {
        vibrantColor.copy(alpha = topAlpha).compositeOver(surfaceColor)
    }
    val fallbackStatusBarColor = if (isSquareMode) {
        remember(vibrantTopColor, surfaceColor, solidAlpha) {
            lerpColor(vibrantTopColor, surfaceColor, solidAlpha)
        }
    } else {
        remember(expandedStatusBarFallback, surfaceColor, solidAlpha) {
            lerpColor(expandedStatusBarFallback, surfaceColor, solidAlpha)
        }
    }
    val titleVerticalBias = lerp(1f, -1f, collapseFraction)
    val shuffleAlignment = BiasAlignment(horizontalBias = 1f, verticalBias = titleVerticalBias)

    val subtitleText = remember(album.artist, songsCount, totalDuration) {
        listOfNotNull(
            album.artist.takeIf { it.isNotBlank() },
            formatSongCount(songsCount),
            totalDuration.takeIf { it.isNotBlank() }
        ).joinToString(" • ")
    }

    PixelPlayStatusBarStyle(color = fallbackStatusBarColor)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(headerHeight)
            .background(surfaceColor)
            .then(
                if (isSquareMode) Modifier.background(vibrantBackgroundBrush) else Modifier
            )
            .clipToBounds()
    ) {
        if (isSquareMode) {
            if (expandedContentAlpha > 0.01f) {
                val screenWidth = LocalConfiguration.current.screenWidthDp.dp
                val squareSize = if (headerActionsLayout == HeaderActionsLayout.SPLIT) {
                    (screenWidth - 110.dp).coerceIn(190.dp, 240.dp)
                } else {
                    (screenWidth - 85.dp).coerceIn(215.dp, 270.dp)
                }
                val squareScale = 1f - (collapseFraction * 0.25f)
                val squareAlpha = (1f - collapseFraction * 1.6f).coerceIn(0f, 1f)

                val artworkAlignment = when (headerActionsLayout) {
                    HeaderActionsLayout.ALL_LEFT -> Alignment.TopEnd
                    HeaderActionsLayout.ALL_RIGHT -> Alignment.TopStart
                    else -> Alignment.TopCenter
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(
                            top = 36.dp,
                            start = if (headerActionsLayout == HeaderActionsLayout.ALL_RIGHT) 20.dp else 16.dp,
                            end = if (headerActionsLayout == HeaderActionsLayout.ALL_LEFT) 20.dp else 16.dp
                        ),
                    contentAlignment = artworkAlignment
                ) {
                    SmartImage(
                        model = album.albumArtUriString,
                        contentDescription = stringResource(R.string.album_cover_for, album.title),
                        contentScale = ContentScale.Crop,
                        targetSize = Size(
                            width = with(LocalDensity.current) { squareSize.roundToPx() },
                            height = with(LocalDensity.current) { squareSize.roundToPx() }
                        ),
                        allowHardware = true,
                        crossfadeDurationMillis = 0,
                        alpha = squareAlpha,
                        onState = onHeaderArtworkState,
                        modifier = Modifier
                            .size(squareSize)
                            .graphicsLayer {
                                scaleX = squareScale
                                scaleY = squareScale
                                alpha = squareAlpha
                            }
                            .clip(RoundedCornerShape(16.dp))
                    )
                }
            }
        } else {
            if (expandedContentAlpha > 0.01f) {
                SmartImage(
                    model = album.albumArtUriString,
                    contentDescription = stringResource(R.string.album_cover_for, album.title),
                    contentScale = ContentScale.Crop,
                    targetSize = headerImageRequestSize,
                    allowHardware = true,
                    crossfadeDurationMillis = 0,
                    alpha = expandedContentAlpha,
                    onState = onHeaderArtworkState,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .ditheredHeaderScrim(surfaceColor)
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .ditheredStatusBarScrim(statusBarColor)
                    .align(Alignment.TopCenter)
            )
        }

        val (collapsedTitleStartPadding, collapsedTitleEndPadding) = when (headerActionsLayout) {
            HeaderActionsLayout.ALL_LEFT -> 108.dp to 24.dp
            HeaderActionsLayout.ALL_RIGHT -> 24.dp to 108.dp
            else -> 68.dp to 68.dp
        }

        CollapsibleCommonTopBar(
            title = album.title,
            subtitle = subtitleText,
            collapseFraction = collapseFraction,
            headerHeight = headerHeight,
            onBackClick = onBackPressed,
            containerColor = surfaceColor.copy(alpha = solidAlpha),
            collapsedTitleStartPadding = collapsedTitleStartPadding,
            expandedTitleStartPadding = 24.dp,
            collapsedTitleEndPadding = collapsedTitleEndPadding,
            expandedTitleEndPadding = 136.dp,
            containerHeightRange = (if (isSquareMode) 100.dp else 112.dp) to 56.dp,
            titleStyle = MaterialTheme.typography.headlineMedium.copy(
                fontFamily = GoogleSansRounded,
                fontWeight = FontWeight.SemiBold,
                textGeometricTransform = TextGeometricTransform(scaleX = 1.08f)
            ),
            titleScaleRange = 1f to 1f,
            titleFontSizeRange = (if (isSquareMode) 22.sp else 30.sp) to 18.sp,
            maxLines = if (collapseFraction < 0.5f) 2 else 1,
            collapsedSubtitleMaxLines = 1,
            expandedSubtitleMaxLines = 2,
            contentColor = MaterialTheme.colorScheme.onSurface,
            subtitleColor = MaterialTheme.colorScheme.onSurfaceVariant,
            fadeSubtitleOnCollapse = false,
            syncStatusBarWithContainer = false,
            customTopBarControls = {
                val isExpanded = collapseFraction < 0.5f

                val backButton: @Composable () -> Unit = {
                    FilledIconButton(
                        onClick = onBackPressed,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.common_back)
                        )
                    }
                }

                val editPencilButton: @Composable () -> Unit = {
                    FilledIconButton(
                        onClick = onEditClick,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = stringResource(R.string.edit_album_title)
                        )
                    }
                }

                when (headerActionsLayout) {
                    HeaderActionsLayout.ALL_LEFT -> {
                        if (isExpanded) {
                            Column(
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(start = 12.dp, top = 4.dp)
                                    .zIndex(1f),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                backButton()
                                editPencilButton()
                            }
                        } else {
                            Row(
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(start = 12.dp, top = 4.dp)
                                    .zIndex(1f),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                backButton()
                                editPencilButton()
                            }
                        }
                    }
                    HeaderActionsLayout.ALL_RIGHT -> {
                        if (isExpanded) {
                            Column(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(end = 12.dp, top = 4.dp)
                                    .zIndex(1f),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                backButton()
                                editPencilButton()
                            }
                        } else {
                            Row(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(end = 12.dp, top = 4.dp)
                                    .zIndex(1f),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                editPencilButton()
                                backButton()
                            }
                        }
                    }
                    else -> { // SPLIT (Default)
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(start = 12.dp, top = 4.dp)
                                .zIndex(1f)
                        ) {
                            backButton()
                        }
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(end = 12.dp, top = 4.dp)
                                .zIndex(1f)
                        ) {
                            editPencilButton()
                        }
                    }
                }
            }
        )

        AlbumPlayPauseFab(
            isPlaying = isPlaying,
            onClick = onPlayClick,
            contentAlpha = expandedContentAlpha,
            modifier = Modifier
                .align(shuffleAlignment)
                .statusBarsPadding()
                .padding(end = 16.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CollapsingAlbumTopBar(
    album: Album,
    songsCount: Int,
    totalDuration: String = "",
    collapseFraction: Float,
    headerHeight: Dp,
    headerImageRequestSize: Size,
    headerCoverStyle: String = HeaderCoverStyle.GRADIENT_BANNER,
    headerActionsLayout: String = HeaderActionsLayout.SPLIT,
    isPlaying: Boolean = false,
    onHeaderArtworkState: ((AsyncImagePainter.State) -> Unit)? = null,
    onBackPressed: () -> Unit,
    onPlayClick: () -> Unit,
    onEditClick: () -> Unit
) {
    val isSquareMode = headerCoverStyle == HeaderCoverStyle.SQUARE
    val surfaceColor = MaterialTheme.colorScheme.surface
    val isDarkTheme = LocalPixelPlayDarkTheme.current
    val statusBarColor =
        if (isDarkTheme) Color.Black.copy(alpha = 0.6f) else Color.White.copy(
            alpha = 0.4f
        )

    // Animation Values
    val fabScale = 1f - collapseFraction
    val backgroundAlpha = collapseFraction
    val headerContentAlpha = 1f - (collapseFraction * 2).coerceAtMost(1f)
    val showExpandedArtwork = headerContentAlpha > 0.01f
    val solidAlpha = (collapseFraction * 2f).coerceIn(0f, 1f)
    val expandedStatusBarFallback = remember(statusBarColor, surfaceColor) {
        statusBarColor.compositeOver(surfaceColor)
    }

    val vibrantColor = MaterialTheme.colorScheme.primary
    val topAlpha = if (isDarkTheme) 0.55f else 0.35f
    val vibrantBackgroundBrush = remember(vibrantColor, surfaceColor, topAlpha, headerContentAlpha) {
        Brush.verticalGradient(
            0.0f to vibrantColor.copy(alpha = topAlpha * headerContentAlpha).compositeOver(surfaceColor),
            0.50f to vibrantColor.copy(alpha = (topAlpha * 0.45f) * headerContentAlpha).compositeOver(surfaceColor),
            0.78f to surfaceColor,
            1.0f to surfaceColor
        )
    }
    val vibrantTopColor = remember(vibrantColor, surfaceColor, topAlpha) {
        vibrantColor.copy(alpha = topAlpha).compositeOver(surfaceColor)
    }
    val fallbackStatusBarColor = if (isSquareMode) {
        remember(vibrantTopColor, surfaceColor, solidAlpha) {
            lerpColor(vibrantTopColor, surfaceColor, solidAlpha)
        }
    } else {
        remember(expandedStatusBarFallback, surfaceColor, solidAlpha) {
            lerpColor(expandedStatusBarFallback, surfaceColor, solidAlpha)
        }
    }

    // Title animation
    val titleScale = lerp(1f, 0.75f, collapseFraction)
    val titlePaddingStart = lerp(
        24.dp,
        when (headerActionsLayout) {
            HeaderActionsLayout.ALL_LEFT -> 108.dp
            HeaderActionsLayout.ALL_RIGHT -> 24.dp
            else -> 68.dp
        },
        collapseFraction
    )
    val titlePaddingEnd = lerp(
        120.dp,
        when (headerActionsLayout) {
            HeaderActionsLayout.ALL_RIGHT -> 108.dp
            HeaderActionsLayout.ALL_LEFT -> 24.dp
            else -> 68.dp
        },
        collapseFraction
    )
    val titleMaxLines = if (collapseFraction < 0.5f) 2 else 1
    val titleVerticalBias = lerp(1f, -1f, collapseFraction)
    val animatedTitleAlignment =
        BiasAlignment(horizontalBias = -1f, verticalBias = titleVerticalBias)
    val titleContainerHeight = lerp(if (isSquareMode) 100.dp else 88.dp, 56.dp, collapseFraction)
    val yOffsetCorrection = if (isSquareMode) 0.dp else lerp((titleContainerHeight / 2) - 64.dp, 0.dp, collapseFraction)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(headerHeight)
            .clipToBounds()
    ) {
        PixelPlayStatusBarStyle(color = fallbackStatusBarColor)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(headerHeight)
                .background(surfaceColor)
                .then(
                    if (isSquareMode) Modifier.background(vibrantBackgroundBrush) else Modifier
                )
        ) {
            if (isSquareMode) {
                if (showExpandedArtwork) {
                    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
                    val squareSize = if (headerActionsLayout == HeaderActionsLayout.SPLIT) {
                        (screenWidth - 110.dp).coerceIn(190.dp, 240.dp)
                    } else {
                        (screenWidth - 85.dp).coerceIn(215.dp, 270.dp)
                    }
                    val squareScale = 1f - (collapseFraction * 0.25f)
                    val squareAlpha = (1f - collapseFraction * 1.6f).coerceIn(0f, 1f)

                    val artworkAlignment = when (headerActionsLayout) {
                        HeaderActionsLayout.ALL_LEFT -> Alignment.TopEnd
                        HeaderActionsLayout.ALL_RIGHT -> Alignment.TopStart
                        else -> Alignment.TopCenter
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(
                                top = 36.dp,
                                start = if (headerActionsLayout == HeaderActionsLayout.ALL_RIGHT) 20.dp else 16.dp,
                                end = if (headerActionsLayout == HeaderActionsLayout.ALL_LEFT) 20.dp else 16.dp
                            ),
                        contentAlignment = artworkAlignment
                    ) {
                        SmartImage(
                            model = album.albumArtUriString,
                            contentDescription = stringResource(R.string.album_cover_for, album.title),
                            contentScale = ContentScale.Crop,
                            targetSize = Size(
                                width = with(LocalDensity.current) { squareSize.roundToPx() },
                                height = with(LocalDensity.current) { squareSize.roundToPx() }
                            ),
                            allowHardware = true,
                            crossfadeDurationMillis = 0,
                            alpha = squareAlpha,
                            onState = onHeaderArtworkState,
                            modifier = Modifier
                                .size(squareSize)
                                .graphicsLayer {
                                    scaleX = squareScale
                                    scaleY = squareScale
                                    alpha = squareAlpha
                                }
                                .clip(RoundedCornerShape(16.dp))
                        )
                    }
                }
            } else {
                if (showExpandedArtwork) {
                    SmartImage(
                        model = album.albumArtUriString,
                        contentDescription = stringResource(R.string.album_cover_for, album.title),
                        contentScale = ContentScale.Crop,
                        targetSize = headerImageRequestSize,
                        allowHardware = true,
                        crossfadeDurationMillis = 0,
                        alpha = headerContentAlpha,
                        onState = onHeaderArtworkState,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .ditheredHeaderScrim(surfaceColor)
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .ditheredStatusBarScrim(statusBarColor)
                        .align(Alignment.TopCenter)
                )
            }

            // Top bar content (buttons, title, etc.)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
            ) {
                val isExpanded = collapseFraction < 0.5f

                val backButton: @Composable () -> Unit = {
                    FilledIconButton(
                        onClick = onBackPressed,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.common_back)
                        )
                    }
                }

                val editPencilButton: @Composable () -> Unit = {
                    FilledIconButton(
                        onClick = onEditClick,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = stringResource(R.string.edit_album_title)
                        )
                    }
                }

                when (headerActionsLayout) {
                    HeaderActionsLayout.ALL_LEFT -> {
                        if (isExpanded) {
                            Column(
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(start = 12.dp, top = 4.dp)
                                    .zIndex(1f),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                backButton()
                                editPencilButton()
                            }
                        } else {
                            Row(
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(start = 12.dp, top = 4.dp)
                                    .zIndex(1f),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                backButton()
                                editPencilButton()
                            }
                        }
                    }
                    HeaderActionsLayout.ALL_RIGHT -> {
                        if (isExpanded) {
                            Column(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(end = 12.dp, top = 4.dp)
                                    .zIndex(1f),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                backButton()
                                editPencilButton()
                            }
                        } else {
                            Row(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(end = 12.dp, top = 4.dp)
                                    .zIndex(1f),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                editPencilButton()
                                backButton()
                            }
                        }
                    }
                    else -> { // SPLIT (Default)
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(start = 12.dp, top = 4.dp)
                                .zIndex(1f)
                        ) {
                            backButton()
                        }
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(end = 12.dp, top = 4.dp)
                                .zIndex(1f)
                        ) {
                            editPencilButton()
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .align(animatedTitleAlignment)
                        .height(titleContainerHeight)
                        .fillMaxWidth()
                        .offset(y = yOffsetCorrection)
                ) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = titlePaddingStart, end = titlePaddingEnd)
                            .graphicsLayer {
                                scaleX = titleScale
                                scaleY = titleScale
                            },
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = album.title,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontSize = if (isSquareMode) 22.sp else 26.sp,
                                textGeometricTransform = TextGeometricTransform(scaleX = 1.08f),
                            ),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = titleMaxLines,
                            overflow = TextOverflow.Ellipsis
                        )
                        val subtitleText = remember(album.artist, songsCount, totalDuration) {
                            listOfNotNull(
                                album.artist.takeIf { it.isNotBlank() },
                                formatSongCount(songsCount),
                                totalDuration.takeIf { it.isNotBlank() }
                            ).joinToString(" • ")
                        }
                        Text(
                            text = subtitleText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                AlbumPlayPauseFab(
                    isPlaying = isPlaying,
                    onClick = onPlayClick,
                    contentAlpha = fabScale,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                )
            }
        }
    }
}
