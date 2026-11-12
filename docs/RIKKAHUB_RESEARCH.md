# RikkaHub UI research and Sleepy Hub scope check

_Reviewed: 2026-11-13. Reference repository: [rikkahub/rikkahub](https://github.com/rikkahub/rikkahub), default branch `master`._

## Why this research exists

The RikkaHub screenshots are a visual and interaction reference. This note records what the upstream app actually does, what can inspire Sleepy Hub, and the architectural mismatch we must resolve before implementing the requested assistant/chat concepts.

## Reference implementation

RikkaHub is a native Android LLM chat app. Its repository describes a Kotlin + Jetpack Compose + Material 3 app and is licensed under **AGPL-3.0**.

Useful source files:

- [RouteActivity.kt](https://github.com/rikkahub/rikkahub/blob/master/app/src/main/java/me/rerere/rikkahub/RouteActivity.kt) — Compose app entry point and Navigation 3 route host.
- [ChatDrawer.kt](https://github.com/rikkahub/rikkahub/blob/master/app/src/main/java/me/rerere/rikkahub/ui/pages/chat/ChatDrawer.kt) — drawer composition, user profile, actions, folder bar, conversation list, assistant picker, and footer actions.
- [ChatDrawerVM.kt](https://github.com/rikkahub/rikkahub/blob/master/app/src/main/java/me/rerere/rikkahub/ui/pages/chat/ChatDrawerVM.kt) — assistant-scoped folder and conversation state.
- [AssistantPicker.kt](https://github.com/rikkahub/rikkahub/blob/master/app/src/main/java/me/rerere/rikkahub/ui/components/ai/AssistantPicker.kt) — assistant selection sheet and selection/edit behavior.
- [AssistantPage.kt](https://github.com/rikkahub/rikkahub/blob/master/app/src/main/java/me/rerere/rikkahub/ui/pages/assistant/AssistantPage.kt) — assistant listing, search, tags, create, reorder, clone, delete, and edit.
- [Assistant.kt](https://github.com/rikkahub/rikkahub/blob/master/app/src/main/java/me/rerere/rikkahub/data/model/Assistant.kt) — assistant data model.
- [Folder.kt](https://github.com/rikkahub/rikkahub/blob/master/app/src/main/java/me/rerere/rikkahub/data/model/Folder.kt) — conversation folder model.
- [Theme.kt](https://github.com/rikkahub/rikkahub/blob/master/app/src/main/java/me/rerere/rikkahub/ui/theme/Theme.kt) and [CustomTheme.kt](https://github.com/rikkahub/rikkahub/blob/master/app/src/main/java/me/rerere/rikkahub/ui/theme/CustomTheme.kt) — global app theme selection and custom color schemes.
- [SettingPreferencesThemePage.kt](https://github.com/rikkahub/rikkahub/blob/master/app/src/main/java/me/rerere/rikkahub/ui/pages/setting/SettingPreferencesThemePage.kt) — global theme controls.

## Observed interaction patterns

### Drawer / dashboard

The chat drawer uses a structured vertical layout rather than a flat menu:

1. Optional update/backup cards.
2. User avatar, editable nickname, and greeting.
3. Quick navigation actions.
4. A horizontally arranged folder selector and sort control.
5. A conversation list, with a scrollable header on short screens.
6. The assistant picker and utility/settings actions.

The reference uses a modal drawer sheet around 300 dp wide. Its structure can inspire a Sleepy Hub navigation dashboard, but labels and actions must match Sleepy Hub's actual features.

### Assistant selection

- The current assistant is visible in the drawer.
- Tapping its picker opens a modal bottom sheet.
- Assistants are shown as selectable cards; tags can filter the list.
- The current assistant has a selected visual state.
- Each item can open its assistant detail/editor.
- Selecting an assistant updates the active assistant ID. The caller then opens a new conversation or reuses an existing one according to the user's preference.

### Assistant-scoped folders

A folder contains an `assistantId`. The drawer view model observes the active assistant and loads only that assistant's folders. Conversation paging is filtered by assistant and selected folder. Switching assistants resets the folder filter so an old assistant's folder is not left selected.

This is a useful ownership pattern if Sleepy Hub later has real independent workspaces/profiles. It should not be added to the current app until we know what the user's "profiles" represent.

### Themes

RikkaHub has a **global** theme system with system/light/dark modes, dynamic color, preset/custom themes, and optional AMOLED dark surfaces.

The upstream `Assistant` model has chat appearance fields such as a background image, background opacity, and gradient background. It does not simply give each assistant an entirely independent global Material color scheme. Sleepy Hub can design its own per-profile accent/theme model if that is the intended product behavior.

## License and originality boundary

RikkaHub's repository states AGPL-3.0. This project is using it as a visual/interaction reference only.

- Do not copy RikkaHub source files, distinctive screen implementations, assets, logos, names, or branding into Sleepy Hub.
- Implement original Sleepy Hub components and data models.
- Re-check license compatibility before ever considering code reuse or a dependency on RikkaHub code.
- Preserve Sleepy Hub's kitty identity and its own visual language.

## Critical product-scope mismatch

The current Sleepy Hub source is a **BlackBox-based Android app container**, not an AI chat client.

Current app structure:

- `MainActivity` creates a `ViewPager2` containing one `AppsFragment` per BlackBox virtual Android user, plus the add-user page.
- The toolbar subtitle is a per-virtual-user remark.
- `AppsFragment` and `AppsViewModel` show and manage apps installed inside that virtual user.
- `AppsRepository` queries BlackBox-installed packages and handles install, launch, uninstall, and clear-data operations.
- Settings are currently implemented using `PreferenceFragmentCompat`.
- The project has no LLM provider clients, assistant/chat data model, conversation database, chat history, or message-generation pipeline.

Relevant current Sleepy Hub sources:

- [MainActivity.kt](https://github.com/Cutie-Cookie/Sleepy-Hub/blob/sleepyhub/identity-icons-no-vpn/app/src/main/java/top/niunaijun/blackboxa/view/main/MainActivity.kt)
- [AppsFragment.kt](https://github.com/Cutie-Cookie/Sleepy-Hub/blob/sleepyhub/identity-icons-no-vpn/app/src/main/java/top/niunaijun/blackboxa/view/apps/AppsFragment.kt)
- [AppsViewModel.kt](https://github.com/Cutie-Cookie/Sleepy-Hub/blob/sleepyhub/identity-icons-no-vpn/app/src/main/java/top/niunaijun/blackboxa/view/apps/AppsViewModel.kt)
- [AppsRepository.kt](https://github.com/Cutie-Cookie/Sleepy-Hub/blob/sleepyhub/identity-icons-no-vpn/app/src/main/java/top/niunaijun/blackboxa/data/AppsRepository.kt)
- [SettingFragment.kt](https://github.com/Cutie-Cookie/Sleepy-Hub/blob/sleepyhub/identity-icons-no-vpn/app/src/main/java/top/niunaijun/blackboxa/view/setting/SettingFragment.kt)

This means a true AI assistant picker + chat screen is not a navigation-only redesign. It would require a new product subsystem: provider configuration, API-key storage, model selection, conversation persistence, message streaming, error handling, chat UI, and privacy/security decisions.

### Scope decision required before implementation

Choose one product direction:

- **A — Keep Sleepy Hub as an app container.** "Profiles" means its existing virtual Android users (or named spaces built around them). The RikkaHub-inspired drawer, horizontal glassy profile cards, folders, and global/per-profile themes organize virtual users and their installed apps. No AI chat engine is added.
- **B — Turn Sleepy Hub into an AI chat app.** Add actual assistants, chat history, folders, provider/model configuration, message generation, and assistant-specific chat appearance. This is a major new product feature, not just a UI makeover.
- **C — A combined app.** Keep the BlackBox container and add a separate AI workspace. This has the largest scope and needs a separate architecture plan.

Do not create the UI implementation branch or replace the main navigation until this decision is confirmed.

## Performance audit: initial findings

These are source-level observations, not measured device benchmarks. They should be verified and fixed in isolated changes.

1. **Synchronous garbage collection from scroll callbacks.** `AppsFragment` calls `MemoryManager.optimizeMemoryForRecyclerView()` when scrolling becomes idle and may call `forceGarbageCollectionIfNeeded()` during fast scrolling. The memory helper calls `System.gc()`; the force-GC path also sleeps for 100 ms. Triggering these operations from scroll callbacks can cause visible stalls.
2. **Icons disappear under a memory threshold.** `AppsRepository.safeLoadAppIcon()` skips icon loading when the runtime heap usage exceeds 75%. This trades away UI completeness instead of using measured, bounded caching or lazy icon loading.
3. **Repeated package/icon work.** App-list refreshes enumerate virtual installed applications and load labels/icons again. Measure refresh duration and icon decode costs before changing caching or invalidation.
4. **Retry sleeps.** `getVmInstallList()` retries package queries with `Thread.sleep`. It runs through the view-model's IO dispatcher, so it is not currently a main-thread sleep, but a suspending retry/backoff would avoid tying up an IO worker.
5. **No baseline metrics recorded yet.** We do not yet have device measurements for cold start, time-to-first-app-list, scroll jank, peak/steady memory, or install/open duration.

### Safe performance investigation plan

- Record device/Android version without storing private device identifiers.
- Measure cold and warm launch, time to render app list, scroll frame times, heap/native memory, and install/launch durations.
- Remove forced GC and sleeps from UI callbacks only after confirming behavior with profiling.
- Replace "skip icons" with bounded/lazy icon loading and a cache that respects density and memory pressure.
- Keep BlackBox core and app virtualization behavior out of UI-only performance changes.
- Compare measurements before and after each change; do not claim a performance improvement without a repeatable measurement.

## Status

- Research completed; no RikkaHub source or assets copied.
- No Sleepy Hub application code changed as part of this research.
- Product direction remains unresolved.
- UI branch creation is on hold until the product-scope decision is made.
