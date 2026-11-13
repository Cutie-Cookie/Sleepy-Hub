# Sleepy Hub UI rebuild plan — strict RikkaHub source fidelity

_Last updated: 2026-11-13_

## Product scope

Sleepy Hub remains a **BlackBox-based Android virtual app manager**.

- Profiles are BlackBox virtual Android users.
- The app's purpose and core app-management behavior do not change.
- RikkaHub's actual source implementation is the UI and interaction specification: match its component hierarchy, visual proportions, spacing, typography, colors, controls, and motion as closely as possible.
- User-provided screenshots are context only and must never override the upstream source.
- Do not improvise, add decorative UI, or invent additional features outside the user's plan.
- Do not add LLM providers, assistant chat, conversation history, message generation, or chat composer.
- Background-running apps are deferred to a later update.
- Do not reintroduce VPN.

## Implementation branch

- Active rebuild branch: `sleepyhub/rikkahub-source-rebuild`
- Base: `sleepyhub/identity-icons-no-vpn`
- The earlier `sleepyhub/ui-rikkahub-inspired` is a rejected visual prototype. Do not build on it or merge it into the rebuild.
- `main` remains untouched.

## Source-based UI requirements

### Main navigation shell

Recreate the structure and behavior of RikkaHub's `ChatPage` using original Sleepy Hub code:

- Material 3 modal navigation drawer with a scrim and proper edge-to-edge slide animation.
- Use the same drawer-sheet width and responsive behavior as the pinned RikkaHub source.
- A Material 3 `Scaffold` for the main workspace.
- Reproduce RikkaHub's transparent top app bar, compact menu-button treatment, title/subtitle hierarchy, and contextual trailing actions. Do not add a separate banner, title card, profile hero, or decorative header above the workspace.
- The app-manager workspace itself remains plain black by default.
- Correct Android Back behavior: close the drawer/sheet first, then navigate/back out.

### Dashboard / drawer

Follow the actual `ChatDrawer` structure, not the supplied screenshots:

- Follow RikkaHub's actual drawer header hierarchy and dimensions; do not add a large logo, decorative image, or custom header card.
- Compact horizontal quick actions.
- The active profile picker as a navigation-drawer item.
- Add Profile must be available in the same compact action placement/pattern used by the source UI. Do not add a giant full-width button or a new standalone promotional card.
- Settings and Fake Location remain available.
- Keep content scrollable on short screens; do not let the header push important actions off-screen.
- Do not place the Sleepy Hub launcher icon as a large profile/dashboard decoration.

### Profile picker

Follow the actual `AssistantPicker` interaction:

- Tapping the active profile row opens a Material 3 modal bottom sheet.
- Sheet height is approximately 80% of the available screen.
- Match the compact selectable profile rows in RikkaHub's picker, including the source avatar size, row height, spacing, selected state, and trailing edit action. No tall cards, wide custom glass cards, wallpaper thumbnails, or decorative art.
- Clearly distinguish the selected profile with the theme's primary container.
- Each profile has a separate edit action that opens profile settings.
- Include Add Profile in the dashboard and picker.
- Switching profiles must switch the BlackBox virtual user and its installed-app list.

### Profile management and detail

Use the actual `AssistantPage` and `AssistantDetailPage` patterns:

- A flexible top app bar with back and add actions.
- Match RikkaHub's compact management-list row dimensions, avatar size, typography, corner treatment, secondary text, and contextual action placement. Translate only the row content to Sleepy Hub profile name, folder, and installed-app status.
- Search profiles if the list becomes long.
- Profile edit screen uses grouped cards/sections, not unrelated legacy dialogs.
- Support rename, optional avatar, profile accent, background, opacity, app-grid columns, and deletion.
- Confirm deletion and protect the last remaining profile.
- Do not show wallpaper imagery inside the picker/list cards by default.

### Profile workspace background

Follow the actual `AssistantBasicPage` and `BackgroundPicker` pattern:

- Workspace background is solid black when no image is selected. No gradient, decorative art, header box, or extra surface is added by default.
- Optional per-profile image appears only behind that profile's app grid, following RikkaHub's source rendering and opacity behavior.
- Provide the corresponding source-style preview, image selection, opacity control, and explicit Remove action.
- Keep icons and labels readable when a user-selected image is active.
- Persist the selected URI permission and URI across restarts; release the old permission when replacing/removing if possible.
- Do not add a decorative image to the dashboard header or profile selection cards.

### Global theme

Follow RikkaHub's global theme architecture with original Sleepy Hub code:

