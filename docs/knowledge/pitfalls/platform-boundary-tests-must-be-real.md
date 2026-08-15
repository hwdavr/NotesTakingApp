# Platform Boundary Tests Must Be Real

## Symptom

A feature can compile and pass JVM tests while failing on an Android device because the production adapter never receives the platform resource or callback it was designed to consume. This is especially easy to miss with microphone, speech-recognition, camera, permission, and hardware APIs.

## Cause

Fake adapters and JVM-only intent tests validate the injectable seam, not the Android runtime contract. A missing emulator, model, locale, permission, or platform service may also be recorded as a skip, allowing unsupported evidence to look like a pass.

## What to do instead

Create `platform-capability-matrix.md` before implementation. Declare minimum/target API behavior, explicit fallbacks, and a `fail_loudly` unsupported-environment policy. Add at least one real instrumented boundary test using a deterministic local fixture and the shipped platform adapter. Run `bash scripts/check-platform-evidence.sh "$FEATURE_DIR" --evaluate`; missing matrices, pending environments, skipped tests, and fake-only platform tests must fail evaluation.

## Reference

- `docs/templates/platform-capability-matrix-template.md`
- `scripts/check-platform-evidence.sh`
- `docs/product/2026-08-14-voice-notes/platform-capability-matrix.md`
