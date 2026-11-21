# Performance & APK Footprint Benchmarks

- **VectorDrawable Size:** `world_map_vector.xml` is `< 45 KB` uncompressed (`~14 KB` compressed inside APK/AAB), compared to `> 1.5 MB` for multi-density raster PNGs.
- **Vertex Budget:** Dissolved continental groups at `1:110m` with `1.5°` graticule densification contain ~2,450 vertices total—well below Android's `VectorDrawable` hardware texture cache limits, rendering in `< 0.8 ms` on mid-range GPUs.
- **Marker Overlay:** Dynamic Compose `Canvas` draws `N` saved cities (`N <= 20` typical) in a single draw pass with zero recomposition of the static `Image` layer.
