# Changelog

## v0.9.0
- Builds are signed with a private key from GitHub secrets. Forks and local builds still fall back to the debug key. The signature changed, so uninstall the old version once
- `scripts/make-release-key.sh` creates the key and sets the secrets
- Crash guard: if System UI or the launcher restarts 5 times in a minute, the hooks turn themselves off. Home shows a button to turn them back on
- Setup checklist on Home (root, module, scope, pill reached)
- Settings has a Copy report button and a link to the issue form, plus issue templates

## v0.8.0
- One universal APK (armeabi-v7a, arm64-v8a, x86, x86_64), Android 12 to 17
- Release build is shrunk with R8. Every build also ships an unshrunk `-debug` APK
- Only the six supported languages are packaged
- Fallback for finding the pill when the window list API is blocked, more handle names recognised
- CPU architecture shown in Settings

## v0.7.0
- Contextual styles: charging, low battery, landscape
- Quick Settings tile to turn the customization on and off
- Surprise me button, share a style as text, paste one from the clipboard
- Backups include contextual styles
- Home cards show the real style, Monet state and matching saved style
- System default option in the language picker

## v0.6.0
- Color editor with hue, saturation, brightness and HEX
- Gradients with 2 to 5 colors, new dashes shape, glow, hue cycle and pulse animations
- Width up to 400%, wider offset range
- Saved styles, tap a slider value to type it
- Preview uses the same renderer as the pill
- New preset: Spectrum

## v0.5.0
- Russian, Spanish, German and French
- Language picker is now a row of chips

## v0.4.0
- First public release
- Color (stock, custom, Monet), opacity, gradient, bar / dot / dots, size, corner radius, offset, outline, animations, presets
- Works with the System UI handle and the launcher taskbar handle
- Floating dock, theme options, backup and restore, update check
- English and Turkish
- Needs root and LSPosed 2.0.1 or newer, tested on one device (Android 16, Galaxy S9+)
