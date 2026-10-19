# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [0.8.13-beta] - 2026-10-19

### Changed & Refined
- **Smooth Arc Animation for Album Detail Heart:** Replaced discrete Column/Row layout switching with a continuous, gesture-driven curved trajectory. As the album header collapses upon scrolling down, the favorite heart button smoothly glides up and out around the pencil icon into its docked top bar position without sudden jumps.
- **Collapsed Header Text Clearance:** Increased collapsed header title and subtitle end padding from 108.dp to 132.dp when the favorites action is enabled, providing 32.dp of clean whitespace and preventing album title and duration text from touching the heart button.
- **Title Scale Anchor:** Fixed title scale transform origin to start (`TransformOrigin(0f, 0.5f)`), ensuring zero text drift during collapse transitions.

## [0.8.12-beta] - 2026-10-19

### Removed
- **Folders in Favorites:** Completely removed folder and subfolder functionality from the Favorites tab in favor of a clean, unified view.

### Added
- **Favorite Playlists:** Playlists can now be marked as favorites and viewed alongside favorite albums in the "Любимое" tab.
- **Filter Chips:** Added `[Все]` (`All`), `[Альбомы]` (`Albums`), and `[Плейлисты]` (`Playlists`) filter chips to quickly sort through favorited items.
- **Playlist Detail Favorite Action:** Added favorite bookmark toggle directly in Playlist Detail top app bar and context menu.

### Changed & Refined
- **Album Detail Heart Button Placement:** Moved the heart button directly beneath the pencil (edit) button on the right side, seamlessly integrated with the top bar header.
- **Native Button Styling:** Reverted the heart button back to the standard native `FilledIconButton` / `FilledTonalIconButton`, eliminating floating animations and ensuring immediate tactile responsiveness.
- **Library Tab Sync:** When the "Любимое" (Favorites) tab is disabled in library configuration settings, the favorite heart button is automatically hidden from album and playlist detail screens.


### Added
- **Circular "Create Folder" Action:** Added a circular green tonal icon button directly beside the Shuffle button in the library header bar when browsing favorite albums.
- **Inside-Folder Header Banner:** Modern expressive folder banner with breadcrumb navigation, dynamic collage artwork, Play All, Shuffle, and Create Subfolder actions.
- **Flight & Tactile Favorite Heart Button:** Completely redesigned the favorite album toggle in Album Detail screen with bouncy spring scale animation, tactile haptic feedback (`HapticFeedbackType.LongPress`), and a flight arc animation that smoothly glides into the top bar beside the back button when scrolling/collapsing.

### Changed & Redesigned
- **Favorite Albums Tab Clean-up:** Completely removed redundant top button rows and obsolete "Add albums to folder" bottom sheet for a clean, cohesive Spotify-like library interface.
- **Native Folder Grid & List Styling:** Upgraded folder cards to match PixelPlayer's native album designs (`AlbumGridItemRedesigned` with 20.dp squircles and `AlbumListItem` with 88.dp height and dithered scrims).
- **Expressive Action & Move Sheets:** Redesigned album and folder context sheets to match PixelPlayer's Material 3 Expressive bottom sheets with squircle artwork preview, Google Sans Rounded typography, pill action items, and quick folder creation.
- **Album Detail Text Clearance:** Relocated the heart button away from the top-right to the side when expanded and beside the back button when collapsed, permanently resolving subtitle/duration text truncation (`... 56 минут`) and cover overlaps.

## [0.8.10-beta] - 2026-10-19

### Added
- **Favorite Albums Tab (Любимое):** Added a dedicated "Любимое" (Favorites) tab to the Library specifically for albums, allowing users to bookmark and collect their favorite albums in one central place.
- **Spotify-style Nested Folders:** Organize favorite albums into custom folders and subfolders with 2x2 artwork collages, breadcrumb navigation, and folder playback/shuffle actions.
- **Album Detail Heart Toggle:** Integrated an animated favorite album heart button directly into the Album Detail screen top bars (both collapsing and shared headers).
- **Material 3 Expressive Design:** Consistent styling conforming to the app's design language, supporting seamless toggling between Grid and List layouts, folder options bottom sheet, and expressive folder cards.
- **Room Migration 42->43:** Added `favorite_albums` and `favorite_album_folders` tables with index optimizations and foreign key relationships.

