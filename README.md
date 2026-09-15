# Clock Widgets

A small Android app that adds three clock widgets to your home screen:

| Widget | What it shows |
| --- | --- |
| **Digital clock** | Time on a rounded card, with the weekday and date underneath |
| **Analog clock** | Classic dial with the date underneath |
| **Minimal clock** | Transparent background, big time only — perfect over a wallpaper |

Every clock can be restyled: 12- or 24-hour, optional seconds, optional date,
six accent colours and a dark / light / transparent background. Because each widget keeps
its own settings, you can put several clocks on the home screen with different looks.

The APK is built by GitHub Actions.

## Get the APK

**Easiest:** open the [Releases](../../releases/latest) page and download `app-debug.apk`
straight to your phone (no account needed).

**From a build:** open the **Actions** tab, pick the newest **Build Android APK** run, and
download the **clock-widgets-debug** artifact (a zip containing `app-debug.apk`).

Then install the APK on the phone (allow "install unknown apps" for your file manager /
browser when asked). The APK is signed with the standard debug key, which is fine for
personal use.

## Add a widget

* In the app: tap **Add to home screen** under the clock you want and confirm the system
  prompt (Android 8.0+).
* Or long-press an empty spot on the home screen → **Widgets** → **Clock Widgets** → drag a
  clock where you want it.
* Tap a clock on the home screen at any time to change its format, colour and background.
* To remove one, long-press it on the home screen and drag it to "Remove".

## How it works

* `MainActivity` — live previews of the three widgets (rendered from the very same
  RemoteViews that the home screen uses), the one-tap add button and the list of clocks that
  are currently placed.
* `ClockConfigActivity` — the settings screen. It is declared with
  `android.appwidget.action.APPWIDGET_CONFIGURE`, so the launcher opens it straight after a
  widget is dropped on the home screen (the widget is only kept when it returns `RESULT_OK`
  with the app widget id). Tapping a placed clock opens the same screen.
* `widget/*WidgetProvider` — `AppWidgetProvider`s; every update rebuilds the RemoteViews from
  the settings stored for that app widget id.
* `ClockRemoteViews` — builds those RemoteViews. Only documented widget APIs are used
  (`setViewVisibility`, `setTextColor`, `setOnClickPendingIntent`) together with
  `TextClock` / `AnalogClock`, which keep ticking on their own — so the widgets need no
  alarms, no background service and no wake locks.
* `ClockPrefs` / `ClockConfig` — per widget id persistence (12/24 hour, seconds, date,
  accent colour, background style).

## Build it yourself

The project uses Gradle 8.7 and Android Gradle Plugin 8.5.0 (`compileSdk 34`, `minSdk 24`).
This machine (Termux, aarch64) cannot run `aapt2`, so the build happens on GitHub's runners:

```bash
gradle assembleDebug         # locally, with an Android SDK installed
```

The workflow in `.github/workflows/build.yml` does the same on every push:

```yaml
- uses: actions/setup-java@v4        # JDK 17
- uses: android-actions/setup-android@v3
- uses: gradle/actions/setup-gradle@v4   # Gradle 8.7
- run: gradle assembleDebug --no-daemon
- uses: actions/upload-artifact@v4   # app-debug.apk
```

## Publishing a signed release

Add a keystore and a signing config to `app/build.gradle`:

```gradle
android {
    signingConfigs {
        release {
            storeFile file("../release.jks")
            storePassword System.getenv("KEYSTORE_PASSWORD")
            keyAlias "release"
            keyPassword System.getenv("KEY_PASSWORD")
        }
    }
    buildTypes {
        release {
            minifyEnabled false
            signingConfig signingConfigs.release
        }
    }
}
```

…then base64 the keystore into the repository secret `KEYSTORE_BASE64` and decode it in a
workflow step before running `gradle assembleRelease`.