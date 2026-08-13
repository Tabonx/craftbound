package com.craftbound.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import com.craftbound.Craftbound;
import com.craftbound.client.jei.BookIngredient;
import com.craftbound.client.jei.CraftboundJeiPlugin;
import com.craftbound.client.jei.RecipeGroup;
import com.craftbound.client.progression.Progression;
import com.mojang.blaze3d.vertex.PoseStack;

import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.inputs.RecipeSlotUnderMouse;
import mezz.jei.api.recipe.RecipeIngredientRole;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.RecipeHolder;

// The recipe book as a real screen widget so it renders in order and receives clicks, scroll and
// typed characters. Hosts a vanilla EditBox for search and ImageButtons for page arrows so it
// inherits their bezel, hover highlight and click sound. Geometry mirrors vanilla's RecipeBookPage.
public final class RecipeBookWidget extends AbstractWidget
{
    public static final int WIDTH = 147;
    public static final int HEIGHT = 166;

    private static final ResourceLocation BACKGROUND =
            ResourceLocation.withDefaultNamespace("textures/gui/recipe_book.png");
    private static final ResourceLocation CRAFTABLE_SLOT =
            ResourceLocation.withDefaultNamespace("recipe_book/slot_craftable");
    private static final ResourceLocation UNCRAFTABLE_SLOT =
            ResourceLocation.withDefaultNamespace("recipe_book/slot_uncraftable");
    private static final ResourceLocation UNLOCKING_CRAFTABLE_SLOT =
            ResourceLocation.fromNamespaceAndPath(Craftbound.MODID, "recipe_book/slot_craftable");
    private static final ResourceLocation UNLOCKING_UNCRAFTABLE_SLOT =
            ResourceLocation.fromNamespaceAndPath(Craftbound.MODID, "recipe_book/slot_uncraftable");
    private static final WidgetSprites FORWARD_SPRITES = new WidgetSprites(
            ResourceLocation.withDefaultNamespace("recipe_book/page_forward"),
            ResourceLocation.withDefaultNamespace("recipe_book/page_forward_highlighted"));
    private static final WidgetSprites BACKWARD_SPRITES = new WidgetSprites(
            ResourceLocation.withDefaultNamespace("recipe_book/page_backward"),
            ResourceLocation.withDefaultNamespace("recipe_book/page_backward_highlighted"));
    private static final ResourceLocation FILTER_ENABLED =
            ResourceLocation.withDefaultNamespace("recipe_book/filter_enabled");
    private static final ResourceLocation FILTER_DISABLED =
            ResourceLocation.withDefaultNamespace("recipe_book/filter_disabled");
    private static final ResourceLocation FILTER_ENABLED_HL =
            ResourceLocation.withDefaultNamespace("recipe_book/filter_enabled_highlighted");
    private static final ResourceLocation FILTER_DISABLED_HL =
            ResourceLocation.withDefaultNamespace("recipe_book/filter_disabled_highlighted");
    private static final Component TOOLTIP_ALL =
            Component.translatable("gui.recipebook.toggleRecipes.all");
    private static final Component TOOLTIP_CRAFTABLE =
            Component.translatable("gui.recipebook.toggleRecipes.craftable");
    // The place-into-grid button, drawn where the craftable filter sits while browsing.
    private static final WidgetSprites PLACE_SPRITES = new WidgetSprites(
            ResourceLocation.fromNamespaceAndPath(Craftbound.MODID, "recipe_book/place_recipe"),
            ResourceLocation.fromNamespaceAndPath(Craftbound.MODID, "recipe_book/place_recipe_disabled"),
            ResourceLocation.fromNamespaceAndPath(Craftbound.MODID, "recipe_book/place_recipe_highlighted"));
    private static final ResourceLocation BOOKMARK_OFF = bookmarkSprite("bookmark");
    private static final ResourceLocation BOOKMARK_OFF_HL = bookmarkSprite("bookmark_highlighted");
    private static final ResourceLocation BOOKMARK_ON = bookmarkSprite("bookmark_on");
    private static final ResourceLocation BOOKMARK_ON_HL = bookmarkSprite("bookmark_on_highlighted");
    private static final ResourceLocation BOOKMARK_TAB_ICON = bookmarkSprite("bookmark_tab");
    private static final Component TOOLTIP_BOOKMARKS =
            Component.translatable("craftbound.recipebook.bookmarks");
    private static final Component TOOLTIP_BOOKMARK =
            Component.translatable("craftbound.recipebook.bookmark");
    private static final Component TOOLTIP_BOOKMARKED =
            Component.translatable("craftbound.recipebook.bookmark.remove");
    private static final Component TOOLTIP_PLACE =
            Component.translatable("craftbound.recipebook.place")
                    .append(CommonComponents.NEW_LINE)
                    .append(Component.translatable("craftbound.recipebook.place.all")
                            .withStyle(ChatFormatting.GRAY));