### Fixed
- **Pre-existing Test Suite Hardening:** Fixed edge cases and assertions in `MusicRepositoryImplTest`, `LoadControlBufferProfileTest`, `LibraryScreenFolderNavigationAnimationTest`, `QueueStateHolderTest`, `LocalArtworkUri`, `BackupSectionTest`, and `LyricsStateHolderTest`. Full test suite achieves 100% pass rate.

## [0.8.9-beta] - 2026-10-19

### Fixed
- **Single Disc End-of-Disc Auto Stop:** Corrected auto-advance and crossfade transitions in `DualPlayerEngine` and `TransitionController` so that when the last track of a disc ends naturally, playback stops immediately (or loops back to track 1 when repeat mode is ALL) instead of advancing into the next disc.
- **Album Cover Disappearing Bug:** Fixed regression in `MediaControllerSyncStateHolder.normalizeArtworkForResolvedSong` where null artwork in media item metadata erroneously erased existing song album art throughout the playback queue and carousel.
- **Carousel Large Jump Glitch:** Added smooth intermediate snapping in `AlbumCarouselSelection` for non-adjacent skips (such as looping back to track 1 from the end of a disc), eliminating multi-page reverse spin and visual layout jitter.
- **Disc Number Extra Fallback:** Hardened disc number extraction across `PlaybackStateHolder`, `MusicService`, and `DualPlayerEngine` to check both standard media metadata and extras bundles.

## [0.8.8-beta] - 2026-10-19

### Fixed
- **Library Infinite Pager Crash:** Fixed `IllegalArgumentException: Key "SINGLES" was already used` crash when opening library in compact navigation mode by restoring unique page index keys in `HorizontalPager`.

## [0.8.7-beta] - 2026-10-19

### Performance & Smoothness
- **Navigation Transition Optimization:** Shifted transition blur and layer transforms in `ScreenWrapper` to execute purely in the GPU draw phase using `RenderEffect`, eliminating full-screen recompositions and frame drops across navigation gestures.
- **Synced Lyrics Recomposition Fix:** Isolated playback position updates in `SyncedLyricsList` so only the active line updates instead of triggering full list recompositions every 100ms.
- **Scrim Gradient GC Reduction:** Replaced repetitive dynamic allocations in `SmoothScrim` with precomputed static float arrays, dramatically reducing memory churn and micro-stutters during list and grid scrolling.
- **Star FAB & State Allocations:** Cached `RoundedStarShape` and simplified state derivations in artist and album detail screens to prevent unnecessary object allocations.
- **Library HorizontalPager Stability:** Switched to semantic tab keys in `HorizontalPager` to reliably maintain page state and avoid item loss or reload glitches during tab navigation.

### Fixed
- **DataStore Emission Storm:** Added `distinctUntilChanged` to base preference flows, preventing single-preference mutations from flooding the entire StateFlow pipeline.
- **Single Disc Boundary & Carousel Prediction:** Corrected disc-boundary checks to strictly verify album identity and return to the first track of the disc. Synchronized optimistic carousel skip prediction with single disc mode to prevent navigation jitter.
- **Hidden Songs in Random Shuffles:** Ensured `shuffleRandomAlbum` and `shuffleRandomSingle` properly respect hidden track filters.
- **Native Resource Cleanup:** Wrapped native `MediaExtractor` usage in `try-finally` blocks within `AudioMetaUtils` to prevent file descriptor leaks.
- **Code Cleanup:** Removed duplicated `BottomToggleRow` implementations and redundant lifecycle listeners.

## [0.8.6-beta] - 2026-10-18