- System / Light / Dark.
- Dynamic colors on Android 12+ where available.
- Global accent or preset color.
- Optional AMOLED true-black mode.
- System status/navigation bar icon contrast follows theme.
- Profile accent and workspace background remain independent from global theme.

### App manager workspace

- Keep the virtual installed-app grid as the primary workspace, using RikkaHub's actual chat-page content/top-bar hierarchy adapted to the active profile and app manager.
- The content area stays black by default. Do not place a new profile summary box or decorative panel above the grid.
- Keep an obvious install action.
- Replace legacy app `PopupMenu` and `MaterialDialog` flows with Material 3 menus, bottom sheets, and confirmation dialogs that match the new shell.
- Preserve launch, app info, clear cache, clear data, stop, uninstall, shortcut, install, and drag-to-reorder behavior where supported.
- Clearly distinguish Clear Cache from Clear Data; Clear Data must warn that app data will be reset.
- Do not implement fake cache clearing: use the BlackBox per-user data/cache directories and verify both internal and external cache paths.

## Technical approach

RikkaHub is implemented with Kotlin and Jetpack Compose. The current Sleepy Hub UI is Android Views + Fragments. A faithful implementation requires introducing Jetpack Compose for the navigation shell, profile picker/detail screens, global theme, and app-action surfaces. Keep the existing BlackBox backend and reuse its repository/view-model operations; do not copy RikkaHub source.

Compatibility guardrails:
- Existing Kotlin version is 1.9.23. Select Compose artifacts/compiler extension compatible with Kotlin 1.9.23 unless the build toolchain is deliberately upgraded and separately verified.
- Existing minimum SDK is 21. Check Compose and activity-compose minimum SDK compatibility before choosing versions; do not accidentally raise minSdk without approval.
- Keep existing Java/Kotlin namespaces and application ID unchanged.
- Retain ARM64 debug CI and run it after each coherent milestone.

## Performance requirements

- No forced garbage collection in RecyclerView/Compose scroll callbacks.
- Do not suppress app icons based on a heap percentage threshold.
- Keep package enumeration and icon loading off the main thread.
- Bound icon caching; avoid repeated icon decode/load work during recomposition.
- Wallpaper decoding must be off the main thread and downsampled to display size.
- No background-running services in this milestone.

## Milestones

1. **Source map and baseline:** record RikkaHub source patterns and licensing; create clean rebuild branch from the app baseline.
2. **Compose foundation:** add compatible Compose dependencies/compiler, theme, ComposeView host, and a minimal compile-safe shell.
3. **Navigation shell:** implement RikkaHub-style modal drawer, top bar, back behavior, and existing app content host.
4. **Profiles:** picker bottom sheet, add/switch/rename/delete, profile detail and appearance.
5. **Workspace:** black app grid by default; optional per-profile wallpaper/opacity only when selected; source-matched app actions.
6. **Global settings:** global theme mode, dynamic colors, accent, AMOLED; preserve advanced BlackBox settings.
7. **Performance and regression:** remove forced-GC/heap-threshold shortcuts, verify app operations and profile isolation.
8. **Debug acceptance:** build, inspect artifact, then user tests on a real phone. No release signing until explicit approval.

## Device acceptance checklist

1. Fresh install starts with at least one usable profile.
2. Dashboard opens/closes with the same modal-drawer feel as RikkaHub.
3. Back closes drawer and sheets before leaving the screen.
4. Profile picker opens as a bottom sheet and has compact rows.
5. Add Profile creates a real BlackBox user and selects it.
6. Rename and appearance settings survive app restart.
7. Switching profiles shows only that user's installed apps.
8. Workspace is plain black by default; a selected profile wallpaper can be previewed, adjusted, removed, and restored after restart.
9. Global System/Light/Dark, dynamic color, accent, and AMOLED work.
10. Install, launch, app info, clear cache, clear data, stop, uninstall, shortcut, and reorder work.
11. Clear Cache does not clear app data; Clear Data warns before reset.
12. Deleting a profile removes its BlackBox user and associated Sleepy Hub metadata; the last profile cannot be deleted.
13. No legacy popups or visually unrelated dialogs remain in the redesigned main flows; no invented banner, header box, artwork, giant profile card, or other unrequested decoration is present.
14. No VPN or background-running feature has been reintroduced.
15. CI passes and the user verifies the APK on a physical device.

## Licensing note

RikkaHub is AGPL-3.0; Sleepy Hub is Apache-2.0. The implementation must be original code informed by the reference. Direct code reuse requires a separate license review and is not part of this plan.
