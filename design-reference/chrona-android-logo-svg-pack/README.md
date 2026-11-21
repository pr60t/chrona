# Chrona logo SVG pack for Android

- `adaptive-layers/` foreground.svg, background.svg, monochrome.svg (108 x 108 dp canvas, mark inside the 66 dp safe zone).
- `logo/` symbol, wordmark (outlined), horizontal, stacked, monochrome, on-dark, on-light.
- `android-res/app/src/main/res/` ready-to-copy VectorDrawables (drawable/), adaptive icon XML (mipmap-anydpi-v26/) and legacy PNG mipmaps.
- `android-res/AndroidManifest.snippet.txt` the two icon attributes to merge into <application>.

Copy `android-res/app/src/main/res/*` into your project's `app/src/main/res/`. Not yet built or tested in a Gradle project.
Palette note: night half on light backgrounds (#3D5275) is a derived colour; confirm it.
