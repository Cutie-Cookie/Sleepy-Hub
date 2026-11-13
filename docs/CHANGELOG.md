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


### 2026-11-13 — Detailed source-level UI audit

- Pinned the RikkaHub reference at commit `b10c168577482abed696669a58c0eb59f1eed270` and inspected `ChatPage.kt`, `ChatDrawer.kt`, `AssistantPicker.kt`, `AssistantPage.kt`, `AssistantDetailPage.kt`, `AssistantBasicPage.kt`, `Background.kt`, `BackgroundPicker.kt`, `Theme.kt`, and `SettingThemePage.kt`.
- Confirmed the phone drawer uses `ModalNavigationDrawer`, while large screens use `PermanentNavigationDrawer`; the drawer sheet is 300dp with compact 8dp spacing and an independently scrolling center list.
- Confirmed the active assistant selector is a compact drawer item that opens an approximately 80%-height `ModalBottomSheet`; picker rows use a 32dp avatar and trailing edit action.
- Confirmed assistant management uses `LargeFlexibleTopAppBar`, rounded search, tag filters, a reorderable list, and compact 20dp cards with 48dp avatars and 12dp vertical padding.
- Confirmed assistant details use a pinned top bar, animated title reveal, and grouped `CardGroup` sections.
- Confirmed assistant wallpaper is rendered behind the workspace with configurable opacity and a readability overlay; the picker supports preview, gallery selection, URL entry, and removal.
- Confirmed global theme behavior includes System/Light/Dark, dynamic color, preset/custom themes, and optional AMOLED black.
- Audited `sleepyhub/rikkahub-source-rebuild`: the Compose foundation and app grid are present, but the current shell still uses manual string-based page state and custom drawer/profile-list/detail styling. Those are recorded as gaps to correct against the upstream component hierarchy, not as a finished implementation.
- Added `docs/RIKKAHUB_SOURCE_AUDIT.md` and indexed it in `docs/README.md`.
- No application source files were changed in this documentation update. `main` remains untouched.


### 2026-11-13 — Strict RikkaHub fidelity clarified

- User explicitly instructed that RikkaHub's actual source code is the UI specification and that no UI/features should be invented beyond the agreed Sleepy Hub plan.
- Confirmed the app-manager workspace remains plain black by default. No added header box, banner, hero card, decorative artwork, or logo decoration above the app grid.
- Optional profile wallpaper is only behind the active profile workspace when deliberately selected; removing it restores plain black. No wallpaper thumbnails in the drawer or profile lists.
- Rejected the earlier custom wide/tall profile-card interpretation. Use RikkaHub's compact picker and management-list component patterns.
- Updated `UI_ROADMAP.md`, `UI_IMPLEMENTATION_PLAN.md`, and `RIKKAHUB_SOURCE_AUDIT.md` to make these hard requirements explicit.
- Documentation-only change; no application source code changed. `main` remains untouched.


### 2026-11-13 — Corrected container/profile ownership after user clarification

- Confirmed the intended mapping: RikkaHub Assistant = Sleepy Hub container; RikkaHub conversation = a BlackBox virtual Android profile inside that container.
- The profile list belongs inline in the drawer and scrolls independently. The compact assistant/container selector belongs near the bottom and opens the assistant-style bottom sheet.
- A new container starts empty. Switching containers filters the profile list and restores that container's last selected profile when returning. Add Profile creates a BlackBox user assigned to the selected container.
- Container identity settings are name, picture/avatar, and optional workspace background/opacity. The background is shown only behind the app-manager workspace; no image means plain black.
- Theme ownership is global only, following RikkaHub. Removed per-container/per-profile theme and accent from the approved plan. Profiles do not own pictures or wallpapers.
- Audited current rebuild branch head `2ebae140fcd0c82c03321de648db00accaed5d98`: it has no container entity, stores appearance by BlackBox user ID, puts profiles in a bottom sheet instead of inline in the drawer, and adds a default accent gradient. These are structural mismatches to correct before visual polish.
- Checked the latest ARM64 build [run 43329277208](https://github.com/Cutie-Cookie/Sleepy-Hub/actions/runs/43329277208): failed during Kotlin compilation; no APK artifact was produced. Errors include missing Compose imports, the wrong view-binding type in `ListActivity`, and a Compose call-context error there.
- Checked app-management callbacks: cache clearing targets the selected BlackBox user's internal and external cache directories; clear-data/uninstall use the selected user ID. No app tests currently exist under `app/src/test` or `app/src/androidTest`.
- Inspected RikkaHub's `Assistant.kt`, `ConversationRepository`, `ChatDrawerVM`, `AssistantPicker`, `AssistantPage`, `AssistantBasicPage`, and global theme source. The Assistant model has avatar/background fields but no theme field; global theme settings are stored separately.
- Updated `RIKKAHUB_RESEARCH.md`, `RIKKAHUB_SOURCE_AUDIT.md`, `UI_IMPLEMENTATION_PLAN.md`, and `UI_ROADMAP.md` to supersede the earlier incorrect flat-profile mapping.
- Documentation-only changes on `sleepyhub/docs-project-notes`. No app source code was changed; `main` remains untouched.


### 2026-11-13 — Deep RikkaHub interaction/source-map audit

- Confirmed the long-press conversation menu in `ConversationList.kt` includes Move to assistant. `ChatDrawer.kt` opens a bottom sheet listing assistants; `ChatVM.kt`, `ChatService.kt`, `ConversationRepository.kt`, and `ConversationDAO.kt` update the existing conversation's assistant membership and clear its assistant-scoped folder assignment. Moving the active conversation also selects the destination assistant.
- Mapped the exact Assistant picker, manager, add sheet, detail editor, avatar/background picker, Settings home, Preferences, Theme pages, and typed navigation routes. Added [RIKKAHUB_SOURCE_MAP.md](RIKKAHUB_SOURCE_MAP.md) with file-by-file paths and Sleepy Hub equivalents.
- Confirmed the entry points: Assistant manager from the dashboard footer and Settings → General Settings → Assistant; Assistant picker near the bottom of the drawer; edit from the picker row; Add Assistant from the manager top-bar plus; Settings from the dashboard bottom-right.
- Sleepy Hub mapping: profile long-press → Move to container; preserve the same BlackBox user ID and app data. Add Profile is a small dashboard plus; Add Container is in the Container Manager top bar; Add App replaces RikkaHub's top-right New Message action.
- Remove Sleepy Hub's top-right MoreVert menu; keep Settings at the dashboard bottom-right and Refresh in the dashboard footer.
- Audited the current storage permission rationale (`MaterialAlertDialogBuilder`) and planned a Compose Material 3 rationale popup while preserving the Android All Files Access permission intent and result check.
- Audited the current app picker: the existing UI needs a real hierarchy/interaction redesign, not just color changes. The plan calls for a LargeFlexibleTopAppBar, rounded search, compact app rows with icons/name/package/Add or Installed status, APK chooser, and loading/empty states.
- Confirmed the container delete popup must have Cancel/Delete and explain the impact on profiles/data. The cascade-vs-move policy remains unconfirmed; do not implement destructive deletion until decided.
- Documentation-only changes on `sleepyhub/docs-project-notes`. No app source code was changed; `main` remains untouched.
