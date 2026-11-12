# UI roadmap — RikkaHub-inspired, Sleepy Hub original

## Goal

Give Sleepy Hub a cleaner, more intentional interface inspired by the design quality and interaction patterns we like in RikkaHub, while keeping Sleepy Hub recognizably its own app.

**Inspiration is not copying.** We can study general ideas such as hierarchy, navigation, spacing, settings organization, reusable surfaces, and interaction feedback. Do not copy RikkaHub source code, assets, branding, or distinctive screens. The kitty identity and Sleepy Hub's own visual language stay ours.

The UI implementation is being developed on the separate branch `sleepyhub/ui-rikkahub-inspired`; this documentation branch remains a notebook.

## Design guardrails

- Keep the calm, dark, monochrome Sleepy Hub identity unless the user approves a broader palette.
- Prefer clear hierarchy and readable labels over decoration.
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

Review the specific RikkaHub screens and interactions that the user wants to use as inspiration. Use screenshots or examples supplied/approved for this project.

Create a short design brief covering:

- navigation model and screen hierarchy;
- information density and grouping;
- card/surface treatment;
- typography and spacing scale;
- icon style;
- color and contrast tokens;
- motion and feedback;
- accessibility and small-screen behavior.

The user reviewed the reference direction and confirmed the product scope on 2026-11-13. Keep the implementation incremental and test each screen/action.

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

Produce a simple screen map and wireframes/mockups for the main shell and one representative screen. Confirm the direction before implementation.

## Phase 3 — Implementation, one screen at a time

Implementation branch: `sleepyhub/ui-rikkahub-inspired`, created from the documentation baseline and kept separate from `main`.

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
- A RikkaHub-inspired side dashboard with wide, short, inset glassy profile cards and a visible Add Profile button.
- A normal installed-app manager workspace for the selected profile.
- Per-profile wallpaper/background image, removable and opacity-adjustable, plus a per-profile accent.
- Global light/dark/system theme, global accent, and app-grid column preference.
- Profile settings for rename, wallpaper, opacity, accent, and delete.
- App actions for launch, app info, clear cache, clear data, stop, uninstall, shortcut, and install.
- Background-running apps are deferred to a later update.

See [UI_IMPLEMENTATION_PLAN.md](UI_IMPLEMENTATION_PLAN.md) for the detailed acceptance checklist and release boundary.


## Build checkpoint

The first polished ARM64 debug APK has now built successfully on `sleepyhub/ui-rikkahub-inspired` at commit `86bd5789e19a9a17a32a1def7aef933eb4eb0581`. See [workflow run 43320730246](https://github.com/Cutie-Cookie/Sleepy-Hub/actions/runs/43320730246) and [UI implementation plan](UI_IMPLEMENTATION_PLAN.md).

Next gate: user installs the debug APK and completes the device acceptance checklist. Fix any reported issues on the feature branch, rebuild, and only discuss signing/release after explicit approval.
