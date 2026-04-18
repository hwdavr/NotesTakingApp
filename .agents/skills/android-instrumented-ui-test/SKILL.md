# Android instrumented UI test skill

Use this skill for Android UI tests on real runtime.

## Good targets
- list screen renders mocked items
- loading / empty / error / success UI states
- click and visible reaction
- click and navigation
- critical multi-screen happy paths
- real UI wiring from mocked backend or fake repository to screen

## Required process
1. inspect existing BaseUiTest/TestCase and similar tests
2. reuse Screen objects and helpers
3. arrange deterministic mocked input through:
   - MockWebServer
   - fake repository
   - DI override
   - fixture loader
4. use Given / When / Then through step blocks
5. assert user-visible behavior only

## Shared JSON scenario usage
If a shared JSON scenario exists:
1. load apiMocks
2. launch the screen under test
3. assert expected.ui

## Rules
- one scenario per test
- no Thread.sleep
- stable selectors only
- prefer destination screen assertion for navigation
- do not duplicate the full API error matrix here