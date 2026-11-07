# Changelog

## v0.8.0

### Compatibility
- One universal APK that runs on armeabi-v7a, arm64-v8a, x86 and x86_64. There are no per-ABI splits
- Targets Android 12 to 17 (API 31 to 37)
- The pill search has a fallback for Android versions where the window list API is not reachable, and it recognizes more handle view names
- More launchers are recommended in the LSPosed scope (One UI Home, HyperOS Home)

### Size and speed
- The release build is shrunk with R8 and unused resources are removed, so the APK is much smaller
- Only the six supported languages are packaged
- Each build now publishes two APKs: the shrunk `-universal` one and an unshrunk `-debug` one as a fallback
- CI prints the ABIs inside the APK and checks 16 KB page alignment

### Diagnostics
- CPU architecture is shown in Settings

## v0.7.0

### Features
- Contextual styles: a different look while charging, on low battery (15 or below) or in landscape. Each one can use the current look or any saved style
- Quick Settings tile to switch the pill customization on and off
- Surprise me button that generates a random look
- Share a saved style as text and paste a style from the clipboard
- Backups now also include contextual styles
- Home page cards now show the real current style, Monet state and matching saved style
- "System default" option in the language picker

## v0.6.0

### More freedom
- Full color editor: hue, saturation, brightness sliders and a HEX field
- Custom gradients with 2 to 5 colors, each one editable
- New shape: dashes. Dots and dashes have adjustable count and gap
- Glow effect, plus two new animations: hue cycle and pulse
- Width goes up to 400% (limited by the view the launcher gives us), offset range is wider
- My styles: save the current look with a name, apply or delete it later. Backups now include saved styles
- Tap any slider value to type an exact number
- Live preview now uses the same renderer as the pill, so it matches what you get
- New preset: Spectrum

## v0.5.0

### Languages
- Added Russian, Spanish, German and French (English and Turkish were already there)
- Language picker is now a scrollable chip row instead of a segmented button
- More languages can be added by contributing a `values-xx/strings.xml` file, see README

