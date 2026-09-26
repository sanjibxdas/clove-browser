# Clove Browser

Clove is a lightweight Android browser built with Kotlin, Jetpack Compose, and the Android WebView. The app aims to provide a clean, Safari-inspired browsing experience with a modern glassmorphism interface, tab management, private browsing, and a native start page.

## Highlights

- Tabbed browsing with normal and private modes
- Safari-style chrome and edge-to-edge layout
- Native start page with quick-access favorites
- Bookmarks, history, and reading list support
- Download support through Android's DownloadManager
- Search engine selection and browser settings
- Lightweight state management centered around a browser ViewModel

## Tech Stack

- Kotlin
- Android Jetpack
- Jetpack Compose
- Android WebView
- Material 3
- Gradle Kotlin DSL

## Project Structure

```text
clove-browser/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/clove/
│   │   │   │   ├── browser/        # Browser state, models, and navigation logic
│   │   │   │   ├── ui/             # Compose UI, composables, and screens
│   │   │   │   └── MainActivity.kt
│   │   │   ├── res/                # Android resources, launcher assets, and themes
│   │   │   └── AndroidManifest.xml
│   │   ├── test/                  # Unit tests
│   │   └── androidTest/           # Instrumentation tests
│   ├── build.gradle.kts
│   ├── proguard-rules.pro
│   └── .gitignore
├── gradle/
│   └── wrapper/
├── build.gradle.kts
├── gradle.properties
├── gradlew
├── gradlew.bat
├── settings.gradle.kts
└── .gitignore
