# MyColourfulGesture

LSPosed module that restyles the gesture pill on Pixel and AOSP-based ROMs (Android 12-17), with a small companion app.

Color (stock / custom / Monet with a full HSV and HEX editor), multi-color and rainbow gradients, bar / dot / dots / dashes, size, corner radius, offset, outline, glow, animations, presets, your own saved styles, contextual styles (charging, low battery, landscape), a Quick Settings tile, backup and restore.

## Requirements

- Root (Magisk or KernelSU) with Zygisk
- LSPosed 2.0.1 or newer (modern libxposed API)

## Setup

1. Install the APK and enable the module in LSPosed.
2. Scope: System UI, this app, and your launcher (the app shows its package name on the home page).
3. Restart System UI and the launcher from the home page.

On newer Android versions the pill is drawn by the launcher's taskbar, so the launcher has to be in scope.

## Signing

Official builds are signed with a private release key, the SHA-256 of its certificate is printed in every build log. A build signed by anything else (your own fork, for example) will not install over an official one, and that is intended. To sign your own builds, run `scripts/make-release-key.sh` in your fork.

## Reporting problems

Open Settings, then Diagnostics, tap Copy report and paste it into a new issue.

## Compatibility

Android 12 to 17 on 32-bit and 64-bit ARM, plus x86. The release APK is universal, so there is nothing to pick. Each build also ships a `-debug` APK that is not shrunk; use it if the main one misbehaves on your device.

## Languages

English, Turkish, Russian, Spanish, German, French. To add one, copy `app/src/main/res/values/strings.xml` to `values-xx/strings.xml`, translate it, add the code to `res/xml/locales_config.xml` and to the `LANGUAGES` list in `SettingsScreen.kt`, then open a pull request.

## Build

Every push to `main` builds a debug APK through GitHub Actions and attaches it to a pre-release.

## Lisans / License

MIT

---

Pixel ve AOSP tabanlı ROM'larda (Android 12-17) hareket çubuğunu özelleştiren LSPosed modülü. Kurulum için LSPosed 2.0.1+ ve root gerekir; kapsama Sistem Arayüzü, uygulamanın kendisi ve başlatıcı eklenmelidir.
