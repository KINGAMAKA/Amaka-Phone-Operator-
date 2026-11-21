# Amaka AI — Android Phone Operator (prototype 0.1)

A native Android Studio starter project for the integrated Amaka AI phone-operator capability.

## What works in this starter
- Typed commands and Android speech recognition.
- Launch an installed app by its visible name (for example `open YouTube`).
- Accessibility actions: `back`, `home`, `scroll down`, `scroll up`, `tap Search`, `click Settings`, and `type hello`.
- Spoken status feedback using Android Text-to-Speech.

## What this is not yet
This is the first working foundation, not a fully autonomous video editor or general-purpose AI agent. It does not yet generate video, understand arbitrary natural-language multi-step goals, inspect screenshots with a vision model, or operate every app reliably. Those are the next development milestones.

## Build
1. Install Android Studio (Hedgehog or newer) on a computer.
2. Open this folder as a project.
3. Allow Gradle sync to complete (Android Gradle Plugin 8.7.3 / Kotlin 2.0.21).
4. Connect an Android phone with USB debugging enabled, or use an emulator.
5. Press **Run**.

## Enable control on your phone
1. Open Amaka Phone Operator.
2. Tap **Enable phone-control permission**.
3. In Android Accessibility settings, select **Amaka Phone Operator** and enable it.
4. Return to the app and try a simple command such as `open YouTube`, then `scroll down`.

Android may show a prominent warning because Accessibility can observe and interact with screen content. Enable it only for this app if you trust the build.

## Important Android constraints
- Android requires the user to enable Accessibility manually; an app cannot silently grant itself this permission.
- Some apps block accessibility interaction or hide their controls.
- Screen lock, secure screens, permission dialogs, payment confirmation and other protected operations may require direct user involvement.
- Keep the phone unlocked while running tasks. For long jobs, keep it charged.
- Speech recognition availability depends on an installed speech-recognition provider and may use network services.

## Roadmap to full AMAKA AI integration
1. Add a secure AI planner endpoint that converts voice instructions into a strict, validated action plan.
2. Add screen understanding (on-device OCR first, optional cloud vision with explicit consent).
3. Add a persistent task queue, step confirmations, retries, cancellation and audit log.
4. Add media workflows: create assets, assemble video with FFmpeg, captions, export and verify files.
5. Integrate with the existing AMAKA AI chat/voice interface and memory settings.

Never store API keys in the Android app. Put provider credentials behind a backend service.

## Build an APK through GitHub (no Android Studio needed locally)
1. Create a GitHub repository and upload the contents of this project folder (not the ZIP file itself).
2. Open the repository's **Actions** tab.
3. Run **Build Android APK** (or push to `main` / `master`).
4. When the workflow completes, open its run and download the `amaka-phone-operator-debug-apk` artifact.
5. Extract the artifact and install `app-debug.apk` on your Android phone. Android may ask you to allow installation from that source.
