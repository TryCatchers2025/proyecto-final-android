# Hotel Pere Maria - Android App

Android application for hotel management built with Kotlin and Jetpack Compose.

## Building the Project

### Prerequisites
- Android Studio Arctic Fox or later
- JDK 17 or later
- Android SDK with API level 34

### Clean Build

If you encounter build errors related to cached artifacts (e.g., Hilt/kapt errors), perform a clean build:

```bash
# Use the provided clean script
./clean-build.sh

# Or manually:
./gradlew clean
rm -rf .gradle/
rm -rf app/build/
rm -rf build/

# Rebuild
./gradlew assembleDebug
```

### CI/CD Builds

For CI/CD environments, ensure a clean build by:
- Using `./gradlew clean build` or running the `clean-build.sh` script before building
- Not caching `.gradle/` or `build/` directories between builds if experiencing issues
- Using Gradle's `--no-build-cache` flag if persistent caching issues occur

### Common Issues

#### Hilt/kapt Metadata Version Error
If you see an error like:
```
error: [Hilt] Unsupported metadata version. Check that your Kotlin version is >= 1.0
```

This is caused by cached build artifacts. The project no longer uses Hilt or kapt. To fix:

1. Clean the project: `./gradlew clean`
2. Remove cached Gradle files: `rm -rf .gradle/ app/build/`
3. In Android Studio: File → Invalidate Caches → Clear file system cache and Local History → Invalidate and Restart
4. Rebuild the project

## Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose with Material 3
- **Architecture**: MVVM
- **Navigation**: Jetpack Navigation Compose
- **Networking**: Retrofit with Moshi
- **Image Loading**: Coil
- **Minimum SDK**: 24 (Android 7.0)
- **Target SDK**: 34 (Android 14)

## Project Structure

```
app/src/main/java/com/trycatchers/hotel/
├── compose/              # Compose UI components and screens
│   ├── screens/         # Screen composables
│   └── components/      # Reusable UI components
├── data/                # Data layer
│   ├── models/         # Data models
│   └── repositories/   # Data repositories
├── viewmodels/         # ViewModels
└── ui/theme/           # Theme configuration
```

## License

Copyright © 2026 TryCatchers