### Added
- **Single Disc Playback Mode:** Added toggle in player controls to restrict playback to the currently playing disc of multi-disc releases, with automatic playback pause upon disc completion.
- **Single Disc Toggle Setting:** Added switch in Settings -> Playback controls to enable or hide the single disc mode button.
- **Track Hiding in Album View:** Added ability to hide individual tracks from an album view via swipe actions or context menu, with persistence in user preferences.
- **Track Unhiding & Restoration:** Added context action to restore/unhide previously hidden tracks directly within the album screen.
- **Disc & Track Metadata Extras:** Enriched media items with `discNumber` and `albumId` metadata for improved disc-boundary tracking across player components.

### Changed
- **Updated Translations:** Added Russian, Spanish, and English string resources for single-disc mode and track hiding actions.

## [0.8.5-beta] - 2026-10-15

### Added
- **Dedicated Singles Library Tab:** Added a new "Singles" tab that isolates short releases (configurable thresholds: default up to 3 tracks and 10 minutes duration).
- **Smart Singles / Albums Exclusion:** Automatically excludes releases that meet the singles criteria from the "Albums" tab (toggleable in Settings -> Filtering).
- **Library Tab Visibility Controls:** Added individual toggle switches in the "Reorder tabs" sheet to show or hide any library tab while ensuring at least one active tab remains.
- **Compact Song Action Menu (Icons-Only):** Added a new toggle in Settings -> Song Menu to hide action labels and render an ultra-compact icon grid.
- **Hide Albums from Library:** Added "Hide from albums" action to the album multi-selection sheet, with a hidden album restore manager in Settings -> Filtering.

### Changed
- **Russian & Multi-language Text Wrapping:** Fixed cramped and overflowing Russian strings in bottom sheets by streamlining action labels and preventing ugly line breaks.
- **Removed Ringtone Button:** Completely removed the obsolete "Set as sound" button from the song info sheet to declutter actions and expand the Pixel Watch transfer button to full width.
- **Unified Multi-Selection:** Unified back handling, multi-selection, and sort options across Albums and Singles tabs.

### Added
- **Dynamic Header Action Buttons Layout:** Added configurable button positioning in Experimental Settings (`SPLIT`, `ALL_LEFT`, `ALL_RIGHT`).
- **Dynamic Artwork Alignment & Scaling:** When header action buttons are grouped on one side (left or right), the square album/artist cover dynamically shifts to the opposite side and increases in size up to 270dp for a bold, expressive look without layout collisions.
- **Atmospheric Vibrant Gradient Background:** Added a rich, atmospheric top gradient behind square cover artwork derived from the album/artist color palette, flowing seamlessly into the title section.

### Changed
- **Streamlined Header Controls:** Removed redundant three-dots menu buttons from album and artist detail headers in favor of a clean, direct edit pencil action.
- **Adaptive Collapsed Title Padding:** Collapsed top bar titles dynamically compute start/end insets to prevent text clipping regardless of button alignment mode.

## [0.8.0-beta] - 2026-10-14

### Added
- **Flat & Shadowless UI:** Completely removed elevation and artificial drop shadows across bottom sheets, dialogs, track cards, menus, and settings.
- **Cover Art Style Switcher:** Added "Cover art style" in Settings -> Appearance allowing users to choose between classic Banner and uncropped 1:1 Spotify-style Square art in both library grid and album/artist detail headers.
- **Swipe-to-Queue:** Added bidirectional Spotify-style swipe gestures in Album and Playlist views with dynamic album palette accenting.
- **Album Metadata Editor:** Added `EditAlbumSheet` for batch metadata editing (Album title, Album Artist, Artist, Genre, ReplayGain, and artwork).
- **Disc Timing:** Added total album playback duration and per-disc duration calculation.

### Changed
- **Gradients & Dithering:** Replaced banded linear gradients with smooth $C^2$ quintic smootherstep scrims and hardware GPU dithering.
- **Detail Header Layout:** Proportional 380dp square header with 215dp cover size ensuring zero overlap with two-line album/artist titles.
- **Settings Reactivity:** Converted preference flows to eager sharing (`SharingStarted.Eagerly`) for instant UI synchronization.

