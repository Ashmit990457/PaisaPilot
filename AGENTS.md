# PaisaPilot agent guide

## Project shape
- Single-module Android app: root includes only `:app` in `settings.gradle.kts`.
- Codebase is classic Android Views + XML, not Compose.
- Main app package/namespace is `com.example.paisapilot` across `app/build.gradle.kts`, `AndroidManifest.xml`, and tests.

## App architecture
- Launch flow starts in `app/src/main/java/com/example/paisapilot/MainActivity.java`.
- `MainActivity` calls `EdgeToEdge.enable(this)` and loads `R.layout.activity_main`.
- Window insets are applied manually to the root view `@id/main` in `activity_main.xml` via `ViewCompat.setOnApplyWindowInsetsListener(...)`.
- The launcher activity is declared in `app/src/main/AndroidManifest.xml` with `android:windowSoftInputMode="adjustResize"` and theme `@style/Theme.PaisaPilot`.

## Resources and UI conventions
- `app/src/main/res/layout/activity_main.xml` uses a `ConstraintLayout` root with id `main`.
- Theme setup is Material 3 DayNight with no action bar: `Theme.Material3.DayNight.NoActionBar` in `values/themes.xml` and `values-night/themes.xml`.
- App name comes from `app/src/main/res/values/strings.xml` (`PaisaPilot`).
- Keep resource IDs and manifest references in sync when changing layouts, themes, or launcher wiring.

## Build and dependency workflow
- Dependencies and versions are centralized in `gradle/libs.versions.toml`; prefer adding aliases there instead of hardcoding versions in module build files.
- Repository policy is strict: `dependencyResolutionManagement` uses `RepositoriesMode.FAIL_ON_PROJECT_REPOS`, so do not add module-level repositories.
- Root `build.gradle.kts` only applies the Android application plugin alias; module-specific Android config stays in `app/build.gradle.kts`.
- Current baseline is AGP `9.2.1`, Gradle `9.4.1`, Java 11, `minSdk 24`, `targetSdk 36`, `compileSdk 36` with `minorApiLevel = 1`.
- Main libraries in use: AppCompat, Material, ConstraintLayout, Activity KTX, plus JUnit/Espresso for tests.

## Developer commands
- `./gradlew.bat assembleDebug` - build the app.
- `./gradlew.bat testDebugUnitTest` - run local JVM tests in `app/src/test`.
- `./gradlew.bat connectedDebugAndroidTest` - run instrumented tests in `app/src/androidTest` on a device/emulator.
- `./gradlew.bat lint` - useful for Android resource and manifest checks.

## Testing conventions
- Host-side unit tests live in `app/src/test/java/com/example/paisapilot/`.
- Device tests live in `app/src/androidTest/java/com/example/paisapilot/` and use `AndroidJUnit4`.
- Existing test scaffolds assert the package name and a basic arithmetic check; follow the same package path for new tests.

## Practical editing notes
- Prefer XML/view-system changes over introducing Compose unless the whole app is being migrated.
- When changing the UI, update the layout file, `MainActivity`, and any string/theme resources together.
- When adding dependencies, update the version catalog first, then reference `libs.*` aliases in `app/build.gradle.kts`.



