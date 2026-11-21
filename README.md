# Chrona

<p align="center">
  <img src="art/chrona-horizontal.svg" alt="Chrona Header" width="360" />
</p>

<p align="center">
  <strong>Pixel-inspired world clock and cross-timezone alarm engine for Android.</strong>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android%2012%2B%20(API%2031%E2%80%9337)-3DDC84?style=flat-square&logo=android&logoColor=white" alt="Platform" />
  <img src="https://img.shields.io/badge/Language-Kotlin%202.0-7F52FF?style=flat-square&logo=kotlin&logoColor=white" alt="Kotlin" />
  <img src="https://img.shields.io/badge/UI-Jetpack%20Compose%20%26%20M3-4285F4?style=flat-square&logo=jetpackcompose&logoColor=white" alt="Compose" />
  <img src="https://img.shields.io/badge/License-Apache%202.0-blue.svg?style=flat-square" alt="License" />
</p>

---

## ✦ Overview

Chrona is a native Android clock application designed with Material 3 and Pixel aesthetic sensibilities. Beyond standard timekeeping, Chrona solves distributed coordination with its **Cross-Timezone Alarm Engine**: schedule an alarm targeting local time in any destination city (e.g., 09:00 AM Tokyo or 08:30 AM Paris), and Chrona automatically maps, converts, and rings on your device at the exact local moment—dynamically handling daylight saving transitions (DST) via IANA `ZoneRules`.

---

## 📸 Visual Showcase

<p align="center">
  <img src="art/screenshots/alarms_screen.png" width="30%" alt="Alarms Screen" />
  &nbsp;&nbsp;&nbsp;&nbsp;
  <img src="art/screenshots/create_alarm.png" width="30%" alt="Create World Alarm" />
  &nbsp;&nbsp;&nbsp;&nbsp;
  <img src="art/screenshots/world_clock_screen.png" width="30%" alt="World Clock Screen" />
</p>

<p align="center">
  <em>Left: Cross-timezone alarms (Paris, Tokyo, Local). Center: Real-time timezone conversion & destination scheduling. Right: Interactive Natural Earth world clock.</em>
</p>

<br/>

<p align="center">
  <img src="art/screenshots/timer_screen.png" width="22%" alt="Timer Screen" />
  &nbsp;&nbsp;
  <img src="art/screenshots/stopwatch_screen.png" width="22%" alt="Stopwatch Screen" />
  &nbsp;&nbsp;
  <img src="art/screenshots/bedtime_screen.png" width="22%" alt="Bedtime Screen" />
  &nbsp;&nbsp;
  <img src="art/screenshots/settings_screen.png" width="22%" alt="Settings Screen" />
</p>

<p align="center">
  <em>Left to right: Monotonic timer with presets, precision chronograph stopwatch, circadian bedtime schedule, and permissions & reliability settings.</em>
</p>

---

## ⚡ Core Capabilities

### 🌐 Cross-Timezone Alarm Scheduling
* **Target-City Time Input**: Set alarms in the destination city's clock time without manual conversions.
* **Dual-Time Preview**: Displays destination time alongside local trigger time and relative day offset (`Today`, `Monday`, `Tomorrow`).
* **Daylight Saving Resilience**: Backed by `java.time.ZonedDateTime` and IANA `ZoneRules`, automatically adjusting for DST boundary shifts.
* **Radial Material 3 Dial**: Tactile 12-hour / 24-hour radial clock picker for rapid time selection.

### 🔔 Reliable Full-Screen Alarm Presentation
* **Android 14+ Runtime Full-Screen Guard**: Checks `NotificationManager.canUseFullScreenIntent()`, waking the display and displaying `AlarmRingingActivity` over the lock screen (`turnScreenOn`, `showWhenLocked`, `FLAG_KEEP_SCREEN_ON`).
* **Graceful Fallback**: If full-screen intent is restricted, drops down cleanly to a high-priority heads-up notification with action buttons without crashing or foreground loops.
* **Acoustically Tuned Audio**: Single-owner `AlarmAudioService` using `USAGE_ALARM` and `CONTENT_TYPE_SONIFICATION` with a gentle 30-second volume ramp starting at an audible baseline (`0.25 × maxVolume`).

