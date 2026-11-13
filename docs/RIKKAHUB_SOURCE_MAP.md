# RikkaHub source map — Sleepy Hub implementation reference

Reference repository: [rikkahub/rikkahub](https://github.com/rikkahub/rikkahub)

Pinned source revision: `b10c168577482abed696669a58c0eb59f1eed270`

This is the file-by-file index for implementing Sleepy Hub's UI and behavior. Keep this document beside the implementation plan and use it before changing a screen. Do not guess where a feature lives: start from the paths below, inspect the current upstream revision, then adapt the behavior to Sleepy Hub's data model.

## Product mapping

| RikkaHub | Sleepy Hub |
|---|---|
| Assistant | **Container** |
| Assistant picker | Container picker |
| Assistant manager | Container manager |
| Assistant detail/basic page | Container details/settings |
| Conversation row | **Virtual Android profile** (one BlackBox user) |
| Move conversation to assistant | Move profile to container |
| Conversation folder | Profile folder scoped to its container |
| Assistant avatar | Container picture |
| Assistant background | Container workspace background |
| Global theme settings | One global Sleepy Hub theme |
| New conversation top-bar action | **Add app** action |
| New profile action in dashboard | Small **+** beside the inline profile-list heading (Sleepy Hub-specific placement) |

A container contains zero or more BlackBox profiles. Creating a container creates no BlackBox user. Moving a profile changes only its container membership; preserve its BlackBox user ID and all installed apps/data.

## 1. Main screen, drawer, and profile list

| UI responsibility | RikkaHub source | What to study |
|---|---|---|
| Main chat/workspace shell | `app/src/main/java/me/rerere/rikkahub/ui/pages/chat/ChatPage.kt` | `ChatPage`, `ChatPageContent`, phone vs large-screen shell, drawer, Scaffold, back handling |
| Top bar | `ChatPage.kt` | `TopBar` around line 636; menu button, title/subtitle, new-chat action, overflow actions |
| Drawer layout and order | `app/src/main/java/me/rerere/rikkahub/ui/pages/chat/ChatDrawer.kt` | `ChatDrawerContent`, header, quick actions, folder row, independently scrolling list, assistant picker, footer |
| Drawer state/data | `app/src/main/java/me/rerere/rikkahub/ui/pages/chat/ChatDrawerVM.kt` | Assistant-scoped list/folders, selected folder, sort order, scroll position, resetting folder filter when switching assistant |
| Scrollable list and row UI | `app/src/main/java/me/rerere/rikkahub/ui/pages/chat/ConversationList.kt` | Lazy list, date/pinned separators, selected row, text-only row, long-press menu |
| Folder row/actions | `ChatDrawer.kt` | `FolderBar`, folder chips, create/rename/delete dialogs, sort control |

### Long-press menu and moving between containers

The exact upstream interaction is split across these files:

1. `ConversationList.kt`: `ConversationItem` uses `combinedClickable`. Normal click opens the row; long press opens a `DropdownMenu`.
2. The menu contains Pin/Unpin, Regenerate title, **Move to assistant**, Move to folder, and Delete.
3. `ChatDrawer.kt`: `onMoveToAssistant` stores the selected conversation and opens a `ModalBottomSheet`. The sheet is capped at 400dp, has a title and scrollable assistant rows, and marks the current assistant.
4. `ChatVM.kt`: `moveConversationToAssistant(conversation, targetAssistantId)` calls the service. If the moved conversation is the currently open one, it also selects the target assistant.
5. `service/ChatService.kt`: `moveConversationToAssistant` updates the active session state and clears the old folder assignment before persistence.
6. `data/repository/ConversationRepository.kt`: `updateConversationAssistant` persists the new assistant ID.
7. `data/db/dao/ConversationDAO.kt`: `updateAssistantId` updates `assistant_id` and resets `folder_id`.

**Sleepy Hub adaptation:** long-press a profile row and show a compact menu with the applicable profile actions, including **Move to container**, Move to folder, Rename, and Delete. Move opens the same kind of bottom sheet listing containers. The move must update only the profile's container ID; it must not recreate the BlackBox user, reinstall apps, or clear profile data. If the currently active profile is moved, switch the active container to the target and keep that same profile selected. Reset its container-scoped folder assignment to the unfiled/default state, matching RikkaHub's move semantics.

Do not add RikkaHub-only actions that have no Sleepy Hub equivalent (such as regenerating an AI-generated chat title).

## 2. Container picker, manager, and details

| UI responsibility | RikkaHub source | What to study |
|---|---|---|
| Compact active-assistant selector | `app/src/main/java/me/rerere/rikkahub/ui/components/ai/AssistantPicker.kt` | Drawer item, current assistant name/avatar, picker sheet |
| Picker rows | `AssistantPicker.kt` | ~80%-height sheet, optional horizontal filters, selected card, 32dp avatar, one-line name, trailing edit action |
| Assistant manager | `app/src/main/java/me/rerere/rikkahub/ui/pages/assistant/AssistantPage.kt` | LargeFlexibleTopAppBar, top-bar Add button, rounded search, filter row, reorderable list, compact 20dp rows/cards, animation |
| Manager state | `app/src/main/java/me/rerere/rikkahub/ui/pages/assistant/AssistantVM.kt` | Add, update, reorder, remove operations |
| Add assistant sheet | `AssistantPage.kt`, `AssistantCreationSheet` | Modal sheet, name entry, Cancel/Save |
| Manager row actions | `AssistantPage.kt`, `AssistantItem` | Row click/edit, trailing action menu, delete confirmation |
| Detail overview | `app/src/main/java/me/rerere/rikkahub/ui/pages/assistant/detail/AssistantDetailPage.kt` | Pinned top bar, animated title reveal, detail header, grouped CardGroup sections |
| Basic identity editor | `app/src/main/java/me/rerere/rikkahub/ui/pages/assistant/detail/AssistantBasicPage.kt` | Avatar edit, name field, grouped settings |
| Avatar component | `app/src/main/java/me/rerere/rikkahub/ui/components/ui/UIAvatar.kt` | Avatar display/edit/crop behavior |
| Background picker | `app/src/main/java/me/rerere/rikkahub/ui/pages/assistant/detail/BackgroundPicker.kt` | Preview, choose/change/remove, optional URL, opacity |
| Detail persistence/cleanup | `app/src/main/java/me/rerere/rikkahub/ui/pages/assistant/detail/AssistantDetailVM.kt` | Update assistant and cleanup of replaced avatar/background files |

### Exact entry points in RikkaHub

- **Switch active assistant:** the compact `AssistantPicker` item near the bottom of the chat drawer. Tap the row to open the picker sheet.
- **Edit the current assistant:** tap its avatar in the compact picker row; this calls `onClickSetting` and navigates to `Screen.AssistantDetail`.
- **Edit any assistant:** tap the trailing edit button on a row in the picker sheet.
- **Open assistant manager:** the `Assistant` action in the drawer footer navigates to `Screen.Assistant`.
- **Alternative manager entry:** Settings home → General Settings → Assistant.
- **Add assistant:** open the manager; tap the small Add icon in `AssistantPage`'s top app bar. This opens `AssistantCreationSheet`; Save adds it to settings. There is no giant Add button in the picker sheet.
- **Delete assistant:** manager row action → confirmation dialog → Delete. The upstream `AssistantVM.removeAssistant` also deletes that assistant's conversations and owned files. Sleepy Hub must show a clear warning about any profile/app data that would be removed before confirming container deletion.

**Sleepy Hub adaptation:** the compact picker selects containers, not profiles. The manager lists containers. The manager top-bar plus creates an empty container. The container detail editor contains only the agreed identity fields: name, picture/avatar, and optional workspace background/opacity. There are no theme/accent controls on a container or profile.

## 3. Settings entry points and global theme

| UI responsibility | RikkaHub source | What to study |
|---|---|---|
| Settings home | `app/src/main/java/me/rerere/rikkahub/ui/pages/setting/SettingPage.kt` | LargeFlexibleTopAppBar, LazyColumn, CardGroup sections |
| Settings home navigation | `SettingPage.kt` | General Settings, Preferences, Assistant manager, data/settings groups |
| Preferences index | `app/src/main/java/me/rerere/rikkahub/ui/pages/setting/SettingPreferencesPage.kt` | CardGroup links to Theme, Notifications, General, UI, Network |
| Global theme preferences | `app/src/main/java/me/rerere/rikkahub/ui/pages/setting/SettingPreferencesThemePage.kt` | Dynamic color, theme settings link, AMOLED option |
| Presets/custom themes | `app/src/main/java/me/rerere/rikkahub/ui/pages/setting/SettingThemePage.kt` | Preset selection, custom theme add/edit/delete, import/export |
| Theme components | `app/src/main/java/me/rerere/rikkahub/ui/pages/setting/components/PresetThemeButton.kt`, `CustomThemeButton.kt` | Theme preview and selection components |
| Theme runtime | `app/src/main/java/me/rerere/rikkahub/ui/theme/Theme.kt`, `CustomTheme.kt`, `PresetTheme.kt` | System/Light/Dark, dynamic color, AMOLED, global scheme, system bar colors, expressive motion |
| Theme persistence | `app/src/main/java/me/rerere/rikkahub/data/datastore/Settings.kt`, `SettingsFields.kt` | Global settings model and serialized fields |
| Navigation/routes | `app/src/main/java/me/rerere/rikkahub/RouteActivity.kt` | Typed `Screen` destinations and `NavDisplay` route registration |

### Settings navigation hierarchy

1. Chat drawer footer, bottom-right **Settings** action → `Screen.Setting`.
2. Settings home has a General Settings group. It contains:
   - Color mode (System/Light/Dark) directly on the page.
   - Preferences.
   - Assistant manager.
   - Extensions.
3. Preferences → Theme.
4. Theme preferences include Dynamic Color, Theme Settings, and AMOLED Dark Mode.
5. Theme Settings contains preset themes and custom themes, including create/edit/delete and import/export.

For Sleepy Hub, preserve the dashboard's bottom-right Settings shortcut. It is the one place for global app settings. Container manager can also be linked from the dashboard footer and from Settings, matching RikkaHub's Assistant entry points. Do not retain a duplicate top-right overflow menu for these actions.

## 4. New-chat action and Sleepy Hub's Add App action

- RikkaHub's new-chat icon is in `app/src/main/java/me/rerere/rikkahub/ui/pages/chat/ChatPage.kt`, inside the private `TopBar` function (around line 718). It calls `onNewChat()` and renders `HugeIcons.MessageAdd01`.
- Sleepy Hub reuses that top-right action position for **Add app / Install app**. It opens Sleepy Hub's app picker for the currently active BlackBox profile. Do not create a chat page or chat composer.
- The top-right three-dot/overflow menu is not needed in Sleepy Hub. Remove it. Its current actions must be reachable through the dashboard: Settings bottom-right, Container manager, profile settings/context menu, and the existing Refresh shortcut.
- The profile-list heading in the dashboard gets a small plus button for **Add Profile**. This is separate from Add Container and separate from Add App.
- Add Container lives in the Container Manager top app bar, mirroring RikkaHub's Assistant Manager Add action.

## 5. App picker and storage permission — Sleepy Hub-specific screens

RikkaHub does not contain Sleepy Hub's BlackBox app-install workflow. Use its component/layout patterns, not its AI-specific screens.

### Current Sleepy Hub files to replace/refine

- `app/src/main/java/top/niunaijun/blackboxa/view/list/InstalledAppPickerScreen.kt`: current app selection UI.
- `app/src/main/java/top/niunaijun/blackboxa/view/list/ListActivity.kt`: hosts the picker and returns the selected APK source.
- `app/src/main/java/top/niunaijun/blackboxa/view/list/ListViewModel.kt` and `data/AppsRepository.kt`: app discovery, installed-state checks, and app data.
- `app/src/main/java/top/niunaijun/blackboxa/view/main/MainActivity.kt`: launches app installation and requests storage access.

### App picker acceptance design

- Use the same page structure as RikkaHub: Scaffold, LargeFlexibleTopAppBar with back and Choose APK action, rounded search field, compact scrolling list.
- Use actual installed app icons, one-line app name, secondary package name, and a clear trailing Add/Installed state. Installed apps should not be selectable for duplicate installation.
- Selecting an available app installs it into the currently selected BlackBox user. Choosing an APK remains available from the top-bar action.
- Preserve loading, empty, no-search-results, and install-result states. No oversized decorative cards, hero art, gradient banners, or fake restyling without interaction fixes.
- Fix the current binding mismatch in `ListActivity.kt` (`ActivityInstallPickerBinding` import vs `ActivityListBinding` property) before building.

### Storage permission dialog

- Current source: `MainActivity.checkStoragePermission()` and `showStoragePermissionDialog()`, which uses `MaterialAlertDialogBuilder`.
- Keep the actual Android All Files Access permission flow and the system Settings screen unchanged.
- Replace only Sleepy Hub's explanatory popup with a native Compose Material 3 rationale dialog inspired by RikkaHub's:
  - `app/src/main/java/me/rerere/rikkahub/ui/components/ui/permission/PermissionRationaleDialog.kt`
  - `PermissionManager.kt`, `PermissionCards.kt`
- RikkaHub uses a Dialog with a rounded Card, 48dp icon, centered title/description, permission details, and Cancel plus Confirm/Go to Settings actions.
- Sleepy Hub's dialog should explain why BlackBox needs storage access, provide Cancel/Later and Grant Permission, and then launch the same app-specific All Files Access Settings intent. It must not falsely imply access was granted before returning from Android Settings.

## 6. Confirmation dialogs

- RikkaHub's reusable confirmation component is `ui/src/main/java/me/rerere/ui/components/ConfirmDialog.kt` (`RikkaConfirmDialog`).
- The assistant manager uses it in `AssistantPage.kt` for Delete/Cancel.
- Sleepy Hub container deletion must show a confirmation popup with **Cancel** and **Delete**. If the container owns profiles, the dialog must clearly state what happens to those profiles and their installed apps/data. Do not perform destructive deletion before the user presses Delete.
- Use the same confirmation pattern for profile deletion and destructive app actions where appropriate.

## 7. Navigation and route registration

- Main route host: `app/src/main/java/me/rerere/rikkahub/RouteActivity.kt`.
- Screen types are registered through `NavDisplay`'s `entryProvider`.
- Assistant manager route: `Screen.Assistant`.
- Assistant detail route: `Screen.AssistantDetail(id)`.
- Assistant basic editor route: `Screen.AssistantBasic(id)`.
- Settings routes: `Screen.Setting`, `Screen.SettingPreferences`, `Screen.SettingPreferencesTheme`, `Screen.SettingTheme`.
- Chat drawer back handling is in `ChatPage.kt`; route back-stack handling is in `RouteActivity.kt`.

Sleepy Hub should replace string-based page flags with a clear typed navigation/back-stack model only after confirming it can coexist with the existing BlackBox Activity/Fragment/ViewPager backend. Do not replace or destabilize the BlackBox backend just to imitate a navigation library.

## 8. License and implementation rule

RikkaHub is AGPL-3.0; Sleepy Hub currently declares Apache-2.0. Use this source map to reproduce the component hierarchy, behavior, and flow with original Sleepy Hub code. Do not paste RikkaHub source files/assets into Sleepy Hub under its current license. Any decision to reuse upstream code requires an explicit licensing change and compliance review.
