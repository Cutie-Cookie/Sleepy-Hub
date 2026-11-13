# Sleepy Hub — living development plan

Last updated: 2026-11-13

This is the working roadmap and bug register for the RikkaHub-source-led Sleepy Hub rebuild. It is intentionally editable: newly reported bugs are appended, statuses change as evidence arrives, and phases may be reordered when a newly discovered blocker requires it.

## Project guardrails

- Work only on `sleepyhub/bugfix-install-switch-polish` (or a clearly documented successor feature branch).
- Never merge into or push to `main` without explicit permission. At the time of this update, `main` remains at `89b59836c66f173756a4ae258cf379a957649820`.
- Keep the known-good baseline `sleepyhub/identity-icons-no-vpn` / `6de07c536a013febe6432148dd23f94eec1bac30` available.
- Protect existing BlackBox user/profile data. Do not ask the user to uninstall the existing app as a casual workaround.
- A separate Preview application ID is a test-installation safety measure, not a reduced-feature/demo scope. Preview builds must retain the full app implementation.
- Keep a persistent preview signing identity so preview APKs can update one another. Verify package ID, label, certificate and ABI in CI before sharing an APK.
- Treat a successful compile as build verification only. It does not prove runtime behavior, installation success, visual parity, or device compatibility.
- Follow the pinned RikkaHub source flows and visual language; do not invent unrelated UI. Preserve Sleepy Hub's own identity and respect the source project's license.
- Keep the workspace black by default. Theme is global. Per-profile name, avatar and workspace background are retained; no per-container/per-profile theme or accent controls.
- Do not add postponed features such as background-running apps, VPN, or remote log uploads. The user has now approved a one-minute idle auto-stop for virtual apps with a Settings opt-out; do not add a keep-alive/background-running mode.

## Status vocabulary

| Status | Meaning |
|---|---|
| Reported | User observed it; cause not yet traced |
| Investigating | Source/runtime path being inspected |
| Ready | Root cause understood and fix scoped |
| Fixing | Source changes are in progress |
| Build verified | Relevant build/automated checks pass |
| Device testing | Needs confirmation on a real target device |
| Done | Expected behavior has been confirmed; a compile alone is insufficient |
| Blocked | Needs missing evidence, environment access, or a user/device reproduction |

Priority: **P0** = blocks basic use or risks data; **P1** = major feature/UX mismatch; **P2** = smaller polish or edge case.

## Current working snapshot

