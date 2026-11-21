# CHRONA Batch 2 — World Clock Interaction Map & Screen Architecture

**Artboard Standard:** `412 × 915dp` logical units (`20dp` horizontal screen margins, `372dp` responsive card width)  
**Navigation:** 5-Destination Bottom Bar (`Alarms`, `World Clock` [Active], `Timer`, `Stopwatch`, `Bedtime`)  
**Sample Data Anchor (Illustrative):** Device Local = **Kathmandu, Nepal (`Asia/Kathmandu`, `UTC +5:45`)**, **Thursday, Nov 19, 2026 · 13:17**

---

## 1. Screen Topology & User Flows

```text
[1. world_clock_default.svg]  (Expanded Map ~42% + 5 Saved Cities + "+ Add place")
   │
   ├──► Scroll / Drag List Up ──► [2. world_clock_expanded.svg] (Collapsing Map Transition)
   │                                  └──► Continue Scroll ──► [3. world_clock_collapsed.svg] (Compact Timeline Strip + Full List)
   │
   ├──► Tap Any City Card ──────► [4. timezone_details.svg] (City Detail Bottom Sheet: Hero Clock, UTC, Day/Night, Secondary Weather)
   │                                  └──► Tap "Set alarm in [City] time" ──► [5. timezone_details_alarm.svg] (Inline Alarm Editor)
   │                                                                             ├── ALARM TIME IN SELECTED CITY (e.g., 09:00 Warsaw)
   │                                                                             └── YOUR PHONE WILL RING AT (e.g., 13:45 Kathmandu)
   │
   ├──► Tap "+ Add place" ──────► [6. timezone_search.svg] (Initial Search + Suggested Cities)
   │                                  ├──► Type Query ("Tok") ───► [7. timezone_search_results.svg] (Canonical City-Card Results)
   │                                  ├──► Unmatched Query ─────► [8. timezone_search_empty.svg] (Empty State + Clear Search)
   │                                  └──► Existing / New City ──► [9. timezone_already_added.svg] (Saved Badge + Added Snackbar)
   │
   └──► System / Settings States
        ├──► Weather Data Lifecycle ──► [10. timezone_weather_states.svg] (Loading, Available, Cached, Offline, Unavailable)
        └──► Show City Photos Toggle ─► [11. timezone_card_variants.svg] (80×64dp Tile Enabled vs Rebalanced Strip-Only Layout)
```

---

## 2. Sample City Matrix (Date-Consistent, November 19, 2026 — Illustrative)

| City & Country | IANA Timezone | UTC Offset | Sample Local Time | Local Date | Offset vs. Device (`Kathmandu`) | Identity Color Strip |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Warsaw**, Poland | `Europe/Warsaw` | `UTC +1` | `08:32` | Thu, Nov 19 | `-4h 45m` (Behind) | `#A7E8D8` Glacier Mint |
| **London**, England | `Europe/London` | `UTC +0` | `07:32` | Thu, Nov 19 | `-5h 45m` (Behind) | `#B8C7F2` Periwinkle Mist |
| **Boston**, United States | `America/New_York` | `UTC -5` | `02:32` | Thu, Nov 19 | `-10h 45m` (Behind) | `#F4C2A1` Apricot Sand |
| **Perth**, Australia | `Australia/Perth` | `UTC +8` | `15:32` | Thu, Nov 19 | `+2h 15m` (Ahead) | `#D4B8F0` Lavender Bloom |
| **Kathmandu**, Nepal | `Asia/Kathmandu` | `UTC +5:45` | `13:17` | Thu, Nov 19 | `Local` (Device) | `#F2DF9B` Buttercream Gold |

---

## 3. SVG Layer & Component Verification Checklist

- **Separate Map & Marker Layers:** Every map artboard isolates `#layer-world-map` into `#map-graticule`, `#map-continents`, and `#map-markers` with individual `#marker-<city>` groups (`marker-warsaw`, `marker-london`, `marker-boston`, `marker-perth`, `marker-kathmandu`).
- **Surface Hierarchy Preserved:** Base screen canvas is pitch-black `#000000`; all cards use `#1A202C` (`#222A38` when selected); bottom sheets and dialogs use `#2B3547`.
- **Card Anatomy Preserved:** `372dp` width (`20dp` side margins on `412dp` artboards), `20dp` corner radius, `80×64dp` (`12dp` radius) city photograph, `4dp` vertical pastel identity strip, and tabular numerals (`tnum`).
