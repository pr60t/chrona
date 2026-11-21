# CHRONA — Standalone World Map Engine & Vector Asset Package

## Overview
Production cartographic asset pipeline for **Chrona** (Android, Kotlin + Jetpack Compose, Material Design 3). Replaces the 7-polygon cartoonish reference map with accurate, dissolved continental vector silhouettes derived from **Natural Earth 1:110m v5.1.1**.

## Package Directory
- `source/` — Untouched Natural Earth `1:110m` dataset, manifest, and attribution.
- `scripts/` — Reproducible Python pipeline for inspection, projection, GeoNames transformation, validation, and preview rendering.
- `projections/` — Objective 3-way projection comparison (`natearth`, `robin`, `eqc`) with identical styling and test markers.
- `production/` — Clean marker-free `world_map_base.svg` (`1200x600`), `world_map_preview.svg`, `world_map_projection.json`, `map_marker_spec.svg`, and `map_marker_colors.json`.
- `city_data/` — GeoNames `cities5000` coordinate schema, sample CSV, and 12-city validation JSON.
- `previews/` — High-resolution PNG renders (`projection_comparison.png`, `map_test_locations.png`, `expanded_world_clock.png`, `collapsed_world_clock.png`, `final_map_detail.png`).
- `android/` — Production `world_map_vector.xml` (`VectorDrawable`) and Jetpack Compose rendering engine (`WorldMapProjection.kt`, `MapMarker.kt`, `WorldMapComposable.kt`).
- `documentation/` — Technical math, Android integration, performance benchmarks, and licensing.
