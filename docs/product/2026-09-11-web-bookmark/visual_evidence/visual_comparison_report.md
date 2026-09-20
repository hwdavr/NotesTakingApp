# Visual Comparison Evaluation Report
**Feature Directory**: `2026-09-11-web-bookmark`
**Visual Target Manifest**: `visual_evidence/visual-target.json` (target `web-bookmark-light-medium-phone-en-us`, light, Medium_Phone(AVD) - 13, 411x914 dp, `en-US`)
**Threshold**: `0.95` (binding approved mockup comparison; structural-anchor proof is separately binding in `check-visual-evidence-contract.sh`)
**Overall Status**: `PASS`

| Actual Screenshot | Approved Mockup | Gate Role | Matched Via | Similarity Score | Diff % | Diff Overlay | Status |
|---|---|---|---|---|---|---|---|
| `web_bookmark_actions_sheet.png` | `mockup_editor_web_bookmark_actions_sheet_from_baseline_v2.png` | binding | explicit-mockup-map | 0.9958 | 0.42% | [`web_bookmark_actions_sheet_diff.png`](web_bookmark_actions_sheet_diff.png) | **PASS** |
| `web_bookmark_add_page.png` | `mockup_add_web_bookmark_full_page_from_baseline_v2.png` | binding | explicit-mockup-map | 0.9843 | 1.57% | [`web_bookmark_add_page_diff.png`](web_bookmark_add_page_diff.png) | **PASS** |
| `web_bookmark_add_page_keyboard.png` | `mockup_add_web_bookmark_full_page_keyboard_from_baseline_v2.png` | binding | explicit-mockup-map | 0.9805 | 1.95% | [`web_bookmark_add_page_keyboard_diff.png`](web_bookmark_add_page_keyboard_diff.png) | **PASS** |
| `web_bookmark_card_content.png` | `mockup_editor_web_bookmark_card_from_baseline_v2.png` | binding | explicit-mockup-map | 0.9696 | 3.04% | [`web_bookmark_card_content_diff.png`](web_bookmark_card_content_diff.png) | **PASS** |
| `web_bookmark_responsive.png` | `mockup_editor_web_bookmark_card_from_baseline_v2.png` | binding | explicit-mockup-map | 0.9706 | 2.94% | [`web_bookmark_responsive_diff.png`](web_bookmark_responsive_diff.png) | **PASS** |
