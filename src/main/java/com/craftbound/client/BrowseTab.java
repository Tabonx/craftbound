package com.craftbound.client;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.craftbound.Craftbound;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.CraftingBookCategory;

// The ribbons on the browse view's rail. The four middle ones mirror vanilla's crafting-book tabs,
// down to borrowing vanilla's own tab icons, with All on top and Bookmarks closing the rail.
public enum BrowseTab implements BookRail.Tab
{
    ALL(itemIcon(() -> Items.COMPASS), "tab.all"),
    EQUIPMENT(itemIcon(() -> Items.IRON_AXE, () -> Items.GOLDEN_SWORD), "tab.equipment"),
    BUILDING(itemIcon(() -> Items.BRICKS), "tab.building"),
    MISC(itemIcon(() -> Items.LAVA_BUCKET, () -> Items.APPLE), "tab.misc"),
    REDSTONE(itemIcon(() -> Items.REDSTONE), "tab.redstone"),
    BOOKMARKS(spriteIcon(ResourceLocation.fromNamespaceAndPath(Craftbound.MODID,
            "recipe_book/bookmark_tab")), "bookmarks");

    // Every tab but Bookmarks, which only earns its place once something is bookmarked.
    private static final List<BrowseTab> ALWAYS_SHOWN = List.of(ALL, EQUIPMENT, BUILDING, MISC, REDSTONE);

    private final Icon icon;
    private final Component title;

    BrowseTab(Icon icon, String key)
    {
        this.icon = icon;
        this.title = Component.translatable("craftbound.recipebook." + key);
    }

    public static List<BrowseTab> visible(boolean hasBookmarks)
    {
        if (!hasBookmarks)
            return ALWAYS_SHOWN;
        List<BrowseTab> tabs = new ArrayList<>(ALWAYS_SHOWN);
        tabs.add(BOOKMARKS);
        return List.copyOf(tabs);
    }

    // Whether the tab holds a subset of the items rather than a whole source of them.
    public boolean isItemCategory()
    {
        return this != ALL && this != BOOKMARKS;
    }

    public static BrowseTab of(CraftingBookCategory category)
    {
        return switch (category)
        {
            case BUILDING -> BUILDING;
            case REDSTONE -> REDSTONE;
            case EQUIPMENT -> EQUIPMENT;
            case MISC -> MISC;
        };
    }

    // Cooking recipes carry their own, coarser category. Fold it onto the crafting ribbons so an
    // item that is only ever smelted still lands somewhere sensible.
    public static BrowseTab of(CookingBookCategory category)
    {
        return switch (category)
        {
            case BLOCKS -> BUILDING;
            case FOOD, MISC -> MISC;
        };
    }

    @Override
    public void drawIcon(GuiGraphics graphics, int x, int y)
    {
        icon.draw(graphics, x, y);
    }

    @Override
    public Component title()
    {
        return title;
    }

    @FunctionalInterface
    private interface Icon
    {
        void draw(GuiGraphics graphics, int x, int y);
    }

    // The same items vanilla puts on its own crafting tabs, named here rather than read off a
    // vanilla category: newer versions keep the icons in their recipe book screen instead of on the
    // category, and these five have not changed. Held behind suppliers so naming them does not force
    // item stacks to be built before the registries exist.
    @SafeVarargs
    private static Icon itemIcon(Supplier<Item>... items)
    {
        return (graphics, x, y) ->
        {
            if (items.length == 1)
                graphics.renderFakeItem(new ItemStack(items[0].get()), x, y);
            else if (items.length >= 2)
            {
                // Vanilla's two-icon tabs sit 6px either side of where a lone icon would go.
                graphics.renderFakeItem(new ItemStack(items[0].get()), x - 6, y);
                graphics.renderFakeItem(new ItemStack(items[1].get()), x + 5, y);
            }
        };
    }

    private static Icon spriteIcon(ResourceLocation sprite)
    {
        return (graphics, x, y) -> Canvas.sprite(graphics, sprite, x, y, 16, 16);
    }
}
