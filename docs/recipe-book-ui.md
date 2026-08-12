# Recipe Book UI — design intentions

Living design doc for Craftbound's reworked recipe book. Captures the settled UX model so
implementation stays aligned. Update this when a decision changes.

## Goal

One recipe book that looks Minecraft-native and unifies **vanilla crafting** and **Create
machine recipes** (mixing, crushing, smelting, …) in a single place. It must not overlay or
replace the whole screen — it lives alongside the inventory the way the vanilla recipe book
does.

## The problem with the "two books" approach

A common existing design uses two separate books: one vanilla crafting book, and a second
book/interface for machine recipes. That splits recipes **by mechanism** (crafting vs.
machine), which forces the player to already know the mechanism before they look. People think
**by result item** ("how do I get brass?"), so they end up hunting through both books.

**Unifying principle:** browse by output, then reveal *every* way to make it — vanilla and
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

1. **Browse** — a search box and a scrollable grid of output items (like the vanilla book).
   Never-crafted items keep the progression tint (see below).
2. **Recipe** — clicking an output **replaces the grid in the same column** with how that item
   is made. A **back button** (`‹ items`) returns to the browse grid.

There is **one predictable behavior**: click → show the recipe. Nothing is ever filled into the
open screen unless the player asks for it with the place button (see below).

### Recipe methods = left rail
When an item has more than one recipe, each method is a **tab on a left rail** that protrudes
from the panel, mirroring vanilla's category tabs. Vanilla crafting and Create machines appear
as peers here — this rail *is* the unification.

- Rail tabs are badged with each recipe **category's icon** (crafting table, furnace, blast
  furnace, Create crushing wheels, mechanical mixer, …).
- If methods overflow the visible height (currently ~5 tabs), the rail **pages with ▲ / ▼**.
  The active selection persists across pages, like vanilla.

### Recipe order within a method
JEI hands recipes over in the order Minecraft hashed them into its recipe map — arbitrary, and it
shifts whenever recipes are added or removed. `RecipeOrder` ranks the ones the player can make
right now first (stable sort, so everything else keeps its relative order), judged from the
recipe's own JEI input slots so it works for Create categories as well as vanilla ones. It is
computed once, when the recipe is opened: re-ranking live would make recipes jump under the cursor.
Counts are ignored — this ranks recipes, it does not promise the craft fits.

### Per-method recipe body
Every recipe kind — vanilla crafting and Create/machine alike — is rendered the same way: by
hosting the recipe's **JEI `IRecipeCategory` drawable** inside the body rect (reusing JEI's and
Create's registered renderers rather than reimplementing them). The drawable is scaled to fit the
body; the panel first grows wider (see framing) so wide recipes need little or no shrinking. This
replaces the earlier plan of a native crafting grid plus a hand-rolled vertical Create layout —
one uniform path is simpler and covers every category, including fluids.

## Why vendor JEI
JEI's source is vendored (`vendor/jei/`) so we can render Create's registered recipe categories
into **our own small rectangle** inside the book, instead of calling JEI's full-screen
`RecipesGui`. We reuse two things: Create's `IRecipeCategory` renderers and JEI's ingredient
drawables.

The embedded runtime finds plugins the way JEI does — `ForgePluginFinder` scans every loaded mod
for `@JeiPlugin` — so any mod with JEI support shows up in the book without being named in our
source. Only JEI's own GUI, internal and debug plugins are dropped, since they build the
full-screen interface the book replaces. Note the flip side of embedding: `neoforge.mods.toml`
declares the JEI mod itself `incompatible`, so a mod that *hard*-depends on JEI cannot be
installed alongside Craftbound; optional JEI support (the common case) works fine.

## Obtained items (already implemented)
`ObtainedItemsTracker` records every item the player has ever held into the synced
`OBTAINED_ITEMS` attachment (`ObtainedItems` / `CraftboundAttachments`). An item counts as obtained
however it was acquired — mined, looted, traded — not just crafted. This one set backs both the
progression tint and progressive unlocking below.

`client/mixin/RecipeButtonMixin` tints recipe-book slots for recipes whose result the player has
never had; in the browse grid the same tint marks an item that is unlocked but has never been made.

## Progressive unlocking (already implemented)
The book starts nearly empty and grows as the player plays, instead of showing every recipe in the
pack on day one. A recipe is **unlocked once everything it is made from has been obtained** — logs
reveal planks, planks reveal sticks — and the chain carries across mechanisms: no Mechanical Mixer
in hand means no mixing tab and no mixing recipes anywhere.

- **Locked means hidden**, not greyed. Locked items are absent from the browse grid and from search;
  locked recipes are absent from an item's rail, and a category with nothing left drops off entirely.
- **Unlock state is derived, never stored.** The obtained set only ever grows, so anything unlocked
  stays unlocked without a second thing to persist and keep in sync. Everything is computed
  client-side from the already-synced attachment — no new packets.
