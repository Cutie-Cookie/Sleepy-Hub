# UI roadmap — strict RikkaHub source-faithful Sleepy Hub

## Goal

Reproduce RikkaHub's actual navigation, layout hierarchy, component proportions, visual treatment, and interaction flow as closely as possible, adapting only the product concepts required for Sleepy Hub's BlackBox virtual Android app manager.

Reference repository: [rikkahub/rikkahub](https://github.com/rikkahub/rikkahub), commit `b10c168577482abed696669a58c0eb59f1eed270`.

The pinned source is the specification. Screenshots are context only. The rejected prototype must not be reused. Work continues on `sleepyhub/bugfix-install-switch-polish`; `main` must remain untouched.

## Correct product hierarchy

| RikkaHub | Sleepy Hub |
|---|---|
| Assistant | **Container** |
| Assistant picker | Container picker |
| Assistant manager | Container manager |
| Assistant detail/basic settings | Container settings |
| Conversation | **Virtual Android profile** (BlackBox user) |
| Assistant-owned folder | Profile folder within that container |
| Assistant avatar | Container picture |
| Assistant chat background | Container workspace background |
| Global theme | One global theme for the whole app |

A container owns zero or more virtual profiles.

- The profile list is inline in the drawer and scrolls independently (the red/list area explained by the user).
- The compact container selector is near the bottom of the drawer and opens the assistant-style picker sheet (the green selector explained by the user).
- Creating a container creates an empty container, not a virtual profile.
- Switching containers shows only their profiles and restores that container's last selected profile when returning.
- Add Profile creates a BlackBox virtual user assigned to the currently selected container.
- Container name, picture, and optional background are editable.
- **Theme is global only. There is no per-container or per-profile theme/accent.**

The complete approved mapping and acceptance checklist are in [UI_IMPLEMENTATION_PLAN.md](UI_IMPLEMENTATION_PLAN.md).

## Visual and interaction guardrails

- Follow RikkaHub's source component hierarchy, spacing, dimensions, typography, colors, surfaces, responsive behavior, and motion. Do not improvise.
- Main app-manager workspace is plain black by default.
- No extra banner, profile-summary box, hero card, decorative header, illustration, or artwork above the app grid.
- A selected container background may appear only behind the active app-manager workspace. Removing it restores plain black.
- No profile avatars or wallpaper thumbnails in the inline profile list.
- Container avatars appear only in the assistant-style container selector and container-management/settings surfaces, as in RikkaHub.
- Do not add AI chat, chat composer, model/provider settings, assistant prompts, or background-running apps.
- Preserve existing BlackBox app management, GMS, fake-location, permissions, and app-data isolation.
- Do not add analytics, remote logging, new permissions, or release-signing secrets.

## Implementation phases

1. **Data model:** add a persistent container entity; assign existing BlackBox users to a default container; store container name/avatar/background; store last selected profile per container. Do not lose existing virtual app data or profile names.
2. **Navigation shell:** match RikkaHub modal drawer on phones, permanent drawer on large screens, top bar, responsive layout, typed back stack, and drawer back behavior.
3. **Drawer:** add the source-style folder row and inline scrollable profile list. Profile rows are compact text-only rows with selected state and source-style context actions.
4. **Container picker/manager:** compact assistant-style bottom sheet for switching containers; source-style manager page for adding, searching, reordering, editing, and confirming destructive actions.
5. **Container settings:** name, picture/avatar, optional workspace background, preview, opacity, change, and remove. No theme controls here.
6. **Global theme:** reproduce RikkaHub's global System/Light/Dark, dynamic colors, preset/custom themes, import/export, and AMOLED flow, after validating compatibility with the legacy BlackBox toolchain.
7. **Workspace:** keep the app grid and existing BlackBox actions; black default; optional active-container background only.
8. **Validation:** compile, run focused persistence/ownership tests, verify CI artifact, then device-test all flows before asking the user to approve a debug APK.

## Current audit checkpoint — 2026-11-13

The feature branch contains the container/profile ownership model and inline profile drawer. The latest user tests confirm switching and container list separation, but report shared Android/GSF IDs and Wi-Fi MAC, a folder UI that had not actually been wired, long-press launch leakage, and a fake-location crash on app selection. Follow-up source commits `30482a1`, `09f2bed`, `b26022f`, `dbfc10d`, `6492d84`, `8ac5aef` and `f3d12dd` address these paths. Current-head CI run [43449035599](https://github.com/Cutie-Cookie/Sleepy-Hub/actions/runs/43449035599) is in progress; see DEVELOPMENT_PLAN.md for the test order.
## License boundary

RikkaHub is AGPL-3.0; Sleepy Hub currently declares Apache-2.0. Reimplement the same flow with original Sleepy Hub code. Do not copy upstream source files/assets into the Apache-2.0 project. Literal source reuse requires an explicit licensing decision and compliance review.


## Additional user-confirmed interaction rules — 2026-11-13

- Long-press a profile in the inline drawer list to open the source-style action menu. Include **Move to container**, Move to folder, Rename, and Delete. Moving opens a scrollable container picker sheet.
- Moving a profile changes its container membership only. Preserve its BlackBox user ID and all installed apps/data. If it is active, switch to the target container and keep the same profile active. Clear its old container-scoped folder assignment.
- Add Profile is a small plus beside the dashboard's inline profile-list heading.
- Add Container is the top-app-bar plus in the Container Manager, like RikkaHub AssistantPage. New containers are empty.
- Container switching is the compact AssistantPicker-style bottom-drawer selector. Container manager is reachable from the dashboard footer and Settings home.
- Settings stays at the dashboard bottom-right. Global theme is Settings → Preferences → Theme. No per-container/profile theme controls.
- The top-right action in the workspace takes the position of RikkaHub's New Message icon but performs **Add App / Install App** for the active profile. No chat or chat composer is added.
- Remove the top-right three-dot menu. Its functions are already available through dashboard Settings, profile actions, Container Manager, and Refresh shortcut.
- Replace the native storage-permission explanation popup with a source-style Compose Material 3 rationale dialog, but keep the actual Android All Files Access permission flow.
- Container deletion always shows Cancel/Delete confirmation and explains the impact on contained profiles/data. Do not implement destructive cascade behavior until the user confirms the policy.
- The app picker needs a genuine interaction/layout pass: compact app rows, icons, search, APK chooser, installed status, loading/empty states. Do not merely recolor the current cards.
- File-by-file upstream references are indexed in [RIKKAHUB_SOURCE_MAP.md](RIKKAHUB_SOURCE_MAP.md).

## Latest feedback-driven implementation pass — 2026-11-13

- Folder row supports create, rename, delete and selection-based profile moves, scoped to the active container.
- The first profile in an empty container defaults to New Profile.
- Virtual device identifiers are persisted per BlackBox user ID; app-data path isolation remains a separate device acceptance check.
- The ineffective Stop action is removed. Inactive virtual apps stop after one minute by default; a global setting opts out.
- Profile switching no longer sweeps horizontally; page transitions use a short fade over the theme background.
- The theme explicitly defines neutral Material 3 roles to prevent default purple containers from leaking into a monochrome theme.
- A labeled loading state appears while the initial app list is populated.
- Long-pressing an app should open its action sheet without launching it on release.
- Run [43439814552](https://github.com/Cutie-Cookie/Sleepy-Hub/actions/runs/43439814552) passed for exact head 0fea772e8f7acc8e84c1ce244ca1c995f294db02, including the file-backed cross-process idle-stop opt-out. Device behavior and visual parity still require the phone checks.
- Fake-location crash remains blocked on the exact failing sub-step or crash trace.