    private static ResourceLocation bookmarkSprite(String name)
    {
        return ResourceLocation.fromNamespaceAndPath(Craftbound.MODID, "recipe_book/" + name);
    }

    private static final float ANIMATION_TICKS = 15f;
    private static final int COLS = 5;
    private static final int PER_PAGE = COLS * 4;
    private static final int CELL = 25;
    private static final int GRID_X = 11;
    private static final int GRID_Y = 31;
    private static final int ITEM_INSET = 4;
    private static final int SEARCH_X = 25;
    private static final int SEARCH_Y = 13;
    private static final int SEARCH_W = 81;
    private static final int SEARCH_H = 14;
    // The craftable-filter toggle, reusing vanilla's filter sprites (26x16). Placed at vanilla's own
    // spot right of the search field, where the book texture already leaves clean parchment.
    private static final int FILTER_W = 26;
    private static final int FILTER_H = 16;
    private static final int FILTER_X = 110;
    private static final int FILTER_Y = 12;
    private static final int PLACE_W = 26;
    private static final int PLACE_H = 16;
    private static final int PLACE_MARGIN = WIDTH - FILTER_X - FILTER_W;
    private static final int BOOKMARK_W = 26;
    private static final int BOOKMARK_H = 16;
    private static final int BOOKMARK_GAP = 4;
    private static final int ARROW_W = 12;
    private static final int ARROW_H = 17;
    private static final int FORWARD_X = 93;
    private static final int BACKWARD_X = 38;
    private static final int ARROW_Y = 137;

    // Recipe state: an arrow-sprite "items" back control at the top and the recipe drawn into a body
    // rect. The control reuses the book's page-arrow sprite so it hovers and clicks like vanilla.
    private static final String BACK_LABEL = "items";
    private static final int BACK_X = 8;
    private static final int BACK_Y = SEARCH_Y - 2;
    private static final int BACK_W = 42;
    private static final int BACK_H = ARROW_H;
    private static final int BODY_X = 8;
    private static final int BODY_Y = 30;
    private static final int BODY_H = ARROW_Y - BODY_Y - 4;

    private final List<BookIngredient> allItems = new ArrayList<>();
    private List<BookIngredient> filtered = List.of();
    private int page = 0;
    private boolean loaded = false;
    private BookIngredient hovered = null;

    // Highlight animations in flight, by unlock key, counting down in ticks.
    private final Map<String, Float> highlights = new HashMap<>();

    // The craftable-filter state: which items are craftable right now (rebuilt when the inventory
    // changes while filtering) and whether the filter button is hovered (for its tooltip).
    private Supplier<Set<Item>> craftableSource = null;
    private Set<Item> craftable = Set.of();
    private int craftableTimesChanged = -1;
    private boolean filterHovered = false;

    private List<RecipeGroup> recipeGroups = List.of();
    private int groupIndex = 0;

    // The rail left of the book: recipe categories while a recipe is open, and while browsing a
    // single tab that switches the grid between everything and the bookmarked items. The ingredient
    // whose recipes are shown is kept so the bookmark button can toggle it.
    private final BookRail categoryRail = new BookRail();
    private final BookRail bookmarkRail = new BookRail();
    private List<BookIngredient> bookmarked = List.of();
    private boolean showingBookmarks = false;
    private BookIngredient focused = null;
    private boolean bookmarkHovered = false;

    // The recipes currently filling the body (one category's, focused or full), built lazily via
    // suppliers; bodyCache holds the built drawable per index (null until first shown).
    private List<Supplier<IRecipeLayoutDrawable<?>>> bodyRecipes = List.of();
    private List<IRecipeLayoutDrawable<?>> bodyCache = new ArrayList<>();
    private int recipeIndex = 0;

    // The book keeps its right edge where the host placed it (baseX + WIDTH) and widens left to a
    // single fixed width in recipe mode; browsing it stays WIDTH. The width never varies per recipe,
    // so the panel's left edge (and the inventory beside it) stays put — recipes scale to fit.
    private int baseX;
    private int baseY;

    // Placement of the recipe drawable from the last render, so clicks can be mapped into its space.
    private int recipeOriginX;
    private int recipeOriginY;
    private float recipeScale = 1f;
    private int recipeBoundsX;
    private int recipeBoundsY;

    private final EditBox search;
    private final ImageButton backButton;
    private final ImageButton forwardButton;
    private final ImageButton placeButton;

    // Fills the open menu's input slots with the shown recipe; absent until the host binds a menu.
    private RecipePlacer placer = null;

