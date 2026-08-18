# Recipe Book UI: design intentions

Living design doc for Craftbound's reworked recipe book. Captures the settled UX model so
implementation stays aligned. Update this when a decision changes.

## Goal

One recipe book that looks Minecraft-native and unifies **vanilla crafting** and **Create
machine recipes** (mixing, crushing, smelting, …) in a single place. It must not overlay or
replace the whole screen. It lives alongside the inventory the way the vanilla recipe book
does.

## The problem with the "two books" approach

A common existing design uses two separate books: one vanilla crafting book, and a second
book/interface for machine recipes. That splits recipes **by mechanism** (crafting vs.
machine), which forces the player to already know the mechanism before they look. People think
**by result item** ("how do I get brass?"), so they end up hunting through both books.

**Unifying principle:** browse by output, then reveal *every* way to make it, with vanilla and
Create as peers. The recipe mechanism is a sub-navigation within one item, never a top-level
split.

## Settled model

### Entry & framing
- A **single book button** replaces the vanilla recipe-book toggle.
- The book opens as a panel to the **left of the inventory**; the inventory stays fully visible
  and usable.
- **Fixed height = inventory height.** In the browse state the width stays at the vanilla book
  width. In the recipe state the panel **grows left to fit the shown recipe** (revised from the
  original "never widen" rule): JEI's category drawables have fixed horizontal layouts, so wide
  multi-step recipes (e.g. Create sequenced assembly) would otherwise scale down to be unreadable.
  The right edge stays anchored beside the inventory; growth is clamped so the left edge and the
  protruding tab rail stay on-screen. A recipe still wider than the clamped width scales down.

### Two states in one column
The book is a single vanilla-width column with two swappable states:

1. **Browse**: a search box and a scrollable grid of output items (like the vanilla book).
   Never-crafted items keep the progression tint (see below).
2. **Recipe**: clicking an output **replaces the grid in the same column** with how that item
   is made. A **back button** (`‹ items`) returns to the browse grid.

There is **one predictable behavior**: click → show the recipe. Nothing is ever filled into the
open screen unless the player asks for it with the place button (see below).

### Recipe methods = left rail
When an item has more than one recipe, each method is a **tab on a left rail** that protrudes
from the panel, mirroring vanilla's category tabs. Vanilla crafting and Create machines appear
as peers here. This rail *is* the unification.

- Rail tabs are badged with each recipe **category's icon** (crafting table, furnace, blast
  furnace, Create crushing wheels, mechanical mixer, …).
- If methods overflow the visible height (currently ~5 tabs), the rail **pages with ▲ / ▼**.
  The active selection persists across pages, like vanilla.

### Recipe order within a method
JEI hands recipes over in the order Minecraft hashed them into its recipe map, which is arbitrary and
shifts whenever recipes are added or removed. `RecipeOrder` ranks the ones the player can make
right now first (stable sort, so everything else keeps its relative order), judged from the
recipe's own JEI input slots so it works for Create categories as well as vanilla ones. It is
computed once, when the recipe is opened: re-ranking live would make recipes jump under the cursor.
Counts are ignored: this ranks recipes, it does not promise the craft fits.

### Per-method recipe body
Every recipe kind, vanilla crafting and Create/machine alike, is rendered the same way: by
hosting the recipe's **JEI `IRecipeCategory` drawable** inside the body rect (reusing JEI's and
Create's registered renderers rather than reimplementing them). The drawable is scaled to fit the
body; the panel first grows wider (see framing) so wide recipes need little or no shrinking. This
replaces the earlier plan of a native crafting grid plus a hand-rolled vertical Create layout.
One uniform path is simpler and covers every category, including fluids.

## Why JEI
JEI is a required dependency. It is what knows every mod's recipes: any mod with JEI support shows
up in the book without being named in our source, because `CraftboundJeiPlugin` is an ordinary
`@JeiPlugin` and JEI hands it the runtime like any other. We reuse two things from it, Create's
`IRecipeCategory` renderers and JEI's ingredient drawables, and render them into **our own small
rectangle** inside the book instead of calling JEI's full-screen `RecipesGui`.

JEI is used as the recipe backend, not as a second interface, so its own UI is kept off screen.
This matters for progression rather than for taste: JEI's views show the whole pack at once, which
is the opposite of a book that reveals things as they are found.

It takes three pieces, ordered by how well each survives a JEI update:

