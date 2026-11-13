# RikkaHub source audit and Sleepy Hub code review

Reviewed 2026-11-13.

## Reference

- Upstream: [rikkahub/rikkahub](https://github.com/rikkahub/rikkahub)
- Pinned commit: `b10c168577482abed696669a58c0eb59f1eed270`
- Latest implementation branch audited: `sleepyhub/bugfix-install-switch-polish`
- Latest inspected head: `cd917c58c7d4f8baa521f6839eafdb318cb40fa7`
- Living roadmap and bug register: [DEVELOPMENT_PLAN.md](DEVELOPMENT_PLAN.md)
- Earlier source-rebuild branch: `sleepyhub/rikkahub-source-rebuild` (`9788409e015b13796bf1fe52820edd0fae5e074d`)
- App baseline: `sleepyhub/identity-icons-no-vpn`
- `main` remains untouched; the rebuild branch is separate.

## Current implementation status — 2026-11-13

The current living plan and append-only bug register are maintained in [DEVELOPMENT_PLAN.md](DEVELOPMENT_PLAN.md). That file is the source of truth for issue IDs, priorities, statuses, phase order and acceptance checks.

<badge color="success">Current feature head build verified</badge>

- Feature branch: `sleepyhub/bugfix-install-switch-polish`
- Latest inspected feature commit: `cd917c58c7d4f8baa521f6839eafdb318cb40fa7` (drawer-row, clone-container, profile-ID allocation, pager selection, fake-location feedback, Google-services error handling, and localized resource-format corrections). The preceding run [43423100800](https://github.com/Cutie-Cookie/Sleepy-Hub/actions/runs/43423100800) failed during resource merging; corrected current-head run [43424012176](https://github.com/Cutie-Cookie/Sleepy-Hub/actions/runs/43424012176) **passed** all build, preview identity/signature/ABI, and artifact-upload steps.
- Previous successful build: [43417371053](https://github.com/Cutie-Cookie/Sleepy-Hub/actions/runs/43417371053) for commit `046e04bb15ab328f531427a6ea64b9d9dba896fb`; its APK was 26,038,134 bytes with SHA-256 `dc2a88d0c76e4c7384aa1744388487b36ed52057d165cb41bd4c95e069d191f6`.
- Current successful build: [43424012176](https://github.com/Cutie-Cookie/Sleepy-Hub/actions/runs/43424012176) for exact head `cd917c58c7d4f8baa521f6839eafdb318cb40fa7`. All ten feature commits and three resource-correction commits are build-verified; physical-device behavior remains unverified.
- Current-head artifact: `sleepy-hub-arm64-preview-debug` (ID `14036965394`), archive size 24,220,845 bytes, GitHub artifact SHA-256 `2f2bb35f5a798b0844a2609955b89f6cb2849a1cfaa6bee218ec779d19b1bdae` (archive digest); expires 2026-11-20 12:22:41 UTC. Extracted APK is 26,039,396 bytes with SHA-256 `4f3bbdc528a24797eb0b8654776155c40a68c7e1c463a1e2e35668510998e2d0`.
- Current-head workflow passed ARM64 debug build, preview APK identity/signature/ABI verification, and artifact upload. CI verified package ID `com.cutiecookie.sleepyhub.preview`, label `Sleepy Hub Preview`, APK signature, and ARM64 native-library presence.
- Preview package uses application ID suffix `.preview`, label `Sleepy Hub Preview`, and a persistent debug keystore at `app/sleepyhub-preview-debug.keystore`; this is a safety identity for testing alongside the existing app, not a reduced-feature build or final release identity.
- The APK has not been installed or interaction-tested on a physical Android device.

- The user-facing device checklist is maintained in [DEVELOPMENT_PLAN.md](DEVELOPMENT_PLAN.md), under “Next phone test session — current-head APK only”. Use the APK from run `43424012176`; do not substitute older artifacts.
- `main` remains untouched at `89b59836c66f173756a4ae258cf379a957649820`.

### Implemented in the latest feature branch

- Persistent container membership, an empty-container flow, and a last-active-profile ID per container.
- A ViewPager2 stale-callback guard (commit `046e04b`) so a delayed event for an old page cannot silently select a different active profile; the CI build passes, while device verification remains pending.
- A list-height fix (commit `dae5f46`) replaces `fillMaxSize()` with `weight(1f)` for the profile-manager list/empty state, fake-location list/empty state, and Google-services list/empty state inside their parent Columns. The latest CI build passes; runtime scrolling and empty-state layout still need phone verification.
- Drawer profile rows (commit `6a11fed`) now follow the pinned `ConversationList.kt` pattern: one text line, no profile image or app-count subtitle, compact 6dp vertical padding, and an elevated selected surface. Current-head CI passes; phone screenshot review remains pending.
- Profile cloning (commit `28167df`) now captures the source container before background work, assigns the new profile to that container even if the user switches away, and cleans up if the container disappears during the copy. Current-head CI passes; device timing tests are pending.
- Profile creation and cloning (commit `bf6752f`) now share a process-wide lock around `max(userId) + 1` allocation and `createUser`, preventing simultaneous requests from reusing one virtual user ID. Current-head CI passes; rapid-action device tests are pending.
- Pager cleanup (commit `168d15b`) now clears `pendingPagerUserId` only when it still belongs to the container refresh that scheduled the callback, preserving a newer profile selection. Current-head CI passes; rapid-switch device tests are pending.
- Fake-location apply/disable (commits `9336b6f`, `f95758a`, `858e911`, `eb870fc`, and `ea11a66`) now exposes Binder write outcomes, verifies the selected point/mode, and only shows success after the operation completes; failures receive an explicit message. Current-head CI passes; apply/restore device tests are pending.
- Google-services install/uninstall (commit `b116166`) now catches operation exceptions and emits a visible failed result rather than leaving the screen without feedback. Current-head CI passes; enable/disable device tests are pending.
- Fake-location resource follow-up (commits `5a2084a`, `a69ca35`, and `cd917c5`) changes multi-argument coordinate strings to positional placeholders across English, Simplified Chinese, and Traditional Chinese, and simplifies English failure text after AAPT2 rejected the previous resource set. The corrected resource set passes current-head CI.
- Inline, independently scrollable profile rows in the drawer, folder filtering, profile sorting, a small Add Profile action, and a separate bottom container selector.
- Profile manager search, compact rows, reorder handles, detail/basic/background pages, and per-profile workspace wallpapers.
- Container settings now cover container name and picture only. Legacy container wallpaper data is migrated to profiles when needed; no per-container theme or accent controls remain.
- One global theme system; the workspace remains black by default, and the selected profile's optional background is drawn behind its app grid only.
- A Compose Google services manager that lists profiles rather than installed Google packages. Google system packages are filtered from the app grid; enabled profiles show the small Play badge beside the profile name.
- Clearer fake-location instructions: choose an app in the active profile, choose a point on the map, apply it, and long-press that app later to disable it. Actual location override behavior still requires device verification.
- Rounded search fields and consistent grouped surfaces across profile management, app picking, fake location, settings, and Google services.
- Transparent system navigation bars with safe-drawing insets retained on the actual content. Bottom controls remain padded above the system navigation area.
- Slower page transitions and animated list placement/color changes.
- APK/profile validation and a detailed failure dialog for installation actions, plus additional package-parser/install logging. Source comparison also found and corrected the external-APK path dispatch: filesystem paths now use the File overload instead of the package-name overload. The installed-app clone path and actual device installation result remain unverified.

### Verification still required

See [DEVELOPMENT_PLAN.md](DEVELOPMENT_PLAN.md) for the active bug IDs and exact acceptance checks. In particular, the reported app-install failure remains unresolved until an app is successfully installed on-device.

- Install the APK on the target phone and test switching profiles, switching between populated and empty containers, and switching back.
- Test adding an app from the installed-app picker and from an external APK. The new validation and error dialog improve diagnosis, but installation success is **not** claimed until this is exercised on-device.
- Test Google services enable/disable and confirm only the Play badge appears on profile names.
- Test fake location with a normal location-aware app and confirm that disabling it restores real location.
- Check screenshots of the drawer, profile manager/detail, container picker, app picker, GMS manager, and system navigation area against the pinned RikkaHub source.
- Specifically test profile-manager and fake-location list scrolling, search/keyboard resizing, and the Google-services empty state after the `weight(1f)` layout fix.
- No automated UI or instrumentation tests have been added yet.

### Reorderable API note

The project uses Reorderable 3.1.0. Its current API uses `ReorderableItem` and the in-scope `longPressDraggableHandle`/drag handle modifiers; the older `Modifier.reorderable(state)` extension is not part of this version. The mistaken old modifier was removed after CI identified it. Existing drag handles remain in the list/grid items.

## User-confirmed mapping

| RikkaHub | Sleepy Hub |
|---|---|
| Assistant | **Container** |
| Assistant picker | Container picker |
| Assistant manager | Container manager |
| Assistant detail/basic settings | Container settings |
| Conversation | **BlackBox virtual Android profile** |
| Assistant-owned folder | Profile folder inside that container |
| Assistant avatar | Container picture |
| Assistant-owned workspace background | Profile workspace background (per profile, behind its app grid only) |
| Global theme | One global Sleepy Hub theme |

A container owns zero or more virtual profiles. The inline scrollable list in RikkaHub's drawer maps to Sleepy Hub profiles. The compact assistant selector near the drawer bottom maps to containers. A new container starts empty; it must not auto-create a BlackBox user. Switching containers filters the profile list and restores the last selected profile when returning. Profile name, avatar, and workspace background belong to the profile; the background is rendered only behind that profile's active app grid, never on drawer rows, profile cards, or the container picker.

Theme ownership is global only. The upstream `Assistant` data class has `avatar`, `background`, `backgroundOpacity`, and a gradient-background flag, but no theme ID or assistant-specific theme. RikkaHub theme state lives in global `Settings` and the theme preferences. Therefore Sleepy Hub must not add per-container or per-profile theme/accent controls.

## RikkaHub source flow inspected

### Main shell and drawer

- `ChatPage.kt`: phone uses `ModalNavigationDrawer`; large screens use `PermanentNavigationDrawer`. The page uses a Material 3 `Scaffold`, transparent top bar, contextual actions, and explicit back handling that closes the drawer first.
- `ChatDrawer.kt`: 300dp drawer sheet, 8dp outer padding/spacing, user header, compact quick actions, folder bar and sort control, independently scrollable center conversation list, assistant picker near the bottom, and bottom utility/settings actions.
- `ChatDrawerVM.kt`: current assistant ID scopes the folder list and conversation query. Changing assistant resets the folder filter. The selected assistant determines which list is shown.
- `ConversationList.kt`: inline lazy list, text-only rows, selected-row treatment, pinned/date separators, and long-press contextual actions. This is the correct structural analogue for Sleepy Hub's profile list.

### Assistant picker and manager

- `AssistantPicker.kt`: compact drawer item opens a modal bottom sheet of about 80% height. Picker rows use a 32dp avatar, one-line name, selected state, and trailing edit action.
- `AssistantPage.kt`: `LargeFlexibleTopAppBar`, add action, rounded search field, reorderable list, compact 20dp cards, 48dp avatars, and animated item placement. AI-specific tags/clone/model features are not automatically part of Sleepy Hub scope.
- `AssistantDetailPage.kt`: pinned top bar, animated title reveal, and grouped `CardGroup` sections.
- `AssistantBasicPage.kt`: editable assistant avatar and name; background is a separate optional assistant property.
- `UIAvatar.kt`: source supports a generated avatar, image crop, emoji, URL, and reset. Sleepy Hub should preserve the agreed picture/avatar behavior without copying the source component.
- `BackgroundPicker.kt`: optional background preview, gallery/URL selection, opacity, change, and removal.

### Global theme

- `Theme.kt`: global `ColorMode` (System/Light/Dark), dynamic color, global theme lookup, AMOLED black, system-bar contrast, `MaterialExpressiveTheme`, and `MotionScheme.expressive()`.
- `SettingPreferencesThemePage.kt` and `SettingThemePage.kt`: global dynamic-color/AMOLED preferences, preset themes, custom themes, import/export, and theme selection.
- `Assistant.kt` contains no per-assistant theme field. Assistant identity/background and global theme are separate.

## Historical pre-container-model audit (superseded by the implementation above)

The following issues described the pre-rebuild code and are retained as audit history. They are not a description of the current feature branch.

### Structural mismatches that must be fixed before visual polish

1. **No container model exists.** `MainActivity` enumerates every BlackBox user globally. `createProfile()` creates a global virtual user, and there is no container ID or membership mapping.
2. **The new-empty-container flow cannot exist yet.** The current data model cannot represent an empty container separate from the global BlackBox user list. The last-profile guard is global, not scoped to a container.
3. **Wrong drawer list placement.** `SleepyHubMainScreen.kt` does not show profiles inline in the drawer. It opens `ProfilePickerSheet` instead. This reverses the RikkaHub structure: the profile/conversation list belongs inline; the assistant/container picker is the bottom sheet.
4. **Wrong picker identity.** The current bottom sheet selects virtual profiles and shows profile avatars. It should select containers and show container pictures. The inline profile list should be text-only, matching `ConversationList`.
5. **Wrong appearance ownership.** `ProfileAppearance.kt` keys accent/background by BlackBox user ID. Move the optional background and picture to a container record. Remove all per-profile/per-container theme or accent controls.
6. **Missing container picture.** Current `ProfileAvatar` is only an initial inside a color circle; there is no persisted container image/avatar.
7. **Wrong default workspace surface.** `ProfileWorkspaceBackground()` draws an accent gradient even when no image is selected. The workspace must be plain black when background is null.
8. **Incomplete global theme.** Current global settings have basic System/Light/Dark, dynamic color, global accent, and AMOLED options, but not the RikkaHub preset/custom theme flow. `SleepyHubTheme.kt` uses ordinary `MaterialTheme`, not the upstream expressive theme/motion APIs.
9. **Navigation is incomplete.** The shell uses string-based page state and only a modal drawer. It lacks the source typed back stack and permanent drawer for large screens. The ViewPager adds custom page scaling/translation not present in RikkaHub's navigation.
10. **Container settings are not represented.** Current `ProfileSettingsScreen` edits a virtual profile's name/folder/accent/background. It needs to become container settings for name, picture, and optional background. Profile names/folders remain attached to virtual users.

### Existing BlackBox operations reviewed

- `AppsFragment.kt` routes actions using the selected BlackBox user ID.
- `AppsRepository.clearApkCache()` clears both internal and external cache directories for that user.
- Clear data uses `BlackBoxCore.clearPackage(packageName, userID)`.
- Uninstall uses `uninstallPackageAsUser(packageName, userID)`.
- Stop uses `BlackBoxCore.stopPackage(packageName, userID)`.
- These per-user backend operations should be preserved while container membership is added.

### Historical build failures (superseded)

Earlier ARM64 debug workflows failed during the first UI rebuild. Those failures were resolved in later commits. The latest successful build is [43413343419](https://github.com/Cutie-Cookie/Sleepy-Hub/actions/runs/43413343419), with artifact ID `14031251227` as recorded above.

There are currently no files under `app/src/test` or `app/src/androidTest`. Add tests for container/profile ownership, empty containers, last-selected-profile restoration, and persistence.

## Remaining implementation and verification order

1. Device-test profile switching, empty-container switching, and restoration of the last profile.
2. Device-test app installation from both the installed-app picker and an external APK; use the new detailed error dialog and logs if it fails.
3. Device-test Google services and fake location behavior.
4. Compare device screenshots against the pinned RikkaHub source and iterate on any concrete layout/animation differences.
5. Add focused tests for container/profile ownership, empty containers, last-selected-profile restoration, and persistence.

## License boundary

RikkaHub is AGPL-3.0; Sleepy Hub currently declares Apache-2.0. Match the source flow with original Sleepy Hub code. Do not paste upstream source files/assets into the Apache-2.0 project. Literal source reuse requires an explicit licensing decision and compliance review.


## Deeper interaction and screen-placement audit — 2026-11-13

### Conversation/profile long-press move flow

Confirmed directly in the pinned source:
- `ConversationList.kt`: `ConversationItem` uses `combinedClickable`; long press opens a dropdown containing Pin/Unpin, Regenerate title, Move to assistant, Move to folder, Delete.
- `ChatDrawer.kt`: Move to assistant opens a `ModalBottomSheet` with a `LazyColumn` of assistant rows. The current assistant is marked.
- `ChatVM.kt`: `moveConversationToAssistant` delegates to `ChatService`; if the moved row is the currently open conversation, the selected assistant is updated too.
- `ChatService.kt`: moves the active session to the new assistant and clears the folder ID.
- `ConversationRepository.kt` and `ConversationDAO.kt`: persist the assistant ID and clear folder assignment.
- `ChatDrawerVM.kt`: folders and conversations are assistant-scoped; changing assistant resets the selected folder filter.

Sleepy Hub must reproduce this interaction as Move Profile to Container. Update only container membership; preserve the same BlackBox user ID and all app data. If the active profile moves, switch to the target container and keep it active. Reset its old container-scoped folder assignment.

### Assistant/container and settings entry points

- `ChatDrawer.kt`: compact `AssistantPicker` appears below the scrollable conversation list. Its trailing drawer action opens the Assistant manager. The footer's bottom-right Settings action opens Settings home.
- `AssistantPicker.kt`: tap the compact selector to open the assistant picker sheet (~80% height). Rows have 32dp avatar, name, selected treatment, and trailing edit action. The trailing edit action opens that assistant's detail screen.
- `AssistantPage.kt`: Assistant manager has a LargeFlexibleTopAppBar Add icon; Add opens `AssistantCreationSheet`. It has rounded search, filter row, reorderable list, compact rows/cards, and deletion confirmation.
- `SettingPage.kt`: Settings home General Settings contains Color Mode, Preferences, Assistant manager, and Extensions. This is the second entry point to Assistant manager.
- `SettingPreferencesPage.kt`: Preferences links Theme, Notifications, General, UI, and Network.
- `SettingPreferencesThemePage.kt`: Dynamic Color, Theme Settings, AMOLED Dark Mode.
- `SettingThemePage.kt`: preset and custom theme list, create/edit/delete, import/export.
- `RouteActivity.kt`: typed routes and `NavDisplay` entries; includes Assistant manager/detail/basic and Settings routes.

Sleepy Hub placement:
- Add Profile is a small plus beside the inline profile-list heading.
- Add Container is in the Container Manager top app bar.
- Container switcher is the compact AssistantPicker-style drawer selector.
- Container manager is available from the dashboard footer and Settings home.
- Settings remains bottom-right in the dashboard footer.
- Workspace top-right action replaces RikkaHub New Message with Add App.
- Remove the top-right MoreVert popup entirely; use dashboard Settings, profile actions, Container Manager, and Refresh shortcut.

### Historical Sleepy Hub UI issues from the initial audit (superseded)

The items below record the initial UI mismatch list. The latest implementation addresses the listed drawer placement, Add App entry point, profile backgrounds, global theme ownership, Compose permission rationale, and app-picker structure; device-level visual verification remains outstanding.

- `SleepyHubMainScreen.kt`: current drawer has a Manage profiles row and profile-picker bottom sheet. Replace with inline independently scrolling profile list; move container switching into the assistant-style sheet. Add the small profile plus beside the list heading.
- `SleepyHubMainScreen.kt`: top bar already has an Add/Install action, but it also has a MoreVert menu with Profile settings, Manage profiles, and Refresh apps. Keep the Add/Install action in the New Message position; remove MoreVert. Keep Settings at dashboard bottom-right and Refresh in dashboard footer.
- `SleepyHubMainScreen.kt`: current drawer also contains an Install app quick action. Prefer the top-right Add App action as the primary entry point rather than duplicating it.
- `ProfileScreens.kt`: current profile settings store per-profile accent/background. Move picture/background to container settings and remove all profile/container theme controls. Profile name/folder remain profile-owned.
- `MainActivity.kt`: storage rationale uses `MaterialAlertDialogBuilder`; replace the explanation popup with a Compose Material 3 rationale dialog while keeping the same Android All Files Access permission intent/result check.
- `InstalledAppPickerScreen.kt`: current screen uses a basic small TopAppBar, OutlinedTextField, and separate cards per app. Rework its hierarchy and row interactions, not just colors: LargeFlexibleTopAppBar, rounded search, compact icon/name/package rows, clear Add/Installed state, APK chooser, and proper loading/empty states.
- `ListActivity.kt`: import says `ActivityInstallPickerBinding` while the property uses `ActivityListBinding`; fix this build blocker.
- Current branch still has the previously recorded missing Compose imports and default accent gradient. The failed workflow has not produced an APK.

### Source-map document

Created [RIKKAHUB_SOURCE_MAP.md](RIKKAHUB_SOURCE_MAP.md), which indexes the upstream file paths and functions for drawer, row long-press, move-to-assistant service/repository/DAO, picker, manager/add, detail/avatar/background, Settings, global theme, route registration, and permission-dialog patterns.

## 2026-11-13 — Device feedback and source-level isolation audit

### Folder behavior mapped to RikkaHub

- Pinned RikkaHub's ChatDrawer folder row and ConversationList long-press menu. The drawer owns the folder filter; profile/conversation actions use a picker rather than asking the user to type a folder name.
- Sleepy Hub now stores folder names per container, keeps General as the reserved default folder, and supports creating, renaming and deleting folders. Drawer folder chips expose rename/delete on long press; moving a profile opens a folder-selection bottom sheet. The profile editor also uses a dropdown of existing folders.
- Deleting a folder moves its profiles to General; it does not delete BlackBox users, installed apps or app data.

### Virtual device identifiers versus app storage

- Device testing showed matching identifiers across profiles and containers. Source tracing found that AndroidIdProxy passed through the host Android ID whenever valid; ITelephonyManagerProxy returned an MD5 of the host package for device ID/IMEI, MEID and subscriber ID; device-ID fallback and serial hooks also used shared values.
- Commit 28443fd adds BUserIdentifiers, keyed by BlackBox user ID and persisted under that virtual user's directory. Android ID, IMEI, MEID, subscriber ID, SIM serial and device serial are now generated per virtual profile and remain stable across app launches/processes. This is source-level implementation only until the current CI build and device checks pass.
- Storage has a separate source-level boundary: BEnvironment.getDataDir(packageName, userId), getUserDir(userId), getExternalUserDir(userId) and getExternalDataDir(packageName, userId) include the BlackBox user ID in their paths. This supports per-profile storage separation, but a device test is still required: write local app data in one profile and verify it is absent in another.
- GMS install/uninstall paths call the selected BlackBox user ID. The apparent global GMS effect may be related to Google apps seeing the same virtual device identifiers; re-test after the new identifiers are installed.

### Current feedback-driven UI/lifecycle changes

- The Google Play profile badge is now a plain white triangle at the existing size/position.
- The default unsaved profile name is New Profile, independent of the global BlackBox user ID.
- App long-press now suppresses the click-on-release event; the action sheet should open without launching the app.
- The nonfunctional Stop menu action was removed. A virtual app that leaves the foreground is scheduled to stop after 60 seconds; a global Settings toggle disables this behavior.
- Profile pager touch swiping is disabled and profile selection snaps to the target. Page changes fade over the app's theme background instead of sliding over white.
- Material 3 container/error roles are explicitly neutral. Selecting a fixed accent disables dynamic wallpaper colors. Color mode chips use a horizontally scrollable row.
- The first app-grid frame now shows a labeled loading indicator instead of an empty black surface.
- Current feature head is 0fea772e8f7acc8e84c1ce244ca1c995f294db02. CI run 43437776339 passed the profile identity/folder/UI changes; run 43439814552 is checking the additional file-backed cross-process auto-stop setting.

### Outstanding device evidence

- The fake-location test crashed. No source change should be guessed until the exact failing step (open screen, select app, open map, apply, disable) or a crash trace is available.
- Re-test GMS profile scope after the per-profile identifiers change.
- Verify storage isolation separately from ID uniqueness.
- Verify idle-stop timing and opt-out, long-press behavior, folder management, monochrome colors, Color mode clipping, loading state and fade-only navigation on the target phone.

### Cross-process auto-stop preference hardening

- The idle-stop lifecycle callback runs in virtual app processes, while the Settings toggle is changed in the Hub process. SharedPreferences can be stale across processes, so commit 0fea772 mirrors the toggle to a small file in the host app's private files directory. Virtual app processes read that file when scheduling and when the 60-second timer fires.
- This prevents a previously cached true value from ignoring the user's opt-out. The current build run is 43439814552; device verification remains pending.
