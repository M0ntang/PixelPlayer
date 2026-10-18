package com.theveloper.pixelplay.presentation.components

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.LocalOverscrollFactory
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.runtime.withFrameNanos


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.automirrored.rounded.PlaylistAdd
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.AudioFile
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.theveloper.pixelplay.R
import com.theveloper.pixelplay.data.model.Song
import com.theveloper.pixelplay.presentation.components.subcomps.AutoSizingTextToFill
import com.theveloper.pixelplay.utils.formatDuration
import com.theveloper.pixelplay.utils.shapes.RoundedStarShape
import racra.compose.smooth_corner_rect_library.AbsoluteSmoothCornerShape
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.theveloper.pixelplay.data.media.CoverArtUpdate
import com.theveloper.pixelplay.ui.theme.MontserratFamily
import com.theveloper.pixelplay.presentation.viewmodel.SongInfoBottomSheetViewModel
import kotlinx.coroutines.launch

import androidx.compose.ui.graphics.TransformOrigin
import com.theveloper.pixelplay.presentation.screens.TabAnimation
import com.theveloper.pixelplay.ui.theme.GoogleSansRounded
import com.theveloper.pixelplay.utils.AudioMetaUtils
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import com.theveloper.pixelplay.presentation.components.subcomps.TightWrapText
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState


@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
@Suppress("UNUSED_PARAMETER")
fun SongInfoBottomSheet(
    song: Song,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onDismiss: () -> Unit,
    onPlaySong: () -> Unit,
    onAddToQueue: () -> Unit,
    onAddNextToQueue: () -> Unit,
    onAddToPlayList: () -> Unit,
    onDeleteFromDevice: (activity: Activity, song: Song, onResult: (Boolean) -> Unit) -> Unit,
    onNavigateToAlbum: () -> Unit,
    onNavigateToArtist: () -> Unit,
    onNavigateToArtistById: (Long) -> Unit = { onNavigateToArtist() },
    onNavigateToGenre: () -> Unit,
    onEditSong: (
        title: String,
        artist: String,
        album: String,
        albumArtist: String,
        composer: String,
        genre: String,
        lyrics: String,
        trackNumber: Int,
        discNumber: Int?,
        replayGainTrackGainDb: String,
        replayGainAlbumGainDb: String,
        coverArtUpdate: CoverArtUpdate?
    ) -> Unit,
    removeFromListTrigger: () -> Unit,
    isSongHiddenInAlbum: Boolean = false,
    onToggleHideFromAlbum: (() -> Unit)? = null,
    songInfoViewModel: SongInfoBottomSheetViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val shareChooserTitle = stringResource(R.string.song_info_share_chooser_title)
    val errorShareSongFormat = stringResource(R.string.song_info_error_share_song)
    var showEditSheet by remember { mutableStateOf(false) }
    var showArtistPicker by remember { mutableStateOf(false) }
    val audioMeta by songInfoViewModel.audioMeta.collectAsStateWithLifecycle()
    val resolvedArtists by songInfoViewModel.resolvedArtists.collectAsStateWithLifecycle()
    val isPixelPlayWatchAvailable by songInfoViewModel.isPixelPlayWatchAvailable.collectAsStateWithLifecycle()
    val isWatchAvailabilityResolved by songInfoViewModel.isWatchAvailabilityResolved.collectAsStateWithLifecycle()
    val isSendingToWatch by songInfoViewModel.isSendingToWatch.collectAsStateWithLifecycle()
    val watchTransfers by songInfoViewModel.watchTransfers.collectAsStateWithLifecycle()
    val watchSongIds by songInfoViewModel.watchSongIds.collectAsStateWithLifecycle()
    val reachableWatchNodeIds by songInfoViewModel.reachableWatchNodeIds.collectAsStateWithLifecycle()
    val songMenuIconsOnly by songInfoViewModel.songMenuIconsOnly.collectAsStateWithLifecycle()
    val latestSongWatchTransfer = remember(song.id, watchTransfers) {
        watchTransfers.values
            .asSequence()
            .filter { it.songId == song.id }
            .maxByOrNull { it.updatedAtMillis }
    }
    val currentSongTransfer = latestSongWatchTransfer?.takeIf {
        it.status == com.theveloper.pixelplay.shared.WearTransferProgress.STATUS_TRANSFERRING
    }
    val currentSongTransferPercent = ((currentSongTransfer?.progress ?: 0f) * 100f).toInt().coerceIn(0, 100)
    val isSongSavedOnWatch = remember(
        song.id,
        watchSongIds,
        reachableWatchNodeIds,
        currentSongTransfer,
    ) {
        currentSongTransfer == null && songInfoViewModel.isSongSavedOnAllReachableWatches(song.id)
    }
    val canSendToWatch = remember(song.path, song.contentUriString) {
        songInfoViewModel.isLocalSongForWatchTransfer(song)
    }

    LaunchedEffect(songInfoViewModel) {
        songInfoViewModel.refreshWatchAvailability()
    }

    var lastShownWatchTransferError by remember(song.id) { mutableStateOf<String?>(null) }
    LaunchedEffect(
        latestSongWatchTransfer?.requestId,
        latestSongWatchTransfer?.status,
        latestSongWatchTransfer?.error,
    ) {
        val failedTransfer = latestSongWatchTransfer?.takeIf {
            it.status == com.theveloper.pixelplay.shared.WearTransferProgress.STATUS_FAILED &&
                    !it.error.isNullOrBlank()
        } ?: return@LaunchedEffect
        val errorKey = "${failedTransfer.requestId}:${failedTransfer.error}"
        if (lastShownWatchTransferError == errorKey) return@LaunchedEffect
        lastShownWatchTransferError = errorKey
        Toast.makeText(context, failedTransfer.error, Toast.LENGTH_SHORT).show()
    }

    val shouldOfferWatchTransfer = remember(
        canSendToWatch,
        currentSongTransfer,
        isPixelPlayWatchAvailable,
        isSongSavedOnWatch,
        isWatchAvailabilityResolved,
    ) {
        currentSongTransfer == null &&
                canSendToWatch &&
                isWatchAvailabilityResolved &&
                isPixelPlayWatchAvailable &&
                !isSongSavedOnWatch
    }
    val shouldShowWatchTransferLoading = remember(
        canSendToWatch,
        isWatchAvailabilityResolved,
    ) {
        canSendToWatch &&
                !isWatchAvailabilityResolved
    }

    val evenCornerRadiusElems = 26.dp

    val listItemShape = remember {
        AbsoluteSmoothCornerShape(
            cornerRadiusTR = 20.dp, smoothnessAsPercentBR = 60, cornerRadiusBR = 20.dp,
            smoothnessAsPercentTL = 60, cornerRadiusTL = 20.dp, smoothnessAsPercentBL = 60,
            cornerRadiusBL = 20.dp, smoothnessAsPercentTR = 60
        )
    }
    val albumArtShape = remember(evenCornerRadiusElems) {
        AbsoluteSmoothCornerShape(
            cornerRadiusTR = evenCornerRadiusElems, smoothnessAsPercentBR = 60, cornerRadiusBR = evenCornerRadiusElems,
            smoothnessAsPercentTL = 60, cornerRadiusTL = evenCornerRadiusElems, smoothnessAsPercentBL = 60,
            cornerRadiusBL = evenCornerRadiusElems, smoothnessAsPercentTR = 60
        )
    }
    val playButtonShape = remember(evenCornerRadiusElems) {
        AbsoluteSmoothCornerShape(
            cornerRadiusTR = evenCornerRadiusElems, smoothnessAsPercentBR = 60, cornerRadiusBR = evenCornerRadiusElems,
            smoothnessAsPercentTL = 60, cornerRadiusTL = evenCornerRadiusElems, smoothnessAsPercentBL = 60,
            cornerRadiusBL = evenCornerRadiusElems, smoothnessAsPercentTR = 60
        )
    }

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { true }
    )


    val infoSegmentContainerShape = remember {
        RoundedCornerShape(20.dp)
    }
    val infoSegmentItemShape = remember {
        RoundedCornerShape(8.dp)
    }

    val audioMetaLabel = remember(audioMeta) {
        val meta = audioMeta ?: return@remember null
        val formatLabel = AudioMetaUtils.mimeTypeToFormat(meta.mimeType)
            .takeIf { it != "-" }
            ?.uppercase(java.util.Locale.getDefault())
        val parts = buildList {
            meta.sampleRate?.takeIf { it > 0 }
                ?.let { add(String.format(java.util.Locale.US, "%.1f kHz", it / 1000.0)) }
            meta.bitrate?.takeIf { it > 0 }
                ?.let { add("${it / 1000} kbps") }
            formatLabel?.let { add(it) }
        }
        parts.takeIf { it.isNotEmpty() }?.joinToString(" · ")
    }
    val songLocationInfo = remember(song.path, song.contentUriString) {
        songInfoViewModel.getSongLocationInfo(song)
    }

    LaunchedEffect(song.id) {
        songInfoViewModel.loadAudioMeta(song)
        songInfoViewModel.loadArtistsForSong(song)
    }

    val pagerState = androidx.compose.foundation.pager.rememberPagerState(pageCount = { 2 })
    val scope = androidx.compose.runtime.rememberCoroutineScope()

    val configuration = LocalConfiguration.current
    val safeInsets = WindowInsets.safeDrawing.asPaddingValues()
    val maxPagerHeight = (
        configuration.screenHeightDp.dp -
            safeInsets.calculateTopPadding() -
            safeInsets.calculateBottomPadding() -
            180.dp
        ).coerceAtLeast(280.dp)

    var heightAnimationEnabled by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        withFrameNanos { }
        withFrameNanos { }
        heightAnimationEnabled = true
    }

    ModalBottomSheet(
        onDismissRequest = {
            android.util.Log.d("PixelPlayerDebug", "ModalBottomSheet: onDismissRequest called, showEditSheet=$showEditSheet")
            if (!showEditSheet) {
                onDismiss()
            }
        },
        sheetState = sheetState,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(),
            contentAlignment = Alignment.TopCenter
        ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                    ) {
                        // Fila para la carátula del álbum y el título (Always visible)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SmartImage(
                                model = song.albumArtUriString,
                                contentDescription = stringResource(R.string.common_album_art),
                                shape = albumArtShape,
                                modifier = Modifier.size(80.dp),
                                contentScale = ContentScale.Crop
                            )
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                AutoSizingTextToFill(
                                    modifier = Modifier.padding(end = 4.dp),
                                    fontWeight = FontWeight.Light,
                                    text = song.title
                                )
                            }
                            val isEditable = remember(song) { songInfoViewModel.isSongEditable(song) }
                            if (isEditable) {
                                FilledTonalIconButton(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .padding(vertical = 6.dp),
                                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceBright,
                                        contentColor = MaterialTheme.colorScheme.onSurface
                                    ),
                                    onClick = { showEditSheet = true },
                                ) {
                                    Icon(
                                        modifier = Modifier.padding(horizontal = 8.dp),
                                        imageVector = Icons.Rounded.Edit,
                                        contentDescription = stringResource(R.string.song_info_cd_edit_metadata)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Swipeable Content
                    var isPageTransitioning by remember { mutableStateOf(false) }
                    LaunchedEffect(pagerState.currentPage, pagerState.isScrollInProgress) {
                        if (pagerState.isScrollInProgress) {
                            isPageTransitioning = true
                        } else {
                            kotlinx.coroutines.delay(300)
                            isPageTransitioning = false
                        }
                    }
                    val sizeAnimationSpec = if (heightAnimationEnabled && isPageTransitioning) {
                        tween<androidx.compose.ui.unit.IntSize>(durationMillis = 280, easing = FastOutSlowInEasing)
                    } else {
                        androidx.compose.animation.core.snap()
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = maxPagerHeight)
                            .animateContentSize(
                                animationSpec = sizeAnimationSpec,
                                alignment = Alignment.TopCenter
                            )
                    ) {
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier
                                .wrapContentHeight()
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.Top
                        ) { page ->
                                when (page) {
                                    0 -> { // Options / Actions
                                        Column(
                                            modifier = Modifier
                                            .fillMaxWidth()
                                            .graphicsLayer {
                                                val pageOffset = page - (pagerState.currentPage + pagerState.currentPageOffsetFraction)
                                                val progress = kotlin.math.abs(pageOffset).coerceIn(0f, 1f)
                                                scaleX = 1f + 0.15f * progress * (1f - progress)
                                                translationX = pageOffset * size.width * 0.15f
                                                transformOrigin = TransformOrigin(0.5f, 0.5f)
                                            }
                                            .verticalScroll(rememberScrollState())
                                            .padding(horizontal = 16.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        val shareSongAction = {
                                            try {
                                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                                    type = "audio/*"
                                                    putExtra(Intent.EXTRA_STREAM, song.contentUriString.toUri())
                                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                }
                                                context.startActivity(
                                                    Intent.createChooser(
                                                        shareIntent,
                                                        shareChooserTitle
                                                    )
                                                )
                                            } catch (e: Exception) {
                                                Toast.makeText(
                                                    context,
                                                    errorShareSongFormat.format(e.localizedMessage ?: ""),
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            }
                                        }

                                        val deleteSongAction = {
                                            (context as? Activity)?.let { activity ->
                                                onDeleteFromDevice(activity, song) { result ->
                                                    if (result) {
                                                        removeFromListTrigger()
                                                        onDismiss()
                                                    }
                                                }
                                            }
                                            Unit
                                        }

                                        val shouldRenderWatchTransferRow =
                                            currentSongTransfer != null ||
                                                    shouldOfferWatchTransfer ||
                                                    shouldShowWatchTransferLoading

                                        if (songMenuIconsOnly) {
                                            CompactSongActions(
                                                isFavorite = isFavorite,
                                                onPlaySong = onPlaySong,
                                                onToggleFavorite = onToggleFavorite,
                                                onShareClick = shareSongAction,
                                                onAddToQueue = onAddToQueue,
                                                onAddNextToQueue = onAddNextToQueue,
                                                onAddToPlaylist = onAddToPlayList,
                                                onDelete = deleteSongAction,
                                                shouldRenderWatchTransfer = shouldRenderWatchTransferRow,
                                                isPixelPlayWatchAvailable = isPixelPlayWatchAvailable,
                                                isSendingToWatch = isSendingToWatch,
                                                shouldOfferWatchTransfer = shouldOfferWatchTransfer,
                                                shouldShowWatchTransferLoading = shouldShowWatchTransferLoading,
                                                onSendToWatchClick = {
                                                    songInfoViewModel.sendSongToWatch(song) { message ->
                                                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                                    }
                                                },
                                                playButtonShape = playButtonShape,
                                                evenCornerRadiusElems = evenCornerRadiusElems,
                                                isSongHiddenInAlbum = isSongHiddenInAlbum,
                                                onToggleHideFromAlbum = onToggleHideFromAlbum
                                            )
                                        } else {
                                            Row1Actions(
                                                isFavorite = isFavorite,
                                                onPlaySong = onPlaySong,
                                                onToggleFavorite = onToggleFavorite,
                                                onShareClick = shareSongAction,
                                                playButtonShape = playButtonShape,
                                                evenCornerRadiusElems = evenCornerRadiusElems
                                            )

                                            Row2Actions(
                                                onAddToQueue = onAddToQueue,
                                                onAddNextToQueue = onAddNextToQueue
                                            )

                                            Row3Actions(
                                                onAddToPlaylist = onAddToPlayList,
                                                onDelete = deleteSongAction
                                            )

                                            if (onToggleHideFromAlbum != null) {
                                                HideFromAlbumActionRow(
                                                    isSongHiddenInAlbum = isSongHiddenInAlbum,
                                                    onToggleHideFromAlbum = onToggleHideFromAlbum,
                                                    compact = false
                                                )
                                            }

                                            if (shouldRenderWatchTransferRow) {
                                                Row4Actions(
                                                    isPixelPlayWatchAvailable = isPixelPlayWatchAvailable,
                                                    isSendingToWatch = isSendingToWatch,
                                                    shouldOfferWatchTransfer = shouldOfferWatchTransfer,
                                                    shouldShowWatchTransferLoading = shouldShowWatchTransferLoading,
                                                    currentSongTransferPercent = currentSongTransferPercent,
                                                    currentSongTransferStatus = currentSongTransfer?.status,
                                                    currentSongTransferTotalBytes = currentSongTransfer?.totalBytes ?: 0L,
                                                    onSendToWatchClick = {
                                                        songInfoViewModel.sendSongToWatch(song) { message ->
                                                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                                        }
                                                    }
                                                )
                                            }
                                        }

                                        Spacer(Modifier.height(80.dp))
                                    }
                                }
                                1 -> { // Details / Info
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .graphicsLayer {
                                                val pageOffset = page - (pagerState.currentPage + pagerState.currentPageOffsetFraction)
                                                val progress = kotlin.math.abs(pageOffset).coerceIn(0f, 1f)
                                                scaleX = 1f + 0.15f * progress * (1f - progress)
                                                translationX = pageOffset * size.width * 0.15f
                                                transformOrigin = TransformOrigin(0.5f, 0.5f)
                                            }
                                            .verticalScroll(rememberScrollState())
                                            .padding(horizontal = 16.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(infoSegmentContainerShape),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            SongInfoSegmentedListItem(
                                                headline = stringResource(R.string.song_info_duration_label),
                                                supporting = formatDuration(song.duration),
                                                icon = Icons.Rounded.Schedule,
                                                iconDescription = stringResource(R.string.song_info_duration_label),
                                                shape = infoSegmentItemShape,
                                            )

                                            if (!song.genre.isNullOrEmpty()) {
                                                SongInfoSegmentedListItem(
                                                    headline = stringResource(R.string.song_info_genre_label),
                                                    supporting = song.genre,
                                                    icon = Icons.Rounded.MusicNote,
                                                    iconDescription = stringResource(R.string.song_info_genre_label),
                                                    shape = infoSegmentItemShape,
                                                    onClick = onNavigateToGenre,
                                                )
                                            }

                                            SongInfoSegmentedListItem(
                                                headline = stringResource(R.string.song_info_album_label),
                                                supporting = song.album,
                                                icon = Icons.Rounded.Album,
                                                iconDescription = stringResource(R.string.song_info_album_label),
                                                shape = infoSegmentItemShape,
                                                onClick = onNavigateToAlbum,
                                            )

                                            SongInfoSegmentedListItem(
                                                headline = stringResource(R.string.song_info_artist_label),
                                                supporting = song.displayArtist,
                                                icon = Icons.Rounded.Person,
                                                iconDescription = stringResource(R.string.song_info_artist_label),
                                                shape = infoSegmentItemShape,
                                                onClick = {
                                                    if (song.artists.size > 1) {
                                                        showArtistPicker = true
                                                    } else {
                                                        onNavigateToArtist()
                                                    }
                                                },
                                            )

                                            if (!audioMetaLabel.isNullOrEmpty()) {
                                                SongInfoSegmentedListItem(
                                                    headline = stringResource(R.string.song_info_audio_format_label),
                                                    supporting = audioMetaLabel,
                                                    icon = Icons.Rounded.Info,
                                                    iconDescription = stringResource(R.string.song_info_audio_format_label),
                                                    shape = infoSegmentItemShape,
                                                )
                                            }

                                            SongInfoSegmentedListItem(
                                                headline = songLocationInfo.label,
                                                supporting = songLocationInfo.value,
                                                icon = if (songLocationInfo.isCloud) Icons.Rounded.Cloud else Icons.Rounded.AudioFile,
                                                iconDescription = stringResource(
                                                    if (songLocationInfo.isCloud) {
                                                        R.string.song_info_cd_provider
                                                    } else {
                                                        R.string.song_info_cd_file
                                                    }
                                                ),
                                                shape = infoSegmentItemShape,
                                            )
                                        }
                                        Spacer(Modifier.height(80.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                // Bottom Tab Bar
                PrimaryTabRow(
                    selectedTabIndex = pagerState.currentPage,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                        .padding(5.dp),
                    containerColor = Color.Transparent,
                    divider = {},
                    indicator = {}
                ) {
                    TabAnimation(
                        index = 0,
                        title = stringResource(R.string.song_info_tab_options),
                        selectedIndex = pagerState.currentPage,
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(0)
                            }
                        },
                        transformOrigin = TransformOrigin(0f, 0.5f)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Rounded.Menu,
                                contentDescription = stringResource(R.string.song_info_tab_options),
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                stringResource(R.string.song_info_tab_options_badge),
                                fontFamily = GoogleSansRounded,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }

                    TabAnimation(
                        index = 1,
                        title = stringResource(R.string.song_info_tab_info),
                        selectedIndex = pagerState.currentPage,
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(1)
                            }
                        },
                        transformOrigin = TransformOrigin(1f, 0.5f)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Rounded.Info,
                                contentDescription = stringResource(R.string.song_info_tab_info),
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                stringResource(R.string.song_info_tab_info_badge),
                                fontFamily = GoogleSansRounded,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
    }

    EditSongSheet(
        visible = showEditSheet,
        song = song,
        onDismiss = { showEditSheet = false },
        onSave = { title, artist, album, albumArtist, composer, genre, lyrics, trackNumber, discNumber, replayGainTrackGainDb, replayGainAlbumGainDb, coverArt ->
            onEditSong(
                title,
                artist,
                album,
                albumArtist,
                composer,
                genre,
                lyrics,
                trackNumber,
                discNumber,
                replayGainTrackGainDb,
                replayGainAlbumGainDb,
                coverArt
            )
            showEditSheet = false
        },
    )

    val artistPickerSheetState = androidx.compose.material3.rememberModalBottomSheetState(skipPartiallyExpanded = true)
    if (showArtistPicker && resolvedArtists.isNotEmpty()) {
        com.theveloper.pixelplay.presentation.components.player.PlayerArtistPickerBottomSheet(
            song = song,
            artists = resolvedArtists,
            sheetState = artistPickerSheetState,
            onDismiss = { showArtistPicker = false },
            onArtistClick = { artist ->
                showArtistPicker = false
                onNavigateToArtistById(artist.id)
            }
        )
    }
}

@Composable
private fun SongInfoSegmentedListItem(
    headline: String,
    supporting: String,
    icon: ImageVector,
    iconDescription: String,
    shape: Shape,
    onClick: (() -> Unit)? = null,
) {
    val modifier = Modifier
        .fillMaxWidth()
        .clip(shape)
        .let { baseModifier ->
            if (onClick != null) {
                baseModifier.clickable(onClick = onClick)
            } else {
                baseModifier
            }
        }

    Surface(
        modifier = modifier,
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        ListItem(
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
            headlineContent = { Text(headline) },
            supportingContent = { Text(supporting) },
            leadingContent = {
                Icon(
                    imageVector = icon,
                    contentDescription = iconDescription,
                )
            }
        )
    }
}

@Composable
private fun HideFromAlbumActionRow(
    isSongHiddenInAlbum: Boolean,
    onToggleHideFromAlbum: () -> Unit,
    compact: Boolean = false,
) {
    val coroutineScope = rememberCoroutineScope()
    var clickPending by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }

    val containerColor by animateColorAsState(
        targetValue = if (isSongHiddenInAlbum) {
            MaterialTheme.colorScheme.secondaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerHigh
        },
        animationSpec = tween(durationMillis = 250),
        label = "HideActionContainerColor"
    )

    val contentColor by animateColorAsState(
        targetValue = if (isSongHiddenInAlbum) {
            MaterialTheme.colorScheme.onSecondaryContainer
        } else {
            MaterialTheme.colorScheme.onSurface
        },
        animationSpec = tween(durationMillis = 250),
        label = "HideActionContentColor"
    )

    FilledTonalButton(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (compact) 58.dp else 66.dp),
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        shape = CircleShape,
        interactionSource = interactionSource,
        contentPadding = PaddingValues(horizontal = 16.dp),
        onClick = {
            if (clickPending) return@FilledTonalButton
            clickPending = true
            coroutineScope.launch {
                kotlinx.coroutines.delay(180)
                clickPending = false
                onToggleHideFromAlbum()
            }
        }
    ) {
        Icon(
            imageVector = if (isSongHiddenInAlbum) Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff,
            contentDescription = stringResource(
                if (isSongHiddenInAlbum) R.string.album_unhide_from_album else R.string.album_hide_from_album
            ),
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(10.dp))
        TightWrapText(
            text = stringResource(
                if (isSongHiddenInAlbum) R.string.album_unhide_from_album else R.string.album_hide_from_album
            ),
            fontFamily = GoogleSansRounded,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.titleSmall,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun CompactSongActions(
    isFavorite: Boolean,
    onPlaySong: () -> Unit,
    onToggleFavorite: () -> Unit,
    onShareClick: () -> Unit,
    onAddToQueue: () -> Unit,
    onAddNextToQueue: () -> Unit,
    onAddToPlaylist: () -> Unit,
    onDelete: () -> Unit,
    shouldRenderWatchTransfer: Boolean,
    isPixelPlayWatchAvailable: Boolean,
    isSendingToWatch: Boolean,
    shouldOfferWatchTransfer: Boolean,
    shouldShowWatchTransferLoading: Boolean,
    onSendToWatchClick: () -> Unit,
    playButtonShape: Shape,
    evenCornerRadiusElems: androidx.compose.ui.unit.Dp,
    isSongHiddenInAlbum: Boolean = false,
    onToggleHideFromAlbum: (() -> Unit)? = null
) {
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()
    var clickPending by remember { mutableStateOf(false) }

    val favoriteButtonCornerRadius by animateDpAsState(
        targetValue = if (isFavorite) evenCornerRadiusElems else 60.dp,
        animationSpec = tween(durationMillis = 300), label = "CompactFavoriteCornerAnimation"
    )
    val favoriteButtonContainerColor by animateColorAsState(
        targetValue = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        animationSpec = tween(durationMillis = 300), label = "CompactFavoriteContainerColorAnimation"
    )
    val favoriteButtonContentColor by animateColorAsState(
        targetValue = if (isFavorite) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(durationMillis = 300), label = "CompactFavoriteContentColorAnimation"
    )

    val favoriteButtonShape = remember(favoriteButtonCornerRadius) {
        AbsoluteSmoothCornerShape(
            cornerRadiusTR = favoriteButtonCornerRadius, smoothnessAsPercentBR = 60, cornerRadiusBR = favoriteButtonCornerRadius,
            smoothnessAsPercentTL = 60, cornerRadiusTL = favoriteButtonCornerRadius, smoothnessAsPercentBL = 60,
            cornerRadiusBL = favoriteButtonCornerRadius, smoothnessAsPercentTR = 60
        )
    }

    val sendToWatchContainerColor by animateColorAsState(
        targetValue = if (isSendingToWatch) {
            MaterialTheme.colorScheme.secondaryContainer
        } else {
            MaterialTheme.colorScheme.primaryContainer
        },
        animationSpec = tween(durationMillis = 250),
        label = "CompactSendToWatchContainerColorAnimation"
    )
    val sendToWatchContentColor by animateColorAsState(
        targetValue = if (isSendingToWatch) {
            MaterialTheme.colorScheme.onSecondaryContainer
        } else {
            MaterialTheme.colorScheme.onPrimaryContainer
        },
        animationSpec = tween(durationMillis = 250),
        label = "CompactSendToWatchContentColorAnimation"
    )

    val buttonHeight = 58.dp

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Row 1: Play, Favorite, Add to Queue, Play Next
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(buttonHeight),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            FilledTonalButton(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                contentPadding = PaddingValues(0.dp),
                shape = playButtonShape,
                onClick = {
                    if (clickPending) return@FilledTonalButton
                    clickPending = true
                    coroutineScope.launch {
                        kotlinx.coroutines.delay(180)
                        clickPending = false
                        onPlaySong()
                    }
                }
            ) {
                Icon(
                    modifier = Modifier.size(28.dp),
                    imageVector = Icons.Rounded.PlayArrow,
                    contentDescription = stringResource(R.string.song_info_cd_play)
                )
            }

            FilledIconButton(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                onClick = {
                    if (clickPending) return@FilledIconButton
                    clickPending = true
                    coroutineScope.launch {
                        kotlinx.coroutines.delay(180)
                        clickPending = false
                        onToggleFavorite()
                    }
                },
                shape = favoriteButtonShape,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = favoriteButtonContainerColor,
                    contentColor = favoriteButtonContentColor
                )
            ) {
                Icon(
                    modifier = Modifier.size(26.dp),
                    imageVector = if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                    contentDescription = stringResource(
                        if (isFavorite) R.string.song_info_cd_remove_from_favorites else R.string.song_info_cd_add_to_favorites
                    )
                )
            }

            FilledTonalButton(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                ),
                contentPadding = PaddingValues(0.dp),
                shape = CircleShape,
                onClick = {
                    if (clickPending) return@FilledTonalButton
                    clickPending = true
                    coroutineScope.launch {
                        kotlinx.coroutines.delay(180)
                        clickPending = false
                        onAddToQueue()
                    }
                }
            ) {
                Icon(
                    modifier = Modifier.size(26.dp),
                    imageVector = Icons.AutoMirrored.Rounded.QueueMusic,
                    contentDescription = stringResource(R.string.song_info_cd_add_to_queue)
                )
            }

            FilledTonalButton(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    contentColor = MaterialTheme.colorScheme.onTertiary
                ),
                contentPadding = PaddingValues(0.dp),
                shape = CircleShape,
                onClick = {
                    if (clickPending) return@FilledTonalButton
                    clickPending = true
                    coroutineScope.launch {
                        kotlinx.coroutines.delay(180)
                        clickPending = false
                        onAddNextToQueue()
                    }
                }
            ) {
                Icon(
                    modifier = Modifier.size(26.dp),
                    imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                    contentDescription = stringResource(R.string.song_info_cd_queue_next)
                )
            }
        }

        // Row 2: Add to Playlist, [Watch if available], Share, Delete
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(buttonHeight),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            FilledTonalButton(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ),
                contentPadding = PaddingValues(0.dp),
                shape = CircleShape,
                onClick = {
                    if (clickPending) return@FilledTonalButton
                    clickPending = true
                    coroutineScope.launch {
                        kotlinx.coroutines.delay(180)
                        clickPending = false
                        onAddToPlaylist()
                    }
                }
            ) {
                Icon(
                    modifier = Modifier.size(26.dp),
                    imageVector = Icons.AutoMirrored.Rounded.PlaylistAdd,
                    contentDescription = stringResource(R.string.song_info_cd_add_to_playlist)
                )
            }

            if (shouldRenderWatchTransfer) {
                FilledTonalButton(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (isPixelPlayWatchAvailable) {
                            sendToWatchContainerColor
                        } else {
                            MaterialTheme.colorScheme.surfaceContainerHigh
                        },
                        contentColor = if (isPixelPlayWatchAvailable) {
                            sendToWatchContentColor
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = CircleShape,
                    enabled = shouldOfferWatchTransfer && !isSendingToWatch,
                    contentPadding = PaddingValues(0.dp),
                    onClick = {
                        if (clickPending) return@FilledTonalButton
                        clickPending = true
                        coroutineScope.launch {
                            kotlinx.coroutines.delay(180)
                            clickPending = false
                            onSendToWatchClick()
                        }
                    }
                ) {
                    if (shouldShowWatchTransferLoading || isSendingToWatch) {
                        LoadingIndicator(modifier = Modifier.size(20.dp))
                    } else {
                        Icon(
                            painter = painterResource(R.drawable.rounded_watch_arrow_down_24),
                            contentDescription = stringResource(
                                if (isPixelPlayWatchAvailable) {
                                    R.string.song_info_cd_send_to_watch
                                } else {
                                    R.string.song_info_watch_unavailable
                                }
                            )
                        )
                    }
                }
            }

            FilledTonalIconButton(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                onClick = {
                    if (clickPending) return@FilledTonalIconButton
                    clickPending = true
                    coroutineScope.launch {
                        kotlinx.coroutines.delay(180)
                        clickPending = false
                        onShareClick()
                    }
                },
                shape = CircleShape,
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Icon(
                    modifier = Modifier.size(26.dp),
                    imageVector = Icons.Rounded.Share,
                    contentDescription = stringResource(R.string.song_info_cd_share_song_file)
                )
            }

            FilledTonalButton(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ),
                contentPadding = PaddingValues(0.dp),
                shape = CircleShape,
                onClick = {
                    if (clickPending) return@FilledTonalButton
                    clickPending = true
                    coroutineScope.launch {
                        kotlinx.coroutines.delay(180)
                        clickPending = false
                        onDelete()
                    }
                }
            ) {
                Icon(
                    modifier = Modifier.size(26.dp),
                    imageVector = Icons.Default.DeleteForever,
                    contentDescription = stringResource(R.string.song_info_action_delete)
                )
            }
        }

        if (onToggleHideFromAlbum != null) {
            HideFromAlbumActionRow(
                isSongHiddenInAlbum = isSongHiddenInAlbum,
                onToggleHideFromAlbum = onToggleHideFromAlbum,
                compact = true
            )
        }
    }
}