- `JeiOverlayHider` registers a global gui handler claiming the whole screen. JEI shows a list only
  where it has room, so this hides the ingredient list and the bookmark list through the api alone,
  without touching the player's JEI settings.
- `JeiScreenBlocker` cancels JEI's recipe screen as it opens, on vanilla's `ScreenEvent.Opening`.
  The show-recipe and show-uses keys work off whatever the cursor is over, inventory slots
  included, and JEI's key mappings are read-only in the api. The screen is matched by class name,
  so JEI stays off the compile classpath.
- The mixins in `client/mixin/jei` are the only part with no api behind it. JEI draws its two corner
  buttons whenever the screen is valid, outside the check for whether its lists have room, so no
  amount of denying it space will hide them. `JeiOverlayMixin` cancels the single call that draws
  everything JEI puts on a screen, and `JeiInputMixin` turns away the clicks that would otherwise
  land on buttons that are no longer drawn.

`craftbound.jei.mixins.json` is deliberately not `required`. A JEI that renamed these should bring
its own interface back, never stop the game from loading.

Not everything JEI registers is a recipe. Its "Tag Info" entries take a whole tag in and hand the
same tag back out, which read as recipes make every member of a tag reachable from any other, so
one dirt block would reveal everything sharing a tag with it. `BookCategories` is the single answer
to which categories the book is about, and both the display and the progression index ask it: a
category counted by one and not the other is exactly what puts an entry in the book that cannot be
opened.

Only JEI's api artifacts are on the compile classpath, so reaching for a JEI internal is a compile
error. That is deliberate: the api is the surface JEI keeps stable across Minecraft versions and
loaders, and staying on it is what makes a port to another version a matter of the mixins and the
vanilla recipe plumbing rather than of JEI.

One internal is still unavoidable. JEI's shapeless marker has no api to suppress it, so
`client/mixin/ShapelessIconMixin` names `mezz.jei.library.gui.recipes.ShapelessIcon` as a string
and hides it only while `BookRecipeRender` says the book is the one drawing, leaving JEI's own
screens as JEI drew them.

Craftbound used to vendor JEI's source instead, which let it run without JEI installed at the cost
of carrying a copy of JEI per Minecraft version. Depending on JEI trades that standalone operation
for JEI's own multi-version, multi-loader support.

## Obtained items (already implemented)
`ObtainedItemsTracker` records every item the player has ever held into the synced
`OBTAINED_ITEMS` attachment (`ObtainedItems` / `CraftboundAttachments`). An item counts as obtained
however it was acquired, whether mined, looted or traded, not just crafted. This one set backs both the
progression tint and progressive unlocking below.

`client/mixin/RecipeButtonMixin` tints recipe-book slots for recipes whose result the player has
never had; in the browse grid the same tint marks an item that is unlocked but has never been made.

## Progressive unlocking (already implemented)
The book starts nearly empty and grows as the player plays, instead of showing every recipe in the
pack on day one. A recipe is **unlocked once everything it is made from has been obtained**. Logs
reveal planks, planks reveal sticks, and the chain carries across mechanisms: no Mechanical Mixer
in hand means no mixing tab and no mixing recipes anywhere.

- **Locked means hidden**, not greyed. Locked items are absent from the browse grid and from search;
  locked recipes are absent from an item's rail, and a category with nothing left drops off entirely.
- **Unlock state is derived, never stored.** The obtained set only ever grows, so anything unlocked
  stays unlocked without a second thing to persist and keep in sync. Everything is computed
  client-side from the already-synced attachment, with no new packets.
- **A slot's alternatives are one requirement.** "Any planks" is satisfied by any one of them, which
  is why the index keeps slots apart instead of flattening a recipe to a list of ingredients the way
  JEI's own `IngredientSupplierBuilder` does. A slot with no item alternatives (a fluid) cannot be
  judged and never locks a recipe, the same convention `RecipeOrder` uses.
- **`minecraft:crafting` is exempt from category gating** by default. Gating it on owning a crafting
  table strands a new player, who needs the crafting category to find out how a table is made.
- Anything the index could not read, such as a category that throws while laying a recipe out, counts as
  unlocked. Showing a recipe early is recoverable; hiding one forever is not.

