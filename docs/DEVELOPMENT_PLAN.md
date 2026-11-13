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
- Do not add postponed features such as background-running apps, VPN, or remote log uploads to this milestone.

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
- Current feature-branch head at this update: `dae5f468ad1fdf52dee22d3572de37f7a9af619c`
- Latest completed CI run: [43417371053](https://github.com/Cutie-Cookie/Sleepy-Hub/actions/runs/43417371053) — **successful** for commit `046e04bb15ab328f531427a6ea64b9d9dba896fb`.
- Current CI run: [43419141589](https://github.com/Cutie-Cookie/Sleepy-Hub/actions/runs/43419141589) — running for the list-layout fixes in commit `dae5f468ad1fdf52dee22d3572de37f7a9af619c`.
- Artifact: `sleepy-hub-arm64-preview-debug` (artifact ID `14032447856`), containing `SleepyHub_4.0.0-preview_arm64-v8a-debug.apk` (26,038,134 bytes).
- APK SHA-256: `dc2a88d0c76e4c7384aa1744388487b36ed52057d165cb41bd4c95e069d191f6`.
- CI verified APK signature, package ID `com.cutiecookie.sleepyhub.preview`, label `Sleepy Hub Preview`, and ARM64 native library presence.
- Preview build uses application ID suffix `.preview`, label `Sleepy Hub Preview`, and the repository's persistent debug signing file. This is not the final release identity or a reduced-feature app.
- Recent changes add a stale-ViewPager-callback guard, clearer fake-location instructions, and corrected remaining-height constraints for the profile manager, fake-location list and Google-services empty state. The previous commit's CI is green; the newest layout-fix build is pending.
- No claim is made that app installation, all profile/container switching cases, fake location, or full RikkaHub visual parity have been proven on a phone.

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
| BUG-001 | P0 | Cannot switch between profiles or reliably switch back. Selecting a profile must select the matching BlackBox page and persist correctly. | Build verified; device testing | Source audit found a stale ViewPager2 callback risk. Commit `046e04b` ignores callbacks whose position is no longer `viewPager.currentItem`; CI run 43417371053 passed. Test A→B→A, reorder, add/delete, app restart and container return on the phone. |
| BUG-002 | P0 | Container switching may also be broken; user has not confirmed either way. Empty containers must remain empty and switching back must restore the previous profile. | Reported as uncertain; device testing | Test two populated containers and one empty container; verify active profile ID is scoped to each container and no profile is silently created or moved. |
| BUG-003 | P0 | Adding apps always reports “Installing Failed”. | Investigating; one source-path defect corrected, runtime still unverified | Comparing against the known-good baseline exposed a real external-APK dispatch defect: a filesystem path was passed to the package-name overload. Current code routes file paths to `installPackageAsUser(File, userId)` and host package names to the package-name overload. This should correct the external-APK path at source level; the installed-app clone path and actual device result remain unverified. Test both paths; if either fails, capture the exact new error message and relevant logcat. |
| BUG-004 | P1 | Google services manager still appears old; user does not want Google packages displayed as ordinary installed apps. Show only the small Google Play logo beside the relevant profile name. | Code path updated; device visual check pending | Branch contains a Compose profile-based GMS manager, Google package filtering from the app grid and a Play indicator on profile rows. Compare actual installed screen and ensure no GMS app tile/card is exposed. |
| BUG-005 | P1 | Navigation bar appears as an opaque bar; user wants transparent but all controls must remain visible and tappable above the system navigation area. | Code-level changes present; device testing | Theme code sets a transparent navigation bar and disables decor fitting; audit every bottom screen for safe insets. Test gesture and three-button navigation; never solve transparency by placing controls behind system buttons. |
| BUG-006 | P1 | Animations are missing or too fast; transitions should be smooth and deliberate like RikkaHub. | Partially implemented; visual audit pending | Page/list animations exist in source, but coverage, duration and easing need screen-by-screen comparison and device review. |
| BUG-007 | P1 | UI feels dry and only about 60–70% like RikkaHub; user is unhappy with logos, frames and overall presentation. | Open; broad source audit required | Rebuild screen by screen from pinned RikkaHub source. Compare hierarchy, component shapes, icons, spacing, typography, frames, states and transitions. Do not add decorative elements the reference does not have. |
| BUG-008 | P1 | Fake location is confusing; user cannot tell what it does or how to use it. | Explanation updated; behavior unverified | Current instructions explain choosing an app, dropping a map pin, applying it, and disabling it later. Verify location override is limited to the chosen virtual app/profile and disabling restores real location. |
| BUG-009 | P1 | Further UI and functional bugs are expected; initial report is incomplete. | Open intake | Append every new report as the next BUG ID. Do not close or overwrite earlier issues without evidence. |
| BUG-010 | P1 | Profile manager and fake-location lists, plus the Google-services empty state, can request the full screen height underneath earlier content in a Column. | Fixing; CI pending | Static audit found `fillMaxSize()` used for vertically stacked children. Commit `dae5f46` changes those children to consume only the remaining height with `weight(1f)`. CI run 43419141589 is pending; verify scrolling and empty states on device. |

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
