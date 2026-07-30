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
- **Fixed height = inventory height.** Width stays at roughly the vanilla book width and does
  **not** grow — matching Minecraft's design language (shared height baseline, no widening).

### Two states in one column
The book is a single vanilla-width column with two swappable states:

1. **Browse** — a search box and a scrollable grid of output items (like the vanilla book).
   Never-crafted items keep the progression tint (see below).
2. **Recipe** — clicking an output **replaces the grid in the same column** with how that item
   is made. A **back button** (`‹ items`) returns to the browse grid.

There is **one predictable behavior**: click → show the recipe. No conditional ghost-filling of
an open crafting grid.

### Recipe methods = left rail
When an item has more than one recipe, each method is a **tab on a left rail** that protrudes
from the panel, mirroring vanilla's category tabs. Vanilla crafting and Create machines appear
as peers here — this rail *is* the unification.

- Rail tabs are badged with each recipe **category's icon** (crafting table, furnace, blast
  furnace, Create crushing wheels, mechanical mixer, …).
- If methods overflow the visible height (currently ~5 tabs), the rail **pages with ▲ / ▼**.
  The active selection persists across pages, like vanilla.

### Per-method recipe body
The body renders polymorphically by recipe kind:
- **Crafting** → a native mini crafting grid → result.
- **Create / machine** → the recipe laid out **vertically** (inputs → machine → requirements
  such as heating → output), so it fits the narrow column without widening the book. This is
  rendered by hosting a **shrunk JEI `IRecipeCategory` drawable** inside the body rect — reusing
  Create's own registered category renderers rather than reimplementing them.

## Why vendor JEI
JEI's source is vendored (`vendor/jei/`) so we can render Create's registered recipe categories
into **our own small rectangle** inside the book, instead of calling JEI's full-screen
`RecipesGui`. We reuse two things: Create's `IRecipeCategory` renderers and JEI's ingredient
drawables.

## Progression tint (already implemented)
`client/mixin/RecipeButtonMixin` tints recipe-book slots for recipes the player has never
crafted, backed by `CraftTracker` / `CraftedItems` / `CraftboundAttachments`. This carries over
to the new browse grid unchanged.

## Trade-offs & open questions
- **No auto-fill.** Dropping ghost-fill loses the vanilla convenience of dumping ingredients
  into an open crafting grid. If wanted back, add a **"place in grid ▸" button inside the recipe
  body** (shown only when a compatible screen is open) rather than click-to-fill.
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
