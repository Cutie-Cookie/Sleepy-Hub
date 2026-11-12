# Sleepy Hub change log

This log records the application work that led to the documentation baseline. Commit hashes are abbreviated for readability; use the linked repository history for the full diff.

## Application changes

### `02faec0` — Rebrand Sleepy Hub, remove VPN/log upload, add ARM64 build

- Changed the application ID to `com.cutiecookie.sleepyhub`.
- Changed the visible app name and Gradle root project name to Sleepy Hub.
- Changed generated APK filenames from the BlackBox prefix to `SleepyHub_`.
- Added a build property to produce an ARM64-only debug APK for CI while retaining the normal ARMv7 + ARM64 split configuration for other builds.
- Replaced the stock launcher foreground/background treatment with the supplied black-and-white kitty identity; added a black launcher background and black splash treatment.
- Changed the core loading screen to black with white text.
- Rebranded the daemon notification channel and title, and connected the custom notification icon.
- Removed the VPN permission prompt from `MainActivity`, the VPN preference and state from the settings UI/loader, the VPN callback from client configuration, the VPN core initializer, manifest declaration/service, VPN proxy hooks, and VPN-specific classes.
- Removed the Send Logs preference and handler, the log-upload helper, the hardcoded upload destination, the core send method, and crash-handler calls that uploaded logs.
- Added the first Sleepy Hub ARM64 debug GitHub Actions workflow.

### `a2e19ef` — Fix adaptive icon foreground resource reference

The first adaptive-icon foreground pointed at `@mipmap/ic_launcher`, which can create a circular resource reference when the mipmap itself is an adaptive icon. It was changed to a drawable reference. This was an intermediate correction; the later artwork restoration gives the adaptive foreground its own dedicated drawable asset.

### `adab14d` — Fix Android SDK setup action

The first workflow setup attempted to use the old Android SDK setup action path that installed the retired `tools` package. The workflow was updated to:

- `actions/checkout@v7`
- `actions/setup-java@v6` with Temurin Java 21
- `android-actions/setup-android@v4` with `platform-tools`

This avoids relying on the retired SDK tools package.

### `b6b1ca5` — Give the workflow a clear run name

Added a descriptive run name so the ARM64 debug workflow is easy to identify in Actions.

### `5edbf91` — Remove the last VPN hook reference

The build exposed a leftover `@ScanClass(VpnCommonProxy.class)` annotation and import in `IConnectivityManagerProxy.java`. They were removed after the VPN proxy class itself had been deleted.

### `87fb80c` — Remove automatic log uploads from crash handling

The build exposed remaining calls to the deleted `BlackBoxCore.sendLogs` method in `SimpleCrashFix.java`. Those upload calls and the now-unused import were removed. Local crash classification/logging and delegation to the previous uncaught-exception handler remain.

### `1e3b706` — Restore activity-result import

Removing the VPN permission flow also removed an import for `ActivityResultContracts`. Other activity-result logic in `MainActivity.kt` still needs that class, so the import was restored without bringing back the VPN prompt.

### `6de07c5` — Restore full-quality launcher artwork

The first icon conversion had been reduced and palette-quantized too aggressively, which visibly damaged the artwork and made the composition look cropped/small. Following user feedback:

- Replaced the degraded icon resources with a cleaner, higher-quality version of the supplied artwork.
- Added a dedicated adaptive foreground bitmap under `drawable-xxhdpi`.
- Kept the splash artwork as a separate drawable.
- Routed the adaptive icon foreground through `drawable-v24/ic_launcher_foreground.xml` to the dedicated foreground asset rather than referencing the adaptive mipmap itself.
- Replaced all four legacy launcher bitmap names across the six density folders so old launchers have a matching icon too.

The user confirmed the corrected result looked good. Preserve the original source artwork for future edits; do not repeat the lossy conversion.

## Build and debugging history

The following workflow runs are part of the recorded iteration history:

