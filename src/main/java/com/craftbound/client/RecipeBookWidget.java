package com.craftbound.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.craftbound.client.jei.CraftboundJeiPlugin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

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

    private final List<ItemStack> allItems = new ArrayList<>();
    private List<ItemStack> filtered = List.of();
    private int page = 0;
    private boolean loaded = false;
    private ItemStack hovered = ItemStack.EMPTY;

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

        this.backButton = new ImageButton(0, 0, ARROW_W, ARROW_H, BACKWARD_SPRITES, b -> setPage(page - 1));
        this.forwardButton = new ImageButton(0, 0, ARROW_W, ARROW_H, FORWARD_SPRITES, b -> setPage(page + 1));
    }

    private void ensureLoaded()
    {
        if (loaded || !CraftboundJeiPlugin.hasRuntime())
            return;
        allItems.clear();
        allItems.addAll(CraftboundJeiPlugin.getAllItemStacks());
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
                    .filter(stack -> stack.getHoverName().getString().toLowerCase(Locale.ROOT).contains(needle))
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

        search.render(graphics, mouseX, mouseY, partialTick);

        int start = page * PER_PAGE;
        hovered = ItemStack.EMPTY;
        for (int i = 0; i < PER_PAGE && start + i < filtered.size(); i++)
        {
            int cellX = x + GRID_X + CELL * (i % COLS);
            int cellY = y + GRID_Y + CELL * (i / COLS);
            graphics.blitSprite(SLOT, cellX, cellY, CELL, CELL);

            ItemStack stack = filtered.get(start + i);
            graphics.renderItem(stack, cellX + ITEM_INSET, cellY + ITEM_INSET);

            if (mouseX >= cellX && mouseX < cellX + CELL && mouseY >= cellY && mouseY < cellY + CELL)
                hovered = stack;
        }

        renderPager(graphics, x, y, mouseX, mouseY, partialTick);
    }

    // Drawn after the whole screen (slots and their placeholder sprites) so the tooltip layers
    // don't interleave with the inventory's empty-slot icons.
    public void renderDeferredTooltip(GuiGraphics graphics, int mouseX, int mouseY)
    {
        if (visible && !hovered.isEmpty())
            graphics.renderTooltip(Minecraft.getInstance().font, hovered, mouseX, mouseY);
    }

    private void renderPager(GuiGraphics graphics, int x, int y, int mouseX, int mouseY, float partialTick)
    {
        boolean paged = pageCount() > 1;
        backButton.visible = paged && page > 0;
        forwardButton.visible = paged && page < pageCount() - 1;
        backButton.render(graphics, mouseX, mouseY, partialTick);
        forwardButton.render(graphics, mouseX, mouseY, partialTick);

        if (paged)
        {
            var font = Minecraft.getInstance().font;
            String label = (page + 1) + "/" + pageCount();
            graphics.drawString(font, label, x + WIDTH / 2 - font.width(label) / 2, y + ARROW_Y + 5, 0xFFFFFF, true);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        if (!visible)
            return false;

        boolean onSearch = inRect(mouseX, mouseY, getX() + SEARCH_X, getY() + SEARCH_Y, SEARCH_W, SEARCH_H);
        search.setFocused(onSearch);
        if (onSearch)
        {
            search.mouseClicked(mouseX, mouseY, button);
            return true;
        }

        if (backButton.mouseClicked(mouseX, mouseY, button) || forwardButton.mouseClicked(mouseX, mouseY, button))
            return true;

        return isMouseOverBook(mouseX, mouseY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY)
    {
        if (!visible || !isMouseOverBook(mouseX, mouseY))
            return false;

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

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output)
    {
        defaultButtonNarrationText(output);
    }
}
