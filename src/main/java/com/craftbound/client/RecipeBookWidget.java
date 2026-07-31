package com.craftbound.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Supplier;

import com.craftbound.client.jei.BookIngredient;
import com.craftbound.client.jei.CraftboundJeiPlugin;
import com.craftbound.client.jei.RecipeGroup;
import com.mojang.blaze3d.vertex.PoseStack;

import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.inputs.RecipeSlotUnderMouse;
import mezz.jei.api.recipe.RecipeIngredientRole;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.TooltipFlag;

// The recipe book as a real screen widget so it renders in order and receives clicks, scroll and
// typed characters. Hosts a vanilla EditBox for search and ImageButtons for page arrows so it
// inherits their bezel, hover highlight and click sound. Geometry mirrors vanilla's RecipeBookPage.
public final class RecipeBookWidget extends AbstractWidget
{
    public static final int WIDTH = 147;
    public static final int HEIGHT = 166;

    private static final ResourceLocation BACKGROUND =
            ResourceLocation.withDefaultNamespace("textures/gui/recipe_book.png");
    private static final ResourceLocation SLOT =
            ResourceLocation.withDefaultNamespace("recipe_book/slot_craftable");
    private static final WidgetSprites FORWARD_SPRITES = new WidgetSprites(
            ResourceLocation.withDefaultNamespace("recipe_book/page_forward"),
            ResourceLocation.withDefaultNamespace("recipe_book/page_forward_highlighted"));
    private static final WidgetSprites BACKWARD_SPRITES = new WidgetSprites(
            ResourceLocation.withDefaultNamespace("recipe_book/page_backward"),
            ResourceLocation.withDefaultNamespace("recipe_book/page_backward_highlighted"));
    private static final WidgetSprites TAB_SPRITES = new WidgetSprites(
            ResourceLocation.withDefaultNamespace("recipe_book/tab"),
            ResourceLocation.withDefaultNamespace("recipe_book/tab_selected"));

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

    // Category tabs protrude to the left of the book, like vanilla's recipe-book category rail. The
    // tab is 35 wide with its right edge tucked under the book's left border (drawn over it).
    private static final int TAB_W = 35;
    private static final int TAB_H = 27;
    private static final int TAB_X = -30;
    private static final int TAB_TOP = 4;
    private static final int TAB_SELECTED_SHIFT = 2;
    private static final int TAB_ICON_DX = 9;
    private static final int TAB_ICON_DY = 6;
    // Up to MAX_TABS fit the rail; beyond that, show PAGED_TABS with ▲/▼ pagers claiming the ends.
    private static final int MAX_TABS = 6;
    private static final int PAGED_TABS = 5;
    private static final int RAIL_ARROW_H = 11;
    private static final String RAIL_UP = "▲";
    private static final String RAIL_DOWN = "▼";

    private final List<BookIngredient> allItems = new ArrayList<>();
    private List<BookIngredient> filtered = List.of();
    private int page = 0;
    private boolean loaded = false;
    private BookIngredient hovered = null;

    private List<RecipeGroup> recipeGroups = List.of();
    private int groupIndex = 0;
    private int railOffset = 0;
    private RecipeGroup hoveredTab = null;

    // The recipes currently filling the body (one category's, focused or full), built lazily via
    // suppliers; bodyCache holds the built drawable per index (null until first shown).
    private List<Supplier<IRecipeLayoutDrawable<?>>> bodyRecipes = List.of();
    private List<IRecipeLayoutDrawable<?>> bodyCache = new ArrayList<>();
    private int recipeIndex = 0;

    // The book keeps its right edge where the host placed it (baseX + WIDTH) and grows left in recipe
    // mode to fit wide, multi-step recipes; panelWidth is the current width, WIDTH while browsing.
    private int baseX;
    private int baseY;
    private int panelWidth = WIDTH;

    // Placement of the recipe drawable from the last render, so clicks can be mapped into its space.
    private int recipeOriginX;
    private int recipeOriginY;
    private float recipeScale = 1f;
    private int recipeBoundsX;
    private int recipeBoundsY;

