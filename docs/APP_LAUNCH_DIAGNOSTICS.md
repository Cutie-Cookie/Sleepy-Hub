# Temporary app-launch and SIM-identity diagnostics

This branch extends the temporary launch report to diagnose why synthetic ICCID and IMSI values appear shared across Sleepy Hub profiles.

## What is recorded

The local report records:

- launch request and resolved virtual activity
- process/provider startup and binder availability
- proxy activity dispatch and framework activity transaction rewriting
- target Application and Activity lifecycle checkpoints
- process death, timeout, and sanitized exception summaries
- **ICCID/IMSI hook-routing metadata only:** the hook method name, the virtual profile ID passed to the identifier store, and whether the virtual app configuration was present
- **Identifier-store metadata only:** whether a value came from the process cache, the profile's saved file, fresh generation, or a deterministic fallback; plus the IDs of other profiles whose saved value matches it (if any)

The report already includes Android release/API, timestamps, process/thread IDs, virtual profile ID, and target package/component names.

## What is not recorded

The diagnostic code does **not** record actual ICCID, IMSI, IMEI, MEID, DRM Media ID, subscription IDs, or hook arguments. It never writes or changes identifier values. The duplicate check reads only Sleepy Hub's own `.sleepyhub-identifiers` files under the virtual profile directories, compares values in memory, and records only matching profile numbers. It does not read target-app files or screens, account data, passwords, messages, contacts, precise location, or network identifiers. It does not upload anything. The trace is stored only in Sleepy Hub's private app files and is copied only when the user taps **Copy report**.

Exception messages are shortened and common URLs, content URIs, emails, and filesystem paths are redacted.

Identifier hook events are rate-limited to at most one report line per hook/profile/app-configuration combination per second. Store-origin and duplicate checks are recorded once per identifier/profile/process, so apps that poll these APIs do not flood the report. The duplicate check is read-only and does not run on every API poll.

## SIM-identity isolation test

Use the same harmless identifier-checking app in both profiles. The user does not need to share the actual identifier values.

1. Install the diagnostic Sleepy Hub Preview APK over the existing Preview. Keep the regular Sleepy Hub installation and its data.
2. Switch to profile A and open the checker. Let it display its SIM ICCID and IMSI fields.
3. Return to Sleepy Hub and copy the diagnostic report. Save it as the profile A report.
4. Switch to profile B, open the same checker, and copy a second report.
5. Compare only the `profile` column, `identifier.iccid.hook` / `identifier.imsi.hook` stages, and the `identifier.iccid.store` / `identifier.imsi.store` events. In store events, `source` means `cache`, `disk`, `generated`, or `fallback`; `duplicateProfiles=4,5` means the saved value matches those other profile stores, while `none` means no match was found. `unavailable` means the scan could not complete. Do not paste the displayed ICCID/IMSI values into the debugging conversation.

Each new app launch attempt clears the previous report, so copy profile A's report before starting the profile B test.

## App-launch test

1. Tap one harmless installed app in the workspace.
2. If a black screen appears, press Back to return to Sleepy Hub. If no app opens and the Hub remains visible, wait about four seconds.
3. When the report dialog appears, tap **Copy report** and paste the text into the debugging conversation.
4. Test only one app before sending the report.

This is a diagnostic build, not a production release. It does not change the current launch-path selection, identifier generators, persisted identifier values, DRM behavior, permissions, or UI/loading design. The additional store check is read-only and runs once per ICCID/IMSI value per virtual app process.