## [0.7.5-beta] - 2026-06-13

### Added
- **Google Drive:** Added Google Drive support and improved player lifecycle management.
- **AI Lyrics:** Integrated AI lyrics translation logic in `LyricsStateHolder` and user preferences.
- **Gemma:** Deleted old Gemini model IDs and integrated Gemma model support.
- **Wear OS:** Added wear lyrics translation/romanization preferences and album art background.
- **Diagnostics:** Added a lag diagnostic tool.
- **Search:** Added multi-selection support to the Search screen.
- **UI:** Added outlined button style for AOD screen.
- **Connectivity:** Added support for HTTP URLs on local-network Navidrome and Jellyfin hosts.
- **Localization:** Added Arabic and Turkish language support, and unrecognized languages.

### Changed
- **Battery Optimization:** Drastically reduced battery consumption via audio offload and adaptive UI polling.
- **Queue System:** Refactored shuffle, queue reordering, and playback orchestration to `QueueStateHolder` using explicit queue indices.
- **Transitions & Animations:** Implemented Material 3 Expressive motion curves for player, queue sheet, and screen transitions.
- **Architecture:** Decomposed `MusicService` and modularized `PlayerViewModel` state listeners.
- **Library Sync:** Optimized library sync with throttled scans and faster artwork loading.
- **Database:** Migrated database to version 42 and updated Navidrome schema.
- **Equalizer:** Added "Save New" action and improved layout.
- **Localization:** Refactored app localization, resource cleanup, and UI text wrapping.
- **Dependencies:** Bumped dependencies including `kotlinx-collections-immutable`, `okhttp`, and Gradle plugins.

### Fixed
- **Playback:** Resolved buffering issues, song skipping lags, and unnecessary recompositions during playback.
- **Media Store Sync:** Improved MediaStore URI resolution, external song deletion, and Android 11+ storage volume resolution.
- **Lyrics & Metadata:** Fixed Chinese lyrics detection, pinyin tone suffixes, and batch metadata/artwork editing consistency.
- **Wear OS:** Resolved memory issues and state persistence.
- **UI:** Fixed marquee text fade glitches, navigation bar corner behavior, blur issues, scrollbar bugs, and layout/padding insets.
- **Other:** Fixed backup playlist update issues and startup AI provider errors.

### New Contributors
- @YtMechnij made their first contribution in https://github.com/theovilardo/PixelPlayer/pull/2106
- @juinc made their first contribution in https://github.com/theovilardo/PixelPlayer/pull/2109
- @ZL114514 made their first contribution in https://github.com/theovilardo/PixelPlayer/pull/2159
- @aliabbasov99 made their first contribution in https://github.com/theovilardo/PixelPlayer/pull/2262
- @Hisham-Alzamzami made their first contribution in https://github.com/theovilardo/PixelPlayer/pull/2335


## [0.7.0-beta] - 2026-05-25

### Added
- **Wear OS:** Music transfer, local playback, queue synchronization, and remote control from the watch.
- **AI:** Groq AI and OpenRouter (experimental) with token optimization and AI-powered playlist generation.
- **Cloud & Streaming:** Jellyfin support.
- Direct song synchronization from server albums in Navidrome.
- Standardized branding for NetEase Music.
- **Lyrics:** Synchronized translation with a dedicated toggle and Kugou LRC format support.
- Text alignment customization and improvements to TTML parsing.
- Advanced romanization for Japanese characters.
- **UI/UX:** Redesigned queue sheet and "Recently Played" pills with a dynamic palette.
- Marquee support for long titles and a compact mode for the navigation bar.
- New horizontal timeline for monthly statistics and multi-artist support.
- **Telegram:** Native support for topics, playlist display, and reactive updates.

