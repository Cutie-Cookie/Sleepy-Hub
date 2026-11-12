# Development rules

The goal is to make changes from evidence, not guesses. Keep each change small enough that we can explain what changed and how we checked it.

## Before changing code

For every bug or requested feature, write down:

1. **Problem:** the exact user-visible symptom or requested behavior.
2. **Expected result:** what should happen after the change.
3. **Reproduction:** steps, Android version, and logs/screenshots if available.
4. **Evidence:** the relevant source, stack trace, build output, or confirmed behavior.
5. **Scope:** the smallest set of files that should need changing.
6. **Risk:** what existing behavior could be affected.
7. **Verification:** the build and manual checks that will prove the change.

If the evidence is incomplete, investigate first. Do not edit several unrelated areas to see whether the problem disappears.

## Safe change loop

1. Confirm the active repository and branch.
2. Read the current implementation and its callers before editing.
3. Make one logical change at a time.
4. Inspect the complete diff for accidental edits and unrelated deletions.
5. Build the affected target.
6. Read and understand every build failure before making the next change.
7. Run the relevant smoke tests on a device when possible.
8. Record the result, build run, and commit in `CHANGELOG.md` or `PROJECT_STATUS.md`.
9. Keep the application branch and documentation branch separate unless a merge is explicitly planned.

## Branch safety

- Do not push directly to `main`.
- Application work belongs on a dedicated `sleepyhub/*` feature branch.
- Documentation work belongs on `sleepyhub/docs-project-notes`.
- The next UI implementation should use a separate branch, proposed name: `sleepyhub/ui-rikkahub-inspired`.
- Do not merge, rebase, force-push, or retarget branches without a clear reason and explicit agreement.
- Before each commit, verify the target branch and inspect the diff.

## Build verification

Current CI command:

```bash
./gradlew --no-daemon :app:assembleDebug -PsleepyHubArm64Only=true
```

Useful local checks:

```bash
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
```

The unit-test task should be run when tests exist and are relevant. Do not report it as passed merely because the APK build passed.

For each build, record:

- commit SHA;
- workflow run URL;
- success/failure;
- failing task and the first useful error, if failed;
- artifact name and expiry, if uploaded.

## Current verification checklist

- [x] Latest ARM64 debug workflow completed successfully at run [43309146086](https://github.com/Cutie-Cookie/Sleepy-Hub/actions/runs/43309146086).
- [x] GitHub uploaded the expected `sleepy-hub-arm64-debug` artifact.
- [x] User confirmed the corrected launcher artwork looks good.
- [x] User confirmed VPN removal is complete.
- [ ] Install the current APK on a physical ARM64 Android device and record the Android version.
- [ ] Confirm first launch and normal app navigation.
- [ ] Confirm virtual app install, open, stop, clear-data, uninstall, and shortcut flows.
- [ ] Confirm no VPN permission prompt/service appears and normal network behavior is acceptable.
- [ ] Confirm Settings no longer shows VPN or Send Logs.
- [ ] Confirm daemon notification title/channel/icon on device.
- [ ] Check launcher icon under the device's actual adaptive mask and on at least one alternate launcher if available.
- [ ] Record any device-specific crashes with local logs; do not upload logs automatically.

## Asset rules

- Keep the original user-supplied image as the master source.
- Preserve the original colors, shading, alpha, composition, and aspect ratio unless the user asks for a redesign.
- Never crop the ears, stars, lettering, or other artwork by assuming the Android adaptive mask will show the full square.
- Export density-appropriate variants from the master; do not copy a tiny raster into every density folder.
- Check both legacy bitmap icons and Android adaptive icons.
- Keep splash art and adaptive foreground assets separate.
- Preview the result at launcher size before committing it.

## Privacy and scope rules

- Do not reintroduce automatic log upload, telemetry, or a remote destination without an explicit product decision.
- Do not add permissions or network services as incidental side effects of a UI task.
- Avoid changing application ID, package namespaces, target SDK, or core virtualization behavior during visual-only work.
- Treat dependency upgrades and Android target-SDK changes as separate tasks with their own compatibility checks.

## Change record template

Copy this template into an issue or the change log when a new task starts:

```text
Problem:
Expected result:
Reproduction / evidence:
Affected files:
Root cause (confirmed or hypothesis):
Smallest proposed fix:
Risks:
Build / tests:
Device verification:
Commit:
Status: planned / investigating / fixed / built / device-tested
```