    public RecipeBookWidget()
    {
        super(0, 0, WIDTH, HEIGHT, Component.literal("Craftbound Recipe Book"));

        this.search = new EditBox(Minecraft.getInstance().font, 0, 0, SEARCH_W, SEARCH_H,
                Component.translatable("itemGroup.search"));
        this.search.setMaxLength(50);
        this.search.setHint(Component.translatable("gui.recipebook.search_hint"));
        this.search.setResponder(this::onSearchChanged);

        this.backButton = new ImageButton(0, 0, ARROW_W, ARROW_H, BACKWARD_SPRITES, b -> stepBack());
        this.forwardButton = new ImageButton(0, 0, ARROW_W, ARROW_H, FORWARD_SPRITES, b -> stepForward());

        this.placeButton = new ImageButton(0, 0, PLACE_W, PLACE_H, PLACE_SPRITES, b -> placeShownRecipe());
        this.placeButton.setTooltip(Tooltip.create(TOOLTIP_PLACE));
        this.placeButton.visible = false;
    }

    // Supplies the set of items craftable right now, bound by the host to the open menu.
    public void setCraftableSource(Supplier<Set<Item>> source)
    {
        this.craftableSource = source;
    }

    public void setPlacer(RecipePlacer placer)
    {
        this.placer = placer;
    }

    private boolean inRecipeMode()
    {
        return !recipeGroups.isEmpty();
    }

    private RecipeGroup currentGroup()
    {
        return recipeGroups.get(groupIndex);
    }

    // Recipes are built lazily so browsing a whole category (right-click) doesn't build thousands
    // of drawables up front; each is created and cached the first time it is shown.
    private IRecipeLayoutDrawable<?> currentRecipe()
    {
        IRecipeLayoutDrawable<?> drawable = bodyCache.get(recipeIndex);
        if (drawable == null)
        {
            drawable = bodyRecipes.get(recipeIndex).get();
            bodyCache.set(recipeIndex, drawable);
        }
        return drawable;
    }

    private Optional<RecipeHolder<?>> placeableRecipe()
    {
        if (!inRecipeMode() || placer == null)
            return Optional.empty();
        return placer.placeable(currentRecipe());
    }

    // Advances the shown recipe's own animations, JEI's cycling through the items an ingredient
    // stands for included. Holding shift stops the cycle, which is JEI's own behaviour.
    public void tick()
    {
        if (visible && inRecipeMode())
            currentRecipe().tick();
    }

    private void placeShownRecipe()
    {
        placeableRecipe().ifPresent(recipe -> placer.place(recipe, Screen.hasShiftDown()));
    }

    // Browse and Recipe share the two page arrows; each step means "previous/next" in the active
    // state: another page of the grid, or another recipe within the shown category.
    private void stepBack()
    {
        if (inRecipeMode())
            setRecipe(recipeIndex - 1);
        else
            setPage(page - 1);
    }

    private void stepForward()
    {
        if (inRecipeMode())
            setRecipe(recipeIndex + 1);
        else
            setPage(page + 1);
    }

    // OUTPUT shows how the ingredient is made; INPUT shows where it is used (right-click).
    private boolean showRecipes(BookIngredient ingredient, RecipeIngredientRole role)
    {
        List<RecipeGroup> groups = CraftboundJeiPlugin.recipeGroupsFor(ingredient, role);
        if (groups.isEmpty())
            return false;
        recipeGroups = groups;
        focused = ingredient;
        hovered = null;
        search.setFocused(false);
        categoryRail.setTabs(recipeGroups, 0);
        selectGroup(0);
        return true;
    }

    private void closeRecipe()
    {
        recipeGroups = List.of();
        bodyRecipes = List.of();
        bodyCache = new ArrayList<>();
        groupIndex = 0;
        recipeIndex = 0;
        focused = null;
        categoryRail.setTabs(List.of(), -1);
        relayout();
    }

    // Clicking an ingredient inside the shown recipe drills into it: left shows its recipes, right
    // shows where it is used.
    private boolean drillUnderMouse(double mouseX, double mouseY, RecipeIngredientRole role)
    {
        if (!inRecipeMode())
            return false;

        double localX = (mouseX - recipeOriginX) / recipeScale + recipeBoundsX;
        double localY = (mouseY - recipeOriginY) / recipeScale + recipeBoundsY;

        Optional<BookIngredient> clicked = currentRecipe().getSlotUnderMouse(localX, localY)
                .map(RecipeSlotUnderMouse::slot)
                .flatMap(IRecipeSlotView::getDisplayedIngredient)
                .flatMap(CraftboundJeiPlugin::toBookIngredient);

        if (clicked.isPresent() && showRecipes(clicked.get(), role))
        {
            playClickSound();
            return true;
        }
        return false;
    }

