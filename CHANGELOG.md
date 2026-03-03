# Changelog

All notable changes to NovaPlayer will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.2.0] - 2026-02-17

### Glassmorphism UI Redesign & Advanced Audio Features

NovaPlayer 0.2.0 is a major update that completely redesigns the user interface with a modern glassmorphism aesthetic and introduces cutting-edge audio features that go beyond what most music players offer.

### Fixed

- **Compilation error in PlaybackViewModel** - Removed invalid `MediaController.player` access; `MediaController` in Media3 is itself a `Player` proxy and does not expose a `.player` property
- **Compilation error in EqualizerDialog** - Fixed `Short` vs `Int` type mismatch on `Equalizer.numberOfBands` for Kotlin 2.2 strict type checking
- **Equalizer not working** - Audio session ID is now properly exposed from `PlaybackService` via companion object, enabling all audio effects to bind to the correct ExoPlayer session
- **Playlists now persist** across app restarts (stored via SharedPreferences + Gson)
- **Settings now persist** across app restarts (all settings stored via SharedPreferences)
- **Album art / cover images** now display correctly for each track using MediaStore album art URIs
- **Deprecated icon warnings** - Migrated `PlaylistPlay`, `QueueMusic`, `VolumeUp`, `VolumeOff` to their `AutoMirrored` variants

### Added

#### Full Audio Effects Chain (new)
- **Bass Boost** (0–100%) - Low-frequency enhancement via Android's `BassBoost` API
- **Virtualizer / 3D Audio** (0–100%) - Spatial audio simulation via Android's `Virtualizer` API
- **Loudness Enhancer / Volume Boost** (0–100%) - Per-session volume normalization via `LoudnessEnhancer`
- **Parametric Equalizer** - Full frequency band control with per-band sliders
- **EQ Presets** - One-tap presets (Normal, Classical, Dance, Flat, Folk, Heavy Metal, Hip Hop, Jazz, Pop, Rock, etc.)
- **Audio Effects Dialog** - Comprehensive glassmorphic panel for all audio effects, replacing the basic equalizer
- **AudioEffectsManager** - New dedicated class managing the complete audio processing chain with graceful error handling

#### Playback Speed Control (new)
- **Variable speed playback** from 0.25x to 3.0x with pitch preservation
- **6 preset speed chips** (0.5x, 0.75x, 1.0x, 1.25x, 1.5x, 2.0x) for quick selection
- **Fine-grained slider** for precise speed adjustment in 0.05x increments
- **Speed badge indicator** on the Now Playing Bar when speed ≠ 1.0x
- Persists across app restarts

#### Skip Silence (new)
- **Auto-skip silent portions** of tracks - a feature borrowed from the podcast world, almost unheard of in music players
- Uses ExoPlayer's built-in `skipSilenceEnabled` for accurate silence detection
- Toggle in Settings with persistent state

#### Crossfade Between Tracks (new)
- **Smooth fade-out/fade-in** transitions between consecutive tracks
- **6 duration presets**: Off, 2s, 4s, 6s, 8s, 12s
- Coroutine-based position monitor in PlaybackService with 50ms polling for smooth volume ramping
- Automatic fade-in on media item transitions

#### Listening Statistics & Insights (new)
- **Play count per track** - Recorded on each track transition
- **Total listening time** - Tracked in 5-second increments while playing
- **Listening streak** - Consecutive days of listening with daily reset logic
- **Most played tracks** - Sorted by play count
- **Insights card** on the Library screen header showing total time listened, streak days, and top track
- All stats persisted via SharedPreferences + Gson serialization

#### Glassmorphism UI Redesign
- **New color palette** - Rich accent colors (NovaPurple, NovaCyan, NovaPink, NovaBlue) with dedicated glass colors (GlassWhite, GlassBorder, GlassHighlight)
- **Reusable glass modifiers** - `Modifier.glassCard()` and `Modifier.glassBackground()` for consistent glassmorphic styling
- **Gradient accent brush** - Purple-to-cyan horizontal gradient used throughout the app
- **Updated typography** - Proper font weights, sizes, and letter spacing for a polished look

#### Redesigned Components
- **Now Playing Bar** - Glass panel with semi-transparent gradient, rounded album art with accent border, gradient play/pause circle button, gradient-colored waveform animation, speed badge
- **Track List Screen** - Each track in a glass card, glassmorphic search bar, music note icon placeholders for missing art, track count in header, listening insights card
- **Playlists Screen** - Glass cards with gradient icon backgrounds, gradient circular add button, styled empty state, glassmorphic create/rename dialogs
- **Settings Screen** - Every setting in glass cards with leading icons, gradient toggle chips for repeat mode, speed selector with chips + slider, crossfade selector, gradient Audio Effects button, scrollable layout with uppercase section labels
- **Navigation Bar** - Custom glassmorphic bottom navigation replacing stock NavigationBar, subtle glow behind selected icon, animated alpha transitions
- **Top App Bar** - Fully transparent with dark scrolled state
- **Splash Screen** - Gradient logo container with radial glow, tagline "Your music, reimagined"
- **About Dialog** - Glass-styled info card, icon-labeled GitHub and Donate buttons with cyan/pink accent colors
- **Equalizer Dialog** - Replaced with comprehensive Audio Effects Dialog featuring glass-styled frequency band sliders, accent-colored labels, effect cards with per-effect accent colors

### Changed