### Requirements a category hides
Create's basin recipes need a heat source under the basin, but `BasinCategory` paints that in
`draw()` rather than declaring it as a slot, so the generic walk cannot see it, and lava looked
makeable the moment its ingredients were in hand. `progression/create/HeatRequirement` turns a heat
condition into **extra input slots** (heated → Blaze Burner; superheated → Blaze Burner *and* Blaze
Cake, as two slots, since a burner alone only reaches "heated"), which is exactly what a heat source
is: another thing you must have obtained. `Unlocks` stays free of any Create knowledge, and
`client/jei/RecipeRequirements` is the only place outside `client/ponder` that touches Create's
classes, behind a `ModList.isLoaded` guard.

## Create is optional
Create is declared `optional` in `neoforge.mods.toml`, and the mod runs without it: the book is
built on JEI's plugin system, not on Create. Two things carry the dependency, and both are isolated
so they simply do not run when Create is absent.

- `client/ponder/` holds the Ponder integration, with its mixins in `client/ponder/mixin/` under a
  separate `craftbound.ponder.mixins.json` marked `"required": false` with `defaultRequire` 0, so
  Mixin skips them when the Ponder classes are missing and an injector that no longer matches a
  changed Create is a warning rather than a crash. `CraftboundPonderPlugin.register()` sits behind a
  `ModList.isLoaded("ponder")` check in `Craftbound`, because resolving the class loads Ponder's own.
- `client/jei/RecipeRequirements` guards the heat requirement described above.

Create is `compileOnly` for compiling and `localRuntime` for the dev client to load, so it never
reaches the published jar. `./gradlew runClient -PnoCreate` drops Create, Ponder, Flywheel and
Registrate from that runtime classpath and starts a client without them, to exercise these paths.

Anything else a mod hides from its own layout stays invisible to progression, which errs towards
showing a recipe too early rather than stranding one.

### Fluids unlock transitively
Fluids can never enter an inventory, so they can never be "obtained". Left at that they would gate
nothing, and a Bar of Chocolate showed up before chocolate itself was reachable. A fluid input is
instead satisfied when **some unlocked recipe produces it**, which makes `unlockedOutputs` a
fixpoint rather than a single pass, since unlocking one recipe can unlock the next. Sugar and cocoa
therefore reveal the chocolate *and* the bar in the same breath.

Items keep the strict obtained rule; only fluids are transitive, precisely because they are the ones
that cannot be held.

Two things stop this from creating gates that never open:
- A fluid **nothing produces** (water) is left unjudged, exactly like a slot we cannot read.
- A fluid produced **only by recipes that also consume it** is likewise unjudged. Create's brewing
  both takes and makes potion fluid; treating that as produced would lock every brewing recipe
  behind a fluid with no way in (`RecipeIndex#bootstrappable`).

A fluid's **bucket** is folded into the slot's items, so having one in hand also satisfies the slot.
It only ever helps: judgeability is decided by the fluids, so a bucket never turns an ungateable
water slot into a gate.

`progression/UnlockKey` owns how outputs are named. Items are registry-level, ignoring counts and
components. Fluids carry a **subtype** as well: every Create potion is `create:potion`, so a
registry-only key let one unlocked brewing step reveal every potion in the game while each one's
recipes stayed locked, fluids you could see with nothing behind them.

The subtype has to come from `IIngredientHelper#getUid`. That is the only path reaching JEI's modern
subtype data, which is where Create's `PotionFluidSubtypeInterpreter` puts the potion type; its
legacy string counterpart, the one `getUniqueId` reads, returns `""`, so keying on that collapsed
the potions all over again.

### Unlock toast
Vanilla keeps unlocking recipes and toasting them even with its book forced hidden, which left two
notifications side by side, one pointing at a book the player does not have, disagreeing with each
other, since vanilla unlocks on its own rules rather than Craftbound's progression.
`client/mixin/RecipeToastMixin` suppresses it, leaving one answer to "what did I just unlock?".

Obtaining an item that unlocks something pops vanilla's recipe toast in the top-right, using the same
`toast/recipe` sprite and same "New Recipes Unlocked! / Check your recipe book" strings, so it reads
as the popup players already know. `RecipeUnlockToast` re-implements it over items because
`net.minecraft`'s `RecipeToast` needs a `RecipeHolder`, which JEI-sourced machine recipes have no
equivalent of. `ProgressionToasts` polls on a client tick rather than leaning on the book's own
refresh: unlocks happen while the player is out mining, not while the book is open. The first pass
over a fresh index is a silent baseline: announcing everything already unlocked would bury the
player in toasts on every world join.

