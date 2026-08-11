package com.craftbound.client;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

// The tab rail protruding from the book's left edge, mirroring vanilla's recipe-book category
// tabs. Used twice: recipe categories while a recipe is open, bookmarks while browsing. Owns its
// scroll offset and hit-testing; the host supplies the tabs and decides what a click means.
public final class BookRail
{
    public static final int TAB_W = 35;
    public static final int TAB_H = 27;
    public static final int TAB_X = -30;
    public static final int TAB_TOP = 4;
    private static final int TAB_SELECTED_SHIFT = 2;
    private static final int TAB_ICON_DX = 9;
    private static final int TAB_ICON_DY = 6;
    private static final int ARROW_H = 11;
    private static final String UP = "▲";
    private static final String DOWN = "▼";

    private static final WidgetSprites TAB_SPRITES = new WidgetSprites(
            ResourceLocation.withDefaultNamespace("recipe_book/tab"),
            ResourceLocation.withDefaultNamespace("recipe_book/tab_selected"));

    // One entry on the rail: draws its own icon (a category's, or a bookmarked ingredient's) and
    // names itself for the tooltip.
    public interface Tab
    {
        void drawIcon(GuiGraphics graphics, int x, int y);

        Component title();
    }

    private List<? extends Tab> tabs = List.of();
    private int selected = -1;
    private int offset = 0;
    private int x;
    private int y;
    private Tab hovered = null;

    public void setPosition(int x, int y)
    {
        this.x = x;
        this.y = y;
    }

    public void setTabs(List<? extends Tab> tabs, int selected)
    {
        this.tabs = tabs;
        this.selected = selected;
        this.offset = RailLayout.clampOffset(offset, tabs.size());
    }

    // Scroll so the given tab is visible, after the host changed the selection.
    public void showTab(int index)
    {
        selected = index;
        offset = RailLayout.offsetShowing(index, offset, tabs.size());
    }

    public void scroll(int delta)
    {
        offset = RailLayout.clampOffset(offset + delta, tabs.size());
    }

    public Tab hovered()
    {
        return hovered;
    }

    public void render(GuiGraphics graphics, int mouseX, int mouseY)
    {
        hovered = null;
        int visible = RailLayout.visibleTabs(tabs.size());
        for (int row = 0; row < visible; row++)
        {
            int index = offset + row;
            int tabX = tabX(index);
            int tabY = tabY(row);

            graphics.blitSprite(TAB_SPRITES.get(true, index == selected), tabX, tabY, TAB_W, TAB_H);
            tabs.get(index).drawIcon(graphics, tabX + TAB_ICON_DX, tabY + TAB_ICON_DY);

            if (inRect(mouseX, mouseY, tabX, tabY, x - tabX, TAB_H))
                hovered = tabs.get(index);
        }

        if (RailLayout.paged(tabs.size()))
        {
            drawArrow(graphics, UP, y + TAB_TOP, offset > 0, mouseX, mouseY);
            drawArrow(graphics, DOWN, downArrowY(), offset < RailLayout.maxOffset(tabs.size()), mouseX, mouseY);
        }
    }

    // The tab index under the mouse, or -1. Ignores the pager arrow rows.
    public int tabAt(double mouseX, double mouseY)
    {
        int visible = RailLayout.visibleTabs(tabs.size());
        for (int row = 0; row < visible; row++)
        {
            int index = offset + row;
            if (inRect(mouseX, mouseY, tabX(index), tabY(row), x - tabX(index), TAB_H))
                return index;
        }
        return -1;
    }

    // +1 for the down pager, -1 for the up pager, 0 if neither was clicked.
    public int arrowAt(double mouseX, double mouseY)
    {
        if (!RailLayout.paged(tabs.size()))
            return 0;
        if (inRect(mouseX, mouseY, x + TAB_X, y + TAB_TOP, -TAB_X, ARROW_H) && offset > 0)
            return -1;
        if (inRect(mouseX, mouseY, x + TAB_X, downArrowY(), -TAB_X, ARROW_H)
                && offset < RailLayout.maxOffset(tabs.size()))
            return 1;
        return 0;
    }

    public boolean isOver(double mouseX, double mouseY)
    {
        int visible = RailLayout.visibleTabs(tabs.size());
        int height = tabTop() - TAB_TOP + visible * TAB_H + (RailLayout.paged(tabs.size()) ? ARROW_H : 0);
        return inRect(mouseX, mouseY, x + TAB_X, y + TAB_TOP, -TAB_X, height);
    }

    private void drawArrow(GuiGraphics graphics, String glyph, int arrowY, boolean enabled,
            int mouseX, int mouseY)
    {
        var font = Minecraft.getInstance().font;
        boolean over = enabled && inRect(mouseX, mouseY, x + TAB_X, arrowY, -TAB_X, ARROW_H);
        int color = !enabled ? 0x808080 : over ? 0xFFFFA0 : 0xFFFFFF;
        graphics.drawString(font, glyph, x + TAB_X + (-TAB_X - font.width(glyph)) / 2, arrowY + 2, color, true);
    }

    // Tabs sit below the ▲ pager when the rail is paged, otherwise flush with the top.
    private int tabTop()
    {
        return TAB_TOP + (RailLayout.paged(tabs.size()) ? ARROW_H : 0);
    }

    private int tabX(int index)
    {
        return x + TAB_X - (index == selected ? TAB_SELECTED_SHIFT : 0);
    }

    private int tabY(int row)
    {
        return y + tabTop() + row * TAB_H;
    }

    private int downArrowY()
    {
        return y + tabTop() + RailLayout.visibleTabs(tabs.size()) * TAB_H;
    }

    private static boolean inRect(double mx, double my, int x, int y, int w, int h)
    {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }
}
