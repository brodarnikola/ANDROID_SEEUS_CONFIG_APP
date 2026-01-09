# SeeUs Admin - Android Application

## Overview
This is a native Android application (SeeUs Admin Configurator) written in Kotlin. It's designed for configuring and managing SeeUs devices via Bluetooth Low Energy (BLE).

**Important:** This is a native Android app that cannot run directly in a web browser. It requires Android Studio and an Android device/emulator to build and execute.

## Project Structure

```
├── admin/          # Main Android application module
│   ├── src/        # Application source code
│   └── build.gradle
├── core/           # Core library module with shared functionality
│   ├── src/        # Library source code
│   └── build.gradle
├── build.gradle    # Root build configuration
├── settings.gradle # Project settings
└── server.js       # Info page for Replit (displays project info)
```

## Technology Stack
- **Platform:** Android SDK 33 (min SDK 26)
- **Language:** Kotlin 1.7.22
- **Build System:** Gradle 8.0.2
- **Architecture:** Multi-module Gradle project

## Key Dependencies
- Firebase Cloud Messaging
- Google Maps & Play Services
- BLE Scanner/Communicator libraries
- Kotlin Coroutines
- AndroidX libraries
- Retrofit for REST API

## Build Instructions

To build this project locally:

1. Clone the repository
2. Open in Android Studio (Arctic Fox or later)
3. Sync Gradle files
4. Build: `./gradlew assembleDebug`
5. Install: `./gradlew installDebug`

## Requirements
- Android Studio Arctic Fox or later
- JDK 17
- Android SDK 33
- Android device or emulator with SDK 26+
- Google Play Services on target device

## Replit Environment
Since this is an Android app, the Replit environment serves an informational web page on port 5000 explaining the project setup and build instructions.