- **A slot's alternatives are one requirement.** "Any planks" is satisfied by any one of them, which
  is why the index keeps slots apart instead of flattening a recipe to a list of ingredients the way
  JEI's own `IngredientSupplierBuilder` does. A slot with no item alternatives (a fluid) cannot be
  judged and never locks a recipe — the same convention `RecipeOrder` uses.
- **`minecraft:crafting` is exempt from category gating** by default. Gating it on owning a crafting
  table strands a new player, who needs the crafting category to find out how a table is made.
- Anything the index could not read — a category that throws while laying a recipe out — counts as
  unlocked. Showing a recipe early is recoverable; hiding one forever is not.

### Unlock toast
Obtaining an item that unlocks something pops vanilla's recipe toast in the top-right — same
`toast/recipe` sprite and same "New Recipes Unlocked! / Check your recipe book" strings, so it reads
as the popup players already know. `RecipeUnlockToast` re-implements it over items because
`net.minecraft`'s `RecipeToast` needs a `RecipeHolder`, which JEI-sourced machine recipes have no
equivalent of. `ProgressionToasts` polls on a client tick rather than leaning on the book's own
refresh: unlocks happen while the player is out mining, not while the book is open. The first pass
over a fresh index is a silent baseline — announcing everything already unlocked would bury the
player in toasts on every world join.

Layering: `progression/Unlocks` is the whole rule set as pure functions over
`RecipeIndex` / `RecipeNode` (plain ids and sets, unit-tested without the game).
`client/jei/RecipeIndexBuilder` + `SlotIngredientCollector` reduce every recipe JEI knows to that
data in one pass — running each category's `setRecipe` against a collector that builds nothing
drawable. `client/progression/Progression` caches the index and the derived unlocked set, rebuilding
when the obtained set grows (detected by its size, since it only grows).

`ProgressionConfig` is a **server** config, so a pack decides the behaviour and every client agrees:
`enabled`, `rule` (`ALL_INPUTS` / `ANY_INPUT`), `gateCategories`, and `exemptCategories`.

## Place button (already implemented)
The recipe state carries a place button in the top strip, where the craftable filter sits while
browsing. It appears only on the tab matching the **open menu** — the plain crafting category, or
the smelting family for a furnace/smoker/blast furnace — for a recipe that fits the menu's grid,
and is greyed out while the ingredients are missing. Gating on the *category* and not just on the
recipe matters: Create lists ordinary shaped recipes again under its own "Automatic Shaped
Crafting" tab, where the recipe is meant for a mechanical crafter, so that tab offers no button
even though the same recipe does under Crafting. Clicking sends `PlaceRecipePayload`; the server hands the recipe to
`RecipeBookMenu.handlePlacement`, i.e. exactly the vanilla fill (shift = as many as possible).
Because vanilla placement refuses recipes its own book has not unlocked, the handler first marks
the recipe as known — Craftbound offers every recipe, so placing one counts as learning it.

## Bookmarks (already implemented)
The left rail carries recipe categories while a recipe is open and **bookmarks while browsing**,
where the rail would otherwise be empty; `BookRail` owns the geometry, paging and hit-testing for
both, and `RailLayout` is the pure paging math behind it. A bookmark stores JEI's own ingredient
identity (`<type uid>|<ingredient uid>`), so items and fluids work alike and a bookmark survives
restarts; it resolves against the ingredients the book already loaded, and one whose mod is gone
just doesn't resolve rather than being deleted.

The rail carries **one** ribbon tab, present once something is bookmarked, and clicking it switches
the grid between everything and the bookmarked items — the bookmarked items are then browsed
exactly like any others: same slots, same tints, same paging, same click-to-open, with search and
the craftable filter still applying on top. Bookmarking itself is the ribbon button in the recipe
view's strip, left of the place button; the same button clears one. Storage is
`config/craftbound-bookmarks.json`, keyed per world (save name) and per server (address), and never
touches the server — bookmarks are a client convenience.

## Trade-offs & open questions
- **Recipe gathering** across vanilla + Create categories for a given output — mechanism TBD.
- **Browse filtering/search** — category tabs vs. plain search in the browse state — not yet
  designed.
- **Responsive positioning.** The panel docks left of the inventory; at small window sizes it
  overlaps the inventory. Needs a fallback (shift/scale/collapse) — deferred.

## Status
- Vanilla recipe book **suppressed** globally via `client/mixin/RecipeBookVisibilityMixin`
  (forces `RecipeBookComponent.isVisible()` false). Note: this also removes the vanilla book at
  crafting tables / furnaces, which our unified book will need to cover.
- JEI-render bet **validated**: a Create recipe renders inside our own rect via
  `IRecipeManager.createRecipeLayoutDrawableOrShowError` + `IRecipeLayoutDrawable`. See the
  throwaway spike (`client/RecipeLayoutSpike` + `CraftboundJeiPlugin.createCreateRecipeLayout`),
  to be replaced by the real docked panel.

## Reference
Interactive layout mock (throwaway, not in-repo) built during design, demonstrated on **brass**
(3 methods) and **iron** (7 methods, to exercise rail paging).