@Composable
private fun Row1Actions(
    isFavorite: Boolean,
    onPlaySong: () -> Unit,
    onToggleFavorite: () -> Unit,
    onShareClick: () -> Unit,
    playButtonShape: Shape,
    evenCornerRadiusElems: androidx.compose.ui.unit.Dp
) {
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()
    var clickPending by remember { mutableStateOf(false) }

    val playInteractionSource = remember { MutableInteractionSource() }
    val isPlayPressed by playInteractionSource.collectIsPressedAsState()
    var playVisualPressed by remember { mutableStateOf(false) }
    LaunchedEffect(isPlayPressed) {
        if (isPlayPressed) {
            playVisualPressed = true
        } else {
            kotlinx.coroutines.delay(180)
            playVisualPressed = false
        }
    }

    val favoriteInteractionSource = remember { MutableInteractionSource() }
    val isFavoritePressed by favoriteInteractionSource.collectIsPressedAsState()
    var favoriteVisualPressed by remember { mutableStateOf(false) }
    LaunchedEffect(isFavoritePressed) {
        if (isFavoritePressed) {
            favoriteVisualPressed = true
        } else {
            kotlinx.coroutines.delay(180)
            favoriteVisualPressed = false
        }
    }

    val shareInteractionSource = remember { MutableInteractionSource() }
    val isSharePressed by shareInteractionSource.collectIsPressedAsState()
    var shareVisualPressed by remember { mutableStateOf(false) }
    LaunchedEffect(isSharePressed) {
        if (isSharePressed) {
            shareVisualPressed = true
        } else {
            kotlinx.coroutines.delay(180)
            shareVisualPressed = false
        }
    }

    val pressSpec = spring<Float>(
        dampingRatio = 0.8f,
        stiffness = 300f
    )

    val pressFractionPlay by animateFloatAsState(
        targetValue = if (playVisualPressed) 1f else 0f,
        animationSpec = pressSpec,
        label = "PlayPressFraction"
    )
    val pressFractionFavorite by animateFloatAsState(
        targetValue = if (favoriteVisualPressed) 1f else 0f,
        animationSpec = pressSpec,
        label = "FavoritePressFraction"
    )
    val pressFractionShare by animateFloatAsState(
        targetValue = if (shareVisualPressed) 1f else 0f,
        animationSpec = pressSpec,
        label = "SharePressFraction"
    )

    val weightPlay = (0.56f + 0.08f * pressFractionPlay - 0.04f * pressFractionFavorite - 0.04f * pressFractionShare).coerceAtLeast(0.1f)
    val weightFavorite = (0.22f + 0.08f * pressFractionFavorite - 0.04f * pressFractionPlay - 0.02f * pressFractionShare).coerceAtLeast(0.05f)
    val weightShare = (0.22f + 0.08f * pressFractionShare - 0.04f * pressFractionPlay - 0.02f * pressFractionFavorite).coerceAtLeast(0.05f)

    // Local favorite button animations
    val favoriteButtonCornerRadius by animateDpAsState(
        targetValue = if (isFavorite) evenCornerRadiusElems else 60.dp,
        animationSpec = tween(durationMillis = 300), label = "FavoriteCornerAnimation"
    )
    val favoriteButtonContainerColor by animateColorAsState(
        targetValue = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        animationSpec = tween(durationMillis = 300), label = "FavoriteContainerColorAnimation"
    )
    val favoriteButtonContentColor by animateColorAsState(
        targetValue = if (isFavorite) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(durationMillis = 300), label = "FavoriteContentColorAnimation"
    )

    val favoriteButtonShape = remember(favoriteButtonCornerRadius) {
        AbsoluteSmoothCornerShape(
            cornerRadiusTR = favoriteButtonCornerRadius, smoothnessAsPercentBR = 60, cornerRadiusBR = favoriteButtonCornerRadius,
            smoothnessAsPercentTL = 60, cornerRadiusTL = favoriteButtonCornerRadius, smoothnessAsPercentBL = 60,
            cornerRadiusBL = favoriteButtonCornerRadius, smoothnessAsPercentTR = 60
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        FilledTonalButton(
            modifier = Modifier
                .weight(weightPlay)
                .heightIn(min = 80.dp),
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ),
            contentPadding = PaddingValues(horizontal = 10.dp),
            shape = playButtonShape,
            interactionSource = playInteractionSource,
            onClick = {
                if (clickPending) return@FilledTonalButton
                clickPending = true
                playVisualPressed = true
                android.util.Log.d("PixelPlayerDebug", "Row1Actions: Play clicked")
                coroutineScope.launch {
                    kotlinx.coroutines.delay(180)
                    playVisualPressed = false
                    clickPending = false
                    onPlaySong()
                }
            }
        ) {
            Icon(
                Icons.Rounded.PlayArrow,
                contentDescription = stringResource(R.string.song_info_cd_play),
            )
            Spacer(Modifier.width(6.dp))
            TightWrapText(
                text = stringResource(R.string.song_info_action_play),
                modifier = Modifier.padding(end = 4.dp),
                overflow = TextOverflow.Ellipsis,
                softWrap = false,
                maxLines = 1,
                style = MaterialTheme.typography.titleMedium
            )
        }

        FilledIconButton(
            modifier = Modifier
                .weight(weightFavorite)
                .fillMaxHeight(),
            onClick = {
                if (clickPending) return@FilledIconButton
                clickPending = true
                favoriteVisualPressed = true
                android.util.Log.d("PixelPlayerDebug", "Row1Actions: Favorite clicked")
                coroutineScope.launch {
                    kotlinx.coroutines.delay(180)
                    favoriteVisualPressed = false
                    clickPending = false
                    onToggleFavorite()
                }
            },
            shape = favoriteButtonShape,
            interactionSource = favoriteInteractionSource,
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = favoriteButtonContainerColor,
                contentColor = favoriteButtonContentColor
            )
        ) {
            Icon(
                modifier = Modifier.size(FloatingActionButtonDefaults.LargeIconSize),
                imageVector = if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                contentDescription = stringResource(
                    if (isFavorite) R.string.song_info_cd_remove_from_favorites else R.string.song_info_cd_add_to_favorites
                )
            )
        }

        FilledTonalIconButton(
            modifier = Modifier
                .weight(weightShare)
                .fillMaxHeight(),
            onClick = {
                if (clickPending) return@FilledTonalIconButton
                clickPending = true
                shareVisualPressed = true
                android.util.Log.d("PixelPlayerDebug", "Row1Actions: Share clicked")
                coroutineScope.launch {
                    kotlinx.coroutines.delay(180)
                    shareVisualPressed = false
                    clickPending = false
                    onShareClick()
                }
            },
            shape = CircleShape,
            interactionSource = shareInteractionSource
        ) {
            Icon(
                modifier = Modifier.size(FloatingActionButtonDefaults.LargeIconSize),
                imageVector = Icons.Rounded.Share,
                contentDescription = stringResource(R.string.song_info_cd_share_song_file)
            )
        }
    }
}