- **SettingsScreen** now accepts additional callbacks for speed, skip silence, crossfade, bass boost, virtualizer, and loudness
- **TrackListScreen** now accepts listening statistics parameters (total time, streak, most played)
- **NowPlayingBar** now accepts `playbackSpeed` parameter for the speed badge
- **PlaybackService** now uses a coroutine-based settings observer for skip silence and crossfade instead of custom MediaSession commands
- **PlaybackViewModel** now manages `AudioEffectsManager`, track stats, listening time tracking, and all new settings
- **PersistenceHelper** now stores 6 additional settings fields, track stats map, listening streak, and total listening time
- **UserSettings** model expanded with: `playbackSpeed`, `skipSilence`, `crossfadeDurationMs`, `bassBoostStrength`, `virtualizerStrength`, `loudnessGain`

### New Files

- `model/TrackStats.kt` - Data model for per-track listening statistics
- `playback/AudioEffectsManager.kt` - Complete audio effects chain manager (Bass Boost, Virtualizer, Loudness Enhancer, Equalizer)

### Resolved from 0.1.0 Known Limitations

- ~~Playlists are stored in-memory only and will be lost when app is closed~~ → **Fixed**: Playlists persist via SharedPreferences
- ~~Settings are not persisted and reset to defaults on app restart~~ → **Fixed**: All settings persist via SharedPreferences
- ~~No album art/cover image display~~ → **Fixed**: Album art displayed via MediaStore album art URIs
- ~~No search or filter functionality~~ → **Fixed**: Search bar with title/artist/album filtering (added in 0.1.0 codebase, now with glassmorphic styling)
- ~~No equalizer or audio effects~~ → **Fixed**: Full audio FX chain (EQ + Bass Boost + Virtualizer + Loudness)
- ~~No shuffle or repeat modes~~ → **Fixed**: Both shuffle and repeat (Off/All/One) with persistent state (added in 0.1.0 codebase)
- ~~No sleep timer~~ → **Fixed**: Sleep timer with minute input (added in 0.1.0 codebase)

### Build Configuration

- Version Code: 2
- Version Name: 0.2.0

---

## [0.1.0] - 2026-02-16

### Initial Release

NovaPlayer 0.1.0 is the first public release of this modern Android music player built with Jetpack Compose and Media3.

### Added

#### Core Playback Features
- **Local music library scanning** using Android MediaStore API
- **Wide audio format support** via ExoPlayer/Media3 (MP3, AAC, FLAC, OGG, WAV, and more)
- **Background playback** with foreground media service
- **Media notification** with playback controls in notification shade
- **Playback state persistence** - automatically resumes last played track and position on app restart
- **Play/pause control** with toggle functionality
- **Skip next/previous track** navigation
- **Track queue management** with Media3 MediaController

#### User Interface
- **Modern dark theme** with neon-inspired design using Material 3
- **Splash screen** with NovaPlayer logo on app launch
- **Bottom navigation** with three main sections: Library, Playlists, and Settings
- **Now Playing Bar** at bottom of screen showing current track info and playback controls
- **Animated waveform visualization** (optional) on Now Playing Bar
- **Track list screen** displaying all music files sorted alphabetically by title
- **Custom app icon** with adaptive icon support for Android 8.0+

#### Playlist Management
- **Create playlists** with custom names
- **Rename playlists** with inline editing
- **Delete playlists** with single tap
- **Add tracks to playlists** (in-memory storage)
- **Remove tracks from playlists**
- **Play entire playlist** starting from any track
- **Track count display** showing number of tracks in each playlist

#### Settings & Customization
- **Dark theme toggle** (always enabled by default)
- **Gapless playback option** to reduce gaps between tracks
- **Animated waveform toggle** for Now Playing Bar visualization
- **Settings persistence** (note: settings are in-memory only in this version)

#### Permissions & Compatibility
- **Runtime permission handling** for storage/audio access
- **Android 13+ support** with READ_MEDIA_AUDIO permission
- **Legacy Android support** (API 26+) with READ_EXTERNAL_STORAGE fallback
- **Foreground service** with proper media playback service type

#### About & Information
- **About dialog** with app information, author details, and support links
- **Author**: Kamil "Novik" Nowicki
- **License**: GNU GPL v3
- **Support**: Ko-fi donation link (https://ko-fi.com/novadesktop)
- **Source code**: GitHub repository (https://github.com/novik133/NovaPlayer)

### Technical Details

#### Architecture
- **MVVM architecture** with ViewModel and StateFlow
- **Jetpack Compose** for declarative UI
- **Media3/ExoPlayer** for audio playback engine
- **MediaSession** integration for media controls
- **Coroutines** for asynchronous operations
- **Repository pattern** for data access layer

#### Dependencies
- AndroidX Core KTX 1.15.0
- Jetpack Compose BOM 2024.10.01
- Material 3 (1.3.0)
- Media3 ExoPlayer 1.4.1
- Media3 Session 1.4.1
- Navigation Compose 2.8.3
- Lifecycle ViewModel Compose 2.8.7
- Accompanist System UI Controller 0.36.0

#### Build Configuration
- Min SDK: 26 (Android 8.0)
- Target SDK: 35 (Android 15)
- Compile SDK: 35
- Version Code: 1
- Version Name: 0.1.0
- JDK: 17
- Android Gradle Plugin: 9.0.1
- Kotlin: 2.2.10

### Known Limitations

- Playlists are stored in-memory only and will be lost when app is closed
- Settings are not persisted and reset to defaults on app restart
- No album art/cover image display
- No search or filter functionality
- No equalizer or audio effects
- No shuffle or repeat modes
- No sleep timer
- No lyrics support
- No online streaming or radio support

### Requirements

- Android device running Android 8.0 (API 26) or higher
- Storage permission to access music files
- Music files stored on device storage

---

[0.2.0]: https://github.com/novik133/NovaPlayer/releases/tag/v0.2.0
[0.1.0]: https://github.com/novik133/NovaPlayer/releases/tag/v0.1.0
