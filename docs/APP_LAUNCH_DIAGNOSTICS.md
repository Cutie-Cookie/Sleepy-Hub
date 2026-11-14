# Temporary app-launch diagnostics

This branch exists only to identify why virtual apps open to a black screen.

## What is recorded

The local report records launch checkpoints from Sleepy Hub's own code:

- launch request and resolved virtual activity
- process/provider startup and binder availability
- proxy activity dispatch
- framework activity transaction rewriting
- target Application and Activity `onCreate` / Activity `onResume`
- process death, timeout, and sanitized exception summaries

The report includes Android release/API, timestamps, process/thread IDs, virtual profile ID, and target package/component names.

## What is not recorded

The diagnostic code does not read target-app files or screens, account data, passwords, messages, contacts, precise location, device identifiers, or network identifiers. It does not upload anything. The trace is stored only in Sleepy Hub's private app files and is copied only when the user taps **Copy report**.

Exception messages are shortened and common URLs, content URIs, emails, and filesystem paths are redacted.

## Test steps

1. Install the diagnostic Sleepy Hub Preview APK over the existing Preview. Keep the regular Sleepy Hub installation and its data.
2. Tap one harmless installed app in the workspace.
3. If a black screen appears, press Back to return to Sleepy Hub. If no app opens and the Hub remains visible, wait about four seconds.
4. When the report dialog appears, tap **Copy report** and paste the text into the debugging conversation.
5. Test only one app before sending the report.

This is a diagnostic build, not a production release. It does not change the current launch-path selection or redesign the loading UI.
