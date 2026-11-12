# Project status

_Last reviewed: 2026-11-13. The original application baseline was `6de07c5`; UI implementation is in progress on a separate feature branch._

## Current state

- **Project name:** Sleepy Hub
- **Application version:** `4.0.0` (`versionCode 400`)
- **Application ID:** `com.cutiecookie.sleepyhub`
- **Android namespace:** `top.niunaijun.blackboxa`
- **Core module namespace:** `top.niunaijun.blackbox`
- **Minimum SDK:** 21
- **Target SDK:** 28
- **Compile SDK:** 35
- **NDK:** `29.0.13846066`
- **Java / Kotlin JVM target:** 21
- **Build type currently verified:** ARM64-v8a debug APK

The application ID was changed for the Sleepy Hub identity. The internal source namespaces were deliberately left as they were; renaming every Java/Kotlin package is a separate, higher-risk refactor and is not required for the application ID change.

## Branch baseline

| Branch | Purpose | Baseline |
|---|---|---|
| `main` | Upstream/default branch; do not push project work here | `89b5983` |
| `sleepyhub/identity-icons-no-vpn` | Current application work | `6de07c5` |
| `sleepyhub/docs-project-notes` | Project notebook and decisions | Created from `6de07c5` |
| `sleepyhub/ui-rikkahub-inspired` | UI redesign and app-manager improvements | Created from the documentation branch; main remains untouched |

At the time these notes were started, the application branch was **8 commits ahead of main and 0 behind**. No application changes were pushed to `main`.

The documentation branch inherits the application code at that baseline, then adds notebook files. Keep it separate until we deliberately decide how to merge or cherry-pick documentation.

## What is currently in place

### Identity and appearance

- App label and application ID use the Sleepy Hub identity.
- APK output filenames use the `SleepyHub_` prefix.
- Root Gradle project name is `Sleepy Hub`.
- Launcher resources use the supplied monochrome kitty artwork and a black background.
- Adaptive launcher icons are defined for regular and round launchers.
- Splash and core loading surfaces use a black background.
- The foreground artwork is a separate drawable resource; the adaptive icon must not point back to its own mipmap resource.
- The daemon notification channel/title is Sleepy Hub branded and uses the custom white notification icon.

### Removed features

- **VPN:** the VPN feature and its permission prompt, settings preference, saved preference handling, core initializer, manifest service/permission, proxy hooks, and VPN support classes were removed. The user has confirmed that the VPN removal is complete.
- **Send Logs / remote log upload:** the settings entry, click handler, upload helper, hardcoded destination configuration, core send method, and automatic crash-upload calls were removed.

Local crash handling and local logging code still exist. Removing remote log uploads does not mean all logging or crash handling was removed.

### Build workflow

Workflow: [Sleepy Hub ARM64 debug build](../.github/workflows/sleepyhub-arm64-debug.yml)

- Runs on pushes to branches matching `sleepyhub/**`, or manually through `workflow_dispatch`.
- Uses Java 21 and Android SDK setup action v4.
- Builds with `./gradlew --no-daemon :app:assembleDebug -PsleepyHubArm64Only=true`.
- Uploads the `sleepy-hub-arm64-debug` artifact for 7 days.
- With `sleepyHubArm64Only=true`, the build includes only `arm64-v8a` and does not create a universal APK.
- Without that property, the Gradle configuration includes `armeabi-v7a` and `arm64-v8a`, with a universal APK enabled.

## Latest verified build

