package com.craftbound.client;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.craftbound.Craftbound;

import net.minecraft.client.RecipeBookCategories;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.CraftingBookCategory;

// The ribbons on the browse view's rail. The four middle ones mirror vanilla's crafting-book tabs,
// down to borrowing vanilla's own tab icons, with All on top and Bookmarks closing the rail.
public enum BrowseTab implements BookRail.Tab
{
    ALL(vanillaIcon(() -> RecipeBookCategories.CRAFTING_SEARCH), "tab.all"),
    EQUIPMENT(vanillaIcon(() -> RecipeBookCategories.CRAFTING_EQUIPMENT), "tab.equipment"),
    BUILDING(vanillaIcon(() -> RecipeBookCategories.CRAFTING_BUILDING_BLOCKS), "tab.building"),
    MISC(vanillaIcon(() -> RecipeBookCategories.CRAFTING_MISC), "tab.misc"),
    REDSTONE(vanillaIcon(() -> RecipeBookCategories.CRAFTING_REDSTONE), "tab.redstone"),
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

    // Held behind a supplier so naming a vanilla category here does not force its class (and the
    // item stacks it builds) to load before the registries exist.
    private static Icon vanillaIcon(Supplier<RecipeBookCategories> category)
    {
        return (graphics, x, y) ->
        {
            List<ItemStack> icons = category.get().getIconItems();
            if (icons.size() == 1)
                graphics.renderFakeItem(icons.get(0), x, y);
            else if (icons.size() >= 2)
            {
                // Vanilla's two-icon tabs sit 6px either side of where a lone icon would go.
                graphics.renderFakeItem(icons.get(0), x - 6, y);
                graphics.renderFakeItem(icons.get(1), x + 5, y);
            }
        };
    }

    private static Icon spriteIcon(ResourceLocation sprite)
    {
        return (graphics, x, y) -> graphics.blitSprite(sprite, x, y, 16, 16);
    }
}
