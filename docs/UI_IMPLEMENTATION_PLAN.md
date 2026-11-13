# Sleepy Hub UI rebuild plan — strict RikkaHub source mapping

Reference: [rikkahub/rikkahub](https://github.com/rikkahub/rikkahub), pinned at `b10c168577482abed696669a58c0eb59f1eed270`.

## Non-negotiable product model

Sleepy Hub remains a BlackBox virtual Android app manager. The mapping is:

| RikkaHub concept | Sleepy Hub concept |
|---|---|
| Assistant | **Container** |
| Assistant picker | Container picker |
| Assistant manager | Container manager |
| Assistant detail/basic settings | Container settings |
| Conversation in the active assistant | **Virtual Android profile** (one BlackBox user) |
| Assistant-owned folder | Profile folder within that container |
| Assistant avatar | Container picture |
| Assistant chat background | Container workspace background |
| Global RikkaHub theme | **One global Sleepy Hub theme** |

A container owns zero or more virtual profiles. The red/list area in the user's reference is the active container's scrollable profile list. The green selector is the active container/assistant selector. The screenshot only clarifies this relationship; the pinned RikkaHub source remains the UI and interaction specification.

### Data ownership

- **Container:** stable ID, display name, avatar/picture, optional workspace background, and background opacity.
- **Profile:** BlackBox user ID, owning container ID, display name, folder assignment, and installed-app count/status. The actual installed apps and app data remain owned by that BlackBox user.
- **Global appearance:** one global color mode/theme selection, dynamic-color setting, preset/custom theme collection, and AMOLED option. These values apply to every container and profile.
- There is no per-container theme, per-container accent, or per-profile theme/accent. Do not add one.
- A profile does not own a picture or wallpaper. The container owns the avatar and optional workspace background.

### Container behavior

1. The default container can contain the existing virtual profiles after migration.
2. Creating a new container creates an empty container only. Do not silently create a BlackBox user/profile when the container is created or selected.
3. Selecting a container filters the drawer's profile list to only that container's profiles and resets the folder filter, matching RikkaHub's assistant-switch behavior.
4. Switching back to a container restores that container's last selected profile if it still exists. If it has no profiles, show the empty black workspace and an Add Profile action.
5. Add Profile creates a real BlackBox user and assigns it to the currently selected container.
6. Selecting a profile switches the app manager to that BlackBox user. Profile app data must remain isolated.
7. Container avatar/name/background are edited in container settings. A selected background appears behind the active app-manager workspace only. With no background, the workspace is plain black.

## RikkaHub flow to reproduce

### Main shell and drawer

Source: `ChatPage.kt`, `ChatDrawer.kt`.

- Phone: Material 3 `ModalNavigationDrawer`; large/wide layout: `PermanentNavigationDrawer`.
- Match the upstream drawer width, spacing, padding, responsive header behavior, top bar, and back behavior.
- The drawer contains, in source order: user header, the agreed Sleepy Hub quick actions, folder/filter row, **an inline independently scrollable profile list**, the active container selector, and the agreed bottom navigation/settings actions.
- The profile list is not a bottom sheet. Use the source `ConversationList` row behavior: compact text rows, selected-row treatment, scrollable list, and long-press contextual actions. Do not put profile avatar tiles in this list.
- Folder filtering belongs to the selected container. Reset the filter when switching containers.
- Back closes the open drawer before navigating away. Keep a real navigation/back stack rather than a handful of string page states.

### Container selector

Source: `AssistantPicker.kt`.

- One compact drawer item near the bottom shows the active container name and its small avatar.
- Tapping it opens the source-style modal bottom sheet (approximately 80% of available height).
- Rows use the compact 32dp avatar, one-line name, selected state, and trailing edit action.
- Selecting a container switches the profile list/workspace. Editing opens container settings.
- Do not put Add Container as a giant button in the picker. Add belongs in the container manager's top-bar action, matching the source management flow.

### Container manager

Source: `AssistantPage.kt`.

- Use the source `LargeFlexibleTopAppBar`, back action, add action, rounded search field, compact reorderable list, row proportions, and item-placement animation.
- Rows represent containers and show the container avatar/name. Do not show workspace wallpaper thumbnails or add custom wide/tall cards.
- Keep only the container-management actions required by the agreed Sleepy Hub plan. Do not add AI assistant tags, cloning, prompts, models, or other RikkaHub AI features.

### Container settings

Sources: `AssistantDetailPage.kt`, `AssistantBasicPage.kt`, `BackgroundPicker.kt`, `UIAvatar.kt`.

- Follow the pinned top bar, animated title reveal, and grouped `CardGroup` settings hierarchy.
- Keep container name editing and editable container picture/avatar.
- Keep optional container workspace background: preview, gallery selection, URL option if retained from the source flow, opacity, change, and remove.
- Store/copy selected images safely in app-private storage rather than relying on a temporary gallery URI.
- The background is rendered behind the selected container's active profile workspace only. It never appears in the drawer header, container picker rows, or profile list.
- No theme controls belong on this screen. No per-container theme or accent.

### Global theme

Sources: `Theme.kt`, `SettingPreferencesThemePage.kt`, `SettingThemePage.kt`, `PresetTheme.kt`, `CustomTheme.kt`.

- One theme applies to the entire app, across all containers and profiles.
- Match RikkaHub's System/Light/Dark color mode, dynamic system colors, preset themes, custom themes, import/export, and optional AMOLED true-black mode.
- System status/navigation bar icon contrast follows the selected global mode.
- Match RikkaHub's expressive theme/motion where the existing BlackBox build toolchain can support it. Do not upgrade Gradle/Kotlin/Compose blindly; prove compatibility with the legacy BlackBox modules first.
- The workspace itself remains pure black by default. No banner, profile-summary box, decorative header, illustration, or artwork above the app grid.

### Workspace and BlackBox app manager

- Keep the installed-app grid as the main workspace and retain existing BlackBox per-user app operations.
- Preserve install, launch, app info, clear cache, clear data, stop, shortcut, uninstall, GMS, and fake-location functionality already in scope.
- Container background is optional; removing it returns to plain black.
- Do not add AI chat, a chat composer, message generation, or background-running apps in this milestone.

## Current source audit findings (2026-11-13)

The current `sleepyhub/rikkahub-source-rebuild` branch is **not yet the requested structure**:

- It has a flat list of BlackBox users. There is no separate container/assistant entity and no profile-to-container membership.
- `MainActivity` creates, selects, and deletes virtual users globally. A new empty container cannot be represented; last-profile protection is global rather than scoped to the active container.
- `ProfileAppearance` stores accent and background by virtual user ID. Those are the wrong owners; background belongs to the container, and profile/container theme accents must be removed.
- The drawer has no inline profile list. It opens a custom profile bottom sheet instead. That reverses the RikkaHub relationship: the profile list belongs inline in the drawer, while the container/assistant picker is the bottom sheet.
- The current profile picker uses profile avatars and a full-width Add Profile button. The source conversation list is text-only; the source assistant picker is the one with container avatars and trailing edit.
- Current workspace background draws an accent gradient even when no image is selected. This violates the plain-black default.
- Current profile settings have a per-profile accent and wallpaper but no uploaded picture. They need to become container settings with name, picture, and background.
- Current global appearance has basic mode/dynamic/accent/AMOLED options, but not the full RikkaHub preset/custom theme flow.
- The shell uses manual string page state and only a modal drawer; it does not yet reproduce the source typed back stack, responsive permanent drawer, or complete back flow.
- The current ViewPager adds custom scale/alpha/translation effects not present in the RikkaHub navigation flow. Re-evaluate it rather than adding more custom motion.

### Build and test status

- Current app branch head audited: `2ebae140fcd0c82c03321de648db00accaed5d98`.
- Latest ARM64 debug workflow: [43329277208](https://github.com/Cutie-Cookie/Sleepy-Hub/actions/runs/43329277208) — **failed**.
- Compiler errors: missing `background` and `CircleShape` imports in `AppGridScreen.kt`; missing `Icons.Outlined.Cloud` import in `SleepyHubMainScreen.kt`; `ListActivity.kt` imports `ActivityInstallPickerBinding` but declares `ActivityListBinding`, followed by a Compose call-context error in that picker.
- No APK was produced by that run.
- The app currently has no `app/src/test` or `app/src/androidTest` tests. Add focused tests for container/profile ownership and persistence before calling the structure complete.
- Existing per-user app operations were inspected: cache clearing targets the selected BlackBox user's internal and external cache directories; data clearing and uninstall use the selected BlackBox user ID.

## Acceptance checklist

- [ ] Existing virtual users migrate into a default container without losing their installed apps or profile names.
- [ ] Creating a new container produces an empty profile list and does not create a virtual user.
- [ ] Switching containers filters the profile list and restores the selected profile when returning.
- [ ] Add Profile creates a BlackBox user assigned only to the active container.
- [ ] The red/list area is inline and independently scrollable in the drawer.
- [ ] The green/container selector opens the compact assistant-style bottom sheet.
- [ ] Container name, picture, and background persist across restart.
- [ ] Removing the container background restores a plain black workspace.
- [ ] There are no per-container or per-profile theme/accent controls.
- [ ] Global theme changes apply identically across every container and profile.
- [ ] BlackBox app operations remain isolated by virtual user.
- [ ] Phone and large-screen drawer behavior and back navigation match the source flow.
- [ ] ARM64 debug build succeeds and is tested on the user's device.
- [ ] `main` remains untouched; no release signing secrets are created in this milestone.

## License boundary

RikkaHub is AGPL-3.0; Sleepy Hub currently declares Apache-2.0. Reproduce the source behavior and component structure with original Sleepy Hub code. Do not paste upstream source files or assets into the Apache-2.0 project. Literal source reuse would require an explicit licensing decision and compliance review.


## Additional confirmed interaction details — 2026-11-13

### Profile long-press and moving between containers

RikkaHub's exact path is `ConversationList.kt` → `ChatDrawer.kt` → `ChatVM.kt` → `ChatService.kt` → `ConversationRepository.kt` → `ConversationDAO.kt`.

- A normal row tap selects/opens the row; a long press opens a compact dropdown menu.
- RikkaHub's menu includes Pin/Unpin, Regenerate title, Move to assistant, Move to folder, and Delete.
- Move to assistant opens a scrollable modal bottom sheet listing assistants. Selecting one moves the existing conversation.
- The service updates the active session and clears its assistant-scoped folder assignment before persisting the new assistant ID.
- If the currently open conversation is moved, RikkaHub also switches the selected assistant.

Sleepy Hub equivalent:
- Long-press a profile row in the inline drawer list.
- Provide the applicable actions: Move to container, Move to folder, Rename, Delete. Do not add AI-only actions such as Regenerate title.
- Move to container opens a scrollable sheet listing containers with their small pictures/names and a selected/current indicator.
- Moving updates only the profile's container membership. Preserve the exact BlackBox user ID, installed apps, app data, and profile name.
- If the active profile is moved, select the target container and keep that same profile active.
- Reset the profile's container-scoped folder assignment to the unfiled/default state, following RikkaHub's assistant-scoped folder semantics.

### Dashboard placement and action ownership

- **Add Profile:** a small plus icon on the dashboard beside the inline profile-list heading. It creates one BlackBox user inside the active container. Do not use a large full-width Add button.
- **Add Container:** in the Container Manager top app bar, following RikkaHub's Assistant Manager top-bar Add action. It creates an empty container and no BlackBox user.
- **Switch Container:** the compact AssistantPicker-style selector near the bottom of the dashboard drawer; it opens the container picker sheet.
- **Edit Container:** trailing edit action on a row in the container picker, or selecting a container row in the manager.
- **Container Manager:** accessible from the dashboard footer (RikkaHub's Assistant drawer action) and Settings home → General Settings → Assistant.
- **Settings:** keep one Settings shortcut at the dashboard's bottom-right, matching RikkaHub's drawer footer. Theme is under Settings → Preferences → Theme.
- **Add App:** reuse the top-right action position occupied by RikkaHub's `MessageAdd01` New Message button in `ChatPage.kt` `TopBar`. It opens the Sleepy Hub app picker for the active BlackBox profile.
- **No top-right dots:** remove Sleepy Hub's MoreVert overflow menu. Its current actions are already owned elsewhere: profile editing through the profile row/manager, container management through the manager, app refresh through the dashboard Refresh shortcut, and global settings through the bottom-right Settings shortcut. Do not replace it with another popup.

### Add-app picker UI

RikkaHub does not have a BlackBox app installer screen. Adapt the app picker using the same page hierarchy rather than copying an AI page:
- Scaffold + LargeFlexibleTopAppBar, back action, Choose APK action.
- Rounded search field and compact scrolling app rows.
- Actual application icon, app name, secondary package name, and trailing Add/Installed status.
- Already-installed apps are not selectable for duplicate installation.
- Preserve loading, empty, no-match, APK selection, install progress/result, and selected-profile routing.
- Avoid a grid of oversized cards, banners, gradients, decorative artwork, or cosmetic-only restyling.

### Storage access popup

- The existing All Files Access request remains required; the Android system Settings page and permission intent remain unchanged.
- Replace only the old native `MaterialAlertDialogBuilder` explanation with a Compose Material 3 rationale dialog following the hierarchy of RikkaHub's `PermissionRationaleDialog.kt`: rounded dialog surface, icon, clear title and reason, and Cancel/Later + Grant Permission actions.
- Grant Permission launches the same Android All Files Access Settings intent. Never claim permission succeeded until the activity result confirms `Environment.isExternalStorageManager()`.

### Container deletion

- Deleting a container always opens a confirmation popup with **Cancel** and **Delete**.
- The dialog must name the container and clearly state what happens to profiles and installed apps/data inside it. Never silently delete BlackBox users or their app data.
- The final cascade-vs-move policy for a non-empty container must be confirmed before implementing destructive deletion.