- **Workflow run:** [43309146086](https://github.com/Cutie-Cookie/Sleepy-Hub/actions/runs/43309146086)
- **Commit:** `6de07c536a013febe6432148dd23f94eec1bac30`
- **Result:** successful; build and artifact upload steps both passed.
- **Artifact:** `sleepy-hub-arm64-debug`
- **Artifact size:** 7,752,711 bytes (ZIP archive)
- **Artifact expiry shown by GitHub:** 2026-11-19 21:22:40 UTC

A successful CI build proves the project compiles and the artifact was uploaded. It does **not**, by itself, prove every feature works on a physical phone. Record device smoke-test results separately.

## Known gaps and follow-ups

1. **Device smoke testing:** record Android version/device class and test installation, first launch, app cloning, permissions, notifications, and normal network access. Do not write private device identifiers.
2. **Documentation drift:** root `README.md`, `Docs.md`, and `RELEASE_NOTES.md` still contain upstream BlackBox/NewBlackbox names and/or old VPN and log-upload descriptions. Review and replace them deliberately.
3. **Target SDK review:** target SDK is currently 28. Evaluate a target-SDK upgrade separately, with a compatibility and permission test plan; do not mix it into the UI redesign.
4. **UI redesign:** implementation is underway on `sleepyhub/ui-rikkahub-inspired`. Follow [UI roadmap](UI_ROADMAP.md) and [UI implementation plan](UI_IMPLEMENTATION_PLAN.md).
5. **Release readiness:** the current verified artifact is a debug build, not a signed production/release package.

## Source-of-truth order

When details disagree, prefer:

1. The current branch source and Gradle configuration.
2. The latest successful GitHub Actions run for that exact commit.
3. These project notes, updated to match the source.
4. Inherited root-level documentation only after it has been checked and corrected.


## RikkaHub-inspired UI and performance research (2026-11-13)

See [RIKKAHUB_RESEARCH.md](RIKKAHUB_RESEARCH.md).

The user confirmed that Sleepy Hub remains a BlackBox virtual app manager. Profiles are virtual Android users, not AI assistants. No AI chat subsystem is being added. The approved scope is in [UI_IMPLEMENTATION_PLAN.md](UI_IMPLEMENTATION_PLAN.md).

Initial source-level performance findings:
- Scroll callbacks can trigger forced garbage collection; the force path also sleeps for 100 ms.
- App icon loading is skipped above a heap-usage threshold, causing missing icons.
- Virtual app list refresh re-enumerates installed packages and loads icons; its duration has not yet been benchmarked.
- No device performance baseline has been collected. See the research note for a measurement-first plan.


## UI implementation status (2026-11-13)

Active branch: `sleepyhub/ui-rikkahub-inspired`.

Implemented in source so far (still awaiting a successful final CI build and physical-device testing):
- Modal side dashboard with profile cards, Add Profile, Settings, and Fake Location.
- Explicit profile creation; empty virtual profiles are retained rather than auto-deleted.
- Profile rename, delete confirmation, per-profile accent, and per-profile wallpaper selection/removal/opacity.
- App-manager profile header with wallpaper and installed-app count.
- Global theme/accent and app-grid preferences.
- App long-press actions including App Info, Clear Cache, Clear Data, Stop, Uninstall, and Shortcut.
- Bounded app-icon drawable cache and removal of forced-GC calls from scrolling/list loading.
- Debug-first acceptance checklist is in `UI_IMPLEMENTATION_PLAN.md`.

Background-running apps are explicitly deferred. No release signing keys/secrets are being created or used in this milestone.


## UI CI iteration

The first UI build attempt ([run 43317494796](https://github.com/Cutie-Cookie/Sleepy-Hub/actions/runs/43317494796)) failed during Kotlin compilation with:
- a Float/Int mismatch for the profile-card stroke width;
- one leftover `scanUser()` reference after removing empty-profile auto-deletion.

Both source issues were corrected. The latest UI branch build is still pending; do not mark the UI milestone as build-verified until the current commit's workflow succeeds and uploads its artifact.


## Build validation update

A full ARM64 debug build succeeded in [workflow run 43319420454](https://github.com/Cutie-Cookie/Sleepy-Hub/actions/runs/43319420454) for UI commit `9775bdc1b81ffe61e199b5512bf57b3ae4806ba7`. Gradle completed with `BUILD SUCCESSFUL`, and artifact `sleepy-hub-arm64-debug` was uploaded (ID `13990365863`).

The UI branch then received a final polish pass: app icons now retain up to 192 px, the app manager explains tap/long-press actions, and the profile-settings sheet has a handle and rounded top. Those later commits still require their own successful build before the final APK is handed over. Device-level testing remains with the user.


## Final UI build status

<badge color="success">Debug build succeeded</badge>

- Branch: `sleepyhub/ui-rikkahub-inspired`
- Commit: `86bd5789e19a9a17a32a1def7aef933eb4eb0581`
- Workflow: [43320730246](https://github.com/Cutie-Cookie/Sleepy-Hub/actions/runs/43320730246)
- Artifact: `sleepy-hub-arm64-debug` (ID `13990921422`)
- APK: `SleepyHub_4.0.0_arm64-v8a-debug.apk`, 9,214,632 bytes
- SHA-256: `42703a57aa8a2f5183d2aa61339bce61b294ed04356b80177527a45f8faa8d1a`
- The downloaded artifact ZIP and extracted APK both passed archive-integrity checks.

**Still required:** physical-device smoke testing. The UI milestone is source-complete and debug-build verified, but it is not yet user-approved or release-ready. Background-running apps remain deferred, and no release signing secrets have been created.