Layering: `progression/Unlocks` is the whole rule set as pure functions over
`RecipeIndex` / `RecipeNode` (plain ids and sets, unit-tested without the game).
`client/jei/RecipeIndexBuilder` + `SlotIngredientCollector` reduce every recipe JEI knows to that
data in one pass, running each category's `setRecipe` against a collector that builds nothing
drawable. `client/progression/Progression` caches the index and the derived unlocked set, rebuilding
when the obtained set grows (detected by its size, since it only grows).

`ProgressionConfig` is a **server** config, so a pack decides the behaviour and every client agrees:
`enabled`, `rule` (`ALL_INPUTS` / `ANY_INPUT`), `gateCategories`, and `exemptCategories`.

## Place button (already implemented)
The recipe state carries a place button in the top strip, where the craftable filter sits while
browsing. It appears only on the tab matching the **open menu**, the plain crafting category, or
the smelting family for a furnace/smoker/blast furnace, for a recipe that fits the menu's grid,
and is greyed out while the ingredients are missing. Gating on the *category* and not just on the
recipe matters: Create lists ordinary shaped recipes again under its own "Automatic Shaped
Crafting" tab, where the recipe is meant for a mechanical crafter, so that tab offers no button
even though the same recipe does under Crafting. Clicking sends `PlaceRecipePayload`; the server hands the recipe to
`RecipeBookMenu.handlePlacement`, i.e. exactly the vanilla fill (shift = as many as possible).
Because vanilla placement refuses recipes its own book has not unlocked, the handler first marks
the recipe as known: Craftbound offers every recipe, so placing one counts as learning it.

## Bookmarks (already implemented)
The left rail carries recipe categories while a recipe is open and **bookmarks while browsing**,
where the rail would otherwise be empty; `BookRail` owns the geometry, paging and hit-testing for
both, and `RailLayout` is the pure paging math behind it. A bookmark stores JEI's own ingredient
identity (`<type uid>|<ingredient uid>`), so items and fluids work alike and a bookmark survives
restarts; it resolves against the ingredients the book already loaded, and one whose mod is gone
just doesn't resolve rather than being deleted.

The rail carries **one** ribbon tab, present once something is bookmarked, and clicking it switches
the grid between everything and the bookmarked items. The bookmarked items are then browsed
exactly like any others: same slots, same tints, same paging, same click-to-open, with search and
the craftable filter still applying on top. Bookmarking itself is the ribbon button in the recipe
view's strip, left of the place button; the same button clears one. Storage is
`config/craftbound-bookmarks.json`, keyed per world (save name) and per server (address), and never
touches the server: bookmarks are a client convenience.

## Trade-offs & open questions
- **Narrow screens.** `RecipeBookLayout` reserves a fixed column wide enough for the widened
  recipe panel and shifts the inventory right by it, so the two never overlap and the inventory
  never moves between browse and recipe states. The reservation is clamped at the screen edge,
  so an effective width below roughly 384 px (a small window at a large GUI scale) pushes the
  pair off-centre. Untested at those sizes.
- **Recipes wider than the clamped panel** still scale down to fit, which is readable for most
  Create categories but tight for long sequenced assembly chains.
- **Category gating leans on the workstation being an item.** A category whose "machine" is not
  a held item cannot be gated, and falls back to being always unlocked.

## Status
Implemented and in use:
- Vanilla recipe book **suppressed** globally via `client/mixin/RecipeBookVisibilityMixin`
  (forces `RecipeBookComponent.isVisible()` false). The Craftbound book replaces it everywhere
  the vanilla one appeared, including crafting tables and the furnace family.
- Recipes render through JEI's own category drawables inside the docked panel, via
  `IRecipeManager.createRecipeLayoutDrawableOrShowError` + `IRecipeLayoutDrawable`. The design
  spike this started as is gone.
- Browse grid with search, item category ribbons, bookmarks, the craftable-only filter and the
  place button, all described above.
- Progression, unlock toasts and the Create heat requirement, all described above.
- Ponder scenes hide items the player has not discovered (`client/ponder`).

## Reference
Interactive layout mock (throwaway, not in-repo) built during design, demonstrated on **brass**
(3 methods) and **iron** (7 methods, to exercise rail paging).