@Composable
private fun Row2Actions(
    onAddToQueue: () -> Unit,
    onAddNextToQueue: () -> Unit
) {
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()
    var clickPending by remember { mutableStateOf(false) }

    val queueInteractionSource = remember { MutableInteractionSource() }
    val isQueuePressed by queueInteractionSource.collectIsPressedAsState()
    var queueVisualPressed by remember { mutableStateOf(false) }
    LaunchedEffect(isQueuePressed) {
        if (isQueuePressed) {
            queueVisualPressed = true
        } else {
            kotlinx.coroutines.delay(180)
            queueVisualPressed = false
        }
    }

    val nextInteractionSource = remember { MutableInteractionSource() }
    val isNextPressed by nextInteractionSource.collectIsPressedAsState()
    var nextVisualPressed by remember { mutableStateOf(false) }
    LaunchedEffect(isNextPressed) {
        if (isNextPressed) {
            nextVisualPressed = true
        } else {
            kotlinx.coroutines.delay(180)
            nextVisualPressed = false
        }
    }

    val pressSpec = spring<Float>(
        dampingRatio = 0.8f,
        stiffness = 300f
    )

    val pressFractionQueue by animateFloatAsState(
        targetValue = if (queueVisualPressed) 1f else 0f,
        animationSpec = pressSpec,
        label = "QueuePressFraction"
    )
    val pressFractionNext by animateFloatAsState(
        targetValue = if (nextVisualPressed) 1f else 0f,
        animationSpec = pressSpec,
        label = "NextPressFraction"
    )

    val weightQueue = (0.5f + 0.08f * pressFractionQueue - 0.08f * pressFractionNext).coerceAtLeast(0.1f)
    val weightNext = (0.5f + 0.08f * pressFractionNext - 0.08f * pressFractionQueue).coerceAtLeast(0.1f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        FilledTonalButton(
            modifier = Modifier
                .weight(weightQueue)
                .heightIn(min = 66.dp),
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer
            ),
            contentPadding = PaddingValues(horizontal = 10.dp),
            shape = CircleShape,
            interactionSource = queueInteractionSource,
            onClick = {
                if (clickPending) return@FilledTonalButton
                clickPending = true
                queueVisualPressed = true
                android.util.Log.d("PixelPlayerDebug", "Row2Actions: AddToQueue clicked")
                coroutineScope.launch {
                    kotlinx.coroutines.delay(180)
                    queueVisualPressed = false
                    clickPending = false
                    onAddToQueue()
                }
            }
        ) {
            Icon(
                Icons.AutoMirrored.Rounded.QueueMusic,
                contentDescription = stringResource(R.string.song_info_cd_add_to_queue),
            )
            Spacer(Modifier.width(6.dp))
            TightWrapText(
                text = stringResource(R.string.song_info_action_add_to_queue),
                modifier = Modifier.padding(end = 4.dp),
                overflow = TextOverflow.Ellipsis,
                softWrap = false,
                maxLines = 1,
                style = MaterialTheme.typography.titleSmall
            )
        }

        FilledTonalButton(
            modifier = Modifier
                .weight(weightNext)
                .heightIn(min = 66.dp),
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = MaterialTheme.colorScheme.tertiary,
                contentColor = MaterialTheme.colorScheme.onTertiary
            ),
            contentPadding = PaddingValues(horizontal = 10.dp),
            shape = CircleShape,
            interactionSource = nextInteractionSource,
            onClick = {
                if (clickPending) return@FilledTonalButton
                clickPending = true
                nextVisualPressed = true
                android.util.Log.d("PixelPlayerDebug", "Row2Actions: AddNextToQueue clicked")
                coroutineScope.launch {
                    kotlinx.coroutines.delay(180)
                    nextVisualPressed = false
                    clickPending = false
                    onAddNextToQueue()
                }
            }
        ) {
            Icon(
                Icons.AutoMirrored.Filled.QueueMusic,
                contentDescription = stringResource(R.string.song_info_cd_queue_next)
            )
            Spacer(Modifier.width(6.dp))
            TightWrapText(
                text = stringResource(R.string.song_info_action_queue_next),
                modifier = Modifier.padding(end = 4.dp),
                overflow = TextOverflow.Ellipsis,
                softWrap = false,
                maxLines = 1,
                style = MaterialTheme.typography.titleSmall
            )
        }
    }
}

