# RikkaHub source-code research

_Reviewed: 2026-11-13. Reference repository: [rikkahub/rikkahub](https://github.com/rikkahub/rikkahub), inspected at commit `b10c168577482abed696669a58c0eb59f1eed270`._

## Source of truth

The user has clarified that the **RikkaHub source code is the UI reference**. Screenshots are context only and must not be used as the implementation specification.

Do not infer component dimensions, navigation behavior, animations, or sheet interactions from screenshots. Inspect the relevant upstream source and follow its actual structure and interaction patterns.

## License boundary

- RikkaHub is licensed under **GNU Affero General Public License v3.0 (AGPL-3.0)**.
- Sleepy Hub currently uses **Apache License 2.0**.
- This redesign will use RikkaHub as a reference and implement original Sleepy Hub components. Do not copy RikkaHub source files, distinctive code, assets, icons, names, or branding into Sleepy Hub.
- If direct source-code reuse is ever proposed, stop and review license compatibility and required notices/source-distribution obligations first.

## Actual RikkaHub implementation examined

### Main chat navigation

Source: [ChatPage.kt](https://github.com/rikkahub/rikkahub/blob/b10c168577482abed696669a58c0eb59f1eed270/app/src/main/java/me/rerere/rikkahub/ui/pages/chat/ChatPage.kt)

- The phone layout uses `ModalNavigationDrawer` with `ChatDrawerContent`.
- The chat content is inside a Material 3 `Scaffold`.
- The top app bar is transparent and uses a menu-button capsule; the title and its secondary model/provider line are aligned beside it.
- The top bar has contextual actions, including new-chat and overflow actions.
- The bottom input is a floating, rounded surface with a border and optional blur/haze treatment. It is not a large rectangular form permanently attached to the bottom.
- The drawer is the primary navigation surface on phones; back handling closes it.

### Drawer structure

Source: [ChatDrawer.kt](https://github.com/rikkahub/rikkahub/blob/b10c168577482abed696669a58c0eb59f1eed270/app/src/main/java/me/rerere/rikkahub/ui/pages/chat/ChatDrawer.kt)

- Uses Material 3 `ModalDrawerSheet` with a 300dp width.
- Drawer content is a vertically composed layout with 8dp spacing and 8dp outer padding.
- Header: a 50dp user avatar, editable nickname, and greeting.
- Quick navigation actions are grouped into a compact horizontal row.
- A horizontal folder-chip row and sort control sit together.
- The conversation list occupies the remaining vertical space and can scroll independently.
- The current assistant picker and utility/settings actions are placed in the drawer's navigation structure.
- On short screens, the header can become part of the scrollable list rather than forcing the content below the fold.

### Assistant picker

Source: [AssistantPicker.kt](https://github.com/rikkahub/rikkahub/blob/b10c168577482abed696669a58c0eb59f1eed270/app/src/main/java/me/rerere/rikkahub/ui/components/ai/AssistantPicker.kt)

- The drawer shows the active assistant as a `NavigationDrawerItem`, with its name and avatar.
- Tapping it opens a `ModalBottomSheet` that uses up to 80% of the available height.
- The sheet contains a title, optional horizontal tag filters, and a vertically scrolling assistant list.
- Each assistant is a selectable Material 3 card; the selected one uses `primaryContainer`.
- The assistant row uses a 32dp avatar, one-line name, and a trailing edit action.
- Selection updates the active assistant; editing opens its detail screen.

### Assistant management

Source: [AssistantPage.kt](https://github.com/rikkahub/rikkahub/blob/b10c168577482abed696669a58c0eb59f1eed270/app/src/main/java/me/rerere/rikkahub/ui/pages/assistant/AssistantPage.kt)

- Uses `LargeFlexibleTopAppBar` with a back action and add action.
- Search and tag filters narrow the list.
- The list is a reorderable `LazyColumn`; items animate into their new positions.
- Each assistant is a compact rounded card (20dp shape) with 12dp vertical padding, a 48dp avatar, title, secondary metadata, and a trailing action menu.
- Create, edit, clone, reorder, and delete are distinct actions; destructive actions are confirmed.

### Assistant detail and background

Sources:
- [AssistantDetailPage.kt](https://github.com/rikkahub/rikkahub/blob/b10c168577482abed696669a58c0eb59f1eed270/app/src/main/java/me/rerere/rikkahub/ui/pages/assistant/detail/AssistantDetailPage.kt)
- [AssistantBasicPage.kt](https://github.com/rikkahub/rikkahub/blob/b10c168577482abed696669a58c0eb59f1eed270/app/src/main/java/me/rerere/rikkahub/ui/pages/assistant/detail/AssistantBasicPage.kt)
- [BackgroundPicker.kt](https://github.com/rikkahub/rikkahub/blob/b10c168577482abed696669a58c0eb59f1eed270/app/src/main/java/me/rerere/rikkahub/ui/pages/assistant/detail/BackgroundPicker.kt)

- Detail uses a pinned top bar whose title appears as the header scrolls away.
- Settings are grouped into rounded `CardGroup` sections, not a long collection of unrelated popups.
- Basic settings include assistant name and tags.
- Chat background settings support a gradient mode, an optional image, image preview, opacity slider, and removal.
- The background picker supports gallery selection and a URL option.
- The background belongs to the assistant's chat workspace; it is not a large image placed inside the assistant list item.

### Global theme

Sources:
- [Theme.kt](https://github.com/rikkahub/rikkahub/blob/b10c168577482abed696669a58c0eb59f1eed270/app/src/main/java/me/rerere/rikkahub/ui/theme/Theme.kt)
- [SettingPreferencesThemePage.kt](https://github.com/rikkahub/rikkahub/blob/b10c168577482abed696669a58c0eb59f1eed270/app/src/main/java/me/rerere/rikkahub/ui/pages/setting/SettingPreferencesThemePage.kt)

- Theme mode can follow system, light, or dark.
- Dynamic system colors are supported on Android 12+.
- Users can choose preset or custom color schemes.
- Optional AMOLED dark mode changes the main background/surface to true black.
- System bar icon contrast follows the selected theme.
- RikkaHub uses Material Expressive motion and Material 3 components.

## Corrected translation into Sleepy Hub — confirmed 2026-11-13

The user's intended hierarchy is now explicit:

| RikkaHub concept | Sleepy Hub equivalent |
|---|---|
| Assistant | **Container** |
| Assistant picker | Container picker |
| Assistant manager | Container manager |
| Assistant detail/basic settings | Container settings |
| Conversation in the selected assistant | **Virtual Android profile** (one BlackBox user) |
| Assistant-owned folder | Profile folder within that container |
| Assistant avatar | Container picture |
| Assistant chat background | Container workspace background |
| Global RikkaHub theme | **One global Sleepy Hub theme** |

A container owns zero or more virtual Android profiles. The red/list area in the user's reference represents the selected container's scrollable profiles. The green selector represents the selected assistant/container. The reference image clarifies this relationship; actual RikkaHub source code remains the specification for component hierarchy, proportions, navigation, and interactions.

- New container: create an empty container only; do not auto-create a BlackBox user.
- Switching container: show only its profiles, reset folder filtering, and restore that container's last selected profile when returning.
- Add Profile: create a BlackBox user assigned to the currently selected container.
- Profile rows: use RikkaHub's inline text-only conversation-list pattern in the drawer; do not use a bottom sheet or avatar cards for profiles.
- Container picker: use RikkaHub's compact assistant picker bottom sheet with container picture/name and trailing edit action.
- Container settings: preserve container name, picture/avatar, and optional workspace background with preview, opacity, change, and remove.
- Global theme only: System/Light/Dark, dynamic color, preset/custom themes, import/export, and AMOLED option are shared across every container. No per-container or per-profile theme/accent.
- Workspace: plain black by default; optional selected container background appears only behind the app-manager workspace.

The current rebuild branch does not yet implement this model. See [UI_IMPLEMENTATION_PLAN.md](UI_IMPLEMENTATION_PLAN.md) for the implementation and acceptance requirements, and [RIKKAHUB_SOURCE_AUDIT.md](RIKKAHUB_SOURCE_AUDIT.md) for the code audit and current build failures.

## License boundary

- RikkaHub is AGPL-3.0; Sleepy Hub currently declares Apache-2.0.
- Recreate the UI and flow with original Sleepy Hub code. Do not copy upstream source files/assets into the Apache-2.0 project.
- Literal source reuse requires an explicit licensing decision and compliance review first.
