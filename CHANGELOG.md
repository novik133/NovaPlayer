# Changelog

All notable changes to NovaPlayer will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

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

[0.1.0]: https://github.com/novik133/NovaPlayer/releases/tag/v0.1.0
