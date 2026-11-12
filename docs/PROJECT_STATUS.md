# Project status

_Last reviewed: 2026-11-12, at application commit `6de07c5`._

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
| `sleepyhub/docs-project-notes` | This documentation-only branch | Created from `6de07c5` |

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
4. **UI redesign:** follow [UI roadmap](UI_ROADMAP.md). No UI implementation is included in this documentation branch.
5. **Release readiness:** the current verified artifact is a debug build, not a signed production/release package.

## Source-of-truth order

When details disagree, prefer:

1. The current branch source and Gradle configuration.
2. The latest successful GitHub Actions run for that exact commit.
3. These project notes, updated to match the source.
4. Inherited root-level documentation only after it has been checked and corrected.
