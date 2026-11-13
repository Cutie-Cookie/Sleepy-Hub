# UI roadmap — RikkaHub-source-faithful Sleepy Hub port

## Goal

Recreate RikkaHub's actual UI structure, visual treatment, navigation, component hierarchy, and motion as faithfully as possible, changing only the product-specific content and actions needed for Sleepy Hub's virtual Android app manager.

The source reference is pinned in [RIKKAHUB_SOURCE_AUDIT.md](RIKKAHUB_SOURCE_AUDIT.md). Sleepy Hub code must be implemented independently; RikkaHub's AGPL-3.0 source files/assets are not copied into the Apache-2.0 project. Match the UI behavior and appearance, not the upstream source text.

The implementation branch is `sleepyhub/rikkahub-source-rebuild`; this documentation branch remains a notebook.

## Design guardrails

- Follow the actual RikkaHub source component hierarchy, dimensions, spacing, colors, typography, and animations; do not invent an alternative design system.
- The app-manager workspace is plain black by default. Do not add a banner, header card, decorative box, illustration, artwork, or other decoration above the app grid.
- A profile wallpaper is optional and appears only behind that profile's app-manager workspace when the user chooses one; remove it to return to plain black.
- Do not add features, controls, cards, shortcuts, or decorative elements beyond the user's agreed plan and the RikkaHub structure being adapted.
- Make frequent actions easy to find and destructive actions hard to trigger accidentally.
- Use consistent spacing, typography, icon sizes, corner radii, and interaction states.
- Design loading, empty, error, permission, and disabled states—not just the happy path.
- Keep UI work separate from virtualization/core logic wherever possible.
- Preserve existing behavior unless a behavior change is explicitly agreed.
- Do not add analytics, remote logging, or new permissions as part of the redesign.

## Phase 0 — Inventory the current app

Before designing, map each current screen and the source files/components that implement it.

Initial inventory to verify against the actual app:

- Welcome / launcher / main landing screen
- Virtual app list and empty state
- Add/install app flow
- App details and actions (open, stop, clear data, uninstall, shortcut)
- Settings
- GMS manager
- Fake location manager
- Module-related screens and dialogs
- Permission prompts, loading states, errors, and confirmation dialogs

For each screen, record: purpose, entry point, important actions, current implementation, and any behavior that must not change.

## Phase 1 — Reference and design brief

Use the pinned RikkaHub source code as the specification. User screenshots are context only and must not override the actual upstream composables, dimensions, layout hierarchy, or motion.

Create a short design brief covering:

- navigation model and screen hierarchy;
- information density and grouping;
- card/surface treatment;
- typography and spacing scale;
- icon style;
- color and contrast tokens;
- motion and feedback;
- accessibility and small-screen behavior.

The user explicitly rejected the first prototype and confirmed source-first fidelity on 2026-11-13. Keep the implementation incremental and test each screen/action.

## Phase 2 — Design system and wireframes

Define reusable tokens and components before styling every screen individually.

Suggested token groups:

- backgrounds and elevated surfaces;
- primary/secondary text and disabled text;
- separators and outlines;
- spacing scale;
- corner radii;
- typography sizes/weights;
- icon sizes;
- pressed, focused, selected, disabled, loading, and error states.

Do not invent wireframes that diverge from RikkaHub. Map each Sleepy Hub screen directly to its upstream counterpart, then implement and compare against the source behavior.

## Phase 3 — Implementation, one screen at a time

Implementation branch: `sleepyhub/rikkahub-source-rebuild`, based on the application baseline and kept separate from `main`. The older `sleepyhub/ui-rikkahub-inspired` prototype was rejected and must not be reused.

Suggested order:

1. App shell and navigation.
2. Main/virtual-app list and its empty/loading states.
3. Add/install flow.
4. App details and action surfaces.
5. Settings.
6. GMS, fake-location, and remaining utility screens.
7. Dialogs, notifications, accessibility, and final polish.

For each screen: agree on the layout, implement it, inspect the diff, build, compare screenshots, and test the existing actions before moving on.

## Phase 4 — Acceptance checks

The redesign is ready for broader use only when:

- the visual direction is approved by the user;
- all existing app-management actions still work;
- navigation and back behavior are predictable;
- loading, empty, error, and permission states are designed;
- text remains readable at different display sizes and font scales;
- touch targets are comfortable;
- launcher, splash, and notification identity remain consistent;
- no unrelated core, permission, networking, or logging changes slipped in;
- CI builds successfully and the changed screens have been checked on a device.

## First UI session checklist

- [ ] Collect or confirm the RikkaHub reference screens.
- [ ] Capture the current Sleepy Hub screens for a before/after baseline.
- [ ] Map each screen to its source files and behavior.
- [ ] Agree on the navigation structure.
- [ ] Agree on design tokens and component style.
- [ ] Create one main-screen wireframe.
- [ ] User approval received; implementation is underway on the feature branch.


## RikkaHub source study (2026-11-13)

See [RIKKAHUB_RESEARCH.md](RIKKAHUB_RESEARCH.md) for the source-by-source study, license boundary, current Sleepy Hub architecture, and performance audit.

