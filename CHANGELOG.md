# Changelog

## 0.1.7

- The book now opens on screens that take recipes from JEI but have no recipe book of their
  own, such as Create's stock keeper, storage terminals, the brewing stand and the smithing
  table. Its button sits just left of the screen. Where JEI could move a recipe into the
  screen, the book's place button does the same: in the stock keeper it adds the recipe to the
  order. A greyed-out button says why, and clicking it marks what the stock is missing.
  In the stock keeper, the craftable filter shows what its stock can be crafted into, and
  placing a recipe already in the order raises its amount.
- The book's search now shares its text with JEI, so mods that sync their search with JEI stay
  in sync with the book. Typing in the book updates the stock keeper's search, and the other
  way around.
- Right-click the book's search field to clear it.
- On a narrow window the recipe panel now narrows to keep its ribbons and the screen beside it
  in view. Before, the ribbons sat against the screen edge.
- Above 1.21.1, the place button no longer offers a 3x3 recipe in the inventory's 2x2 grid.
  Placing one used to lay out only part of the recipe and pull its ingredients out of your
  inventory. The craftable filter now also leaves these recipes out.
- Items sitting in a furnace, smoker or blast furnace no longer count as yours. Smelting raw
  iron no longer marks iron ingots as craftable, and ingots in the output slot no longer mark
  iron nuggets.
- Above 1.21.1, the craftable filter in a furnace, smoker or blast furnace only looks at that
  block's own recipes, not at crafting recipes.
- Ponder's index and category pages now only list what you have held, the same rule the book
  already used for its own entries. Before, you could open the scenes of every machine whose
  recipe you had unlocked.
- Create's mixing, packing and brewing categories now unlock with the Mechanical Mixer or
  Mechanical Press, and deploying with the Deployer. Holding a Basin or a Depot alone no longer
  opens them.
- The bookmark button now bookmarks what the shown recipe makes. Before, it bookmarked the item
  you opened, so after moving on to a recipe that uses that item, the button still showed it as
  bookmarked.
- With the book open, the book and the inventory now sit centered on the screen. Before, the
  pair sat a little to the right.
- Newer JEI releases no longer show their bookmark and config buttons in the bottom corners.
- Shift-click an item in the book to fill the crafting grid or furnace with as many crafts as
  your inventory allows. If you cannot make the item right now, the click opens its recipes as
  before.
- An item's recipes now open on the tab of the block you are using. In a furnace, copper ingots
  open on smelting rather than on crafting from nuggets.
- A newly unlocked recipe stops waiting to play its highlight once you have seen it in a recipe
  view. Before, the item still played it when you later found it in the book.
- Search now also finds what you can make from the items you name. Type "oak log" and the book
  lists items named oak log first, then what you craft from them, such as planks. This works for
  items no recipe makes, which the book does not list on their own.
- Newer JEI releases no longer stamp a "#" on recipe slots that accept several items, and holding
  shift over a recipe's result no longer shows its recipe id.
- Variants share one slot in the book, as in vanilla's recipe book: every chest boat, bed or wool
  color sits behind a single entry that cycles through them. Click it to pick one. Search for a
  single variant and it shows on its own.
- The lens no longer marks an item whose only news is another variant of a recipe you already
  see. Once one chest boat is in the book, the other boats stop being marked.
- On 1.21.10 and newer, double-clicking a word in the search field selects it, and shift-clicking
  extends the selection, as in vanilla's recipe book.
- Potions, tipped arrows and other items that come in many kinds now unlock one kind at a time.
  Before, getting a Spout unlocked the water bottle and with it every potion in the game. A recipe
  that asks for one potion now wants that potion, so a water bottle no longer counts as every
  potion, and searching a potion's name shows only what it is used for.
- Recipes filled from a fluid nothing can make, such as a Spout filling a Potion of Luck, stay
  hidden.
- A recipe slot that takes any of several ingredients now cycles only through the ones you have
  held. Mundane potion no longer shows a breeze rod or a cobweb you have never found. A slot where
  you have held none of them still shows them all, so the recipe still says what it needs.
- Click the greyed-out place button to see what a recipe is missing. The ingredients you do not
  have turn red, as in vanilla's recipe book.

## 0.1.6

- Downloads for every supported Minecraft version are now published together on GitHub, Modrinth
  and CurseForge. The 0.1.5 release stopped after only some versions reached each platform.

## 0.1.5

- Craftbound now runs on Minecraft 1.21.1, 1.21.4, 1.21.5, 1.21.8, 1.21.10, 1.21.11 and
  26.1.2, with a separate download for each. The book looks and behaves the same on all of them.
- Above 1.21.1 the book has no Create categories, because Create has no release for those
  versions yet. Nothing else is missing.
- Above 1.21.1, placing a recipe into the crafting grid from the book needs a server that also has
  Craftbound. On a server without it the place button stays greyed out, since the game no longer
  gives the client a way to ask for a recipe it has not already learned.
- Craftbound now requires Just Enough Items instead of refusing to run alongside it.
  Install the matching JEI release next to Craftbound. In exchange, mods that hard-depend on
  JEI now work in the same pack, and you get JEI's own screens alongside the book.
- JEI stays out of sight. Its item list, bookmark list and recipe screen do not appear, so
  the book remains the one place recipes are shown and nothing is spoiled ahead of time.
  Your JEI settings are left alone.
- The book itself is unchanged: the same recipes from the same mods, shown the same way.
- Tag listings no longer count as recipes. They treated everything sharing a tag as made from
  everything else in it, so a single block could reveal dozens of entries that then refused
  to open.

## 0.1.4

- Craftbound is now published on CurseForge as well as Modrinth. The 0.1.3 upload there
  did not go through.

## 0.1.3

- Craftbound is now published on CurseForge as well as Modrinth.

## 0.1.2

- The book no longer marks the items that would unlock more recipes until you upgrade it.
  Craft a Bookbinder's Lens (amethyst, copper and glass panes) and use it to bind it into
  your recipe book: the marks appear and the recipe book button shows the lens.
  Shift + right-clicking the recipe book button takes the lens back out if you want the
  hints gone again. Dying drops the lens with the rest of your things, so you can fetch it back and bind it
  again, unless keepInventory is on. On a server without Craftbound the lens cannot be
  obtained, so the marks show from the start as before. Servers can turn the whole gate
  off in the config.

- Ponder can no longer be opened from a book entry you have never held: the hold-to-ponder
  shortcut is simply absent on those. Items lying in chests, inventories and machines are
  unaffected, so anything you can actually see in the world can still be pondered.

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
