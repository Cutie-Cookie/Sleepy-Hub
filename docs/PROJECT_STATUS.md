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
| `main` | Upstream/default branch; do not push project work here | `89b5983` — untouched |
| `sleepyhub/bugfix-install-switch-polish` | Current application feature branch | `6df0bbc` — current candidate |
| `sleepyhub/docs-project-notes` | Project notebook and decisions | Documentation-only updates; kept separate from app source |
| `sleepyhub/identity-icons-no-vpn` | Known-good application baseline | `6de07c5` |

No application changes have been pushed to main. The documentation branch remains separate from the feature branch.
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

## Latest verified build and current candidate

- Last successful current-feature build: [43424012176](https://github.com/Cutie-Cookie/Sleepy-Hub/actions/runs/43424012176), exact source head `cd917c58c7d4f8baa521f6839eafdb318cb40fa7`.
- Artifact: `sleepy-hub-arm64-preview-debug`, artifact ID `14036965394`; extracted APK `SleepyHub_4.0.0-preview_arm64-v8a-debug.apk`.
- Package ID: `com.cutiecookie.sleepyhub.preview`; label: `Sleepy Hub Preview`; it is intended to install alongside the regular app.
- Current candidate: feature head `6df0bbcc031bace16bb2461c4963ea23520277ea`; Actions run [43437776339](https://github.com/Cutie-Cookie/Sleepy-Hub/actions/runs/43437776339) is in progress. There is no current APK for the new identity/folder/lifecycle/UI changes until this run succeeds.
- The previous APK is stale for the latest fixes. Do not tell the user to test it as the current candidate.

A successful CI build proves compilation and artifact upload only. It does not prove runtime behavior, data isolation, visual parity, or physical-device compatibility.
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

**Debug build succeeded.**

- Branch: `sleepyhub/ui-rikkahub-inspired`
- Commit: `86bd5789e19a9a17a32a1def7aef933eb4eb0581`
- Workflow: [43320730246](https://github.com/Cutie-Cookie/Sleepy-Hub/actions/runs/43320730246)
- Artifact: `sleepy-hub-arm64-debug` (ID `13990921422`)
- APK: `SleepyHub_4.0.0_arm64-v8a-debug.apk`, 9,214,632 bytes
- SHA-256: `42703a57aa8a2f5183d2aa61339bce61b294ed04356b80177527a45f8faa8d1a`
- The downloaded artifact ZIP and extracted APK both passed archive-integrity checks.

**Still required:** physical-device smoke testing. The UI milestone is source-complete and debug-build verified, but it is not yet user-approved or release-ready. Background-running apps remain deferred, and no release signing secrets have been created.

## Corrected UI rebuild status — 2026-11-13

The user rejected the first UI prototype and clarified that **RikkaHub's actual source code is the reference; screenshots are context only**.

- Active clean rebuild branch: `sleepyhub/rikkahub-source-rebuild`
- Base branch: `sleepyhub/identity-icons-no-vpn`
- Rejected prototype: `sleepyhub/ui-rikkahub-inspired` (do not merge into rebuild)
- RikkaHub source inspected at `b10c168577482abed696669a58c0eb59f1eed270`.
- RikkaHub is AGPL-3.0; Sleepy Hub is Apache-2.0. Reimplement the patterns with original Sleepy Hub code; do not copy upstream files/assets.
- The new UI must keep the BlackBox app-container purpose. No AI chat functionality.
- No code changes have yet been made on the new rebuild branch beyond branch creation.


## Implementation work started — 2026-11-13

Clean rebuild branch `sleepyhub/rikkahub-source-rebuild` is now being implemented from the app baseline. Latest recorded source commit at this checkpoint: `2eed812937ffd8d86d454e554c5e8f089f757a8c`.

Files added/rewritten so far:
- Compose-enabled `activity_main.xml` and Compose dependencies compatible with Kotlin 1.9.23.
- `SleepyHubTheme.kt`: global System/Light/Dark, dynamic color, accent, and AMOLED theme.
- `SleepyHubMainScreen.kt`: modal navigation drawer, RikkaHub-structured top bar, profile picker sheet, profile workspace background.
- `ProfileScreens.kt`: profile manager, profile detail/appearance, global settings.
- `ProfileAppearance.kt`: persistent global and per-profile appearance preferences.
- `AppGridScreen.kt` and Compose-based `AppsFragment.kt`: app grid and Material 3 action sheet/confirmations.
- `AppsRepository.kt` and `AppsViewModel.kt`: actual internal/external virtual cache clearing; removed forced-GC and heap-threshold icon suppression; stopped auto-deleting intentionally empty profiles.
- `MainActivity.kt` and `ViewPagerAdapter.kt`: virtual-user profile lifecycle and stable profile IDs.

**Validation state:** implementation is in progress. The ARM64 debug workflow has been triggered, but no successful build or device acceptance is claimed at this checkpoint. Resolve compiler/runtime issues before adding more UI features.


## Corrected container/profile architecture and audit — 2026-11-13

The user's clarified model supersedes earlier UI notes:
- RikkaHub Assistant maps to a Sleepy Hub **container**.
- RikkaHub conversation list maps to the container's inline, scrollable **virtual profile** list.
- The container picker is the compact assistant-style bottom sheet near the bottom of the drawer.
- A new container starts empty. It does not create a BlackBox user automatically.
- Container identity owns name, picture/avatar, and optional workspace background. A missing background means plain black.
- Theme is **global only**, following RikkaHub. There is no per-container or per-profile theme/accent.

Current rebuild branch: `sleepyhub/rikkahub-source-rebuild`, audited head `2ebae140fcd0c82c03321de648db00accaed5d98`.

Audit findings: the branch currently has a flat BlackBox-user list, no container model, profile-level accent/background storage, a profile picker sheet instead of an inline drawer list, no container avatar, manual string page state, and a default accent gradient. It is not yet the approved design.

Latest ARM64 debug workflow [43329277208](https://github.com/Cutie-Cookie/Sleepy-Hub/actions/runs/43329277208) failed during Kotlin compilation. No APK was produced. The errors are listed in [RIKKAHUB_SOURCE_AUDIT.md](RIKKAHUB_SOURCE_AUDIT.md). The branch has no app unit or instrumentation tests yet. No application code was changed during this audit; only documentation was updated. `main` remains untouched.


## Deeper RikkaHub source map and interaction decisions — 2026-11-13

See [RIKKAHUB_SOURCE_MAP.md](RIKKAHUB_SOURCE_MAP.md) for the file-by-file upstream index.

Confirmed source behavior: long-pressing a conversation row opens a menu with Move to assistant. The move sheet lists assistants; service/repository/DAO update the same conversation's assistant ID and clear its folder. Moving the active conversation also switches the selected assistant. Sleepy Hub must map this to Move Profile to Container while preserving the same BlackBox user and installed app data.

Confirmed UI placements:
- Add Profile: small plus beside the dashboard's inline profile-list heading.
- Add Container: top-bar plus in Container Manager.
- Container picker: compact assistant-style selector in the drawer.
- Container manager: dashboard footer and Settings home.
- Settings: dashboard bottom-right.
- Add App: workspace top-right action replacing RikkaHub's New Message action.
- No workspace top-right MoreVert menu.
- Storage rationale popup gets a Compose Material 3 redesign; Android All Files Access permission flow remains unchanged.
- App picker gets a real structural redesign, not a color repaint.
- Container delete shows Cancel/Delete confirmation. The impact on contained profiles/data must be clear; destructive cascade behavior is not yet authorized.

Only documentation changed in this audit. Application source remains unchanged and the latest ARM64 workflow remains failed with no APK.

## Device feedback and current source candidate — 2026-11-13

The user tested the previous successful preview APK and reported:

- Drawer/profile presentation: PASS, with a request for a plain white Google Play triangle.
- Profile switching and container list separation: switching works and app lists do not combine. Device identifiers appeared shared across profiles/containers; app-data isolation still needs a separate test.
- Empty container behavior: PASS; first profile name incorrectly used global numbering.
- Clone/container targeting: clone remained in its source container after switching away.
- Installed-app and external-APK installation: both PASS.
- App long-press: app also launched on release; fix committed.
- GMS UI: PASS, but profile scope appeared global; source calls install/uninstall with a user ID, so retest after identifier isolation.
- Fake location: CRASHED; exact failing step and trace still needed.
- Navigation/settings: Color mode text clipped and page transitions flashed white.

The latest source candidate adds persistent profile-scoped virtual identifiers, folder create/rename/delete and selection, New Profile defaults, long-press click suppression, one-minute idle auto-stop with an opt-out, neutral theme roles, fade-only transitions and an initial loading state. These changes are on the feature branch, not main, and remain build/device-unverified until run 43437776339 and phone retesting complete.