    private final EditBox search;
    private final ImageButton backButton;
    private final ImageButton forwardButton;

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
        railOffset = 0;
        hovered = null;
        search.setFocused(false);
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
        railOffset = 0;
        updatePanelWidth();
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

    // Left-click a tab: the focused ingredient's recipes in that category.
    private void selectGroup(int target)
    {
        setActiveTab(target);
        setBody(constantSuppliers(currentGroup().recipes()));
    }

    // Right-click a tab: every recipe in that category, not just ones involving the focused item.
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
        if (groupIndex < railOffset)
            railOffset = groupIndex;
        else if (groupIndex >= railOffset + visibleTabs())
            railOffset = groupIndex - visibleTabs() + 1;
        railOffset = Math.max(0, Math.min(railMax(), railOffset));
    }

    private void setBody(List<Supplier<IRecipeLayoutDrawable<?>>> recipes)
    {
        bodyRecipes = recipes;
        bodyCache = new ArrayList<>(Collections.nCopies(recipes.size(), null));
        recipeIndex = 0;
        updatePanelWidth();
    }

    private void setRecipe(int target)
    {
        recipeIndex = Math.max(0, Math.min(bodyRecipes.size() - 1, target));
        updatePanelWidth();
    }

    private static List<Supplier<IRecipeLayoutDrawable<?>>> constantSuppliers(
            List<IRecipeLayoutDrawable<?>> built)
    {
        return built.stream().<Supplier<IRecipeLayoutDrawable<?>>>map(drawable -> () -> drawable).toList();
    }

    private void scrollRail(int delta)
    {
        railOffset = Math.max(0, Math.min(railMax(), railOffset + delta));
    }

    private void ensureLoaded()
    {
        if (loaded || !CraftboundJeiPlugin.hasRuntime())
            return;
        allItems.clear();
        allItems.addAll(CraftboundJeiPlugin.getAllIngredients());
        applyFilter();
        loaded = true;
    }

    private void onSearchChanged(String value)
    {
        applyFilter();
    }

    private void applyFilter()
    {
        String needle = search.getValue().toLowerCase(Locale.ROOT);
        if (needle.isEmpty())
            filtered = allItems;
        else
            filtered = allItems.stream()
                    .filter(item -> item.displayName().toLowerCase(Locale.ROOT).contains(needle))
                    .toList();
        setPage(page);
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

    // Apply the current width and position: anchored right edge, grown left, sub-widgets re-placed.
    private void relayout()
    {
        int w = inRecipeMode() ? Math.min(panelWidth, maxPanelWidth()) : WIDTH;
        int px = baseX + WIDTH - w;
        super.setPosition(px, baseY);
        setWidth(w);

        search.setPosition(px + SEARCH_X, baseY + SEARCH_Y);
        int center = px + w / 2;
        backButton.setPosition(center + BACKWARD_X - WIDTH / 2, baseY + ARROW_Y);
        forwardButton.setPosition(center + FORWARD_X - WIDTH / 2, baseY + ARROW_Y);
    }

    // Widen to fit the shown recipe, but never past the screen: the left edge (with the tab rail that
    // protrudes TAB_X further left) must stay on-screen.
    private int maxPanelWidth()
    {
        return Math.max(WIDTH, baseX + WIDTH + TAB_X - 2);
    }

    private void updatePanelWidth()
    {
        if (!inRecipeMode())
        {
            panelWidth = WIDTH;
            relayout();
            return;
        }
        IRecipeLayoutDrawable<?> recipe = currentRecipe();
        recipe.setPosition(0, 0);
        int natural = recipe.getRectWithBorder().getWidth();
        panelWidth = Math.max(WIDTH, Math.min(maxPanelWidth(), natural + 2 * BODY_X));
        relayout();
    }

    private int bodyWidth()
    {
        return getWidth() - 2 * BODY_X;
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
    {
        ensureLoaded();

        int x = getX();
        int y = getY();
        // The panel texture is stretched horizontally to the current width (identity while browsing).
        graphics.blit(BACKGROUND, x, y, getWidth(), HEIGHT, 1, 1, WIDTH, HEIGHT, 256, 256);

        // Tabs are drawn over the book (like vanilla), their edge overlapping the book's left border.
        hoveredTab = null;
        if (inRecipeMode())
        {
            renderRail(graphics, x, y, mouseX, mouseY);
            renderRecipe(graphics, x, y, mouseX, mouseY);
        }
        else
        {
            renderBrowse(graphics, x, y, mouseX, mouseY, partialTick);
        }

        renderPager(graphics, x, y, mouseX, mouseY, partialTick);
    }

    private boolean railPaged()
    {
        return recipeGroups.size() > MAX_TABS;
    }

    private int visibleTabs()
    {
        return railPaged() ? PAGED_TABS : recipeGroups.size();
    }

    // Tabs sit below the ▲ pager when the rail is paged, otherwise flush with the top.
    private int tabTop()
    {
        return TAB_TOP + (railPaged() ? RAIL_ARROW_H : 0);
    }

    private int railMax()
    {
        return Math.max(0, recipeGroups.size() - visibleTabs());
    }

    private void renderRail(GuiGraphics graphics, int x, int y, int mouseX, int mouseY)
    {
        int visible = visibleTabs();
        for (int row = 0; row < visible; row++)
        {
            int gi = railOffset + row;
            boolean selected = gi == groupIndex;
            int tabX = x + TAB_X - (selected ? TAB_SELECTED_SHIFT : 0);
            int tabY = y + tabTop() + row * TAB_H;

            graphics.blitSprite(TAB_SPRITES.get(true, selected), tabX, tabY, TAB_W, TAB_H);
            recipeGroups.get(gi).drawIcon(graphics, tabX + TAB_ICON_DX, tabY + TAB_ICON_DY);

            if (inRect(mouseX, mouseY, tabX, tabY, x - tabX, TAB_H))
                hoveredTab = recipeGroups.get(gi);
        }

        if (railPaged())
        {
            var font = Minecraft.getInstance().font;
            drawRailArrow(graphics, font, RAIL_UP, y + TAB_TOP, railOffset > 0, mouseX, mouseY);
            drawRailArrow(graphics, font, RAIL_DOWN, downArrowY(), railOffset < railMax(), mouseX, mouseY);
        }
    }

    private void drawRailArrow(GuiGraphics graphics, Font font, String glyph,
            int arrowY, boolean enabled, int mouseX, int mouseY)
    {
        int x = getX();
        boolean hovered = enabled && inRect(mouseX, mouseY, x + TAB_X, arrowY, -TAB_X, RAIL_ARROW_H);
        int color = !enabled ? 0x808080 : hovered ? 0xFFFFA0 : 0xFFFFFF;
        int glyphX = x + TAB_X + (-TAB_X - font.width(glyph)) / 2;
        graphics.drawString(font, glyph, glyphX, arrowY + 2, color, true);
    }

    private int downArrowY()
    {
        return getY() + tabTop() + visibleTabs() * TAB_H;
    }

    // The visible tab whose window position holds the mouse, or -1. Ignores the pager arrow rows.
    private int railTabAt(double mouseX, double mouseY)
    {
        int x = getX();
        int y = getY();
        int visible = visibleTabs();
        for (int row = 0; row < visible; row++)
        {
            int gi = railOffset + row;
            int tabX = x + TAB_X - (gi == groupIndex ? TAB_SELECTED_SHIFT : 0);
            int tabY = y + tabTop() + row * TAB_H;
            if (inRect(mouseX, mouseY, tabX, tabY, x - tabX, TAB_H))
                return gi;
        }
        return -1;
    }

    // +1 for the down pager, -1 for the up pager, 0 if neither was clicked.
    private int railArrowAt(double mouseX, double mouseY)
    {
        if (!railPaged())
            return 0;
        int x = getX();
        if (inRect(mouseX, mouseY, x + TAB_X, getY() + TAB_TOP, -TAB_X, RAIL_ARROW_H) && railOffset > 0)
            return -1;
        if (inRect(mouseX, mouseY, x + TAB_X, downArrowY(), -TAB_X, RAIL_ARROW_H) && railOffset < railMax())
            return 1;
        return 0;
    }

    private boolean isOverRail(double mouseX, double mouseY)
    {
        int x = getX();
        int height = tabTop() - TAB_TOP + visibleTabs() * TAB_H + (railPaged() ? RAIL_ARROW_H : 0);
        return inRect(mouseX, mouseY, x + TAB_X, getY() + TAB_TOP, -TAB_X, height);
    }

    private void renderBrowse(GuiGraphics graphics, int x, int y, int mouseX, int mouseY, float partialTick)
    {
        search.render(graphics, mouseX, mouseY, partialTick);

        int start = page * PER_PAGE;
        hovered = null;
        for (int i = 0; i < PER_PAGE && start + i < filtered.size(); i++)
        {
            int cellX = x + GRID_X + CELL * (i % COLS);
            int cellY = y + GRID_Y + CELL * (i / COLS);
            graphics.blitSprite(SLOT, cellX, cellY, CELL, CELL);

            BookIngredient item = filtered.get(start + i);
            item.render(graphics, cellX + ITEM_INSET, cellY + ITEM_INSET);

            if (mouseX >= cellX && mouseX < cellX + CELL && mouseY >= cellY && mouseY < cellY + CELL)
                hovered = item;
        }
    }

    private void renderRecipe(GuiGraphics graphics, int x, int y, int mouseX, int mouseY)
    {
        // The search bar and its magnifier are baked into the book texture. Cover that strip with a
        // slice of the book's own plain interior (sourced from the grid area), stretched to the
        // current width, so the back control sits on clean parchment instead of the magnifier.
        graphics.blit(BACKGROUND, x + BACK_X, y + SEARCH_Y - 3, getWidth() - 2 * BACK_X, SEARCH_H + 6,
                BACK_X + 1, GRID_Y + 1, WIDTH - 2 * BACK_X, SEARCH_H + 6, 256, 256);

        var font = Minecraft.getInstance().font;
        boolean overBack = inRect(mouseX, mouseY, x + BACK_X, y + BACK_Y, BACK_W, BACK_H);
        graphics.blitSprite(BACKWARD_SPRITES.get(true, overBack), x + BACK_X, y + BACK_Y, ARROW_W, ARROW_H);
        graphics.drawString(font, BACK_LABEL, x + BACK_X + ARROW_W + 3, y + BACK_Y + (ARROW_H - 8) / 2,
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
        if (hoveredTab != null)
        {
            graphics.renderTooltip(minecraft.font, hoveredTab.title(), mouseX, mouseY);
            return;
        }
        if (hovered != null)
        {
            TooltipFlag flag = minecraft.options.advancedItemTooltips ? TooltipFlag.ADVANCED : TooltipFlag.NORMAL;
            graphics.renderComponentTooltip(minecraft.font, hovered.tooltip(flag), mouseX, mouseY);
        }
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

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        if (!visible)
            return false;

        if (inRecipeMode())
        {
            int arrow = railArrowAt(mouseX, mouseY);
            if (arrow != 0)
            {
                playClickSound();
                scrollRail(arrow);
                return true;
            }
            int tab = railTabAt(mouseX, mouseY);
            if (tab >= 0)
            {
                // Left-click a tab: the item's recipes in that category. Right-click: the whole category.
                playClickSound();
                if (isRightClick(button))
                    showAllRecipes(tab);
                else
                    selectGroup(tab);
                return true;
            }
            if (inRect(mouseX, mouseY, getX() + BACK_X, getY() + BACK_Y, BACK_W, BACK_H))
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

        if (inRecipeMode() && isOverRail(mouseX, mouseY))
        {
            scrollRail(scrollY < 0 ? 1 : -1);
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
