## NovaPlayer 0.1.0

NovaPlayer is a modern Android music player built with Jetpack Compose and Media3.

### Features

- **Local library**: Scans the device for music files using `MediaStore`.
- **Wide format support**: Uses ExoPlayer/Media3, which supports most popular audio formats (MP3, AAC, FLAC, OGG, WAV, etc.).
- **Background playback**: Foreground media service with notification channel.
- **Modern UI**: Dark, neon-inspired design with a bottom now-playing bar.

### Requirements

- **Android Studio Ladybug or newer**
- **Android Gradle Plugin 8.6+**
- **JDK 17**
- **Min SDK 26**, target SDK 35

### How to run

1. **Open the project**  
   Open the `NovaPlayer` folder in Android Studio.

2. **Sync Gradle**  
   Let Android Studio download dependencies and sync the Gradle project.

3. **Run on device/emulator**  
   - Use a real device (recommended) with some music files on storage.  
   - Click “Run” and choose the `app` configuration.

4. **Grant permissions**  
   On first launch, grant storage/audio permission so NovaPlayer can read your music.

### App icon

The app uses a custom vector icon in `app/src/main/res/drawable/ic_nova_player.xml` and adaptive icons in `mipmap-anydpi-v26/`. A standalone SVG source is available in `design/nova_player_icon.svg`.

### About & license

- **Author**: Kamil "Novik" Nowicki  
- **Copyright**: © 2026  
- **License**: GNU GPL v3  
- **Source**: https://github.com/novik133/NovaPlayer  
- **Support**: If you like NovaPlayer, you can support development via Ko-fi: https://ko-fi.com/novadesktop

