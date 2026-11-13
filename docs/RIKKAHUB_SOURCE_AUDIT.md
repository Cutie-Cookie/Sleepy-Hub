# RikkaHub source audit and Sleepy Hub code review

Reviewed 2026-11-13.

## Reference

- Upstream: [rikkahub/rikkahub](https://github.com/rikkahub/rikkahub)
- Pinned commit: `b10c168577482abed696669a58c0eb59f1eed270`
- Latest implementation branch audited: `sleepyhub/bugfix-install-switch-polish`
- Latest inspected head: `168d15ba71e7f1f879d1869c32c676519ba39590`
- Living roadmap and bug register: [DEVELOPMENT_PLAN.md](DEVELOPMENT_PLAN.md)
- Earlier source-rebuild branch: `sleepyhub/rikkahub-source-rebuild` (`9788409e015b13796bf1fe52820edd0fae5e074d`)
- App baseline: `sleepyhub/identity-icons-no-vpn`
- `main` remains untouched; the rebuild branch is separate.

## Current implementation status — 2026-11-13

The current living plan and append-only bug register are maintained in [DEVELOPMENT_PLAN.md](DEVELOPMENT_PLAN.md). That file is the source of truth for issue IDs, priorities, statuses, phase order and acceptance checks.

<badge color="warning">Current feature head is not yet build-verified</badge>

- Feature branch: `sleepyhub/bugfix-install-switch-polish`
- Latest inspected feature commit: `168d15ba71e7f1f879d1869c32c676519ba39590` (drawer-row, clone-container, profile-ID allocation, and pager selection changes; CI status not yet retrieved).
- Previous successful build: [43417371053](https://github.com/Cutie-Cookie/Sleepy-Hub/actions/runs/43417371053) for commit `046e04bb15ab328f531427a6ea64b9d9dba896fb`; its APK was 26,038,134 bytes with SHA-256 `dc2a88d0c76e4c7384aa1744388487b36ed52057d165cb41bd4c95e069d191f6`.
- Most recent successful build known: [43419141589](https://github.com/Cutie-Cookie/Sleepy-Hub/actions/runs/43419141589) for the older commit `dae5f468ad1fdf52dee22d3572de37f7a9af619c`; it does **not** contain the four newer source commits. The newer commits `6a11fedfd2b5b1ea6b40e8610575ea587609a9b3`, `28167dfaf336938ed387e2f0fff653dbddbee66f`, `bf6752f3c0dfbb9ad7f3affdc95d8b5ece95f4b4`, and `168d15ba71e7f1f879d1869c32c676519ba39590` have not yet been build-verified.
- Most recent artifact from the older build: `sleepy-hub-arm64-preview-debug` (ID `14033183422`), archive size 24,219,966 bytes, GitHub artifact SHA-256 `ec367179f93612dd9cf2e8d94716420e61dd85a6030ecbe133bb1de5e2bb0116` (archive digest, not APK file checksum); expires 2026-11-20 11:47:43 UTC.
- That older workflow passed ARM64 debug build, preview APK identity/signature/ABI verification, and artifact upload. No artifact is currently confirmed for head `168d15ba71e7f1f879d1869c32c676519ba39590`. CI verified package ID `com.cutiecookie.sleepyhub.preview`, label `Sleepy Hub Preview`, APK signature, and ARM64 native-library presence.
- Preview package uses application ID suffix `.preview`, label `Sleepy Hub Preview`, and a persistent debug keystore at `app/sleepyhub-preview-debug.keystore`; this is a safety identity for testing alongside the existing app, not a reduced-feature build or final release identity.
- The APK has not been installed or interaction-tested on a physical Android device.

- The user-facing device checklist is maintained in [DEVELOPMENT_PLAN.md](DEVELOPMENT_PLAN.md), under “Next phone test session — current-head APK only”. Do not ask the user to test the older `dae5f46` artifact as if it contained commits `6a11fed`, `28167df`, `bf6752f`, or `168d15b`.
- `main` remains untouched at `89b59836c66f173756a4ae258cf379a957649820`.

### Implemented in the latest feature branch

- Persistent container membership, an empty-container flow, and a last-active-profile ID per container.
- A ViewPager2 stale-callback guard (commit `046e04b`) so a delayed event for an old page cannot silently select a different active profile; the CI build passes, while device verification remains pending.
- A list-height fix (commit `dae5f46`) replaces `fillMaxSize()` with `weight(1f)` for the profile-manager list/empty state, fake-location list/empty state, and Google-services list/empty state inside their parent Columns. The latest CI build passes; runtime scrolling and empty-state layout still need phone verification.
- Drawer profile rows (commit `6a11fed`) now follow the pinned `ConversationList.kt` pattern: one text line, no profile image or app-count subtitle, compact 6dp vertical padding, and an elevated selected surface. CI and phone screenshot review are still pending for this newest change.
- Profile cloning (commit `28167df`) now captures the source container before background work, assigns the new profile to that container even if the user switches away, and cleans up if the container disappears during the copy. CI and device timing tests are pending.
- Profile creation and cloning (commit `bf6752f`) now share a process-wide lock around `max(userId) + 1` allocation and `createUser`, preventing simultaneous requests from reusing one virtual user ID. CI and rapid-action device tests are pending.
- Pager cleanup (commit `168d15b`) now clears `pendingPagerUserId` only when it still belongs to the container refresh that scheduled the callback, preserving a newer profile selection. CI and rapid-switch device tests are pending.
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
