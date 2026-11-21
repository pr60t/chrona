# Android Jetpack Compose Integration Guide

## 1. Resource Placement
- Copy `android/world_map_vector.xml` into `app/src/main/res/drawable/world_map_vector.xml`.
- Do **not** bundle raw Natural Earth shapefiles or raw `cities500.txt` into the APK.

## 2. Database Schema & Search Disambiguation
- Pre-populate Room/SQLite with `cities5000` (`64,145` cities with `population >= 5,000`), storing precomputed `mapX` and `mapY` `Float` columns.
- Disambiguate duplicate city names (e.g., *London, England, United Kingdom* vs. *London, Ontario, Canada*) by appending `admin1Name` whenever `cityName` collides across distinct administrative regions.

## 3. Responsive Collapsing Behavior
- `ChronaWorldMapHeader` accepts `collapseFraction: Float` (`0f` = Expanded `196dp`, `1f` = Collapsed `56dp`).
- `WorldMapProjection.computeRenderedMapRect` uses `ContentScale.Fit` math so markers remain locked onto their exact coastlines across every intermediate collapse frame and screen aspect ratio.
