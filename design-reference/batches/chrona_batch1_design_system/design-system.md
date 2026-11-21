# CHRONA Design System — Batch 1 Specification

**Product:** CHRONA — Native Android Smart Clock & Cross-Timezone Application  
**Design Language:** Google Pixel-inspired Material Design 3 · True Pitch-Black AMOLED Aesthetic  
**Version:** 1.0.0 (Batch 1 Design Foundation)

---

## 1. Visual Identity & Architectural Principles

CHRONA pairs a **true pitch-black AMOLED background (`#000000`)** with **deep blue-gray elevated surfaces (`#1A202C` – `#2B3547`)** so cards, controls, and overlays remain crisply separated from the screen bezel without relying on heavy drop shadows.

### Core Guardrails
1. **Pitch-Black Canvas, Blue-Gray Cards:** The base screen background is strictly `#000000`. Cards must **never** be `#000000`; they use `#1A202C` (`surfaceContainer`) or `#222A38` (`surfaceContainerHigh`).
2. **Dual Chromatic Separation:**
   - **Main Application Accent:** Desaturated periwinkle-slate blue (`#93A8C7`) for primary pill buttons, active toggles, sliders, and selected navigation containers (`#2A384D`).
   - **Timezone Identity System:** 8 persistent pastel colors (`#A7E8D8`, `#B8C7F2`, `#F4C2A1`, `#D4B8F0`, `#F2DF9B`, `#F2B5C8`, `#A3D5F7`, `#BCE3B4`) dedicated strictly to city strips, world map pins, and cross-timezone comparison tracks.
3. **Viewport & Responsive Card Geometry:**
   - **Reusable Component Reference Base:** `360dp` width.
   - **Batch 2+ Mobile Screen Artboards:** `412 × 915dp` logical design units.
   - **Responsive Timezone Cards:** `20dp` left and right screen margins, yielding `372dp` card width on `412dp` screens.
4. **Navigation & Global Chrome:**
   - **Bottom Navigation:** Strictly **5 evenly distributed destinations** (`Alarms`, `World Clock`, `Timer`, `Stopwatch`, `Bedtime`). Never use a 4-item layout.
   - **3-Dot Overflow Menu:** Contains exactly **3 items**: `Settings`, `Screensaver`, `About`.

---

## 2. Color Token System

| Token | Hex | Role |
| :--- | :--- | :--- |
| `color.amoled.background` | `#000000` | Pitch-black AMOLED canvas |
| `color.amoled.surfaceContainerLowest` | `#080B10` | Recessed containers |
| `color.amoled.surfaceContainerLow` | `#11161F` | Bottom navigation bar & inactive cards |
| `color.amoled.surfaceContainer` | `#1A202C` | Primary timezone, alarm, timer & weather cards |
| `color.amoled.surfaceContainerHigh` | `#222A38` | Elevated cards, search fields, overflow menus |
| `color.amoled.surfaceContainerHighest` | `#2B3547` | Bottom sheets, dialogs, time pickers |
| `color.amoled.mapLandmass` | `#242C3A` | Muted slate-blue world map silhouette |
| `color.amoled.onSurface` | `#F1F5F9` | Primary white text & live clock numerals |
| `color.amoled.onSurfaceVariant` | `#94A3B8` | Country, GMT offset & secondary metadata |
| `color.amoled.outline` | `#334155` | Input & elevated container borders |
| `color.amoled.outlineVariant` | `#1E293B` | Hairline dividers |
| `color.accent.primary` | `#93A8C7` | Primary pill button fill & active switch track |
| `color.accent.onPrimary` | `#0B1320` | High-contrast dark ink on primary button |
| `color.accent.primaryContainer` | `#2A384D` | Selected bottom navigation pill (`56 × 32dp`) |
| `color.accent.onPrimaryContainer` | `#DCE6F7` | Active navigation icon & selected input text |

### 8 Persistent Timezone Identity Colors
1. `tz01_glacierMint`: `#A7E8D8` (e.g., Warsaw · GMT +0)
2. `tz02_periwinkleMist`: `#B8C7F2` (e.g., London · GMT -1)
3. `tz03_apricotSand`: `#F4C2A1` (e.g., Boston · GMT -6)
4. `tz04_lavenderBloom`: `#D4B8F0` (e.g., Perth · GMT +6)
5. `tz05_buttercreamGold`: `#F2DF9B` (e.g., Tokyo · GMT +9)
6. `tz06_roseQuartz`: `#F2B5C8` (e.g., New Delhi · GMT +5:30)
7. `tz07_skyBreeze`: `#A3D5F7` (e.g., Sydney · GMT +10)
8. `tz08_sageLeaf`: `#BCE3B4` (e.g., San Francisco · GMT -8)

---

## 3. Typography & Tabular Numerals

- **Font Family:** `Google Sans Display` / `Google Sans` / `Roboto`
- **Tabular Numerals:** `font-variant-numeric: tabular-nums` (`tnum`) enforced on all live clocks, card timestamps, timers, and stopwatch readouts.

| Style | Size / Line Height | Weight | Tracking | Usage |
| :--- | :--- | :--- | :--- | :--- |
| `clockHero` | `56sp / 64sp` | 400 | `-0.5sp` | Stopwatch, Timer & Screensaver hero time |
| `clockCardTime` | `24sp / 30sp` | 500 | `0sp` | Right-aligned live city time on cards |
| `pageHeading` | `24sp / 32sp` | 500 | `0sp` | Top app bar / screen headers |
| `cardHeading` | `18sp / 24sp` | 600 | `0sp` | City name (`Warsaw`, `London`) |
| `bodyLarge` | `16sp / 24sp` | 400 | `+0.25sp` | Primary settings & dialog body copy |
| `secondaryLabel` | `13sp / 18sp` | 500 | `+0.1sp` | Country & offset (`Poland (GMT +0)`) |
| `metadata` | `12sp / 16sp` | 400 | `+0.4sp` | Countdown hints & navigation labels |

---

## 4. Canonical Timezone Card Anatomy

From left to right inside a `372 × 80dp` (`20dp` radius) `#1A202C` card:
1. **Left (`8dp` inset):** Replaceable photographic city tile (`80 × 64dp`, `12dp` corner radius).
2. **Next (`8dp` gap):** Narrow vertical pastel identity strip (`4 × 64dp`, `2dp` corner radius).
3. **Center (`12dp` gap):** Stacked two-line label:
   - Top: Country & current offset (`13sp`, `#94A3B8`)
   - Bottom: City name (`18sp` SemiBold, `#F1F5F9`)
4. **Right (`20dp` trailing inset):** Live local time (`24sp` Medium `tnum`, `#F1F5F9`).
