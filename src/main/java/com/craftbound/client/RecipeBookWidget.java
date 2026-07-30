package com.craftbound.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import com.craftbound.client.jei.BookIngredient;
import com.craftbound.client.jei.CraftboundJeiPlugin;
import com.mojang.blaze3d.vertex.PoseStack;

import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.inputs.RecipeSlotUnderMouse;

import net.minecraft.client.Minecraft;
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
    private static final int BODY_W = WIDTH - 2 * BODY_X;
    private static final int BODY_H = ARROW_Y - BODY_Y - 4;

    private final List<BookIngredient> allItems = new ArrayList<>();
    private List<BookIngredient> filtered = List.of();
    private int page = 0;
    private boolean loaded = false;
    private BookIngredient hovered = null;

    private List<IRecipeLayoutDrawable<?>> recipeLayouts = List.of();
    private int recipeIndex = 0;

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
        return !recipeLayouts.isEmpty();
    }

    // Browse and Recipe share the two page arrows; each step means "previous/next" in the active state.
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

    private boolean openRecipe(BookIngredient ingredient)
    {
        List<IRecipeLayoutDrawable<?>> layouts = CraftboundJeiPlugin.recipesFor(ingredient);
        if (layouts.isEmpty())
            return false;
        recipeLayouts = layouts;
        recipeIndex = 0;
        hovered = null;
        search.setFocused(false);
        return true;
    }

    private void closeRecipe()
    {
        recipeLayouts = List.of();
        recipeIndex = 0;
    }

    // Clicking an ingredient inside the shown recipe drills into that ingredient's own recipe.
    private boolean openRecipeUnderMouse(double mouseX, double mouseY)
    {
        if (recipeLayouts.isEmpty())
            return false;

        IRecipeLayoutDrawable<?> layout = recipeLayouts.get(recipeIndex);
        double localX = (mouseX - recipeOriginX) / recipeScale + recipeBoundsX;
        double localY = (mouseY - recipeOriginY) / recipeScale + recipeBoundsY;

        Optional<BookIngredient> clicked = layout.getSlotUnderMouse(localX, localY)
                .map(RecipeSlotUnderMouse::slot)
                .flatMap(IRecipeSlotView::getDisplayedIngredient)
                .flatMap(CraftboundJeiPlugin::toBookIngredient);

        if (clicked.isPresent() && openRecipe(clicked.get()))
        {
            playClickSound();
            return true;
        }
        return false;
    }

    private void setRecipe(int target)
    {
        recipeIndex = Math.max(0, Math.min(recipeLayouts.size() - 1, target));
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
        super.setPosition(x, y);
        search.setPosition(x + SEARCH_X, y + SEARCH_Y);
        backButton.setPosition(x + BACKWARD_X, y + ARROW_Y);
        forwardButton.setPosition(x + FORWARD_X, y + ARROW_Y);
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
    {
        ensureLoaded();

        int x = getX();
        int y = getY();
        graphics.blit(BACKGROUND, x, y, 1, 1, WIDTH, HEIGHT);

        if (inRecipeMode())
            renderRecipe(graphics, x, y, mouseX, mouseY);
        else
            renderBrowse(graphics, x, y, mouseX, mouseY, partialTick);

        renderPager(graphics, x, y, mouseX, mouseY, partialTick);
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
        // slice of the book's own plain interior (sourced from the grid area) so the back control
        // sits on clean parchment instead of overlapping the magnifier.
        graphics.blit(BACKGROUND, x + BACK_X, y + SEARCH_Y - 3, BACK_X + 1, GRID_Y + 1,
                WIDTH - 2 * BACK_X, SEARCH_H + 6);

        var font = Minecraft.getInstance().font;
        boolean overBack = inRect(mouseX, mouseY, x + BACK_X, y + BACK_Y, BACK_W, BACK_H);
        graphics.blitSprite(BACKWARD_SPRITES.get(true, overBack), x + BACK_X, y + BACK_Y, ARROW_W, ARROW_H);
        graphics.drawString(font, BACK_LABEL, x + BACK_X + ARROW_W + 3, y + BACK_Y + (ARROW_H - 8) / 2,
                0xFFFFFF, true);

        IRecipeLayoutDrawable<?> layout = recipeLayouts.get(recipeIndex);
        layout.setPosition(0, 0);
        Rect2i bounds = layout.getRectWithBorder();
        int bw = Math.max(1, bounds.getWidth());
        int bh = Math.max(1, bounds.getHeight());

        float scale = Math.min(1f, Math.min((float) BODY_W / bw, (float) BODY_H / bh));
        int drawW = Math.round(bw * scale);
        int drawH = Math.round(bh * scale);
        int originX = x + BODY_X + (BODY_W - drawW) / 2;
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
        if (!visible || hovered == null)
            return;

        Minecraft minecraft = Minecraft.getInstance();
        TooltipFlag flag = minecraft.options.advancedItemTooltips ? TooltipFlag.ADVANCED : TooltipFlag.NORMAL;
        graphics.renderComponentTooltip(minecraft.font, hovered.tooltip(flag), mouseX, mouseY);
    }

    private void renderPager(GuiGraphics graphics, int x, int y, int mouseX, int mouseY, float partialTick)
    {
        int count = inRecipeMode() ? recipeLayouts.size() : pageCount();
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
            graphics.drawString(font, label, x + WIDTH / 2 - font.width(label) / 2, y + ARROW_Y + 5, 0xFFFFFF, true);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        if (!visible)
            return false;

        if (inRecipeMode())
        {
            if (inRect(mouseX, mouseY, getX() + BACK_X, getY() + BACK_Y, BACK_W, BACK_H))
            {
                playClickSound();
                closeRecipe();
                return true;
            }
            if (backButton.mouseClicked(mouseX, mouseY, button) || forwardButton.mouseClicked(mouseX, mouseY, button))
                return true;
            if (openRecipeUnderMouse(mouseX, mouseY))
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

        BookIngredient clicked = ingredientAt(mouseX, mouseY);
        if (clicked != null)
        {
            openRecipe(clicked);
            return true;
        }

        return isMouseOverBook(mouseX, mouseY);
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
        if (!visible || !isMouseOverBook(mouseX, mouseY))
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
        return inRect(mouseX, mouseY, getX(), getY(), WIDTH, HEIGHT);
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
