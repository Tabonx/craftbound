package com.craftbound.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.CraftingBookCategory;

class BrowseTabTest
{
    @Test
    void craftingCategoriesMapOntoTheirOwnRibbon()
    {
        assertEquals(BrowseTab.BUILDING, BrowseTab.of(CraftingBookCategory.BUILDING));
        assertEquals(BrowseTab.REDSTONE, BrowseTab.of(CraftingBookCategory.REDSTONE));
        assertEquals(BrowseTab.EQUIPMENT, BrowseTab.of(CraftingBookCategory.EQUIPMENT));
        assertEquals(BrowseTab.MISC, BrowseTab.of(CraftingBookCategory.MISC));
    }

    @Test
    void cookingCategoriesFoldOntoTheCraftingRibbons()
    {
        assertEquals(BrowseTab.BUILDING, BrowseTab.of(CookingBookCategory.BLOCKS));
        assertEquals(BrowseTab.MISC, BrowseTab.of(CookingBookCategory.FOOD));
        assertEquals(BrowseTab.MISC, BrowseTab.of(CookingBookCategory.MISC));
    }

    @Test
    void bookmarksRibbonAppearsOnlyWhenThereAreBookmarks()
    {
        assertEquals(List.of(BrowseTab.ALL, BrowseTab.EQUIPMENT, BrowseTab.BUILDING,
                BrowseTab.MISC, BrowseTab.REDSTONE), BrowseTab.visible(false));

        List<BrowseTab> withBookmarks = BrowseTab.visible(true);
        assertEquals(BrowseTab.BOOKMARKS, withBookmarks.get(withBookmarks.size() - 1));
        assertEquals(RailLayout.MAX_TABS, withBookmarks.size(), "the rail must not need paging");
    }

    @Test
    void onlyTheFourVanillaRibbonsNarrowTheGrid()
    {
        assertFalse(BrowseTab.ALL.isItemCategory());
        assertFalse(BrowseTab.BOOKMARKS.isItemCategory());
        assertTrue(BrowseTab.EQUIPMENT.isItemCategory());
        assertTrue(BrowseTab.REDSTONE.isItemCategory());
    }
}