### 🗺️ Natural Earth World Clock
* **Interactive Cartographic Projection**: Natural Earth vector coordinates projected onto a theme-aware map canvas.
* **Instant City Inspection**: Tap pins to inspect local time, UTC offsets, and relative time differences.
* **Custom City Library**: Search and add cities across all global IANA time zones.

### ⏱️ Timers, Stopwatch & Bedtime
* **Multi-Timer Engine**: Keypad entry, quick presets (+1m, +5m, +10m, +15m), active progress ring, and continuous background notification alerts.
* **Chronograph Stopwatch**: 60fps sweep animation, precision tick markers, split times, and lap differentials.
* **Sleep Routine**: Target sleep consistency tracker with customizable wind-down reminders.

---

## 🏛️ Architecture

```
com.example.chrona/
├── core/
│   ├── alarm/          # AlarmScheduler, AlarmAudioService, AlarmReceiver, BootReceiver
│   ├── data/           # AlarmRepository, CityRepository, BedtimeRepository, PreferencesRepository
│   ├── database/       # ChronaDatabaseHelper, AlarmDao, SavedCityDao, TimerDao (SQLite)
│   ├── map/            # WorldMapComposable, WorldMapProjection, MapMarker
│   ├── model/          # Alarm, City, TimerItem, BedtimeSchedule, UserPreferences
│   └── timezone/       # TimezoneEngine, DST & UTC offset resolvers
├── feature/
│   ├── alarms/         # AlarmsScreen, CreateAlarmSheet, ChronaRadialTimePicker, AlarmRingingActivity
│   ├── bedtime/        # BedtimeScreen
│   ├── settings/       # SettingsScreen (Theme, Accents, Permissions & Reliability)
│   ├── stopwatch/      # StopwatchScreen, StopwatchManager
│   ├── timer/          # TimerScreen, TimerManager
│   └── worldclock/     # WorldClockScreen, CityDetailSheet, AddCitySheet
├── navigation/         # ChronaNavHost, Bottom navigation with animated indicators
└── ui/theme/           # Material 3 tokens, AMOLED dark & light color schemes, typography
```

* **Modern Stack**: 100% Jetpack Compose, Kotlin Coroutines & Flow, Material 3.
* **State Management**: Unidirectional Data Flow (UDF) with lifecycle-aware state observation (`collectAsStateWithLifecycle`).
* **Persistence**: Structured local SQLite storage with Jetpack DataStore preferences.

---

## 🔒 Permissions & System Integration

| Permission | API Level | Purpose |
|:---|:---|:---|
| `USE_FULL_SCREEN_INTENT` | API 34+ | Presents full-screen alarm over lock screen with runtime fallback to heads-up alert. |
| `SCHEDULE_EXACT_ALARM` | API 31+ | Schedules exact millisecond alarm triggers via Android `AlarmManager`. |
| `POST_NOTIFICATIONS` | API 33+ | Posts ongoing timer status and alarm alert banners. |
| `FOREGROUND_SERVICE_MEDIA_PLAYBACK` | API 34+ | Keeps alarm audio playback active and uninterrupted by OS task killing. |
| `RECEIVE_BOOT_COMPLETED` | All | Reschedules alarms and bedtime notifications on device reboot. |

---

## 🛠️ Building & Running

```bash
# Clone the repository
git clone https://github.com/your-username/chrona.git
cd chrona

# Run unit tests
./gradlew testDebugUnitTest

# Assemble debug APK
./gradlew assembleDebug

# Install on connected device/emulator
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

## 📄 License

Licensed under the [Apache License, Version 2.0](LICENSE).
