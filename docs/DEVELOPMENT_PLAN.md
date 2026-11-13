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
- Current feature-branch head: 0fea772e8f7acc8e84c1ce244ca1c995f294db02.
- Current-head CI run: [43439814552](https://github.com/Cutie-Cookie/Sleepy-Hub/actions/runs/43439814552) — **passed**. ARM64 debug build, preview identity/signature/ABI verification and artifact upload all succeeded.
- Current verified artifact: sleepy-hub-arm64-preview-debug, artifact ID 14046061691, created 2026-11-13 14:12:29 UTC; expires 2026-11-20 14:12:27 UTC. Archive size: 24,229,676 bytes. Archive SHA-256: 8f5e4914278612dafb35ffd531548bb1500b3f852b809058eb6c37a8ea4f973b.
- Current extracted APK: SleepyHub_4.0.0-preview_arm64-v8a-debug.apk, 26,050,469 bytes. APK SHA-256: a14390801ff923333e0414d11c569a4c46308613a41cda7b454cb6299bea08cf.
- Current CI verified package ID com.cutiecookie.sleepyhub.preview, label Sleepy Hub Preview, APK signature and ARM64 native-library presence. Preview uses the .preview application ID suffix and persistent preview debug signing identity; it is intended to install alongside the regular app.
- The preceding run [43423100800](https://github.com/Cutie-Cookie/Sleepy-Hub/actions/runs/43423100800) failed during `:app:mergeDebugResources` because of fake-location string resource parsing/formatting. The correction commits `5a2084aa6aee2700eafc67e8be81169c9bc1d3a3`, `a69ca35a1d01ac9b792f7abec58e50413caa592a`, and `cd917c58c7d4f8baa521f6839eafdb318cb40fa7` fixed the English and Chinese positional placeholders and simplified the English failure strings. The corrected exact head now passes CI.
- The thirteen earlier feature/resource commits through cd917c5 were build-verified by run 43424012176. Commits 28443fd7dfaebaf774935b7c6f601345b7535c0e and 6df0bbcc031bace16bb2461c4963ea23520277ea passed run 43437776339. The cross-process auto-stop opt-out hardening in commit 0fea772e8f7acc8e84c1ce244ca1c995f294db02 passed run 43439814552.
- Earlier successful builds remain historical only: [43419141589](https://github.com/Cutie-Cookie/Sleepy-Hub/actions/runs/43419141589) for `dae5f468ad1fdf52dee22d3572de37f7a9af619c`, and [43417371053](https://github.com/Cutie-Cookie/Sleepy-Hub/actions/runs/43417371053) for `046e04bb15ab328f531427a6ea64b9d9dba896fb`. Do not use their APKs for current-head testing.
- Current source head 0fea772e8f7acc8e84c1ce244ca1c995f294db02 is build-verified by run 43439814552. Device behavior, storage isolation, GMS scope, fake-location stability and visual parity still require phone testing.
- `main` remains untouched at `89b59836c66f173756a4ae258cf379a957649820`.

## Next phone test session — current candidate

**Use only the current Preview APK from run 43439814552**, built from source head 0fea772e8f7acc8e84c1ce244ca1c995f294db02. It includes the file-backed auto-stop opt-out fix. Install it alongside the regular app; do not uninstall the regular app, clear its data, or use valuable profiles for destructive tests.

### Before testing
- [ ] Confirm the APK is labelled **Sleepy Hub Preview** and its package ID is `com.cutiecookie.sleepyhub.preview`.
- [ ] Install it alongside the normal Sleepy Hub app. Do not uninstall the normal app, clear its data, or use valuable profiles for destructive tests.
- [ ] Record the phone model, Android version, and whether gesture or three-button navigation is enabled.

### Test in this order
1. **Launch and drawer appearance**
   - [ ] Open the Preview app and confirm it reaches the workspace.
   - [ ] Open the drawer. Profile rows should be compact, one line, and text-only (no profile image or app-count subtitle).
   - [ ] Confirm the selected profile is visibly selected and the container selector remains at the bottom.
2. **Profile switching**
   - [ ] In a disposable container with two profiles, tap A, then B, then A again.
   - [ ] Repeat with quick consecutive taps and, if available, swipe between profiles.
   - [ ] Confirm each profile shows its own app grid and the selected profile name matches the visible grid.
   - [ ] Close and reopen Preview; confirm the last selected profile is restored.
3. **Container switching**
   - [ ] Create/use two disposable populated containers and one empty container.
   - [ ] Switch populated A → populated B → A. Confirm each container restores its own last profile.
   - [ ] Switch to the empty container. It must remain empty; no profile should be created automatically.
   - [ ] Switch back to A and confirm its previous profile and apps are still there.
4. **Clone destination and profile IDs**
   - [ ] In container A, clone a disposable profile that has several apps.
   - [ ] While cloning is still running, switch to container B.
   - [ ] When cloning finishes, check that the new profile appears in A, not B, and that the original profile is unchanged.
   - [ ] Create two disposable profiles quickly, then make two separate clone requests. Confirm each result is a distinct profile and neither replaces another.
5. **App installation**
   - [ ] Install a harmless app from the installed-app picker into a disposable virtual profile.
   - [ ] Install a second harmless APK obtained from its developer's official source.
   - [ ] Launch each installed app. If either fails, note which route failed and capture the full error dialog.
6. **Google services and fake location**
   - [ ] Toggle Google services for a disposable profile. Confirm only the small Play indicator appears beside the profile name; Google service packages must not appear as ordinary app tiles.
   - [ ] Apply a non-sensitive test location to one test app, verify that app sees it, then disable fake location and verify the override is removed.
7. **Scrolling, keyboard and system navigation**
   - [ ] Scroll the profile manager and fake-location list to the bottom.
   - [ ] Search while the keyboard is open; confirm the list resizes and remains scrollable.
   - [ ] Check the Google-services empty state.
   - [ ] Confirm bottom controls remain visible and tappable above the transparent navigation area.

### Feedback format
For each failed step, report the step number, what you tapped, what you expected, what happened, and the exact error text. A screenshot is helpful; hide personal app names, account details, and precise location information. Mark untested steps as **Not tested** rather than guessing.


## Phase 0 — project safety and repeatable builds

1. Keep feature work isolated from `main`.
2. Preserve the baseline and user data.
3. Make the preview package/signing identity reproducible.
4. Make CI verify package name, app label, signature and ARM64 native libraries.
5. Record every build's commit, workflow, artifact, size and SHA-256 when available.

**Exit criteria:** a repeatable, verifiable preview APK can be produced without replacing the user's existing installation or touching its data.

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

## Phase 2 — source-led RikkaHub UI reconstruction

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
| BUG-001 | P0 | Profile switching must select the matching BlackBox page and persist correctly. | Core switching passed; identity isolation open | User confirmed A→B→A switching works and app lists do not combine. The same device identifiers still appear in different profiles; see BUG-018. Retest switching with identifier and storage isolation after the current candidate builds. |
| BUG-002 | P0 | Container switching must keep profile lists scoped and restore the previous selection. | Container/list behavior passed; naming fix pending build | User confirmed the empty container remained empty and profile lists did not combine. Its first profile was incorrectly named Profile 3; see BUG-020. |
| BUG-003 | P0 | Adding apps must work through both the installed-app picker and external APK chooser. | Device test passed; regression check pending | User reports both installed-app picker and external APK paths PASS. Retest after the current core changes. |
| BUG-004 | P1 | Google services must remain scoped to the selected profile and not appear as ordinary app tiles. | UI passed; profile scope needs retest | User marked the GMS UI PASS but said the effect appears global. Source calls install/uninstall with the selected BlackBox user ID; the shared identifiers in BUG-018 may have made Google apps recognize profiles as the same device. Re-test after identity isolation. |
| BUG-005 | P1 | Navigation bar appears as an opaque bar; user wants transparent but all controls must remain visible and tappable above the system navigation area. | Code-level changes present; device testing | Theme code sets a transparent navigation bar and disables decor fitting; audit every bottom screen for safe insets. Test gesture and three-button navigation; never solve transparency by placing controls behind system buttons. |
| BUG-006 | P1 | Animations should be smooth and restrained, with no sweeping profile/container transitions. | Source polish committed; device review pending | Current candidate replaces page slides with a short fade and adds a labeled initial app-loading state. Profile selection now snaps. Verify on device. |
| BUG-007 | P1 | UI should follow the pinned RikkaHub source rather than an approximate preview. | User reports major improvement; final polish remains | User now describes the UI as amazing. Remaining concrete concerns are color leakage, clipped Color mode text and white transition flashes; see BUG-024. Do not claim 100% parity. |
| BUG-008 | P1 | Fake location must apply only to the selected virtual app/profile and restore real location when disabled. | Device test crashed; reproduction detail needed | User reports the fake-location test crashed. The exact sub-step and crash trace are not yet known; see BUG-023. Earlier source result handling is not sufficient to close this issue. |
| BUG-009 | P1 | Further UI and functional bugs are expected; initial report is incomplete. | Open intake | Append every new report as the next BUG ID. Do not close or overwrite earlier issues without evidence. |
| BUG-010 | P1 | Profile manager and fake-location lists, plus the Google-services empty state, can request the full screen height underneath earlier content in a Column. | Build verified; device testing | Static audit found `fillMaxSize()` used for vertically stacked children. Commit `dae5f46` changes those children to consume only the remaining height with `weight(1f)`. CI run 43419141589 passed build, APK identity/signature/ABI verification and upload. Source review confirms the affected list/empty-state branches are direct children of Columns. Test scrolling, keyboard/search behavior, and empty states on device. |
| BUG-011 | P1 | Drawer profile rows are too tall and show a profile picture plus an app-count subtitle; RikkaHub's conversation rows are compact, text-only, single-line rows. | Build verified; device testing | Compared against pinned upstream `ConversationList.kt` at `b10c168`. Commit `6a11fed` removes the row avatar and app-count subtitle, reduces vertical padding to 6dp, uses the source's elevated selected surface, and keeps only the small Play/pin indicators. Verify the new commit builds and compare the drawer on the target phone. |
| BUG-012 | P1 | Cloning must remain attached to its source container if the user switches containers during cloning. | Clone target behavior passed; identity isolation open | User confirmed the clone stayed with its own container after switching away. Matching device identifiers remain open under BUG-018; app-data isolation still needs a separate test. |
| BUG-013 | P1 | Profile creation and cloning independently allocate `max(userId) + 1`; concurrent requests can both receive the same ID because BlackBox's `createUser(id)` returns the already-existing user when that ID is taken. | Build verified; device testing | Commit `bf6752f` adds a process-wide lock shared by profile creation and cloning, covering both ID allocation and `createUser`. Verify the build and test rapid/double create or clone actions on device. |
| BUG-014 | P1 | A delayed `loadContainerProfiles` callback unconditionally clears `pendingPagerUserId`; if a newer profile selection happens before that callback runs, the older refresh can erase the newer selection guard. | Build verified; device testing | Commit `168d15b` clears the pending ID only when it still matches that refresh's selected profile. Verify rapid container/profile switching and swipe-back behavior on device. |
| BUG-015 | P1 | Fake-location apply/disable must report the real Binder result, not optimistic success. | Source fix on prior head; latest device flow crashed | Earlier changes wait for Binder completion and verify stored mode/coordinates. Latest device test crashed somewhere in the fake-location flow; see BUG-023 and keep this open until the failing step is known. |
| BUG-016 | P1 | Google-services failures must produce an explicit result and refresh the selected profile state. | Source fix on prior head; device scope retest pending | Exception reporting is implemented. User reports the UI PASS but questions whether it is truly profile-scoped; verify after BUG-018. |
| BUG-017 | P0 | ARM64 preview build must pass resource merge, identity/signature/ABI verification and artifact upload. | Previous head passed; current candidate building | Exact head cd917c5 passed run 43424012176. New candidate 6df0bbc is being checked by run 43437776339; no APK is ready for these fixes until it succeeds. |

## Device test report — 2026-11-13

| Test | User result | Recorded observation |
|---|---|---|
| 1. Drawer appearance | PASS | Compact drawer/profile appearance accepted. Requested only a plain white Google Play triangle at the same position/size; implemented in current candidate. |
| 2. Profile switching and folders | UNSURE | A↔B switching works and app lists do not combine. Folder filtering works but management/move selection needed improvement; see BUG-019. Device identifiers appear shared; see BUG-018. Storage isolation is not yet proven. |
| 3. Empty container and first profile | PASS with issue | Empty container behavior is correct, but its first profile was named Profile 3; see BUG-020. |
| 4. Clone/container ownership | UNSURE | Clone stayed with its own container after switching away. Same identifiers appear across profiles/containers; storage still needs a separate test. |
| 5. Rapid profile creation/cloning | UNSURE | User cannot give a clean pass while identity isolation is unresolved; keep concurrency/unique-ID regression coverage. |
| 6. App installation | PASS | Installed-app picker and external APK paths both passed. Long-press still launched on release; see BUG-021. |
| 7. Google services | PASS with scope concern | UI flow passed, but it appeared to affect every profile. Source targets a specific user ID; re-test after BUG-018. |
| 8. Fake location | CRASHED | Exact failing sub-step and crash trace not yet known; see BUG-023. |
| 9. Scrolling/search/navigation | UNSURE | Color mode text was clipped and page changes flashed white. Current candidate adds a scroll-safe mode row and fade/background transition; verify after build. |

## Next device retest order

1. Wait for the current candidate CI run to pass before installing its APK; install Preview alongside the regular app and do not clear existing data.
2. Create profiles in different containers. Compare Android/device identifiers in the same test app if available; each profile must differ and remain stable after closing/reopening the Hub.
3. Save a local note or setting in the same app in Profile A; Profile B must start with independent app data. This is separate from identifier uniqueness.
4. Create, rename, move between and delete a folder. Deleting a folder must move its profiles to General without deleting apps/data.
5. Create the first profile in an empty container and confirm its label is New Profile.
6. Long-press an app: the action sheet should open without launching the app. A normal tap should still launch it.
7. With auto-stop enabled, return to Hub and reopen before 60 seconds; the app should still be available. Wait over 60 seconds and confirm it stops. Turn the setting off and verify it remains running.
8. Enable GMS only for Profile A, then inspect Profile B and another container; verify package state and switch are independent.
9. Recheck monochrome theme, Color mode text, initial loading, and profile/container transitions.

| BUG-018 | P0 | Different virtual profiles and containers expose the same Android/device identifiers. | Root cause found; fix committed, CI/device verification pending | Android ID previously passed through the host value; telephony hooks returned the same host-package hash for device ID/IMEI/MEID/subscriber ID; serial hooks were shared too. Commit 28443fd adds persistent per-BlackBox-user Android ID, IMEI, MEID, subscriber ID, SIM serial and device serial. Retest uniqueness and stability. |
| BUG-019 | P1 | Folder organization needs create/delete/rename and a picker for moving profiles instead of typed names. | Implemented in source; current CI/device verification pending | Commit 6df0bbc adds a per-container folder registry, New Folder action, long-press rename/delete, a move-to-folder bottom sheet and a folder dropdown in profile editing. Deleting a folder returns its profiles to General without touching apps/data. |
| BUG-020 | P2 | First profile in an empty container is named Profile 3 because numbering is global. | Implemented in source; current CI/device verification pending | Commit 28443fd changes the unsaved/default profile label to New Profile; user-defined names remain unchanged. |
| BUG-021 | P1 | Long-pressing an app opens it on release instead of only opening its action sheet. | Fix committed; current CI/device verification pending | Commit 28443fd suppresses the click generated after the long-press/reorder gesture. Test long-press without dragging and normal tap separately. |
| BUG-022 | P1 | Manual Stop action does nothing; inactive apps should stop after one minute, with an option to disable auto-stop. | Build verified; device testing pending | Commit 28443fd removes the ineffective Stop action and adds a 60-second background idle-stop hook. Commit 0fea772 mirrors the toggle to a file so virtual processes see changes immediately rather than relying on stale SharedPreferences caches. Run 43439814552 passed; test timeout, resume-before-timeout and toggle-off behavior on device. |
| BUG-023 | P1 | Fake-location flow crashes during device testing. | Blocked; exact failing sub-step and crash trace needed | User reports test 8 crashed but has not identified whether it occurs opening Fake Location, selecting an app, opening the map, applying or disabling. Ask for the single failing step and, if possible, the Android crash dialog or logcat before changing this path. |
| BUG-024 | P1 | Monochrome theme leaks purple/other colors; Color mode is clipped; page navigation flashes white. | Source polish committed; current CI/device verification pending | Commit 28443fd explicitly sets neutral Material 3 container/error roles, makes Color mode chips horizontally scrollable, disables dynamic wallpaper colors when a fixed accent is selected, and fades pages over the theme background. |
| BUG-025 | P2 | Profile/container switching sweeps across the screen, especially noticeable with separate backgrounds. | Source polish committed; current CI/device verification pending | Commit 28443fd disables ViewPager2 touch swiping and uses non-animated profile selection. Page navigation uses a short fade rather than horizontal slides. |

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