@Composable
private fun Row3Actions(
    onAddToPlaylist: () -> Unit,
    onDelete: () -> Unit
) {
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()
    var clickPending by remember { mutableStateOf(false) }

    val playlistInteractionSource = remember { MutableInteractionSource() }
    val isPlaylistPressed by playlistInteractionSource.collectIsPressedAsState()
    var playlistVisualPressed by remember { mutableStateOf(false) }
    LaunchedEffect(isPlaylistPressed) {
        if (isPlaylistPressed) {
            playlistVisualPressed = true
        } else {
            kotlinx.coroutines.delay(180)
            playlistVisualPressed = false
        }
    }

    val deleteInteractionSource = remember { MutableInteractionSource() }
    val isDeletePressed by deleteInteractionSource.collectIsPressedAsState()
    var deleteVisualPressed by remember { mutableStateOf(false) }
    LaunchedEffect(isDeletePressed) {
        if (isDeletePressed) {
            deleteVisualPressed = true
        } else {
            kotlinx.coroutines.delay(180)
            deleteVisualPressed = false
        }
    }

    val pressSpec = spring<Float>(
        dampingRatio = 0.8f,
        stiffness = 300f
    )

    val pressFractionPlaylist by animateFloatAsState(
        targetValue = if (playlistVisualPressed) 1f else 0f,
        animationSpec = pressSpec,
        label = "PlaylistPressFraction"
    )
    val pressFractionDelete by animateFloatAsState(
        targetValue = if (deleteVisualPressed) 1f else 0f,
        animationSpec = pressSpec,
        label = "DeletePressFraction"
    )

    val weightPlaylist = (0.5f + 0.08f * pressFractionPlaylist - 0.08f * pressFractionDelete).coerceAtLeast(0.1f)
    val weightDelete = (0.5f + 0.08f * pressFractionDelete - 0.08f * pressFractionPlaylist).coerceAtLeast(0.1f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        FilledTonalButton(
            modifier = Modifier
                .weight(weightPlaylist)
                .heightIn(min = 66.dp),
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            ),
            contentPadding = PaddingValues(horizontal = 10.dp),
            shape = CircleShape,
            interactionSource = playlistInteractionSource,
            onClick = {
                if (clickPending) return@FilledTonalButton
                clickPending = true
                playlistVisualPressed = true
                android.util.Log.d("PixelPlayerDebug", "Row3Actions: AddToPlaylist clicked")
                coroutineScope.launch {
                    kotlinx.coroutines.delay(180)
                    playlistVisualPressed = false
                    clickPending = false
                    onAddToPlaylist()
                }
            }
        ) {
            Icon(
                Icons.AutoMirrored.Rounded.PlaylistAdd,
                contentDescription = stringResource(R.string.song_info_cd_add_to_playlist)
            )
            Spacer(Modifier.width(6.dp))
            TightWrapText(
                text = stringResource(R.string.common_playlist),
                modifier = Modifier.padding(end = 4.dp),
                overflow = TextOverflow.Ellipsis,
                softWrap = false,
                maxLines = 1,
                style = MaterialTheme.typography.titleSmall
            )
        }

        FilledTonalButton(
            modifier = Modifier
                .weight(weightDelete)
                .heightIn(min = 66.dp),
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            ),
            contentPadding = PaddingValues(horizontal = 10.dp),
            shape = CircleShape,
            interactionSource = deleteInteractionSource,
            onClick = {
                if (clickPending) return@FilledTonalButton
                clickPending = true
                deleteVisualPressed = true
                android.util.Log.d("PixelPlayerDebug", "Row3Actions: Delete clicked")
                coroutineScope.launch {
                    kotlinx.coroutines.delay(180)
                    deleteVisualPressed = false
                    clickPending = false
                    onDelete()
                }
            }
        ) {
            Icon(
                Icons.Default.DeleteForever,
                contentDescription = stringResource(R.string.song_info_action_delete)
            )
            Spacer(Modifier.width(6.dp))
            TightWrapText(
                text = stringResource(R.string.song_info_action_delete),
                modifier = Modifier.padding(end = 4.dp),
                overflow = TextOverflow.Ellipsis,
                softWrap = false,
                maxLines = 1,
                style = MaterialTheme.typography.titleSmall
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun Row4Actions(
    isPixelPlayWatchAvailable: Boolean,
    isSendingToWatch: Boolean,
    shouldOfferWatchTransfer: Boolean,
    shouldShowWatchTransferLoading: Boolean,
    currentSongTransferPercent: Int,
    currentSongTransferStatus: String?,
    currentSongTransferTotalBytes: Long,
    onSendToWatchClick: () -> Unit,
) {
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()
    var clickPending by remember { mutableStateOf(false) }

    val watchInteractionSource = remember { MutableInteractionSource() }

    val sendToWatchContainerColor by animateColorAsState(
        targetValue = if (isSendingToWatch) {
            MaterialTheme.colorScheme.secondaryContainer
        } else {
            MaterialTheme.colorScheme.primaryContainer
        },
        animationSpec = tween(durationMillis = 250),
        label = "SendToWatchContainerColorAnimation"
    )
    val sendToWatchContentColor by animateColorAsState(
        targetValue = if (isSendingToWatch) {
            MaterialTheme.colorScheme.onSecondaryContainer
        } else {
            MaterialTheme.colorScheme.onPrimaryContainer
        },
        animationSpec = tween(durationMillis = 250),
        label = "SendToWatchContentColorAnimation"
    )

    FilledTonalButton(
        modifier = Modifier
            .fillMaxWidth()
            .height(66.dp),
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = if (isPixelPlayWatchAvailable) {
                sendToWatchContainerColor
            } else {
                MaterialTheme.colorScheme.surfaceContainerHigh
            },
            contentColor = if (isPixelPlayWatchAvailable) {
                sendToWatchContentColor
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        shape = CircleShape,
        enabled = shouldOfferWatchTransfer && !isSendingToWatch,
        interactionSource = watchInteractionSource,
        onClick = {
            if (clickPending) return@FilledTonalButton
            clickPending = true
            android.util.Log.d("PixelPlayerDebug", "Row4Actions: SendToWatch clicked")
            coroutineScope.launch {
                kotlinx.coroutines.delay(180)
                clickPending = false
                onSendToWatchClick()
            }
        }
    ) {
        if (shouldShowWatchTransferLoading) {
            LoadingIndicator(modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(10.dp))
            Text(
                text = stringResource(R.string.song_info_checking_watch),
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
        } else if (isSendingToWatch) {
            LoadingIndicator(modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(10.dp))
            Text(
                text = when {
                    currentSongTransferStatus == com.theveloper.pixelplay.shared.WearTransferProgress.STATUS_TRANSFERRING && currentSongTransferTotalBytes > 0L ->
                        stringResource(
                            R.string.song_info_transferring_percent,
                            currentSongTransferPercent
                        )
                    currentSongTransferStatus == com.theveloper.pixelplay.shared.WearTransferProgress.STATUS_TRANSFERRING ->
                        stringResource(R.string.song_info_transferring_to_watch)
                    else ->
                        stringResource(R.string.song_info_transfer_in_progress)
                },
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
        } else {
            Icon(
                painter = painterResource(R.drawable.rounded_watch_arrow_down_24),
                contentDescription = stringResource(
                    if (isPixelPlayWatchAvailable) {
                        R.string.song_info_cd_send_to_watch
                    } else {
                        R.string.song_info_watch_unavailable
                    }
                )
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = stringResource(
                    if (isPixelPlayWatchAvailable) {
                        R.string.song_info_send_to_watch
                    } else {
                        R.string.song_info_watch_unavailable
                    }
                ),
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