    // Left-click a tab: the focused ingredient's recipes in that category, the ones the player can
    // make right now first.
    private void selectGroup(int target)
    {
        setActiveTab(target);
        setBody(constantSuppliers(RecipeOrder.craftableFirst(currentGroup().recipes())));
    }

    // Right-click a tab: every recipe in that category, not just ones involving the focused item.
    // Left unsorted — ranking these would mean building every drawable in the category up front,
    // which is the cost the lazy suppliers exist to avoid.
    private void showAllRecipes(int target)
    {
        int previous = groupIndex;
        setActiveTab(target);
        List<Supplier<IRecipeLayoutDrawable<?>>> all = CraftboundJeiPlugin.allRecipesFor(currentGroup());
        if (all.isEmpty())
            setActiveTab(previous);
        else
            setBody(all);
    }

    private void setActiveTab(int target)
    {
        groupIndex = Math.max(0, Math.min(recipeGroups.size() - 1, target));
        categoryRail.showTab(groupIndex);
    }

    private void setBody(List<Supplier<IRecipeLayoutDrawable<?>>> recipes)
    {
        bodyRecipes = recipes;
        bodyCache = new ArrayList<>(Collections.nCopies(recipes.size(), null));
        recipeIndex = 0;
        relayout();
    }

    private void setRecipe(int target)
    {
        recipeIndex = Math.max(0, Math.min(bodyRecipes.size() - 1, target));
    }

    private static List<Supplier<IRecipeLayoutDrawable<?>>> constantSuppliers(
            List<IRecipeLayoutDrawable<?>> built)
    {
        return built.stream().<Supplier<IRecipeLayoutDrawable<?>>>map(drawable -> () -> drawable).toList();
    }

    private void ensureLoaded()
    {
        if (loaded || !CraftboundJeiPlugin.hasRuntime())
            return;
        allItems.clear();
        allItems.addAll(CraftboundJeiPlugin.getAllIngredients());
        refreshBookmarks(); // also applies the filter, so the grid is ready
        loaded = true;
    }

    // Resolve the saved uids against the browsable ingredients, keeping the order they were
    // bookmarked in. A bookmark whose mod is gone simply does not resolve; it stays in the file, so
    // reinstalling the mod brings it back. The rail carries one tab, shown once there is something
    // to show, that switches the grid over to these.
    private void refreshBookmarks()
    {
        Map<String, BookIngredient> byUid = new HashMap<>();
        for (BookIngredient ingredient : allItems)
            byUid.putIfAbsent(ingredient.uid(), ingredient);

        bookmarked = BookmarkStore.current().stream()
                .map(byUid::get)
                .filter(Objects::nonNull)
                .toList();

        if (bookmarked.isEmpty())
            showingBookmarks = false;
        updateBookmarkTab();
        applyFilter();
    }

    private void updateBookmarkTab()
    {
        bookmarkRail.setTabs(bookmarked.isEmpty() ? List.of() : List.of(BOOKMARKS_TAB),
                showingBookmarks ? 0 : -1);
    }

    private void toggleBookmark()
    {
        if (focused == null)
            return;
        BookmarkStore.toggle(focused.uid());
        refreshBookmarks();
    }

    private void toggleBookmarkView()
    {
        showingBookmarks = !showingBookmarks;
        updateBookmarkTab();
        page = 0;
        applyFilter();
    }

    private static final BookRail.Tab BOOKMARKS_TAB = new BookRail.Tab()
    {
        @Override
        public void drawIcon(GuiGraphics graphics, int x, int y)
        {
            graphics.blitSprite(BOOKMARK_TAB_ICON, x, y, 16, 16);
        }

        @Override
        public Component title()
        {
            return TOOLTIP_BOOKMARKS;
        }
    };

    private void onSearchChanged(String value)
    {
        applyFilter();
    }

    private void applyFilter()
    {
        List<BookIngredient> source = (showingBookmarks ? bookmarked : allItems).stream()
                .filter(Progression::isUnlocked)
                .toList();
        String needle = search.getValue().toLowerCase(Locale.ROOT);
        List<BookIngredient> result = needle.isEmpty() ? source
                : source.stream()
                        .filter(item -> item.displayName().toLowerCase(Locale.ROOT).contains(needle))
                        .toList();
        if (RecipeBookState.isFiltering())
            result = result.stream()
                    .filter(item -> item.item().map(craftable::contains).orElse(false))
                    .toList();
        filtered = result;
        setPage(page);
    }

    // Picking up a new kind of item can unlock recipes, so the grid re-filters as soon as the
    // obtained set grows rather than waiting for the book to be reopened.
    private void refreshUnlocksIfStale()
    {
        if (Progression.refresh())
            applyFilter();
    }

