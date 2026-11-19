<div align="center">

<br/>

<picture>
  <source media="(prefers-color-scheme: dark)" srcset="art/chrona-horizontal.svg">
  <source media="(prefers-color-scheme: light)" srcset="art/chrona-horizontal-light.svg">
  <img alt="Chrona" src="art/chrona-horizontal.svg" width="380">
</picture>

<br/><br/>

### Precision World Clock & Cross-Timezone Alarm Engine

[![Platform](https://img.shields.io/badge/Platform-Android%2012%2B%20(API%2031--37)-3DDC84?style=flat-square&logo=android&logoColor=white)](https://developer.android.com)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20%26%20Material%203-4285F4?style=flat-square&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin%202.0-7F52FF?style=flat-square&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Architecture](https://img.shields.io/badge/Architecture-Clean%20%2F%20Unidirectional%20Data%20Flow-00B0FF?style=flat-square)](https://developer.android.com/topic/architecture)
[![License](https://img.shields.io/badge/License-Apache%202.0-6C757D?style=flat-square)](LICENSE)

<br/>

**Chrona** is a refined, production-grade Android clock application built with Jetpack Compose and Material 3. Inspired by the calm simplicity and visual precision of Google Pixel Clock, Chrona introduces true **cross-timezone alarm scheduling** — allowing users to schedule alarms based on target local times in global cities with automated daylight-saving transition resolution.

</div>

---

## ✦ Key Features

### 🌐 Cross-Timezone Alarms *(Signature Feature)*
* **Target-Timezone Scheduling:** Schedule an alarm for 09:00 in Tokyo while physically situated in Kathmandu or London without manual mental math.
* **DST Transition Engine:** Resolves daylight-saving gaps and overlaps dynamically using real-time IANA `ZoneRules` and `ZonedDateTime`.
* **Instant Conversion Display:** Real-time dual-card previews showing destination time, local device ring time, and day-offset indicators (`Tomorrow`, `Yesterday`, `+1d`).
* **Radial M3 Dial:** Authentic dual-ring 24-hour / single-ring 12-hour tactile radial dial for rapid, precise time selection.

### 🗺️ Natural Earth World Map
* **True Cartographic Projection:** Rendered using Natural Earth geometry mathematically projected into a responsive 1200×600 coordinate space.
* **Theme-Aware Continents:**
  * **AMOLED Dark:** Deep obsidian oceans (`#0B1320`) with cool slate landmasses (`#1E293B`).
  * **Material Light:** Soft polar blue water (`#EFF3F8`) with neutral slate continents (`#C8D3E3`).
* **Interactive City Tooltips:** Tappable pins with instant UTC offset badges, localized current times, and quick-alarm creation.

### 🔔 Bulletproof Alarm Execution
* **Single-Owner Audio Service:** `AlarmAudioService` runs as a high-priority foreground service (`mediaPlayback`) with continuous, looping `MediaPlayer` audio and a silent notification channel (`setSound(null, null)`), preventing OS notification audio collisions.
* **Smooth Volume Ramp:** Gradual acoustic volume ramp over 30 seconds for gentle waking.
* **Dedicated Lock-Screen Activity:** `AlarmRingingActivity` wakes the display (`turnScreenOn`, `showWhenLocked`), keeps the screen awake (`FLAG_KEEP_SCREEN_ON`), and dismisses cleanly back to the system lock screen without leaking user data.

### ⏱️ Monotonic Timers & Chronograph Stopwatch
* **Stopwatch:** 60fps chronograph dial with 60 precision tick markers, sweep hand, lap tracking, and split-differential calculations.
* **Multi-Timer Manager:** Keypad and preset inputs (+1m, +5m, +10m, +15m) with simultaneous running timers, circular progress indicators, and +1:00 extensions.
* **State Preservation:** Monotonic clock baselines (`SystemClock.elapsedRealtime()`) ensure timing continuity across device rotation, process suspension, and app restarts.

### 🌙 Bedtime & Sleep Regularity
* **Circadian Routine:** Bedtime schedule with planned sleep duration targets and custom active-day toggles.
* **Gentle Reminders:** Non-disruptive notifications prior to scheduled wind-down.

### 🎨 Pixel Material 3 & AMOLED Black
* **Pixel Aesthetic:** Tabular numeric typography (`tnum`), quiet fluid navigation bubble indicators, and rounded container cards (18–24dp).
* **Palette Options:** Pure AMOLED Black, Material Light, and System Default, paired with Periwinkle, Mint Teal, Soft Coral, Lavender, Soft Gold, and Pale Blue accents.
* **Adaptive App Icon:** The *Meridian Offset* symbol — two interlocking day/night crescents stepped across a meridian gap — fully supported with dynamic Monet themed icon colouring on Android 13+.

---

## 🏗️ Architecture & Tech Stack

```
com.example.chrona/
├── core/
│   ├── alarm/          # AlarmScheduler, AlarmReceiver, AlarmAudioService, BootReceiver
│   ├── data/           # AlarmRepository, CityRepository, BedtimeRepository, PreferencesRepository
│   ├── database/       # ChronaDatabaseHelper, AlarmDao, SavedCityDao, TimerDao (SQLite)
│   ├── map/            # WorldMapComposable, WorldMapProjection, MapMarker
│   ├── model/          # Alarm, City, TimerItem, BedtimeSchedule, UserPreferences
│   ├── timezone/       # TimezoneEngine (IANA ZoneRules, DST gap/overlap calculations)
│   └── ui/             # ChronaTopBar, ChronaBottomBar, TimezoneCard, components
├── feature/
│   ├── alarms/         # AlarmsScreen, CreateAlarmSheet, ChronaRadialTimePicker, AlarmRingingActivity
│   ├── bedtime/        # BedtimeScreen
│   ├── onboarding/     # OnboardingDialog, PermissionsSheet
│   ├── screensaver/    # ScreensaverScreen (Minimal, Typography, World Clock modes)
│   ├── settings/       # SettingsScreen (Theme, Accents, Permissions, Preferences)
│   ├── stopwatch/      # StopwatchScreen, StopwatchManager
│   ├── timer/          # TimerScreen, TimerManager
│   └── worldclock/     # WorldClockScreen, CityDetailSheet, TimezoneSearchSheet
├── navigation/         # ChronaNavHost, Screen definitions
└── ui/theme/           # Color, Theme, Shape, Type, ChronaThemeTokens
```

* **UI Toolkit:** Jetpack Compose (100% declarative UI) with Material 3.
* **State Management:** Unidirectional Data Flow (UDF) powered by Kotlin `StateFlow` and Compose `collectAsStateWithLifecycle`.
* **Concurrency:** Kotlin Coroutines (`Dispatchers.IO`, `Dispatchers.Main`) with structured lifecycle scoping.
* **Persistence:** Robust local SQLite persistence for alarms, saved cities, and active timers, combined with Jetpack DataStore Preferences for user configuration.
* **Time API:** Java Time (`java.time.*`) exclusively — `ZoneId`, `ZonedDateTime`, `ZoneRules`, `Instant`.

---

## 🚀 Getting Started

### Prerequisites
* **Android Studio:** Ladybug (2024.2) or newer.
* **JDK:** Java 11 or Java 17.
* **Android SDK:** Compile SDK 37, Min SDK 31 (Android 12+).

### Building from Source

```bash
# Clone the repository
git clone https://github.com/your-username/chrona.git
cd chrona

# Build debug APK
./gradlew assembleDebug

# Run unit tests (Projection math, Timezone engine, Stopwatch logic)
./gradlew testDebugUnitTest
```

### Installing on Device / Emulator

```bash
# Install to connected device via ADB
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Launch MainActivity
adb shell am start -n com.example.chrona/.MainActivity
```

---

## 🔒 Permissions & Security

Chrona requests only the permissions necessary for reliable timekeeping:

| Permission | Purpose |
|:---|:---|
| `android.permission.SCHEDULE_EXACT_ALARM` | Guarantees alarms ring at the precise millisecond requested. |
| `android.permission.USE_EXACT_ALARM` | Ensures exact alarm execution under battery optimization and Doze mode. |
| `android.permission.POST_NOTIFICATIONS` | Displays ongoing timer notifications and persistent alarm alerts. |
| `android.permission.USE_FULL_SCREEN_INTENT` | Allows the ringing alarm screen to wake and display over the locked device. |
| `android.permission.FOREGROUND_SERVICE` | Manages reliable, continuous looping alarm playback via `AlarmAudioService`. |
| `android.permission.WAKE_LOCK` | Keeps the CPU active during active alarm ringing and gradual volume ramping. |
| `android.permission.VIBRATE` | Provides haptic feedback during alarms, timers, and button interactions. |
| `android.permission.RECEIVE_BOOT_COMPLETED` | Reschedules all active alarms and bedtime schedules after device restart. |

---

## 📄 License

```text
Copyright 2026 Chrona Contributors

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0
```