### Changed
- **Audio Engine:** Complete overhaul with support for MIDI, improvements to ALAC/M4A/Opus, and decoder optimization (including Samsung-specific decoders).
- **Energy Efficiency:** Drastically reduced battery consumption and thermal optimization through UI task gates.
- **Database and Cache:** Massive optimizations to queries, cover art cache controller v3, and support for Scoped Storage.
- **Startup:** Improved load times through optimized generation of Baseline Profiles.
- Project license changed from MIT to Proprietary License.

### Fixed
- **Playback:** Fixed stuttering in Opus/MP3, errors in ReplayGain during crossfades, and flickering during album art changes.
- **Navigation:** Fixed navigation loops in Telegram and improved screen entry/exit animations.
- **Stability:** Eliminated crashes on Android 12+, fixed memory leaks (ANRs), and improved exception handling in background services.
- **Security:** CI hardening, encryption of cloud storage credentials, and media server access control.

### Localization
- 🇪🇸 **Spanish** | 🇫🇷 **French** | 🇷🇺 **Russian**
- 🇨🇳 **Simplified Chinese** | 🇮🇩 **Indonesian** | 🇮🇹 **Italian** | 🇩🇪 **German**

## [0.6.0-beta] - 2026-03-05

### Added
- Added Android Auto support through Media3 `MediaLibraryService`.
- Added Wear OS companion support, including watch transfer and playback controls.
- Added cloud provider expansions: Telegram playlist management, NetEase sync improvements, QQ Music integration, Subsonic/Navidrome, and Google Drive streaming (WIP).
- Added a modernized backup/restore system (v3), account management, and persistent queue restoration.
- Added smarter lyrics workflows (manual fallback search + storage refactor), Recently Played, and new multi-selection flows (songs/albums/playlists).
- Added home and UI customization features: collage patterns, quick settings tiles, expressive scrollbar refinements, and new widget styles.

### Changed
- Reworked player architecture and interaction model (unified player sheet refactors, predictive back handling, gesture tuning).
- Redesigned key surfaces including Lyrics, Cast, Artist, Genre, and Daily Mix experiences.
- Refined library/search/navigation behavior with safer navigation APIs and better state restoration.
- Improved audio compatibility and metadata handling (JAudioTagger fallback, URI handling, surround/noisy behavior).
- Expanded integration UX across Telegram/NetEase/QQ login and sync flows.

### Fixed
- Fixed multiple queue/shuffle edge cases (anchored shuffle, start-at-zero shuffle, queue synchronization).
- Fixed playback interruption behavior when headphones disconnect and resolved foreground service start restrictions.
- Fixed Cast-related crash cases and improved cast reliability.
- Fixed Sleep Timer UI issues, files tab navigation, album artist crash, and state-sync regressions in settings/reorder flows.
- Fixed release build stability (`R8`) and numerous UI polish issues across bottom sheets and controls.

### Performance
- Reduced recompositions and state overhead across Player, Library, Queue, and detail screens.
- Improved startup behavior (eliminated blank flash and deferred heavy Telegram native loading off main thread).
- Optimized folder/genre/artist loading, bottom navigation responsiveness, and gesture fluidity.
- Reduced CPU/main-thread pressure and improved service/widget runtime efficiency.
- Reduced APK size using ABI splits, downloadable fonts, and SDK cleanup.

### New Contributors
- @ThatOneCalculator
- @ryan7zoom
- @LarveyOfficial
- @Dv1101
- @Sincere-Bhattarai

## [0.5.0-beta] - 2026-01-14

### Added
- Implemented 10-band Equalizer and effects suite (feat: @theovilardo)
- Added M3U playlist import/export support (feat/fix: @lostf1sh, @theovilardo)
- Integrated Deezer API for artist images (feat: @lostf1sh)
- Added Gemini AI model selection, system prompt settings, and AI playlist entry point (feat: @lostf1sh, @theovilardo)
- Added sync offset support for lyrics and multi-strategy remote search (feat/fix: @lostf1sh, @theovilardo)
- Added Baseline Profiles for improved performance (feat/fix: @theovilardo, @google-labs-julesbot)
- Added support for custom playlist covers

