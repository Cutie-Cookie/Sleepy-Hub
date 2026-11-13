# Sleepy Hub project notebook

This folder is the working notebook for the Sleepy Hub fork. It records the current baseline, decisions, changes, fixes, tests, and the next UI redesign plan.

## Read these first

1. [Project status](PROJECT_STATUS.md) — what exists now, branch/build baseline, and known gaps.
2. [Change log](CHANGELOG.md) — what changed, why, and which build problems were found and fixed.
3. [Development rules](DEVELOPMENT_RULES.md) — how we investigate, change, test, and record work without guessing.
4. [RikkaHub source research](RIKKAHUB_RESEARCH.md) — the upstream components and behavior being used as the reference.
5. [RikkaHub source audit](RIKKAHUB_SOURCE_AUDIT.md) — exact source-level patterns and gaps found in the current Sleepy Hub rebuild.
6. [UI roadmap](UI_ROADMAP.md) — the agreed source-faithful RikkaHub rebuild direction.
7. [UI implementation plan](UI_IMPLEMENTATION_PLAN.md) — confirmed scope, container/profile ownership, and device acceptance tests.
8. [RikkaHub source map](RIKKAHUB_SOURCE_MAP.md) — file-by-file paths for the drawer, long-press/move flow, container picker/manager, settings/theme, top-bar action, permissions, and Sleepy Hub adaptations.

## Notebook rules

- Update the change log whenever a code, build, asset, or configuration change is made.
- Record the symptom and evidence before choosing a fix.
- Record failed builds as well as successful ones; a failure is useful information.
- Keep **confirmed**, **user-reported**, **built**, and **device-tested** statuses separate.
- Never write passwords, tokens, signing keys, private device identifiers, or other secrets here.
- Do not treat inherited root-level `README.md`, `Docs.md`, or `RELEASE_NOTES.md` as current Sleepy Hub documentation until they are reviewed; they still contain upstream BlackBox/NewBlackbox wording and stale feature descriptions.

## Current documentation baseline

These notes were created on `sleepyhub/docs-project-notes`, based on the verified application commit `6de07c536a013febe6432148dd23f94eec1bac30` from `sleepyhub/identity-icons-no-vpn`.

The documentation branch is intentionally separate. The active UI work is on `sleepyhub/rikkahub-source-rebuild`, based on `sleepyhub/identity-icons-no-vpn`. The earlier `sleepyhub/ui-rikkahub-inspired` prototype was rejected and must not be merged into the rebuild. Neither branch has been merged into `main`.
