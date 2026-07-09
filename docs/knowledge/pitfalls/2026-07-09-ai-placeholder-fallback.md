# AI Placeholder Hidden by Fallback

## Symptom

An AI-backed feature appears to work for simple inputs, but the model is never called. Behavior comes entirely from heuristic fallback code, so semantic cases fail when the note text has no keyword overlap with the expected folder.

## Cause

The smart category implementation kept the Gemini/AICore prompt and inference path as commented placeholder code. Because keyword matching ran afterward, the feature could look functional in demos while never sending an active model prompt.

## What to do instead

- Put model access behind a small injectable data-layer client so tests can prove the prompt path is called and used.
- Write a regression test where the AI-selected category has no keyword overlap with the note text.
- Keep heuristic fallback for unavailable or invalid model output, but never let fallback be the only executable implementation of an AI feature.
- Delete commented sample inference code once a real client exists.

## Reference

- `app/src/main/java/com/example/notesapp/data/summary/GeminiNanoFolderCategorizer.kt`
- `app/src/test/java/com/example/notesapp/data/summary/GeminiNanoFolderCategorizerTest.kt`
