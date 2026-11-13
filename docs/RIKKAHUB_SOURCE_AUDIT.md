# RikkaHub source audit and Sleepy Hub code review

Reviewed 2026-11-13.

## Reference

- Upstream: [rikkahub/rikkahub](https://github.com/rikkahub/rikkahub)
- Pinned commit: `b10c168577482abed696669a58c0eb59f1eed270`
- Sleepy Hub branch audited: `sleepyhub/rikkahub-source-rebuild`
- Audited Sleepy Hub head: `2ebae140fcd0c82c03321de648db00accaed5d98`
- App baseline: `sleepyhub/identity-icons-no-vpn`
- `main` remains untouched; the rebuild branch is separate.

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
| Assistant chat background | Container workspace background |
| Global theme | One global Sleepy Hub theme |

A container owns zero or more virtual profiles. The inline scrollable list in RikkaHub's drawer maps to Sleepy Hub profiles. The compact assistant selector near the drawer bottom maps to containers. A new container starts empty; it must not auto-create a BlackBox user. Switching containers filters the profile list and restores the last selected profile when returning.

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

## Sleepy Hub code audit

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

### Current build failure

Latest ARM64 debug workflow: [43329277208](https://github.com/Cutie-Cookie/Sleepy-Hub/actions/runs/43329277208) — failed; no APK artifact was produced.

Compiler output:
- `AppGridScreen.kt`: unresolved `background` and `CircleShape` references (missing imports).
- `SleepyHubMainScreen.kt`: unresolved `Icons.Outlined.Cloud` (missing import).
- `ListActivity.kt`: imports `ActivityInstallPickerBinding` but declares `ActivityListBinding`; the file also reports a Compose call-context error.
- These compile errors must be fixed and the workflow rerun before any debug APK is offered.

There are currently no files under `app/src/test` or `app/src/androidTest`. Add tests for container/profile ownership, empty containers, last-selected-profile restoration, and persistence.

## Required implementation order

1. Add a persistent container model and a migration that assigns existing virtual users to a default container without touching their BlackBox app data.
2. Make profile membership container-scoped; store last active profile per container.
3. Rebuild the drawer to show the inline scrollable profile list and the bottom container selector.
4. Add the assistant-style container picker and container manager.
5. Move name/avatar/background to container settings; remove profile-level accent/background.
6. Replace the theme controls with one global RikkaHub-style theme flow.
7. Keep the app workspace black by default and render only the selected container's optional background behind it.
8. Fix compile errors, add focused tests, run CI, then device-test the full flow.

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

### Sleepy Hub's own UI issues to fix

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