### Changed
- **Material 3 Expressive UI**: Modernized Settings, Stats, Player, Bottom Sheets, and dialogs (refactor: @theovilardo, @lostf1sh)
- **Library Sync**: Rebuilt initial sync flow with phase-based progress reporting and linear indicators (feat: @lostf1sh)
- **Settings Architecture**: Introduced category sub-screens and improved navigation handling (refactor/fix: @theovilardo)
- **Queue & Player**: Decoupled queue updates from scroll animations, added animated queue scrolling (feat/fix: @lostf1sh, @theovilardo)
- Improved widget previews and case-insensitive sorting logic (feat/fix: @lostf1sh, @google-labs-julesbot)

### Fixed
- Fixed casting stability, queue transitions, and reduced latency (fix: @theovilardo)
- Fixed delayed content rendering and unwanted collapses in Player Sheet (fix/refactor: @theovilardo)
- Fixed reordering issues in queue
- General crash fixes and minor UX improvements (fix: @lostf1sh, @theovilardo)

## [0.4.0-beta] - 2025-12-15

### Added
- Major navigation redesign
- New file explorer for choosing source directories
- Landscape mode (thanks to "leave this blank for now")
- New Connectivity and casting functionalities
- Seamless continuity between remote devices
- Gapless transition between songs
- Crossfade
- New Custom Transitions feature (only for playlists)
- Keep playing after closed the app
- UI Optimizations
- Improved stats feature
- Redesigned Queue control with more features
- Improved different filetypes support for playing and metadata editing
- Improved permission controller
- Minor bug fixes

## [0.3.0-beta] - 2025-10-28

### What's new
- Introduced a richer listening stats hub with deeper insights into your sessions.
- Launched a floating quick player to instantly open and preview local files.
- Added a folders tab with a tree-style navigator and playlist-ready view.

### Improvements
- Refined the overall Material 3 UI for a cleaner and more cohesive experience.
- Smoothed out animations and transitions across the app for more fluid navigation.
- Enhanced the artist screen layout with richer details and polish.
- Upgraded DailyMix and YourMix generation with smarter, more diverse selections.
- Strengthened the AI assistant to deliver more relevant playback suggestions.
- Improved search relevance and presentation for faster discovery.
- Expanded support for a broader range of audio file formats.

### Fixes
- Resolved metadata quirks so song details stay accurate everywhere.
- Restored notification shortcuts so they reliably jump back into playback.

## [0.2.0-beta] - 2024-09-15

### Added
- Chromecast support for casting audio from your device (temporarily disabled).
- In-app changelog to keep you updated on the latest features.
- Improved lyrics search
- Support for .LRC files, both embedded and external.
- Offline lyrics support.
- Synchronized lyrics (synced with the song).
- New screen to view the full queue.
- Reorder and remove songs from the queue.
- Mini-player gestures (swipe down to close).
- Added more material animations.
- New settings to customize the look and feel.
- New settings to clear the cache.

### Changed
- Complete redesign of the user interface.
- Complete redesign of the player.
- Performance improvements in the library.
- Improved application startup speed.
- The AI now provides better results.

### Fixed
- Fixed various bugs in the tag editor.
- Fixed a bug where the playback notification was not clearing.
- Fixed several bugs that caused the app to crash.

## [0.1.0-beta] - 2024-08-30

### Added
- Initial beta release of PixelPlayer Music Player.
- Local music scanning and playback (MP3, FLAC, AAC).
- Background playback using a foreground service and Media3.
- Modern UI with Jetpack Compose, Material 3, and Dynamic Color support.
- Music library organization by songs, albums, and artists.
- Home screen widget for music control.
- Real-time audio waveform visualization.
- Built-in tag editor for song metadata.
- AI-powered features using Gemini.
- Smooth in-app permission handling.
