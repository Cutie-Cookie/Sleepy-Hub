# UI roadmap — strict RikkaHub source-faithful Sleepy Hub

## Goal

Reproduce RikkaHub's actual navigation, layout hierarchy, component proportions, visual treatment, and interaction flow as closely as possible, adapting only the product concepts required for Sleepy Hub's BlackBox virtual Android app manager.

Reference repository: [rikkahub/rikkahub](https://github.com/rikkahub/rikkahub), commit `b10c168577482abed696669a58c0eb59f1eed270`.

The pinned source is the specification. Screenshots are context only. The rejected `sleepyhub/ui-rikkahub-inspired` prototype must not be reused. Work remains on `sleepyhub/rikkahub-source-rebuild`; `main` must remain untouched.

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

The current rebuild branch does not yet match the approved hierarchy. It is flat-profile based, has per-profile accents/backgrounds, puts the profile list in a bottom sheet instead of inline in the drawer, and has no container entity or container avatar. Its default workspace also draws an accent gradient. The latest ARM64 build failed and no APK was produced. See [RIKKAHUB_SOURCE_AUDIT.md](RIKKAHUB_SOURCE_AUDIT.md).

The latest failure is recorded rather than hidden. No application code was changed during this audit; documentation was corrected on `sleepyhub/docs-project-notes` only.

## License boundary

RikkaHub is AGPL-3.0; Sleepy Hub currently declares Apache-2.0. Reimplement the same flow with original Sleepy Hub code. Do not copy upstream source files/assets into the Apache-2.0 project. Literal source reuse requires an explicit licensing decision and compliance review.