    private int inventoryTimesChanged()
    {
        var player = Minecraft.getInstance().player;
        return player == null ? -1 : player.getInventory().getTimesChanged();
    }

    // Rebuild the craftable set when the inventory changes, then update an active filter.
    private void refreshCraftableIfStale()
    {
        if (craftableSource == null)
            return;
        int changed = inventoryTimesChanged();
        if (changed != craftableTimesChanged)
        {
            craftable = craftableSource.get();
            craftableTimesChanged = changed;
            if (RecipeBookState.isFiltering())
                applyFilter();
        }
    }

    private void toggleFilter()
    {
        RecipeBookState.toggleFiltering();
        if (RecipeBookState.isFiltering() && craftableSource != null)
        {
            craftable = craftableSource.get();
            craftableTimesChanged = inventoryTimesChanged();
        }
        applyFilter();
    }

    private void setPage(int target)
    {
        page = Math.max(0, Math.min(pageCount() - 1, target));
    }

    private int pageCount()
    {
        return Math.max(1, (filtered.size() + PER_PAGE - 1) / PER_PAGE);
    }

    @Override
    public void setPosition(int x, int y)
    {
        baseX = x;
        baseY = y;
        relayout();
    }

    // Apply the current width and position: anchored right edge, widened left in recipe mode to a
    // fixed width (clamped to the screen), sub-widgets re-placed.
    private void relayout()
    {
        int w = inRecipeMode() ? Math.min(RecipeBookLayout.RECIPE_WIDTH, maxPanelWidth()) : WIDTH;
        int px = baseX + WIDTH - w;
        super.setPosition(px, baseY);
        setWidth(w);

        search.setPosition(px + SEARCH_X, baseY + SEARCH_Y);
        placeButton.setPosition(placeX(), baseY + FILTER_Y);
        categoryRail.setPosition(px, baseY);
        bookmarkRail.setPosition(px, baseY);
        int center = px + w / 2;
        backButton.setPosition(center + BACKWARD_X - WIDTH / 2, baseY + ARROW_Y);
        forwardButton.setPosition(center + FORWARD_X - WIDTH / 2, baseY + ARROW_Y);
    }

    // Cap the recipe-mode width so the left edge (with the tab rail that protrudes further left)
    // stays on-screen on very narrow windows.
    private int maxPanelWidth()
    {
        return Math.max(WIDTH, baseX + WIDTH + BookRail.TAB_X - 2);
    }

    // The panel texture (bezel included) is stretched horizontally in recipe mode, so edge insets
    // are scaled with it; a fixed inset would sit closer to the border than it does while browsing.
    private float panelScale()
    {
        return (float) getWidth() / WIDTH;
    }

    private int backX()
    {
        return getX() + (int) Math.ceil(BACK_X * panelScale());
    }

    // The right-hand button slot, shared by the place button and — when there is no recipe to place —
    // the bookmark toggle, which otherwise sits to its left.
    private int placeX()
    {
        return getX() + getWidth() - (int) Math.ceil(PLACE_MARGIN * panelScale()) - PLACE_W;
    }

    private int bookmarkX()
    {
        return placeableRecipe().isPresent() ? placeX() - BOOKMARK_GAP - BOOKMARK_W : placeX();
    }

