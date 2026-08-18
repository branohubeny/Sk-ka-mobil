# Android Download App

A modern Android 16 (API 35) download manager application built with Kotlin and Material Design 3.

## Features

- ✅ Download files from URLs
- ✅ Progress tracking with visual feedback
- ✅ Material Design UI
- ✅ Coroutine-based async operations
- ✅ OkHttp3 for robust HTTP handling
- ✅ Proper permission handling for Android 13+
- ✅ Error handling and user feedback

## Requirements

- Android SDK 35 (Android 16)
- Kotlin 1.9+
- Gradle 8.0+
- Java 17+

## Permissions

- `INTERNET` - For downloading files
- `READ_EXTERNAL_STORAGE` / `READ_MEDIA_*` - For accessing storage
- `WRITE_EXTERNAL_STORAGE` - For saving downloaded files
- `ACCESS_NETWORK_STATE` - For checking network status

## Project Structure

```
src/main/
├── java/com/example/downloadapp/
│   ├── MainActivity.kt          # Main UI activity
│   └── service/
│       └── DownloadManager.kt   # Download logic
├── res/
│   ├── layout/
│   │   └── activity_main.xml    # Main UI layout
│   ├── drawable/
│   │   ├── edit_text_background.xml
│   │   └── status_background.xml
│   └── values/
│       ├── strings.xml
│       ├── colors.xml
│       └── themes.xml
└── AndroidManifest.xml
```

## Building and Running

1. Open the project in Android Studio
2. Sync Gradle files
3. Build the project: `./gradlew build`
4. Run on emulator or device: `./gradlew installDebug`

## Usage

1. Enter a valid file URL in the text input
2. Tap the "Download" button
3. Monitor progress with the progress bar
4. Check status messages for completion or errors

## Dependencies

- AndroidX Core, AppCompat, ConstraintLayout
- Material Design Components
- Kotlin Coroutines
- Retrofit 2 (for future API integration)
- OkHttp 3 (HTTP client)

## License

This project is released under the Unlicense.
