package com.theveloper.pixelplay.presentation.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.DragIndicator
import androidx.compose.material.icons.rounded.Equalizer
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.FloatingToolbarExitDirection
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.view.HapticFeedbackConstantsCompat
import com.theveloper.pixelplay.R
import com.theveloper.pixelplay.data.media.AudioMetadataReader
import com.theveloper.pixelplay.data.media.CoverArtUpdate
import com.theveloper.pixelplay.data.model.Album
import com.theveloper.pixelplay.data.model.Song
import com.theveloper.pixelplay.presentation.utils.LocalAppHapticsConfig
import com.theveloper.pixelplay.presentation.utils.performAppCompatHapticFeedback
import com.theveloper.pixelplay.presentation.viewmodel.AlbumTrackEditItem
import com.theveloper.pixelplay.ui.theme.GoogleSansRounded
import com.theveloper.pixelplay.utils.formatDuration
import com.theveloper.pixelplay.utils.formatSongCount
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import racra.compose.smooth_corner_rect_library.AbsoluteSmoothCornerShape
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import timber.log.Timber
import java.io.File
import java.util.Locale

private data class EditableAlbumSong(
    val song: Song,
    val discNumber: Int
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun EditAlbumSheet(
    visible: Boolean,
    album: Album,
    songs: List<Song>,
    onDismiss: () -> Unit,
    onSave: (
        newAlbumTitle: String,
        newAlbumArtist: String,
        newArtist: String,
        newGenre: String,
        newReplayGainAlbumGainDb: String,
        coverArtUpdate: CoverArtUpdate?,
        trackEdits: List<AlbumTrackEditItem>
    ) -> Unit
) {
    val transitionState = remember { MutableTransitionState(false) }
    transitionState.targetState = visible

    if (transitionState.currentState || transitionState.targetState) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false
            )
        ) {
            AnimatedVisibility(
                visibleState = transitionState,
                enter = slideInVertically(initialOffsetY = { it / 6 }) + fadeIn(animationSpec = tween(220)),
                exit = slideOutVertically(targetOffsetY = { it / 6 }) + fadeOut(animationSpec = tween(200))
            ) {
                EditAlbumContent(
                    album = album,
                    songs = songs,
                    onDismiss = onDismiss,
                    onSave = onSave
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun EditAlbumContent(
    album: Album,
    songs: List<Song>,
    onDismiss: () -> Unit,
    onSave: (
        newAlbumTitle: String,
        newAlbumArtist: String,
        newArtist: String,
        newGenre: String,
        newReplayGainAlbumGainDb: String,
        coverArtUpdate: CoverArtUpdate?,
        trackEdits: List<AlbumTrackEditItem>
    ) -> Unit
) {
    var albumTitle by remember { mutableStateOf(album.title) }
    var albumArtist by remember { mutableStateOf(album.albumArtist ?: album.artist) }
    var artist by remember { mutableStateOf("") }
    var genre by remember {
        mutableStateOf(songs.firstOrNull { !it.genre.isNullOrBlank() }?.genre ?: "")
    }
    var replayGainAlbumGainDb by remember { mutableStateOf("") }

    var coverArtPreview by remember { mutableStateOf<ImageBitmap?>(null) }
    var editedCoverArt by remember { mutableStateOf<CoverArtUpdate?>(null) }
    var isCoverArtDeleted by remember { mutableStateOf(false) }
    var showCoverArtCropper by remember { mutableStateOf(false) }
    var pendingCoverArtUri by remember { mutableStateOf<Uri?>(null) }

    var showInfoDialog by remember { mutableStateOf(false) }

    val initialTracks = remember(songs) {
        songs.map { song ->
            EditableAlbumSong(
                song = song,
                discNumber = song.discNumber?.takeIf { it > 0 } ?: 1
            )
        }
    }
    var editableTracks by remember(songs) { mutableStateOf(initialTracks) }
    var discs by remember(songs) {
        mutableStateOf(
            initialTracks.map { it.discNumber }.ifEmpty { listOf(1) }.distinct().sorted()
        )
    }

    val pickCoverArtLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            pendingCoverArtUri = uri
            showCoverArtCropper = true
        }
    }

    LaunchedEffect(album, songs) {
        albumTitle = album.title
        albumArtist = album.albumArtist ?: album.artist
        genre = songs.firstOrNull { !it.genre.isNullOrBlank() }?.genre ?: ""
        coverArtPreview = null
        editedCoverArt = null
        isCoverArtDeleted = false

        val firstSong = songs.firstOrNull { it.path.isNotBlank() }
        if (firstSong != null) {
            val embeddedMetadata = withContext(Dispatchers.IO) {
                try {
                    val file = File(firstSong.path)
                    if (file.exists()) {
                        AudioMetadataReader.read(file, readArtwork = false)
                    } else null
                } catch (e: Exception) {
                    Timber.e(e, "Failed to read embedded metadata for album edit")
                    null
                }
            }
            embeddedMetadata?.replayGainAlbumGainDb?.let { gain ->
                replayGainAlbumGainDb = String.format(Locale.US, "%.2f", gain)
            }
        }
    }

    if (showCoverArtCropper && pendingCoverArtUri != null) {
        CoverArtCropperDialog(
            sourceUri = pendingCoverArtUri!!,
            onDismiss = {
                showCoverArtCropper = false
                pendingCoverArtUri = null
            },
            onConfirm = { result ->
                coverArtPreview = result.preview
                editedCoverArt = result.update
                isCoverArtDeleted = false
                showCoverArtCropper = false
                pendingCoverArtUri = null
            }
        )
    }

    val textFieldColors = TextFieldDefaults.colors(
        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        focusedIndicatorColor = Color.Transparent,
        unfocusedIndicatorColor = Color.Transparent,
        disabledIndicatorColor = Color.Transparent,
    )

    val textFieldShape = AbsoluteSmoothCornerShape(
        cornerRadiusTL = 10.dp,
        smoothnessAsPercentBL = 60,
        cornerRadiusTR = 10.dp,
        smoothnessAsPercentBR = 60,
        cornerRadiusBL = 10.dp,
        smoothnessAsPercentTL = 60,
        cornerRadiusBR = 10.dp,
        smoothnessAsPercentTR = 60
    )

    val scrollBehavior = FloatingToolbarDefaults.exitAlwaysScrollBehavior(
        exitDirection = FloatingToolbarExitDirection.Bottom
    )

    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            icon = {
                Icon(
                    Icons.Rounded.Info,
                    contentDescription = stringResource(R.string.edit_song_cd_info_icon)
                )
            },
            title = { Text(stringResource(R.string.edit_album_dialog_title)) },
            text = { Text(stringResource(R.string.edit_album_dialog_body)) },
            confirmButton = {
                TextButton(onClick = { showInfoDialog = false }) {
                    Text(
                        stringResource(R.string.edit_song_dialog_got_it),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        )
    }

    val density = LocalDensity.current
    val imeInsets = WindowInsets.ime
    val isKeyboardVisible by remember { derivedStateOf { imeInsets.getBottom(density) > 0 } }
    val listState = rememberLazyListState()
    val view = LocalView.current
    val appHapticsConfig = LocalAppHapticsConfig.current

    val reorderableState = rememberReorderableLazyListState(
        lazyListState = listState,
        onMove = { from, to ->
            val fromKey = from.key as? String ?: return@rememberReorderableLazyListState
            val toKey = to.key as? String ?: return@rememberReorderableLazyListState

            if (!fromKey.startsWith("track_")) return@rememberReorderableLazyListState
            val fromSongId = fromKey.removePrefix("track_")
            val fromIdx = editableTracks.indexOfFirst { it.song.id == fromSongId }
            if (fromIdx == -1) return@rememberReorderableLazyListState

            if (toKey.startsWith("track_")) {
                val toSongId = toKey.removePrefix("track_")
                val toIdx = editableTracks.indexOfFirst { it.song.id == toSongId }
                if (toIdx == -1 || fromIdx == toIdx) return@rememberReorderableLazyListState

                val targetDisc = editableTracks[toIdx].discNumber
                val movedItem = editableTracks[fromIdx].copy(discNumber = targetDisc)

                editableTracks = editableTracks.toMutableList().apply {
                    removeAt(fromIdx)
                    add(toIdx, movedItem)
                }
            } else if (toKey.startsWith("disc_header_")) {
                val targetDisc = toKey.removePrefix("disc_header_").toIntOrNull()
                if (targetDisc != null) {
                    val movedItem = editableTracks[fromIdx].copy(discNumber = targetDisc)
                    val firstIdxOfDisc = editableTracks.indexOfFirst { it.discNumber == targetDisc }
                    val insertIdx = if (firstIdxOfDisc != -1) firstIdxOfDisc else editableTracks.size
                    editableTracks = editableTracks.toMutableList().apply {
                        removeAt(fromIdx)
                        val adjustedInsert = if (insertIdx > fromIdx) insertIdx - 1 else insertIdx
                        add(adjustedInsert.coerceIn(0, size), movedItem)
                    }
                }
            }
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        modifier = Modifier.padding(start = 10.dp),
                        text = stringResource(R.string.edit_album_toolbar_title),
                        fontFamily = GoogleSansRounded,
                        style = MaterialTheme.typography.displaySmall
                    )
                },
                actions = {
                    FilledTonalIconButton(
                        modifier = Modifier.padding(end = 10.dp),
                        onClick = { showInfoDialog = true },
                        shape = CircleShape
                    ) {
                        Icon(
                            Icons.Rounded.Info,
                            contentDescription = stringResource(R.string.edit_song_cd_show_metadata_info)
                        )
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.statusBars
    ) { innerPadding ->
        val navBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding(),
                contentPadding = PaddingValues(
                    top = innerPadding.calculateTopPadding() + 8.dp,
                    bottom = if (isKeyboardVisible) 8.dp else (navBarBottom + 100.dp),
                    start = 16.dp,
                    end = 16.dp
                ),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item(key = "cover_art_editor") {
                    CoverArtEditorCard(
                        modifier = Modifier.fillMaxWidth(),
                        albumArtUri = album.albumArtUriString,
                        preview = coverArtPreview,
                        isDeleted = isCoverArtDeleted,
                        onPickNewArt = {
                            pickCoverArtLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        onDelete = {
                            coverArtPreview = null
                            isCoverArtDeleted = true
                            editedCoverArt = CoverArtUpdate(isDeletion = true)
                        },
                        onReset = {
                            coverArtPreview = null
                            editedCoverArt = null
                            isCoverArtDeleted = false
                        }
                    )
                }

                // Album Title
                item(key = "field_album_title") {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            modifier = Modifier.padding(start = 4.dp),
                            text = stringResource(R.string.edit_song_field_album),
                            color = MaterialTheme.colorScheme.tertiary,
                            style = MaterialTheme.typography.labelLarge
                        )
                        OutlinedTextField(
                            value = albumTitle,
                            shape = textFieldShape,
                            colors = textFieldColors,
                            onValueChange = { albumTitle = it },
                            placeholder = { Text(stringResource(R.string.edit_song_field_album)) },
                            leadingIcon = {
                                Icon(
                                    Icons.Rounded.Album,
                                    tint = MaterialTheme.colorScheme.tertiary,
                                    contentDescription = null
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }

                // Album Artist
                item(key = "field_album_artist") {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            modifier = Modifier.padding(start = 4.dp),
                            text = stringResource(R.string.edit_song_field_album_artist),
                            color = MaterialTheme.colorScheme.tertiary,
                            style = MaterialTheme.typography.labelLarge
                        )
                        OutlinedTextField(
                            value = albumArtist,
                            shape = textFieldShape,
                            colors = textFieldColors,
                            onValueChange = { albumArtist = it },
                            placeholder = { Text(stringResource(R.string.edit_song_field_album_artist)) },
                            leadingIcon = {
                                Icon(
                                    Icons.Rounded.Person,
                                    tint = MaterialTheme.colorScheme.tertiary,
                                    contentDescription = null
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }

                // Track Artist Override (Optional)
                item(key = "field_artist_override") {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            modifier = Modifier.padding(start = 4.dp),
                            text = stringResource(R.string.edit_song_field_artist),
                            color = MaterialTheme.colorScheme.tertiary,
                            style = MaterialTheme.typography.labelLarge
                        )
                        OutlinedTextField(
                            value = artist,
                            shape = textFieldShape,
                            colors = textFieldColors,
                            onValueChange = { artist = it },
                            placeholder = { Text(stringResource(R.string.edit_album_field_artist_hint)) },
                            leadingIcon = {
                                Icon(
                                    Icons.Rounded.Person,
                                    tint = MaterialTheme.colorScheme.tertiary,
                                    contentDescription = null
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }

                // Genre
                item(key = "field_genre") {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            modifier = Modifier.padding(start = 4.dp),
                            text = stringResource(R.string.edit_song_field_genre),
                            color = MaterialTheme.colorScheme.tertiary,
                            style = MaterialTheme.typography.labelLarge
                        )
                        OutlinedTextField(
                            value = genre,
                            shape = textFieldShape,
                            colors = textFieldColors,
                            onValueChange = { genre = it },
                            placeholder = { Text(stringResource(R.string.edit_song_field_genre)) },
                            leadingIcon = {
                                Icon(
                                    Icons.Rounded.Category,
                                    tint = MaterialTheme.colorScheme.tertiary,
                                    contentDescription = null
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }

                // ReplayGain Album
                item(key = "field_replaygain_album") {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            modifier = Modifier.padding(start = 4.dp),
                            text = stringResource(R.string.edit_song_field_replaygain_album),
                            color = MaterialTheme.colorScheme.tertiary,
                            style = MaterialTheme.typography.labelLarge
                        )
                        OutlinedTextField(
                            value = replayGainAlbumGainDb,
                            shape = textFieldShape,
                            colors = textFieldColors,
                            onValueChange = { replayGainAlbumGainDb = it },
                            placeholder = { Text(stringResource(R.string.edit_song_replaygain_album_placeholder)) },
                            leadingIcon = {
                                Icon(
                                    Icons.Rounded.Equalizer,
                                    tint = MaterialTheme.colorScheme.tertiary,
                                    contentDescription = null
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }

                // Tracks and Discs Section Header
                item(key = "tracks_and_discs_header") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, bottom = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${stringResource(R.string.edit_album_tracks_header)} (${editableTracks.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            fontFamily = GoogleSansRounded,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        FilledTonalButton(
                            onClick = {
                                val nextDisc = (discs.maxOrNull() ?: 1) + 1
                                discs = (discs + nextDisc).sorted()
                            },
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                Icons.Rounded.Add,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                stringResource(R.string.edit_album_add_disc),
                                style = MaterialTheme.typography.labelMedium,
                                fontFamily = GoogleSansRounded
                            )
                        }
                    }
                }

                // Disc Sections and Tracks
                discs.sorted().forEach { discNum ->
                    val tracksForThisDisc = editableTracks.filter { it.discNumber == discNum }

                    item(key = "disc_header_$discNum") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainer)
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = stringResource(R.string.edit_album_disc_header, discNum),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontFamily = GoogleSansRounded
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "•  ${formatSongCount(tracksForThisDisc.size)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            if (discs.size > 1 && tracksForThisDisc.isEmpty()) {
                                IconButton(
                                    onClick = { discs = discs.filterNot { it == discNum } },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Delete,
                                        contentDescription = stringResource(R.string.edit_album_delete_disc),
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    if (tracksForThisDisc.isEmpty()) {
                        item(key = "empty_disc_$discNum") {
                            Text(
                                text = stringResource(R.string.edit_album_empty_disc),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                            )
                        }
                    } else {
                        itemsIndexed(
                            items = tracksForThisDisc,
                            key = { _, item -> "track_${item.song.id}" }
                        ) { index, item ->
                            ReorderableItem(
                                state = reorderableState,
                                key = "track_${item.song.id}"
                            ) { isDragging ->
                                val scale by animateFloatAsState(
                                    targetValue = if (isDragging) 1.03f else 1f,
                                    label = "scale"
                                )

                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .graphicsLayer {
                                            scaleX = scale
                                            scaleY = scale
                                        },
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (isDragging) {
                                        MaterialTheme.colorScheme.surfaceBright
                                    } else {
                                        MaterialTheme.colorScheme.surfaceContainerHigh
                                    },
                                    tonalElevation = if (isDragging) 4.dp else 0.dp
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 8.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        IconButton(
                                            onClick = {},
                                            modifier = Modifier
                                                .draggableHandle(
                                                    onDragStarted = {
                                                        performAppCompatHapticFeedback(
                                                            view,
                                                            appHapticsConfig,
                                                            HapticFeedbackConstantsCompat.GESTURE_START
                                                        )
                                                    },
                                                    onDragStopped = {
                                                        performAppCompatHapticFeedback(
                                                            view,
                                                            appHapticsConfig,
                                                            HapticFeedbackConstantsCompat.GESTURE_END
                                                        )
                                                    }
                                                )
                                                .size(36.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.DragIndicator,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }

                                        Spacer(Modifier.width(4.dp))

                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .background(
                                                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                                                    shape = CircleShape
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "${index + 1}",
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }

                                        Spacer(Modifier.width(10.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = item.song.title,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "${item.song.displayArtist} • ${formatDuration(item.song.duration)}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        if (discs.size > 1) {
                                            var showDiscMenu by remember { mutableStateOf(false) }
                                            Box {
                                                FilledTonalButton(
                                                    onClick = { showDiscMenu = true },
                                                    shape = RoundedCornerShape(10.dp),
                                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                    modifier = Modifier.height(28.dp),
                                                    colors = ButtonDefaults.filledTonalButtonColors(
                                                        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                                        contentColor = MaterialTheme.colorScheme.onSurface
                                                    )
                                                ) {
                                                    Text(
                                                        text = "D$discNum",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontFamily = GoogleSansRounded
                                                    )
                                                    Icon(
                                                        imageVector = Icons.Rounded.ArrowDropDown,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                }

                                                DropdownMenu(
                                                    expanded = showDiscMenu,
                                                    onDismissRequest = { showDiscMenu = false },
                                                    shape = RoundedCornerShape(16.dp),
                                                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                                                ) {
                                                    discs.sorted().forEach { targetDisc ->
                                                        if (targetDisc != discNum) {
                                                            DropdownMenuItem(
                                                                text = {
                                                                    Text(
                                                                        text = stringResource(
                                                                            R.string.edit_album_move_to_disc,
                                                                            targetDisc
                                                                        ),
                                                                        fontFamily = GoogleSansRounded
                                                                    )
                                                                },
                                                                onClick = {
                                                                    showDiscMenu = false
                                                                    editableTracks = editableTracks.map { track ->
                                                                        if (track.song.id == item.song.id) {
                                                                            track.copy(discNumber = targetDisc)
                                                                        } else track
                                                                    }
                                                                }
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = !isKeyboardVisible,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = innerPadding.calculateBottomPadding() + 24.dp)
            ) {
                HorizontalFloatingToolbar(
                    expandedShadowElevation = 0.dp,
                    colors = FloatingToolbarDefaults.standardFloatingToolbarColors(
                        toolbarContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    expanded = true,
                    scrollBehavior = scrollBehavior,
                    content = {
                        FilledTonalButton(
                            onClick = onDismiss,
                            modifier = Modifier.height(48.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        ) {
                            Text(
                                stringResource(R.string.common_cancel),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val trackEdits = mutableListOf<AlbumTrackEditItem>()
                                discs.sorted().forEach { discNum ->
                                    val discTracks = editableTracks.filter { it.discNumber == discNum }
                                    discTracks.forEachIndexed { idx, item ->
                                        trackEdits.add(
                                            AlbumTrackEditItem(
                                                song = item.song,
                                                trackNumber = idx + 1,
                                                discNumber = discNum
                                            )
                                        )
                                    }
                                }
                                onSave(
                                    albumTitle.trim(),
                                    albumArtist.trim(),
                                    artist.trim(),
                                    genre.trim(),
                                    replayGainAlbumGainDb.trim(),
                                    editedCoverArt,
                                    trackEdits
                                )
                            },
                            modifier = Modifier.height(48.dp)
                        ) {
                            Text(stringResource(R.string.common_save))
                        }
                    }
                )
            }
        }
    }
}