Important findings:
- RikkaHub's drawer combines profile, quick actions, assistant-scoped folders, conversation history, assistant picker, and settings.
- Assistant selection opens a sheet; selection changes the active assistant and then opens/reuses a conversation.
- RikkaHub folders are owned by an assistant and the folder filter resets when the assistant changes.
- RikkaHub global themes and per-assistant chat backgrounds are separate concepts.
- Sleepy Hub currently has no AI chat engine or assistant/conversation models. Its existing profiles are BlackBox virtual Android users.

**Implementation gate:** before creating `sleepyhub/ui-rikkahub-inspired`, confirm whether Sleepy Hub remains an app container, becomes an AI chat app, or combines both. Do not silently add an AI subsystem as part of a visual redesign.


## Confirmed implementation direction (2026-11-13)

The app remains a BlackBox virtual app manager. Profiles are virtual Android users, not AI assistants. The approved UI scope includes:
- RikkaHub's actual side drawer and compact profile-picker pattern, with Add Profile placed in the corresponding profile actions. Do not invent wide glassy cards or tall profile tiles.
- A normal installed-app manager workspace for the selected profile, keeping the workspace black by default and without an extra header box or decorative artwork.
- Optional per-profile workspace wallpaper, removable and opacity-adjustable, plus per-profile theme customization. Wallpaper is behind the app grid only; no wallpaper thumbnails or art in the drawer/profile rows.
- Global light/dark/system theme, global accent, and app-grid column preference.
- Profile settings for rename, wallpaper, opacity, accent, and delete.
- App actions for launch, app info, clear cache, clear data, stop, uninstall, shortcut, and install.
- Background-running apps are deferred to a later update.

See [UI_IMPLEMENTATION_PLAN.md](UI_IMPLEMENTATION_PLAN.md) for the detailed acceptance checklist and release boundary.


## Build checkpoint

The first polished ARM64 debug APK has now built successfully on `sleepyhub/ui-rikkahub-inspired` at commit `86bd5789e19a9a17a32a1def7aef933eb4eb0581`. See [workflow run 43320730246](https://github.com/Cutie-Cookie/Sleepy-Hub/actions/runs/43320730246) and [UI implementation plan](UI_IMPLEMENTATION_PLAN.md).

Next gate: user installs the debug APK and completes the device acceptance checklist. Fix any reported issues on the feature branch, rebuild, and only discuss signing/release after explicit approval.

## 2026-11-13 — RikkaHub source-first rebuild

The user clarified that the upstream RikkaHub source is the design specification and screenshots are context only. The first visual prototype was rejected. A clean branch, `sleepyhub/rikkahub-source-rebuild`, was created from the app baseline. See [RikkaHub source research](RIKKAHUB_RESEARCH.md) and [revised implementation plan](UI_IMPLEMENTATION_PLAN.md).

The direction is a source-faithful Compose implementation of RikkaHub's modal drawer, top bar, profile picker sheet, compact profile-management list, grouped detail settings, optional workspace wallpaper, and global theme—adapted only where required for Sleepy Hub profiles and app management. No AI chat or background-running feature is added. No extra visual elements or unrequested features are to be invented.


## Implementation work started — 2026-11-13

Clean rebuild branch `sleepyhub/rikkahub-source-rebuild` is now being implemented from the app baseline. Latest recorded source commit at this checkpoint: `2eed812937ffd8d86d454e554c5e8f089f757a8c`.

Files added/rewritten so far:
- Compose-enabled `activity_main.xml` and Compose dependencies compatible with Kotlin 1.9.23.
- `SleepyHubTheme.kt`: global System/Light/Dark, dynamic color, accent, and AMOLED theme.
- `SleepyHubMainScreen.kt`: modal navigation drawer, RikkaHub-structured top bar, profile picker sheet, profile workspace background.
- `ProfileScreens.kt`: profile manager, profile detail/appearance, global settings.
- `ProfileAppearance.kt`: persistent global and per-profile appearance preferences.
- `AppGridScreen.kt` and Compose-based `AppsFragment.kt`: app grid and Material 3 action sheet/confirmations.
- `AppsRepository.kt` and `AppsViewModel.kt`: actual internal/external virtual cache clearing; removed forced-GC and heap-threshold icon suppression; stopped auto-deleting intentionally empty profiles.
- `MainActivity.kt` and `ViewPagerAdapter.kt`: virtual-user profile lifecycle and stable profile IDs.

**Validation state:** implementation is in progress. The ARM64 debug workflow has been triggered, but no successful build or device acceptance is claimed at this checkpoint. Resolve compiler/runtime issues before adding more UI features.


## Latest user correction — strict RikkaHub fidelity (2026-11-13)

The user explicitly said: follow RikkaHub 100% as the UI reference, add nothing beyond the agreed Sleepy Hub plan, and do not improvise. The actual upstream code—not screenshots or the rejected prototype—defines navigation, component hierarchy, sizing, spacing, colors, typography, and animation.

- The app-manager workspace is black by default.
- No extra box/banner/header card above the app grid, no decorative artwork, no large logo, and no invented profile hero.
- Optional profile wallpaper is allowed only behind that profile's workspace, only when selected, and can be removed to restore plain black.
- The old wide-glassy profile-card concept is rejected. Use RikkaHub's compact profile selector and management rows.
- Keep only the requested Sleepy Hub adaptations: virtual profiles, Add Profile, app management, profile/global customization, and the previously agreed profile/folder organization. Do not add AI chat or background-running apps.
