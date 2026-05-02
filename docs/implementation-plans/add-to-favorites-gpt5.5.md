# Plan For Add To Favorites

  I found the current UI already has Add to Favorites in both folder and note action sheets, but the callback only
  dismisses the sheet.

  Assumption: “Favorites” is the existing special folder named Favorites, since the app already counts favorites by
  finding a folder with that name and counting notes in it. I’ll implement the feature by moving selected notes/folders
  into that Favorites folder, creating it if missing.

  Implementation plan:

  1. Update FoldersViewModel
      - Add addNoteToFavorites(note).
      - Add addFolderToFavorites(folder).43
      - Find existing Favorites folder by name.
      - If missing, create a root Favorites folder with a stable id.
      - Move the selected note/folder into that folder.
      - Prevent moving the Favorites folder into itself.
  2. Update FoldersScreen
      - Wire action sheet callbacks to the new ViewModel methods.
      - Add stable test tags for Add to Favorites.
  3. Update CollectionNotesViewModel
      - Make the favorites collection show items inside the Favorites folder instead of always returning empty.
  4. Tests
      - Unit test ViewModel behavior for note and folder add-to-favorites.
      - UI test that tapping Add to Favorites emits the correct callback/action.
      - Run focused local tests first, then build if needed.

  No API contract change required because this reuses the existing move-folder/move-note API behavior through parentId.

  Per the workflow’s mandatory review step: please confirm this plan, especially the assumption that “favorite” means
  “move into the Favorites folder.”