| Workflow run | Outcome | Note |
|---|---|---|
| [43304013885](https://github.com/Cutie-Cookie/Sleepy-Hub/actions/runs/43304013885) | Failed | Early CI attempt; SDK setup needed correction. |
| [43304543015](https://github.com/Cutie-Cookie/Sleepy-Hub/actions/runs/43304543015) | Failed | Follow-up build exposed remaining cleanup/build issues. |
| [43305035923](https://github.com/Cutie-Cookie/Sleepy-Hub/actions/runs/43305035923) | Failed | Follow-up build exposed the remaining crash-handler upload references. |
| [43305603631](https://github.com/Cutie-Cookie/Sleepy-Hub/actions/runs/43305603631) | Successful | First successful ARM64 debug build after source/build fixes. |
| [43309146086](https://github.com/Cutie-Cookie/Sleepy-Hub/actions/runs/43309146086) | Successful | Latest build at the corrected-artwork baseline; build and artifact upload passed. |

### Lessons to carry forward

- Read the actual compiler/build error and fix the referenced source; do not guess at unrelated code.
- When removing a feature, search for its references across UI, state, configuration, manifests, core initialization, hooks, and crash/error paths.
- Keep imports used by unrelated code even when one feature is removed.
- For Android adaptive icons, keep the foreground asset separate from the adaptive mipmap to avoid resource recursion.
- Preserve artwork masters. Do not aggressively downsample, palette-quantize, crop, or reuse a tiny image as every density output.
- A green CI build is necessary but not a substitute for device testing.


### 2026-11-13 — Begin RikkaHub-inspired Sleepy Hub UI redesign

The user confirmed that Sleepy Hub remains a virtual Android app manager; RikkaHub is a visual/interaction reference only.

On `sleepyhub/ui-rikkahub-inspired`:
- Replaced the old profile-page shell with a left-side dashboard and wide, inset profile cards.
- Added explicit Add Profile and profile switching. Empty profiles are retained.
- Added per-profile wallpaper, opacity, accent, rename, and delete controls.
- Added global theme/accent and app-grid preferences.
- Redesigned the profile workspace header while keeping the installed-app manager as the main screen.
- Added app info and cache-clear actions alongside existing app-management actions.
- Added sampled background-image loading and a bounded app-icon cache; removed forced GC from list/scroll paths.
- Added profile card/page transitions and a profile-settings bottom sheet.

**Not yet a release:** the UI branch still needs a successful final CI build and physical-device smoke testing. Background-running apps remain a later-update feature. No signing secrets were created or used.


### UI build iteration — compile errors found and corrected

The first RikkaHub-inspired UI build attempt ([run 43317494796](https://github.com/Cutie-Cookie/Sleepy-Hub/actions/runs/43317494796)) reached Kotlin compilation and reported two errors:

1. `ProfileAppearance.glassBackground()` passed a floating-point dp value to `GradientDrawable.setStroke`, which requires an integer pixel width. The stroke width is now converted with the integer dp helper.
2. `AppsRepository.unInstall()` retained one call to `scanUser()` after the old auto-delete helper was removed. The stale call was removed so empty virtual profiles remain persistent.

The current UI branch build is being validated again. Record the successful run and artifact here only after both build and upload complete.


### 2026-11-13 — First complete UI debug build passed

- Workflow run [43319420454](https://github.com/Cutie-Cookie/Sleepy-Hub/actions/runs/43319420454) completed successfully for UI commit `9775bdc1b81ffe61e199b5512bf57b3ae4806ba7`.
- Gradle reported `BUILD SUCCESSFUL`; the ARM64 debug artifact uploaded successfully as artifact `sleepy-hub-arm64-debug` (artifact ID `13990365863`).
- This build predates the final polish commits for 192 px app-icon handling, the app-actions hint, the profile-settings sheet finish, and the narrowed workflow trigger. A fresh build is required for the final candidate.
- The earlier two compile errors are fixed. No runtime testing on a physical Android device has been performed yet.


### 2026-11-13 — Final polished UI debug build succeeded

- Final UI commit: `86bd5789e19a9a17a32a1def7aef933eb4eb0581`.
- GitHub Actions run: [43320730246](https://github.com/Cutie-Cookie/Sleepy-Hub/actions/runs/43320730246) — **success**.
- Artifact: `sleepy-hub-arm64-debug`, artifact ID `13990921422`.
- APK: `SleepyHub_4.0.0_arm64-v8a-debug.apk`, 9,214,632 bytes.
- SHA-256: `42703a57aa8a2f5183d2aa61339bce61b294ed04356b80177527a45f8faa8d1a`.
- The artifact ZIP and APK both passed `unzip -t` integrity checks.
- This is still a **debug APK**. No signed release build or signing secrets were created.
- Physical-device interaction testing has not been performed by CI; the user must test profile operations, wallpaper persistence, theme changes, app actions, and navigation on their phone.

### 2026-11-13 — UI direction corrected to follow RikkaHub source code

- User explicitly clarified that RikkaHub source code, not user screenshots, is the design and behavior reference.
- Documented actual RikkaHub navigation, drawer, assistant picker, assistant management/detail, background picker, and theme patterns.
- Confirmed RikkaHub is AGPL-3.0 and Sleepy Hub is Apache-2.0; implementation will be independently written rather than copying upstream source.
- Rejected the earlier oversized profile-card prototype. A clean rebuild branch, `sleepyhub/rikkahub-source-rebuild`, was created from `sleepyhub/identity-icons-no-vpn`.
- No changes were made to `main`.