- Feature branch: `sleepyhub/bugfix-install-switch-polish`
- Current feature-branch head: `2905dac7b5800d4e374f41863bb03e5c3903d394`.
- Current-head CI run: [43459349834](https://github.com/Cutie-Cookie/Sleepy-Hub/actions/runs/43459349834) — **success**; ARM64 debug build, preview package identity, APK signature, ABI verification and artifact upload all passed.
- Previous candidate run [43451229123](https://github.com/Cutie-Cookie/Sleepy-Hub/actions/runs/43451229123) failed Kotlin compilation because `selectedFolder` was scoped inside the drawer branch while folder dialogs referenced it outside. Commit `c7b28cd` moves that state to the screen-level scope; corrected build is green.
- Current artifact: `sleepy-hub-arm64-preview-debug`, ID `14056805069`, from run `43459349834`; ZIP size 24,277,393 bytes; ZIP SHA-256 `0e68561e3c004ee28c3957cce4fbc3d33a7cf64ab042505ece99749fb0fb8960`.
- APK: `SleepyHub_4.0.0-preview_arm64-v8a-debug.apk`, 26,097,990 bytes; package `com.cutiecookie.sleepyhub.preview`, label `Sleepy Hub Preview`, version `4.0.0-preview`, ARM64-v8a. APK SHA-256: `448ca06eadb1015a78017f85e0e6c2fbedce2be65959289021bbf17104d7a4f9`.
- CI verified APK v1/v2 signing, certificate SHA-256 `cbd3ce49b15de6f54b8b191f8e99e8dcd6610e6f6895ca7f5d522d855c009f20`, package ID/label and the `arm64-v8a/libblackbox.so` library. This remains a debug preview, not a production release.
- The APK archive passed ZIP integrity validation after download. CI verified APK v1/v2 signing, preview package/label and the `arm64-v8a/libblackbox.so` library. This is a debug preview build, not a production release.
- The older artifact ID `14046061691` is historical only; use artifact `14052746172` for this test session.
- Current CI verified package ID com.cutiecookie.sleepyhub.preview, label Sleepy Hub Preview, APK signature and ARM64 native-library presence. Preview uses the .preview application ID suffix and persistent preview debug signing identity; it is intended to install alongside the regular app.
- The preceding run [43423100800](https://github.com/Cutie-Cookie/Sleepy-Hub/actions/runs/43423100800) failed during `:app:mergeDebugResources` because of fake-location string resource parsing/formatting. The correction commits `5a2084aa6aee2700eafc67e8be81169c9bc1d3a3`, `a69ca35a1d01ac9b792f7abec58e50413caa592a`, and `cd917c58c7d4f8baa521f6839eafdb318cb40fa7` fixed the English and Chinese positional placeholders and simplified the English failure strings. The corrected exact head now passes CI.
- The thirteen earlier feature/resource commits through cd917c5 were build-verified by run 43424012176. Commits 28443fd7dfaebaf774935b7c6f601345b7535c0e and 6df0bbcc031bace16bb2461c4963ea23520277ea passed run 43437776339. The cross-process auto-stop opt-out hardening in commit 0fea772e8f7acc8e84c1ce244ca1c995f294db02 passed run 43439814552.
- Earlier successful builds remain historical only: [43419141589](https://github.com/Cutie-Cookie/Sleepy-Hub/actions/runs/43419141589) for `dae5f468ad1fdf52dee22d3572de37f7a9af619c`, and [43417371053](https://github.com/Cutie-Cookie/Sleepy-Hub/actions/runs/43417371053) for `046e04bb15ab328f531427a6ea64b9d9dba896fb`. Do not use their APKs for current-head testing.
- Previous head `81599d812db623a5f744d64ff69664b4507f8363` passed run `43449856130`. Current source head `e4bb695838b47677cdc41628f7e3999b0f7ea3b2` is pending run `43451229123`.
- `main` remains untouched at `89b59836c66f173756a4ae258cf379a957649820`.

## Next phone test session — current candidate

**Use the new APK from run [43459349834](https://github.com/Cutie-Cookie/Sleepy-Hub/actions/runs/43459349834), artifact `14056805069`.** It is labelled **Sleepy Hub Preview** with package ID `com.cutiecookie.sleepyhub.preview`. Install alongside the regular app; do not uninstall the regular app or clear its data.

### Immediate regression checks for commit 2905dac

1. **App launch first:** open a harmless installed app from the workspace. Confirm its real UI appears instead of a black screen. Repeat with one other harmless app if the first works.
2. **App actions and reorder:** tap once to launch; long-press the app icon or label to open the action sheet without launching on release; drag using the separate 24dp grip to reorder.
3. **Fake location with GPS off:** configure a harmless test app, turn the device GPS/location toggle off, and check whether the app sees the fake point. Repeat with GPS on. If it still requires GPS, report which app/API you tested; do not delete the feature.
4. **Visual checks:** confirm Color mode is no longer clipped, the folder row has a small gap above profiles, and the Play badge is only the multicolour triangle on transparency.

Then continue with the remaining regression checks below.

### Test in this order

1. **Profile identity isolation**
   - [ ] In Mantle Verify, compare Profile A and Profile B: Android ID, GSF Android ID and Wi-Fi MAC should all differ.
   - [ ] Restart Preview and check each profile keeps its own three values.
   - [ ] Shared Wi-Fi SSID/BSSID is expected and is not a failure.
2. **App-data isolation**
   - [ ] Use the same harmless test app in A and B. Save a different harmless note/setting in each profile.
   - [ ] Switch A → B → A and restart Preview. Each profile must retain only its own value.
3. **Folder controls**
   - [ ] Confirm All, General and custom-folder chips appear in the drawer.
   - [ ] Use Add to create a folder; long-press a custom folder chip to rename it or delete it.
   - [ ] Move a disposable profile using the folder bottom sheet. The selected folder should show a checkmark.
   - [ ] Delete a test folder and confirm its profiles move to General while their apps/data remain intact.
4. **App tap, long-press and reorder**
   - [ ] A normal tap launches the app.
   - [ ] A long-press opens the app action flow and must not launch the app when released.
   - [ ] Use the separate grip to reorder; confirm the order remains after leaving and reopening the workspace.
5. **Fake location**
   - [ ] Open Fake Location, choose a harmless test app, open the map and select a location.
   - [ ] Apply it, verify the intended app/profile receives it, then disable it and confirm the override is removed.
   - [ ] If it crashes, stop there and send the exact step plus a screenshot/error dialog; do not keep retrying.
6. **Google services scope**
   - [ ] Enable Google services for A only. B should remain disabled.
   - [ ] Switch between profiles and restart Preview to confirm the state stays separate.
7. **Idle auto-stop**
   - [ ] With the setting enabled, leave a virtual app and wait about one minute; check that it stops.
   - [ ] Turn the setting off and repeat once; the app should not be auto-stopped by this policy.
8. **Quick visual regression**
   - [ ] Check the Play badge is the original multicolour Play icon.
   - [ ] Check whether any Settings controls are still clipped. A screenshot of the exact page is most useful.

### Feedback format

Report each result as **Pass**, **Fail**, or **Not tested**. For a failure, include the step number, what you tapped, what you expected, what happened, and the exact error text. Hide personal app names, account details and precise location information in screenshots.

## Phase 0 — project safety and repeatable builds

1. Keep feature work isolated from `main`.
2. Preserve the baseline and user data.
3. Make the preview package/signing identity reproducible.
4. Make CI verify package name, app label, signature and ARM64 native libraries.
5. Record every build's commit, workflow, artifact, size and SHA-256 when available.

**Exit criteria:** a repeatable, verifiable preview APK can be produced without replacing the user's existing installation or touching its data.

**Status: Complete.** The protected branches, preview package/signing identity, and repeatable CI verification are in place.

## Phase 1 — core behavior and high-priority bug fixes

Work through the P0 bugs first. For each one: reproduce or trace it, identify the owning code path, make the smallest correct fix, build, then test the exact reproduction path.

1. Profile switching and switching back.
2. Container switching, including empty containers and returning to a previous container.
3. App installation from the installed-app picker and from an external APK.
4. Profile creation, cloning, deletion, moving between containers, reordering and persistence.
5. App launch, uninstall, clear-data/cache and app-info actions.
6. Google services profile state and app-grid filtering.
7. Fake-location selection, application, per-profile/per-app scope, and disable/restore behavior.

**Exit criteria:** core operations work across at least two profiles and two containers (including an empty container), with no profile/data mix-up and a confirmed successful app installation.

**Status: Active.** Identity interception/cache clearing, per-profile Wi-Fi MAC, folder controls, long-press/reorder separation, original Play icon loading and fake-location hardening are committed. The current ARM64 build is [43452740407](https://github.com/Cutie-Cookie/Sleepy-Hub/actions/runs/43452740407); all runtime behavior still requires device retesting.

## Phase 2 — source-led RikkaHub UI reconstruction

**Status: Deferred for the dedicated Phase 2 pass; not signed off.** The user has explicitly asked to keep the full UI overhaul in Phase 2. Phase 1 may include only the smallest UI wiring needed to correct a functional bug (such as real folder create/rename/delete/move controls); do not expand into broad screen redesign, logo/icon/frame replacement, spacing/typography sweeps, or animation polish during Phase 1.

Audit and implement one screen/flow at a time against the pinned upstream source, not merely by recoloring the old interface.

1. Main shell, drawer, profile list and bottom container selector.
2. Compact profile picker and add-profile interaction.
3. Profile manager: search, folders/tags, sorting, reorder, clone, edit, delete and long-press actions.
4. Profile detail/basic/background pages.
5. Global settings and theme navigation.
6. App picker and app manager.
7. Google services manager and profile Play indicator.
8. Fake-location page.
9. Storage permission rationale and other system-facing UI.
10. Empty states, dialogs, bottom sheets, menus, loading/error states and accessibility/touch targets.

For every screen compare: hierarchy, top/bottom bars, dimensions, spacing, shapes, typography, icon style, selected/pressed states, scroll behavior, transitions and action placement. Avoid arbitrary decorations, extra header boxes, giant profile cards, or popup behavior that is not in the reference flow.

**Exit criteria:** every relevant screen and interaction has been source-compared and the user has reviewed target-device screenshots.

## Screen-by-screen RikkaHub visual parity matrix

This matrix separates source-code presence from actual visual acceptance. A screen is not considered visually complete just because a Compose implementation exists.

| Sleepy Hub screen / flow | Pinned RikkaHub reference | Current source observation | Parity status / next work |
|---|---|---|---|
| Main workspace shell | `ChatPage.kt` | Black workspace, compact transparent top bar, ViewPager2 profile content, Add App action, drawer. | Structural mapping present; compare exact toolbar height, title/icon treatment, insets, and scroll behavior on device. |
| Dashboard drawer | `ChatDrawer.kt` | 300dp sheet, 50dp profile header, quick actions, folder chips, profile sort, scrollable rows, bottom container selector and settings footer. | Main structure mapped; refine details only against source screenshots and actual phone rendering. |
| Container picker | `AssistantPicker.kt` | 80%-height bottom sheet, selected row uses primaryContainer, trailing edit action. Current row uses a 34dp avatar and two text lines (name + profile count). | Close structurally; compare against upstream compact 32dp avatar and one-line name row. Remove any secondary metadata only if it does not serve the container mapping. |
| Profile manager | `AssistantPage.kt` | Large flexible top bar, search, folder chips, reorderable list, 48dp avatar, 20dp rounded row cards and trailing actions. | Major source patterns present; compare row padding, menu placement, elevation, sorting and reorder animation. |
| Profile detail/basic/background | `AssistantDetailPage.kt`, `AssistantBasicPage.kt`, `BackgroundPicker.kt` | Separate detail, basic and workspace-background pages with grouped settings sections. | Navigation exists; compare section order, top-bar collapse, preview, image selection/removal and transitions. |
| Global theme | `Theme.kt`, `SettingThemePage.kt` | Global system/light/dark mode, dynamic color, global accent and AMOLED true black. No profile/container theme controls. | Ownership is correct; compare palette controls, presets and theme preview behavior. |
| Profile workspace background | `Background.kt` | Per-profile image is rendered behind the app grid, not in profile rows or container picker. | Mapping implemented; verify opacity, image crop and black fallback on device. |
| App grid / app manager | Sleepy Hub-specific adaptation | Reorderable app grid, empty state, launch and app actions. | Source-specific behavior exists; detailed spacing, icon sizing, context actions and animations still need review. |
| Installed-app / APK picker | Sleepy Hub-specific adaptation | Compose search/list screen and external APK import. | Structural implementation exists; test both install routes and compare row density, toolbar and loading/error states. |
| Google services manager | Sleepy Hub-specific adaptation | Compose profile rows, per-profile switch and Play badge; GMS packages are filtered from the app grid. | Matches requested behavior in code; verify that no Google service package appears as a normal workspace app. |
| Fake location | Sleepy Hub-specific adaptation | Compose app list, map flow, explanation and disable action. | Instructions updated; verify layout, actual location override and restore behavior. |
| Storage permission rationale | Sleepy Hub-specific adaptation | Compose rationale dialog and Android settings handoff. | Needs target-device review for exact wording, system return flow and safe insets. |
| Motion and system bars | Cross-cutting | AnimatedContent page transition (340ms fade-in / 300ms slide-in), animated list placement/color, transparent navigation bar and safe-drawing insets. | Code is present; compare easing/duration and verify gesture + three-button navigation without obscured controls. |

## Phase 3 — motion, insets and visual finish

**Status: Pending final pass.** Profile/container sweeping has been removed, but the user still wants more natural motion and reports some settings clipping.

1. Use deliberate, smooth RikkaHub-like transitions; avoid near-instant or missing animations.
2. Animate list placement and state changes consistently.
3. Make the system navigation bar transparent while preserving safe insets. No content or buttons may be hidden behind system navigation controls.
4. Verify bottom sheets, dialogs, keyboard/IME behavior, edge-to-edge layout and gesture/three-button navigation.
5. Refine icons, logos, frames, shapes, spacing and typography consistently across all screens.

**Exit criteria:** transitions feel intentional and all interactive controls remain visible and tappable on the target phone.

## Phase 4 — regression and acceptance

- Re-run every registered bug reproduction after relevant changes.
- Test profile/container persistence after app restart.
- Test switching from populated to empty containers and back.
- Test add/launch/remove app flows and GMS/fake-location interactions.
- Test system navigation insets with the phone's navigation mode.
- Compare the drawer, picker, manager, settings, app picker, GMS and fake-location screenshots against RikkaHub.
- Record device model, Android version, reproduction steps and evidence for any remaining issue.

**Exit criteria:** all P0 bugs are Done; P1 bugs are Done or explicitly accepted as known limitations; no regression is left unrecorded.

## Phase 5 — final delivery

- Produce the intended final package identity and release signing configuration.
- Verify signature, package ID, version, ABI and APK integrity.
- Test upgrade/install behavior without data loss.
- Publish a concise change log, test record, known limitations and APK checksum.

Do not call the app complete or claim 100% parity until the acceptance checks above are actually passed.

## Living bug register

| ID | Priority | Issue / expected behavior | Current status | Evidence, fix and next check |
|---|---|---|---|---|
| BUG-001 | P0 | Profile switching must select the matching BlackBox page and persist correctly, while each profile exposes its own virtual identifiers and data. | Switching/list separation passed; identity isolation failed | User confirms A↔B switching works and app lists do not combine. Mantle Verify shows identical Android ID, GSF ID and Wi-Fi MAC in A/B; IMEI, hardware serial and App Set ID differ. New Settings-provider/GSF-provider interception, cache clearing and per-profile Wi-Fi MAC are committed at `6492d84` + `f3d12dd`; CI/device verification pending. App-data isolation still needs its own test. Source review confirms `BEnvironment.getDataDir()` and `getExternalDataDir()` include the BlackBox user ID, and `IOCore` redirects each virtual user to that user's data directory; this is source-level evidence, not a substitute for the on-device note/setting test. |
| BUG-002 | P0 | Container switching must keep profile lists scoped and restore the previous selection. | Container/list behavior passed; naming fix pending build | User confirmed the empty container remained empty and profile lists did not combine. Its first profile was incorrectly named Profile 3; see BUG-020. |
| BUG-003 | P0 | Adding apps must work through both the installed-app picker and external APK chooser. | Device test passed; regression check pending | User reports both installed-app picker and external APK paths PASS. Retest after the current core changes. |
| BUG-004 | P1 | Google services must remain scoped to the selected profile and not appear as ordinary app tiles. | UI passed; profile scope needs retest | User is unsure whether GMS enablement is truly profile-scoped. Source calls install/uninstall with the selected BlackBox user ID; repeat the A-only/B-off test after the identifier fixes are installed. |
| BUG-005 | P1 | Navigation bar appears as an opaque bar; user wants transparent but all controls must remain visible and tappable above the system navigation area. | Code-level changes present; device testing | Theme code sets a transparent navigation bar and disables decor fitting; audit every bottom screen for safe insets. Test gesture and three-button navigation; never solve transparency by placing controls behind system buttons. |
| BUG-006 | P1 | Animations should be smooth and restrained, with no sweeping profile/container transitions. | Source polish committed; device review pending | Current candidate replaces page slides with a short fade and adds a labeled initial app-loading state. Profile selection now snaps. Verify on device. |
| BUG-007 | P1 | UI should follow the pinned RikkaHub source rather than an approximate preview. | User reports major improvement; final polish remains | User now describes the UI as amazing. Remaining concrete concerns are color leakage, clipped Color mode text and white transition flashes; see BUG-024. Do not claim 100% parity. |
| BUG-008 | P1 | Fake location must apply only to the selected virtual app/profile and restore real location when disabled. | Selecting an app crashes; source hardening committed, device retest pending | User narrowed the crash to selecting an app in the fake-location list. The map path now declares INTERNET/ACCESS_NETWORK_STATE, uses an app-private osmdroid tile cache and user agent, guards initialization and detaches the map safely. The exact crash trace is still unknown; verify on device and collect the Android crash dialog/logcat if it persists. |
| BUG-009 | P1 | Further UI and functional bugs are expected; initial report is incomplete. | Open intake | Append every new report as the next BUG ID. Do not close or overwrite earlier issues without evidence. |
| BUG-010 | P1 | Profile manager and fake-location lists, plus the Google-services empty state, can request the full screen height underneath earlier content in a Column. | Build verified; device testing | Static audit found `fillMaxSize()` used for vertically stacked children. Commit `dae5f46` changes those children to consume only the remaining height with `weight(1f)`. CI run 43419141589 passed build, APK identity/signature/ABI verification and upload. Source review confirms the affected list/empty-state branches are direct children of Columns. Test scrolling, keyboard/search behavior, and empty states on device. |
| BUG-011 | P1 | Drawer profile rows are too tall and show a profile picture plus an app-count subtitle; RikkaHub's conversation rows are compact, text-only, single-line rows. | Build verified; device testing | Compared against pinned upstream `ConversationList.kt` at `b10c168`. Commit `6a11fed` removes the row avatar and app-count subtitle, reduces vertical padding to 6dp, uses the source's elevated selected surface, and keeps only the small Play/pin indicators. Verify the new commit builds and compare the drawer on the target phone. |
| BUG-012 | P1 | Cloning must remain attached to its source container if the user switches containers during cloning. | Clone target behavior passed; identity isolation open | User confirmed the clone stayed with its own container after switching away. Matching device identifiers remain open under BUG-018; app-data isolation still needs a separate test. |
| BUG-013 | P1 | Profile creation and cloning independently allocate `max(userId) + 1`; concurrent requests can both receive the same ID because BlackBox's `createUser(id)` returns the already-existing user when that ID is taken. | Build verified; device testing | Commit `bf6752f` adds a process-wide lock shared by profile creation and cloning, covering both ID allocation and `createUser`. Verify the build and test rapid/double create or clone actions on device. |
| BUG-014 | P1 | A delayed `loadContainerProfiles` callback unconditionally clears `pendingPagerUserId`; if a newer profile selection happens before that callback runs, the older refresh can erase the newer selection guard. | Build verified; device testing | Commit `168d15b` clears the pending ID only when it still matches that refresh's selected profile. Verify rapid container/profile switching and swipe-back behavior on device. |
| BUG-015 | P1 | Fake-location apply/disable must report the real Binder result, not optimistic success. | Source fix on prior head; latest device flow crashed | Earlier changes wait for Binder completion and verify stored mode/coordinates. Latest device test crashed somewhere in the fake-location flow; see BUG-023 and keep this open until the failing step is known. |
| BUG-016 | P1 | Google-services failures must produce an explicit result and refresh the selected profile state. | Source fix on prior head; device scope retest pending | Exception reporting is implemented. User reports the UI PASS but questions whether it is truly profile-scoped; verify after BUG-018. |
| BUG-017 | P0 | ARM64 preview build must pass resource merge, identity/signature/ABI verification and artifact upload. | Prior head passed; current head building | Previous head `0fea772` passed run `43439814552`. Current head `81599d81` is being checked by run `43449856130`; no APK for the latest fixes is ready until that run succeeds. |

## Device test report — 2026-11-13

| Test | User result | Recorded observation |
|---|---|---|
| 1. Drawer appearance | PASS with icon correction pending | Compact drawer/profile appearance accepted. User clarified the badge must be the original multicolour Play triangle, not a white hand-drawn triangle; source now loads the real Play Store adaptive foreground. |
| 2. Profile switching and folders | FAIL for folders; switching PASS | A↔B switching works and app lists do not combine. The folder UI was still a text field with no manager/picker; new manager and selection picker are committed under BUG-019. Android/GSF IDs and Wi-Fi MAC were shared; see BUG-018. Storage isolation is not yet proven. |
| 3. Empty container and first profile | PASS with issue | Empty container behavior is correct, but its first profile was named Profile 3; see BUG-020. |
| 4. Clone/container ownership | UNSURE | Clone stayed with its own container after switching away. Same identifiers appear across profiles/containers; storage still needs a separate test. |
| 5. Rapid profile creation/cloning | UNSURE | User cannot give a clean pass while identity isolation is unresolved; keep concurrency/unique-ID regression coverage. |
| 6. App installation | PASS with long-press failure | Installed-app picker and external APK paths both passed. Long-press still launched on release; a separate gesture and reorder-handle fix is committed under BUG-021. |
| 7. Google services | PASS with scope concern | UI flow passed, but it appeared to affect every profile. Source targets a specific user ID; re-test after BUG-018. |
| 8. Fake location | CRASHED when selecting an app | The failing action is now known. Map/network/cache hardening is committed, but no crash trace is available yet; see BUG-023. |
| 9. Scrolling/search/navigation | PASS with polish remaining | User accepts the navigation behavior and no longer reports the sweeping transition. Some Settings content is still clipped and animations feel slightly off; keep visual polish open under BUG-024/025. |

## Next device retest order

1. Wait for run `43449035599` to pass. Install only its Preview artifact alongside the regular app; do not uninstall the regular app or clear data.
2. Compare Android ID, GSF Android ID and Wi-Fi MAC across profiles. Each must differ between profiles and remain stable after closing/reopening the Hub. The same SSID and access-point BSSID are expected on the same Wi-Fi network.
3. Save a note/setting in the same test app in Profile A. Profile B must not see it; this separately verifies app-data isolation.
4. Open Profile Manager → folder icon. Create, rename and delete a folder. Move a profile using the folder picker, not typed text. Deleting a folder should return its profiles to General without deleting apps/data.
5. Create the first profile in an empty container and confirm its label is New Profile.
6. Long-press an app: the action sheet should open without launching it. A normal tap should launch it; use the small grip to reorder.
7. With auto-stop enabled, return to Hub and reopen before 60 seconds; it should remain running. Wait over 60 seconds and confirm it stops. Turn the setting off and verify it remains running.
8. Enable GMS only for Profile A, then inspect Profile B and another container; verify package state and switch are independent.
9. Open Fake Location and select an app. Confirm the map opens, apply a test point, then disable it. If selecting the app still crashes, capture the Android crash dialog or logcat instead of repeatedly retrying.
10. Check the original multicolour Play triangle, remaining Settings clipping, monochrome theme and the feel of profile/container/page animations.

| BUG-018 | P0 | Different virtual profiles and containers expose the same Android ID, GSF Android ID and Wi-Fi MAC. | Root cause traced; fix committed, CI/device verification pending | The Settings provider path was returning the host Android ID because the earlier hook was not active; the GSF `gservices` provider query passed through unchanged; Wi-Fi MAC was hard-coded. Commits `6492d84` and `f3d12dd` add per-user persisted Android/GSF IDs, intercept both provider paths, clear the framework's cached `android_id`, and return a per-profile locally administered Wi-Fi MAC. Re-test uniqueness and stability. |
| BUG-019 | P1 | Folder organization needs create/delete/rename and a picker for moving profiles instead of typed names. | UI fix committed; current CI/device verification pending | The user's latest test correctly found the previous UI still had a free-text folder field; the earlier implementation only had repository methods. Commits `30482a1`, `09f2bed` and `b26022f` wire a container-scoped folder manager with create/rename/delete, a selection-based move picker and existing-folder chips in profile editing. Deleting a folder returns its profiles to General without touching apps/data. |
| BUG-020 | P2 | First profile in an empty container is named Profile 3 because numbering is global. | Implemented in source; current CI/device verification pending | Commit 28443fd changes the unsaved/default profile label to New Profile; user-defined names remain unchanged. |
| BUG-021 | P1 | Long-pressing an app must open its action menu without launching; apps must remain reorderable. | Build verified; device retest pending | User confirms release no longer launches the app, but the long-press menu did not appear and the small grip did not permit reorder. Commit `2905dac` adds explicit click/long-click handlers to the AndroidView icon and enlarges the separate grip to 24dp. Re-test tap, long-press menu, and drag from the grip. |
| BUG-022 | P1 | Manual Stop action does nothing; inactive apps should stop after one minute, with an option to disable auto-stop. | Build verified; device testing pending | Commit 28443fd removes the ineffective Stop action and adds a 60-second background idle-stop hook. Commit 0fea772 mirrors the toggle to a file so virtual processes see changes immediately rather than relying on stale SharedPreferences caches. Run 43439814552 passed; test timeout, resume-before-timeout and toggle-off behavior on device. |
| BUG-023 | P1 | Fake location must work when the device GPS toggle is off, while remaining scoped to the selected virtual app/profile; the earlier app-selection crash must also stay covered. | Build verified; device retest pending | The latest device report says target apps only see the fake location when device GPS is on. Android 13 exposes `isLocationEnabledForUser` and `registerLocationListener`; the previous proxy did not intercept either. Commit `2905dac` adds those hooks plus unregister/provider-state coverage and fixes a duplicate `getProviderProperties` call. Keep Fake Location; test with GPS both off and on. Timezone handling is a separate later task and must not replace this feature. |
| BUG-024 | P1 | Settings text must not be clipped, and folder controls must have a small visible gap before the profile rows. | Build verified; device retest pending | The latest screenshot shows the Color mode label clipped at the left edge and the folder-chip row touching the first profile row. Commit `2905dac` adds 14dp horizontal padding to the Color mode label/chips and a restrained 6dp gap below the folder row. Recheck the exact screenshot after the new build; keep broad animation polish in Phase 3. |
| BUG-025 | P2 | Profile/container switching sweeps across the screen, especially noticeable with separate backgrounds. | Sweep removed; visual polish remains | User confirms the sweep issue is now PASS. Keep only the remaining subtle motion/transition feel as Phase 3 polish; do not reintroduce horizontal sweeps. |

| BUG-027 | P0 | Tapping an installed virtual app opens a black screen instead of the app. | Build verified; device retest pending | The latest device test reports that apps do not open. The optional `LauncherActivity` splash wrapper is an extra asynchronous hand-off; commit `2905dac` bypasses it and moves the direct BlackBox launch request to a background thread with package/profile logging. This is a targeted hypothesis, not a confirmed runtime root cause. Test several harmless apps; if the screen remains black, capture the exact app and any visible error so the activity/process path can be traced further. |

## Bug intake and update rules

- New screenshots, recordings, logs or reproduction steps are appended to this register with the next available ID.
- Preserve the user's exact symptom and expected behavior; do not reinterpret a report as fixed because a nearby code path changed.
- Update status only when supported by source inspection, CI output or device evidence.
- When one fix touches another feature, add regression checks to both issues.
- Keep a short implementation note with each code commit and update this plan after meaningful changes.
- If a user reports more bugs while a phase is underway, record them immediately, triage by severity, and adjust the order without losing the current work.
- Ask the user for a device reproduction only when source/CI evidence cannot establish the result; do not make them repeat information already supplied.

## Reference and license notes

- RikkaHub upstream: https://github.com/rikkahub/rikkahub
- Pinned source commit: `b10c168577482abed696669a58c0eb59f1eed270`
- Detailed source mapping and implementation audit: [RIKKAHUB_SOURCE_AUDIT.md](RIKKAHUB_SOURCE_AUDIT.md)
- Preserve the license obligations of any upstream code used. Where the implementation is independently re-created, use the source as a behavioral/layout reference without copying protected branding/assets.

| BUG-026 | P1 | The profile Google Play badge must show only the multicolour Play triangle, on transparency, aligned and filling its allowed space. | Build verified; device retest pending | The latest screenshot shows the loaded Play Store icon still has a white square with a tiny triangle. Commit `2905dac` replaces that package-icon rendering with a transparent Canvas vector using the four Play colours, sized to fill the existing badge bounds. Verify alignment and scale on device. |

### RikkaHub FolderBar alignment

- Inspected the pinned upstream `ChatDrawer.kt` implementation. Commit `e4bb695` adds an Add chip, long-press Rename/Delete menus on folder chips, and a folder-selection bottom sheet for moving profiles. This is the same interaction pattern rather than a separate hidden folder settings screen.
- Core identity, folder-manager, gesture, original-icon and fake-location source passed CI in run `43449856130`; the final drawer refinement is pending run `43451229123`.


### 2026-11-13 — First device retest of the 43452740407 APK and follow-up fixes

The user tested the successful build from run [43452740407](https://github.com/Cutie-Cookie/Sleepy-Hub/actions/runs/43452740407) and reported:

- **P0 app launch failure:** tapping installed apps opens a black screen. Tracked as BUG-027.
- **App action/reorder partial failure:** long-press no longer launches on release (improvement), but the action menu is missing and the reorder grip was not usable. Updated BUG-021.
- **Spacing/clipping:** the Color mode label is clipped and the folder chips touch the first profile row. Updated BUG-024.
- **Play badge:** the actual package icon still has a white square with a tiny triangle. Updated BUG-026.
- **Fake location:** apps only see the fake point while the device GPS toggle is on. Preserve the feature; Android 13 API coverage is being improved. Timezone handling is a separate later investigation. Updated BUG-023.

Follow-up source commit: 2905dac7b5800d4e374f41863bb03e5c3903d394.

- Bypasses the optional LauncherActivity splash wrapper and runs the direct launch request off the UI thread.
- Adds explicit icon tap/long-press handlers and enlarges the separate reorder grip to 24dp.
- Adds 14dp horizontal padding to the Color mode label/chips and a restrained 6dp gap below the folder chips.
- Replaces the package-icon square with a transparent multicolour Play triangle vector.
- Adds Android 13 isLocationEnabledForUser and registerLocationListener/unregister handling, makes provider state conditional on fake mode, and fixes the duplicate provider-properties call.

CI run [43459349834](https://github.com/Cutie-Cookie/Sleepy-Hub/actions/runs/43459349834) passed. Artifact `14056805069` is the current candidate; the APK SHA-256 is `448ca06eadb1015a78017f85e0e6c2fbedce2be65959289021bbf17104d7a4f9`. No runtime fix is claimed until the user retests on the phone. Phase 2 remains deferred; these are targeted Phase 1 corrections only. Main remains untouched.
