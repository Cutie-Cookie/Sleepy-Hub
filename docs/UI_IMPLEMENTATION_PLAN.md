# Sleepy Hub UI redesign implementation plan

_Last updated: 2026-11-13_

## Product decision — confirmed

Sleepy Hub remains a **BlackBox-based Android virtual app manager**.

- A profile is a BlackBox virtual Android user, not an AI assistant.
- RikkaHub is a visual and interaction reference only. Sleepy Hub is not becoming an AI chat client.
- The main workspace remains a familiar installed-app manager.
- The side dashboard is the main way to browse and switch profiles.
- Background-running apps are explicitly deferred to a later update.

## Design direction

### Dashboard / side navigation

- A left-side modal dashboard with a dimmed content scrim and a smooth slide-in/out transition.
- Sleepy Hub identity at the top.
- A profile section with wide, low-height, inset cards. The cards should read as horizontal glass panels, not tall tiles.
- Every profile card shows its name, a short status, its own accent colour, and its optional background image.
- A visible **Add profile** button lives in the dashboard.
- Settings and Fake Location remain accessible from the dashboard.

### Profile workspace

- The main page is an app manager, not a chat screen.
- A profile hero/header shows its name and optional background image with a dark readability overlay.
- Installed apps remain in a normal, scrollable grid.
- The floating action button opens the existing install-app flow.
- Profiles remain switchable with the dashboard and a restrained horizontal page transition.

### Profile-specific appearance

Each virtual profile may have:
- A name.
- An optional wallpaper/background image.
- A background visibility/opacity control.
- A separate accent colour.

Wallpaper selection must use the system document picker and retain access across restarts. Removing a wallpaper must also release its persisted URI permission where possible. A profile with no wallpaper falls back to its accent-colour gradient.

### Global appearance

Settings will provide:
- Light, dark, and follow-system modes.
- A global accent colour for common controls.
- A 3/4/5-column app grid choice.

Global appearance and profile appearance are separate: the profile's accent and wallpaper should not unexpectedly change the rest of the app.

### App management

Keep the existing BlackBox app operations and make them easier to reach:
- Launch an installed virtual app.
- Show package and APK details.
- Clear virtual app cache.
- Clear app data (with a clear warning that this resets the app).
- Stop a running virtual app.
- Uninstall from the selected profile.
- Create a shortcut where supported.
- Install an APK into the selected profile.

Cache clearing is limited to the virtual app's internal and external cache directories. It is not the same as clearing all app data.

## RikkaHub interaction and animation study

The upstream RikkaHub implementation uses:
- Material 3 modal navigation drawers with a scrim and a dedicated drawer sheet.
- Bottom sheets for selection flows.
- Animated list-item placement/appearance for changing lists.
- Shared-element/shared-bounds transitions for an item opening into its detail page.
- Assistant background images with a configurable opacity and a gradient overlay to keep foreground content legible.

Sleepy Hub uses a different UI toolkit (Android Views rather than Jetpack Compose). The implementation maps the *interaction principles*, not source code:
- AndroidX DrawerLayout provides the modal slide and scrim.
- ViewPager2 uses a subtle fade/scale/translation page transformer.
- Profile cards enter with a short staggered fade/translation.
- RecyclerView keeps its item animations and drag-to-reorder behavior.
- Profile wallpapers are sampled off the main thread and shown under a dark overlay.

No RikkaHub code, images, icons, name, or branding are copied. RikkaHub's repository is AGPL-3.0; the project uses it as a reference only.

## Performance guardrails

- Do not force garbage collection from RecyclerView scroll callbacks.
- Do not skip app icons just because heap usage crosses a threshold.
- Reuse app icon drawable constant states in a bounded cache.
- Decode profile wallpapers off the main thread and downsample them to a display-sized bitmap.
- Keep app enumeration and BlackBox operations off the main thread.
- Do not add background-running services in this UI milestone.

## Debug-first acceptance checklist

The UI milestone is not complete merely because Gradle builds. Test on a real Android device:

1. Fresh install creates a usable first profile.
2. Add a second and third profile; each remains even while empty.
3. Switch profiles from the dashboard and by swiping; app lists do not leak across profiles.
4. Rename a profile; the name survives a force-stop and restart.
5. Set a wallpaper; it appears on the profile card and profile workspace after restart.
6. Change wallpaper visibility; text remains readable.
7. Remove wallpaper; the accent gradient returns and the old URI permission is released where possible.
8. Change a profile accent; only that profile's surfaces/cards update.
9. Change global light/dark/system mode and global accent; all screens remain legible.
10. Change app grid columns; the app grid uses the selected count.
11. Install and launch a test APK in one profile; it does not appear in another profile unless installed there too.
12. Verify App Info, Clear Cache, Clear Data, Stop, Uninstall, and Shortcut actions.
13. Clear Cache does not clear app data; Clear Data clearly warns that app data will be reset.
14. Delete a profile; its virtual apps/data and appearance preferences are removed. The last remaining profile cannot be accidentally deleted.
15. Test drawer opening/closing, Android Back, profile switching, scrolling, long-press menus, and app install while watching for jank or crashes.
16. Verify no VPN feature or background-running feature has been reintroduced.

## Release boundary

- Build and test a debug APK first.
- The user installs and tests the debug APK and reports anything that feels wrong.
- Only after approval should a signed release build be planned.
- Signing keys/secrets are not created, uploaded, or used during this debug milestone.
- The main branch remains untouched; work stays on \`sleepyhub/ui-rikkahub-inspired\`.
