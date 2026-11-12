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

## Translation into Sleepy Hub

Sleepy Hub is a BlackBox-based virtual Android app manager, not an AI chat client. The mapping is:

| RikkaHub concept | Sleepy Hub equivalent |
|---|---|
| Assistant | BlackBox virtual Android profile |
| Assistant picker | Profile picker bottom sheet |
| Assistant management list | Profile management screen |
| Assistant detail | Profile settings |
| Assistant chat background | Profile workspace background |
| Assistant background opacity | Profile background opacity |
| Global RikkaHub theme | Sleepy Hub global theme |
| Conversation list / folders | Not added; Sleepy Hub has no chat history |
| Chat composer | Not added; Sleepy Hub remains an app manager |
| Chat actions | App installation and profile actions |

Sleepy Hub profile backgrounds belong to the **profile workspace**, not to oversized profile cards. Profile selection should use compact avatar/name rows like the upstream assistant picker. The user explicitly requires an Add Profile action in the dashboard.

The main workspace remains the virtual installed-app grid. The top bar should follow the RikkaHub chat top-bar structure, adapted to profile name, installed-app count, install action, and overflow actions. Do not add chat UI.

## Source links

- [RikkaHub repository](https://github.com/rikkahub/rikkahub)
- [RikkaHub AGPL-3.0 license](https://github.com/rikkahub/rikkahub/blob/b10c168577482abed696669a58c0eb59f1eed270/LICENSE)
- [Sleepy Hub Apache-2.0 license](https://github.com/Cutie-Cookie/Sleepy-Hub/blob/sleepyhub/identity-icons-no-vpn/LICENSE)
