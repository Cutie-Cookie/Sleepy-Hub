# RikkaHub source-level UI audit

_Reviewed: 2026-11-13._

## Reference pinned for this audit

- Repository: [rikkahub/rikkahub](https://github.com/rikkahub/rikkahub)
- Inspected commit: `b10c168577482abed696669a58c0eb59f1eed270`
- License: AGPL-3.0
- Sleepy Hub remains Apache-2.0. This is a behavioral and visual reference only; do not copy upstream source files, assets, branding, or distinctive code.

## User's non-negotiable direction

The **actual RikkaHub source code** is the UI specification. User screenshots are context only and must not drive dimensions, component hierarchy, navigation, or animation decisions.

Sleepy Hub remains a BlackBox-based virtual Android app manager:
- A profile is a virtual Android user.
- The workspace is that profile's installed-app manager.
- No AI providers, chat messages, conversation history, or chat composer.
- Profile-specific workspace wallpaper is wanted.
- Global and per-profile customization are wanted.
- Background-running apps are deferred to a later update.
- Do not reintroduce VPN or remote log uploads.
- Keep `main` untouched; use the dedicated rebuild branch.

## RikkaHub components inspected

### 1. Main navigation — `ChatPage.kt`

[Source](https://github.com/rikkahub/rikkahub/blob/b10c168577482abed696669a58c0eb59f1eed270/app/src/main/java/me/rerere/rikkahub/ui/pages/chat/ChatPage.kt)

- Phone layout uses `ModalNavigationDrawer`; larger layouts use `PermanentNavigationDrawer`.
- The main content is hosted by Material 3 `Scaffold`.
- The top bar is transparent and compact, with a menu action, title/subtitle, and contextual actions.
- Back handling closes the drawer before navigating away.
- The drawer is a real navigation surface, not a separate full-screen dashboard pretending to be one.

### 2. Side dashboard — `ChatDrawer.kt`

[Source](https://github.com/rikkahub/rikkahub/blob/b10c168577482abed696669a58c0eb59f1eed270/app/src/main/java/me/rerere/rikkahub/ui/pages/chat/ChatDrawer.kt)

- `ModalDrawerSheet` width: 300dp.
- Outer drawer content uses 8dp padding and compact 8dp vertical spacing.
- Header: 50dp avatar, editable nickname, greeting.
- Compact quick actions follow the header.
- Folder chips and sorting controls share a row.
- The central list receives the remaining height and scrolls independently.
- The active assistant picker sits near the bottom.
- Utility/navigation shortcuts and Settings form the footer.
- On short screens, the header joins the scrolling list instead of pushing the useful content away.

### 3. Active profile selector — `AssistantPicker.kt`

[Source](https://github.com/rikkahub/rikkahub/blob/b10c168577482abed696669a58c0eb59f1eed270/app/src/main/java/me/rerere/rikkahub/ui/components/ai/AssistantPicker.kt)

- The drawer selector is one compact `NavigationDrawerItem`, not a tall profile card.
- It shows the current assistant name and a small avatar/edit action.
- Tapping it opens a Material 3 `ModalBottomSheet` at approximately 80% screen height.
- The sheet has a title, optional horizontal tag filters, and a scrolling list.
- Each selectable row is a compact card containing a `ListItem`, a 32dp avatar, one-line name, and trailing edit action.
- Selected state uses the theme's `primaryContainer`.
- Add/edit/select actions must remain obvious and must not become oversized tiles.

### 4. Management list — `AssistantPage.kt`

[Source](https://github.com/rikkahub/rikkahub/blob/b10c168577482abed696669a58c0eb59f1eed270/app/src/main/java/me/rerere/rikkahub/ui/pages/assistant/AssistantPage.kt)

- Uses `LargeFlexibleTopAppBar` with back and add actions.
- Search uses a rounded, single-line Material 3 `TextField`.
- Tags filter the list.
- The list is reorderable and items use Compose item-placement animation.
- Each row is a full-width 20dp rounded card with 12dp vertical padding.
- Row layout: 48dp avatar, title, compact secondary metadata/tags, trailing action menu.
- Create, edit, clone, reorder, and delete are distinct actions. Destructive deletion is confirmed.

Sleepy Hub adaptation: use the same row proportions and hierarchy; show profile name, folder, installed-app count/status, and a trailing profile action menu. Do not place wallpaper thumbnails in these rows.

### 5. Detail/settings — `AssistantDetailPage.kt`

[Source](https://github.com/rikkahub/rikkahub/blob/b10c168577482abed696669a58c0eb59f1eed270/app/src/main/java/me/rerere/rikkahub/ui/pages/assistant/detail/AssistantDetailPage.kt)

- Uses a pinned top bar.
- The title fades/slides into the app bar after the large header scrolls away (`AnimatedVisibility`, `fadeIn`, `slideInVertically`).
- Settings are grouped into consistent `CardGroup` sections.
- Detail actions navigate to focused settings pages instead of opening a pile of unrelated legacy popups.

Sleepy Hub adaptation: profile identity, folder, accent, workspace background, grid layout, and virtual-profile management should be grouped into clear sections.

### 6. Workspace wallpaper — `Background.kt` and `BackgroundPicker.kt`

[Background renderer](https://github.com/rikkahub/rikkahub/blob/b10c168577482abed696669a58c0eb59f1eed270/app/src/main/java/me/rerere/rikkahub/ui/pages/chat/Background.kt) · [Picker](https://github.com/rikkahub/rikkahub/blob/b10c168577482abed696669a58c0eb59f1eed270/app/src/main/java/me/rerere/rikkahub/ui/pages/assistant/detail/BackgroundPicker.kt)

- Background is rendered behind the active assistant's chat workspace.
- Image opacity is independently configurable.
- A gradient/overlay preserves text and content readability.
- The settings page previews the current image and offers Change and Remove actions.
- Gallery selection and URL entry are supported upstream.

Sleepy Hub adaptation: the image belongs behind the active profile's app grid, not on the drawer header, profile picker, or management cards. Keep preview, opacity, change, remove, and persistent URI permission handling.

### 7. Global theme — `Theme.kt` and `SettingThemePage.kt`

[Theme](https://github.com/rikkahub/rikkahub/blob/b10c168577482abed696669a58c0eb59f1eed270/app/src/main/java/me/rerere/rikkahub/ui/theme/Theme.kt) · [Theme settings](https://github.com/rikkahub/rikkahub/blob/b10c168577482abed696669a58c0eb59f1eed270/app/src/main/java/me/rerere/rikkahub/ui/pages/setting/SettingThemePage.kt)

- System / Light / Dark appearance.
- Dynamic system colors when supported.
- Preset and custom color themes.
- Optional AMOLED true-black mode.
- Theme and system-bar colors stay synchronized.
- Uses Material Expressive motion and Material 3 components.

Sleepy Hub adaptation: preserve global mode, dynamic color, global accent and AMOLED settings; evaluate preset/custom theme support. Per-profile accent and wallpaper remain independent from global appearance.

## Current Sleepy Hub rebuild branch audit

Branch: `sleepyhub/rikkahub-source-rebuild`, based on `sleepyhub/identity-icons-no-vpn`.

Files inspected:
- [SleepyHubMainScreen.kt](https://github.com/Cutie-Cookie/Sleepy-Hub/blob/sleepyhub/rikkahub-source-rebuild/app/src/main/java/top/niunaijun/blackboxa/view/main/SleepyHubMainScreen.kt)
- [ProfileScreens.kt](https://github.com/Cutie-Cookie/Sleepy-Hub/blob/sleepyhub/rikkahub-source-rebuild/app/src/main/java/top/niunaijun/blackboxa/view/main/ProfileScreens.kt)
- [AppGridScreen.kt](https://github.com/Cutie-Cookie/Sleepy-Hub/blob/sleepyhub/rikkahub-source-rebuild/app/src/main/java/top/niunaijun/blackboxa/view/apps/AppGridScreen.kt)
- [MainActivity.kt](https://github.com/Cutie-Cookie/Sleepy-Hub/blob/sleepyhub/rikkahub-source-rebuild/app/src/main/java/top/niunaijun/blackboxa/view/main/MainActivity.kt)
- [SleepyHubTheme.kt](https://github.com/Cutie-Cookie/Sleepy-Hub/blob/sleepyhub/rikkahub-source-rebuild/app/src/main/java/top/niunaijun/blackboxa/view/main/SleepyHubTheme.kt)

### What is already useful

- Compose is enabled and hosted by `ComposeView`.
- The app grid is rendered through a Compose screen inside the existing profile fragment.
- The profile picker already uses a Material 3 bottom sheet.
- Per-profile wallpaper is rendered behind the workspace, not inside the picker.
- Per-profile name, folder, accent, wallpaper, opacity, and global appearance preferences are persisted.
- App actions use a Compose bottom sheet and confirmations.
- The repository has explicit per-user cache-clearing logic.

### What still needs source-faithful correction

- `SleepyHubMainScreen` uses manual string-based page state instead of a clear navigation/back-stack model.
- The drawer's custom `DrawerEntry` and `DrawerShortcut` surfaces are not yet the same component hierarchy as RikkaHub's `ChatDrawer`.
- The current picker has custom folder/sort controls and a full-width Add button; align the row hierarchy, spacing, selected state, and action placement with the upstream picker.
- `ProfileManagerScreen` uses `LargeTopAppBar`, an outlined search field, and a basic list; align it with `LargeFlexibleTopAppBar`, rounded search, filter row, compact assistant-style rows, and reorder animation.
- `ProfileSettingsScreen` and global settings use custom cards; align section hierarchy, spacing, and scroll behavior with RikkaHub's `CardGroup`-based detail/settings pages.
- `SleepyHubTheme` currently uses ordinary `MaterialTheme`; inspect compatible options for Material Expressive motion without destabilizing the existing Kotlin 1.9.23 toolchain.
- The shell embeds a `ViewPager2` through `AndroidView`; preserve BlackBox behavior while deciding whether the main navigation can be made Compose-native without risky backend changes.
- Check legacy Android View dialogs/menus in the primary flows. Replace only app-owned legacy UI that conflicts with the new Material 3 shell; keep necessary Android permission/system flows functional.

## Implementation sequence

1. Keep this source audit pinned and use it as the review checklist.
2. Rework the main shell and drawer against `ChatPage` / `ChatDrawer`.
3. Rework the picker and profile-management rows against `AssistantPicker` / `AssistantPage`.
4. Rework profile detail and wallpaper controls against `AssistantDetailPage` / `AssistantBasicPage` / `BackgroundPicker`.
5. Rework global appearance against `Theme.kt` / `SettingThemePage`.
6. Audit app-action sheets and permission/error surfaces for consistent Material 3 treatment.
7. Build after each coherent milestone, inspect the APK, and run the device acceptance checklist before asking the user to test.
8. Do not create release signing keys or secrets until the user approves the debug build.

## Acceptance guardrails

- No AI chat functionality.
- No giant profile tiles or wallpaper thumbnails in profile rows.
- No Sleepy Hub launcher icon used as dashboard decoration.
- Wallpaper only behind the active profile workspace; it can be changed, previewed, adjusted, and removed.
- Add Profile is always easy to find.
- Profile switching must switch the actual BlackBox virtual user and app list.
- All profile and global appearance settings survive restart.
- Clear Cache and Clear Data remain distinct; Clear Data warns before resetting app data.
- No background-running feature in this milestone.
- No VPN or remote log upload is reintroduced.
- `main` remains untouched.


## Latest user correction — strict UI fidelity and black workspace (2026-11-13)

The user explicitly requires: **100% RikkaHub UI reference fidelity; do not add anything beyond the agreed Sleepy Hub plan.** RikkaHub's actual source code defines the layout, dimensions, component hierarchy, spacing, typography, visual surfaces, and animations. Screenshots are not a competing specification.

Hard rules:
- Keep the app-manager workspace plain black by default.
- Do not add a box, banner, hero card, profile summary panel, illustration, artwork, or decorative header above the app grid.
- Do not add a large Sleepy Hub logo to the workspace or drawer as decoration.
- Do not use the rejected wide/tall custom profile cards. Use the compact RikkaHub picker and management-list patterns from source.
- Profile wallpaper is optional and only appears behind the selected profile's app-manager workspace when the user explicitly chooses an image. Removing it restores plain black. No wallpaper thumbnail in the drawer, picker, or profile list.
- Keep only the agreed product adaptations: virtual profiles, profile selection/addition/management, folders/profile organization already in scope, virtual app management, global and per-profile customization, and optional profile workspace wallpaper.
- No AI chat, no background-running feature in this milestone, no speculative shortcuts, and no additional unrequested UI/feature ideas.
- Reproduce RikkaHub's own navigation and interaction surfaces rather than replacing them with custom dialogs or invented controls. Android system permission surfaces remain system-owned.