    private int bodyWidth()
    {
        return getWidth() - 2 * BODY_X;
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
    {
        ensureLoaded();
        refreshUnlocksIfStale();
        refreshCraftableIfStale();

        int x = getX();
        int y = getY();
        // The panel texture is stretched horizontally to the current width (identity while browsing).
        graphics.blit(BACKGROUND, x, y, getWidth(), HEIGHT, 1, 1, WIDTH, HEIGHT, 256, 256);

        // Tabs are drawn over the book (like vanilla), their edge overlapping the book's left border.
        filterHovered = false;
        bookmarkHovered = false;
        if (inRecipeMode())
        {
            categoryRail.render(graphics, mouseX, mouseY);
            renderRecipe(graphics, x, y, mouseX, mouseY);
            renderBookmarkButton(graphics, x, y, mouseX, mouseY);
        }
        else
        {
            bookmarkRail.render(graphics, mouseX, mouseY);
            renderBrowse(graphics, x, y, mouseX, mouseY, partialTick);
        }

        renderPlaceButton(graphics, mouseX, mouseY, partialTick);
        renderPager(graphics, x, y, mouseX, mouseY, partialTick);
    }

    private BookRail rail()
    {
        return inRecipeMode() ? categoryRail : bookmarkRail;
    }

    private void renderBookmarkButton(GuiGraphics graphics, int x, int y, int mouseX, int mouseY)
    {
        if (focused == null)
            return;

        int buttonX = bookmarkX();
        bookmarkHovered = inRect(mouseX, mouseY, buttonX, y + FILTER_Y, BOOKMARK_W, BOOKMARK_H);
        boolean on = BookmarkStore.contains(focused.uid());
        ResourceLocation sprite = on
                ? (bookmarkHovered ? BOOKMARK_ON_HL : BOOKMARK_ON)
                : (bookmarkHovered ? BOOKMARK_OFF_HL : BOOKMARK_OFF);
        graphics.blitSprite(sprite, buttonX, y + FILTER_Y, BOOKMARK_W, BOOKMARK_H);
    }

    private void renderBrowse(GuiGraphics graphics, int x, int y, int mouseX, int mouseY, float partialTick)
    {
        search.render(graphics, mouseX, mouseY, partialTick);
        renderFilterButton(graphics, x, y, mouseX, mouseY);

        int start = page * PER_PAGE;
        hovered = null;
        for (int i = 0; i < PER_PAGE && start + i < filtered.size(); i++)
        {
            int cellX = x + GRID_X + CELL * (i % COLS);
            int cellY = y + GRID_Y + CELL * (i / COLS);
            BookIngredient item = filtered.get(start + i);
            renderCell(graphics, item, cellX, cellY, partialTick);

            if (mouseX >= cellX && mouseX < cellX + CELL && mouseY >= cellY && mouseY < cellY + CELL)
                hovered = item;
        }
    }

    // A newly unlocked entry swells and settles once, vanilla's recipe-book highlight: a half sine
    // over ANIMATION_TICKS, scaling the slot and its item about the item's centre.
    private void renderCell(GuiGraphics graphics, BookIngredient item, int cellX, int cellY, float partialTick)
    {
        if (Progression.takeHighlight(item))
            highlights.put(item.unlockKey(), ANIMATION_TICKS);

        Float remaining = highlights.get(item.unlockKey());
        if (remaining != null)
        {
            float scale = 1f + 0.1f * (float) Math.sin(remaining / ANIMATION_TICKS * Math.PI);
            int centerX = cellX + ITEM_INSET + 8;
            int centerY = cellY + ITEM_INSET + 8;
            graphics.pose().pushPose();
            graphics.pose().translate(centerX, centerY, 0f);
            graphics.pose().scale(scale, scale, 1f);
            graphics.pose().translate(-centerX, -centerY, 0f);

            float left = remaining - partialTick;
            if (left > 0f)
                highlights.put(item.unlockKey(), left);
            else
                highlights.remove(item.unlockKey());
        }

        graphics.blitSprite(slotFor(item), cellX, cellY, CELL, CELL);
        item.render(graphics, cellX + ITEM_INSET, cellY + ITEM_INSET);

        if (remaining != null)
            graphics.pose().popPose();
    }

    // Marked slots are the ones worth getting hold of: obtaining them opens recipes the book is
    // still hiding.
    private ResourceLocation slotFor(BookIngredient ingredient)
    {
        boolean canCraft = ingredient.item().map(craftable::contains).orElse(false);
        if (Progression.unlocksMore(ingredient))
            return canCraft ? UNLOCKING_CRAFTABLE_SLOT : UNLOCKING_UNCRAFTABLE_SLOT;
        return canCraft ? CRAFTABLE_SLOT : UNCRAFTABLE_SLOT;
    }

    private void renderFilterButton(GuiGraphics graphics, int x, int y, int mouseX, int mouseY)
    {
        boolean on = RecipeBookState.isFiltering();
        filterHovered = inRect(mouseX, mouseY, x + FILTER_X, y + FILTER_Y, FILTER_W, FILTER_H);
        ResourceLocation sprite = on
                ? (filterHovered ? FILTER_ENABLED_HL : FILTER_ENABLED)
                : (filterHovered ? FILTER_DISABLED_HL : FILTER_DISABLED);
        graphics.blitSprite(sprite, x + FILTER_X, y + FILTER_Y, FILTER_W, FILTER_H);
    }

    private void renderRecipe(GuiGraphics graphics, int x, int y, int mouseX, int mouseY)
    {
        // The search bar and its magnifier are baked into the book texture. Cover that strip with a
        // slice of the book's own plain interior (sourced from the grid area), stretched to the
        // current width, so the back control sits on clean parchment instead of the magnifier. The
        // book texture (and its bezel) is stretched horizontally from WIDTH to getWidth(), so stretch
        // this slice by the same factor; otherwise its edges fall a pixel or two inside the bezel.
        // Round each edge inward (ceil left, floor right) so the slice stops just inside the bezel
        // rather than spilling onto it.
        float coverScale = (float) getWidth() / WIDTH;
        int coverLeft = backX();
        int coverRight = x + (int) Math.floor((WIDTH - BACK_X) * coverScale);
        graphics.blit(BACKGROUND, coverLeft, y + SEARCH_Y - 3, coverRight - coverLeft, SEARCH_H + 6,
                BACK_X + 1, GRID_Y + 1, WIDTH - 2 * BACK_X, SEARCH_H + 6, 256, 256);

        var font = Minecraft.getInstance().font;
        int backX = backX();
        boolean overBack = inRect(mouseX, mouseY, backX, y + BACK_Y, BACK_W, BACK_H);
        graphics.blitSprite(BACKWARD_SPRITES.get(true, overBack), backX, y + BACK_Y, ARROW_W, ARROW_H);
        graphics.drawString(font, BACK_LABEL, backX + ARROW_W + 3, y + BACK_Y + (ARROW_H - 8) / 2,
                0xFFFFFF, true);

        IRecipeLayoutDrawable<?> layout = currentRecipe();
        layout.setPosition(0, 0);
        Rect2i bounds = layout.getRectWithBorder();
        int bw = Math.max(1, bounds.getWidth());
        int bh = Math.max(1, bounds.getHeight());

        float scale = Math.min(1f, Math.min((float) bodyWidth() / bw, (float) BODY_H / bh));
        int drawW = Math.round(bw * scale);
        int drawH = Math.round(bh * scale);
        int originX = x + BODY_X + (bodyWidth() - drawW) / 2;
        int originY = y + BODY_Y + (BODY_H - drawH) / 2;

        recipeScale = scale;
        recipeOriginX = originX;
        recipeOriginY = originY;
        recipeBoundsX = bounds.getX();
        recipeBoundsY = bounds.getY();

        double localX = (mouseX - originX) / scale + bounds.getX();
        double localY = (mouseY - originY) / scale + bounds.getY();

        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.translate(originX, originY, 0);
        pose.scale(scale, scale, 1f);
        pose.translate(-bounds.getX(), -bounds.getY(), 0);
        layout.drawRecipe(graphics, (int) localX, (int) localY);
        layout.drawOverlays(graphics, (int) localX, (int) localY);
        pose.popPose();
    }

    // Drawn after the whole screen (slots and their placeholder sprites) so the tooltip layers
    // don't interleave with the inventory's empty-slot icons.
    public void renderDeferredTooltip(GuiGraphics graphics, int mouseX, int mouseY)
    {
        if (!visible)
            return;

        Minecraft minecraft = Minecraft.getInstance();
        BookRail.Tab hoveredTab = rail().hovered();
        if (hoveredTab != null)
        {
            graphics.renderTooltip(minecraft.font, hoveredTab.title(), mouseX, mouseY);
            return;
        }
        if (bookmarkHovered && focused != null)
        {
            graphics.renderTooltip(minecraft.font,
                    BookmarkStore.contains(focused.uid()) ? TOOLTIP_BOOKMARKED : TOOLTIP_BOOKMARK,
                    mouseX, mouseY);
            return;
        }
        if (filterHovered)
        {
            graphics.renderTooltip(minecraft.font,
                    RecipeBookState.isFiltering() ? TOOLTIP_CRAFTABLE : TOOLTIP_ALL, mouseX, mouseY);
            return;
        }
        if (hovered != null)
        {
            TooltipFlag flag = minecraft.options.advancedItemTooltips ? TooltipFlag.ADVANCED : TooltipFlag.NORMAL;
            graphics.renderComponentTooltip(minecraft.font, hovered.tooltip(flag), mouseX, mouseY);
        }
    }

    // Offered only for recipes the open menu can lay out, and greyed out while the ingredients for
    // them are missing.
    private void renderPlaceButton(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
    {
        Optional<RecipeHolder<?>> recipe = placeableRecipe();
        placeButton.visible = recipe.isPresent();
        if (!placeButton.visible)
            return;

        placeButton.active = placer.hasIngredients(recipe.get());
        placeButton.render(graphics, mouseX, mouseY, partialTick);
    }

    private void renderPager(GuiGraphics graphics, int x, int y, int mouseX, int mouseY, float partialTick)
    {
        int count = inRecipeMode() ? bodyRecipes.size() : pageCount();
        int index = inRecipeMode() ? recipeIndex : page;
        boolean paged = count > 1;

        backButton.visible = paged && index > 0;
        forwardButton.visible = paged && index < count - 1;
        backButton.render(graphics, mouseX, mouseY, partialTick);
        forwardButton.render(graphics, mouseX, mouseY, partialTick);

        if (paged)
        {
            var font = Minecraft.getInstance().font;
            String label = (index + 1) + "/" + count;
            graphics.drawString(font, label, x + getWidth() / 2 - font.width(label) / 2, y + ARROW_Y + 5, 0xFFFFFF, true);
        }
    }

    // The rail's pagers and tabs. Categories: left-click shows the item's recipes there, right-click
    // the whole category. The bookmark tab switches the grid to the bookmarked items and back.
    private boolean railClicked(double mouseX, double mouseY, int button)
    {
        BookRail rail = rail();
        int arrow = rail.arrowAt(mouseX, mouseY);
        if (arrow != 0)
        {
            playClickSound();
            rail.scroll(arrow);
            return true;
        }

        int tab = rail.tabAt(mouseX, mouseY);
        if (tab < 0)
            return false;

        playClickSound();
        if (inRecipeMode())
        {
            if (isRightClick(button))
                showAllRecipes(tab);
            else
                selectGroup(tab);
        }
        else
        {
            toggleBookmarkView();
        }
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        if (!visible)
            return false;

        if (placeButton.visible && placeButton.mouseClicked(mouseX, mouseY, button))
            return true;

        if (railClicked(mouseX, mouseY, button))
            return true;

        if (inRecipeMode())
        {
            if (focused != null && inRect(mouseX, mouseY, bookmarkX(), getY() + FILTER_Y, BOOKMARK_W, BOOKMARK_H))
            {
                playClickSound();
                toggleBookmark();
                return true;
            }
            if (inRect(mouseX, mouseY, backX(), getY() + BACK_Y, BACK_W, BACK_H))
            {
                playClickSound();
                closeRecipe();
                return true;
            }
            if (backButton.mouseClicked(mouseX, mouseY, button) || forwardButton.mouseClicked(mouseX, mouseY, button))
                return true;
            if (drillUnderMouse(mouseX, mouseY, roleFor(button)))
                return true;
            return isMouseOverBook(mouseX, mouseY);
        }

        if (inRect(mouseX, mouseY, getX() + FILTER_X, getY() + FILTER_Y, FILTER_W, FILTER_H))
        {
            playClickSound();
            search.setFocused(false);
            toggleFilter();
            return true;
        }

        boolean onSearch = inRect(mouseX, mouseY, getX() + SEARCH_X, getY() + SEARCH_Y, SEARCH_W, SEARCH_H);
        search.setFocused(onSearch);
        if (onSearch)
        {
            search.mouseClicked(mouseX, mouseY, button);
            return true;
        }

        if (backButton.mouseClicked(mouseX, mouseY, button) || forwardButton.mouseClicked(mouseX, mouseY, button))
            return true;

        // Left-click an item: how it is made. Right-click: where it is used.
        BookIngredient clicked = ingredientAt(mouseX, mouseY);
        if (clicked != null)
        {
            showRecipes(clicked, roleFor(button));
            return true;
        }

        return isMouseOverBook(mouseX, mouseY);
    }

    private static boolean isRightClick(int button)
    {
        return button == 1;
    }

    private static RecipeIngredientRole roleFor(int button)
    {
        return isRightClick(button) ? RecipeIngredientRole.INPUT : RecipeIngredientRole.OUTPUT;
    }

    private BookIngredient ingredientAt(double mouseX, double mouseY)
    {
        int start = page * PER_PAGE;
        for (int i = 0; i < PER_PAGE && start + i < filtered.size(); i++)
        {
            int cellX = getX() + GRID_X + CELL * (i % COLS);
            int cellY = getY() + GRID_Y + CELL * (i / COLS);
            if (inRect(mouseX, mouseY, cellX, cellY, CELL, CELL))
                return filtered.get(start + i);
        }
        return null;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY)
    {
        if (!visible)
            return false;

        if (rail().isOver(mouseX, mouseY))
        {
            rail().scroll(scrollY < 0 ? 1 : -1);
            return true;
        }
        if (!isMouseOverBook(mouseX, mouseY))
            return false;

        if (inRecipeMode())
            setRecipe(scrollY < 0 ? recipeIndex + 1 : recipeIndex - 1);
        else
            setPage(scrollY < 0 ? page + 1 : page - 1);
        return true;
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers)
    {
        return visible && search.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers)
    {
        if (!visible)
            return false;
        if (keyCode == 256) // let escape close the screen rather than being swallowed
            return false;
        if (search.keyPressed(keyCode, scanCode, modifiers))
            return true;
        return search.canConsumeInput(); // swallow other keys while the search box is focused
    }

    private boolean isMouseOverBook(double mouseX, double mouseY)
    {
        return inRect(mouseX, mouseY, getX(), getY(), getWidth(), HEIGHT);
    }

    private static boolean inRect(double mx, double my, int x, int y, int w, int h)
    {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    private static void playClickSound()
    {
        Minecraft.getInstance().getSoundManager()
                .play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output)
    {
        defaultButtonNarrationText(output);
    }
}
