# Changelog

## 0.1.2

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
