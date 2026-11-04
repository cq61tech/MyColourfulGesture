# MyColourfulGesture

**Your imagination can completely transform one tiny line.**

An LSPosed module that customizes the Gesture Navigation Pill on Pixel and AOSP-based Android 12–17, with a Material 3 Expressive companion app.

> **Status: Stage 3.** Gesture pill: stock/custom/Monet color, gradient, breathing / flowing animation, bar / dot / dots shapes, size, position, outline, presets. App: floating dock, theme options, backup/restore, update check. Android 16 first; other versions untested.

## Planned features

- Color, Monet (accent / neutral / secondary), transparency, gradient
- Width, height, corner radius, stroke, alternative shapes
- Vertical offset and horizontal alignment
- Contextual styles: keyboard, landscape, charging / low battery, per-app
- Style profiles, backup / restore
- Floating Dock navigation mode

## Requirements

- Pixel or AOSP-based ROM, Android 12–17 (Android 16 tested first)
- Root with Zygisk (Magisk / KernelSU)
- **LSPosed 2.0.1 or newer** (or a fork supporting the modern libxposed API 101+). Older LSPosed versions will not list this module.

## Setup

1. Install the APK and open LSPosed → Modules → enable **MyColourfulGesture**.
2. Scope: tick **System UI** and **MyColourfulGesture** itself (the second one lets the app show an accurate status).
3. Restart System UI (button on the Home page, or `su -c killall com.android.systemui`).
4. Open the app → Customize.

## Build

Builds run on GitHub Actions. Every push to `main` produces a debug APK as a workflow artifact and as a pre-release.

## Roadmap

1. App shell + CI (this stage)
2. Hook MVP: color, transparency, size, corner radius on a single Android version
3. Customization UI wired to the hook, live preview
4. Gradient, animation, shapes, profiles, backup
5. Contextual styles
6. Multi-version support

## License

MIT

---

## Türkçe

MyColourfulGesture, Pixel ve AOSP tabanlı Android 12–17 sürümlerinde hareket çubuğunu (Gesture Pill) özelleştiren bir LSPosed modülüdür. Arayüz Material 3 Expressive ile hazırlanmıştır.

**Durum: Aşama 1 – uygulama iskeleti.** Arayüz ve CI hazır, SystemUI hook'u henüz yazılmadı.
