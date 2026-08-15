# Changelog

## 0.1.2

- The book no longer marks the items that would unlock more recipes until you upgrade it.
  Craft a Bookbinder's Lens (amethyst, copper and glass panes) and use it to bind it into
  your recipe book: the marks appear and the recipe book button shows the lens.
  Shift + right-clicking the recipe book button takes the lens back out if you want the
  hints gone again. Dying drops the lens with the rest of your things, so you can fetch it back and bind it
  again, unless keepInventory is on. On a server without Craftbound the lens cannot be
  obtained, so the marks show from the start as before. Servers can turn the whole gate
  off in the config.

- Tooltips in the book are quieter: they no longer name the mod an item or a recipe came
  from, no longer list the tag a slot accepts, and shapeless recipes are no longer marked
  in the corner. Tooltips inside a recipe are also drawn at full size instead of shrinking
  with the recipe.

- Updating Create no longer risks crashing the game. If a new version no longer fits
  Craftbound's Ponder integration, that integration turns itself off and the book keeps
  working.

- Craftbound can now be used on servers that do not have it installed. Such servers no
  longer refuse the connection, and progression keeps working: the client tracks the
  items you obtain itself and remembers them per server.
- On a server without Craftbound, placing a recipe into the crafting grid works for
  recipes your vanilla recipe book has already learned. The place button is greyed out
  for the rest instead of doing nothing when clicked.

## 0.1.0

First release.

- Replaces the vanilla recipe book with a restyled book: a searchable, paged browse
  grid covering items and fluids, category tab rail, and a craftable-only filter.
- Shows recipes from any mod through an embedded JEI runtime, discovered from every
  loaded mod rather than a fixed list, including Create's machine recipe categories.
- Adds a progression system: recipes stay hidden until their ingredients have been
  obtained, unlocks are gated on fluids and machine heat, and a toast announces newly
  unlocked recipes.
- Adds bookmarks with their own rail tab, a button that places the shown recipe into
  the open menu, and search aliases for items.
- Hides undiscovered items from Ponder.
