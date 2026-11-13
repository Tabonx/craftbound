package com.craftbound.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
//? if >=1.21.4 {
/*import net.minecraft.client.RecipeBookCategories;
*///?} else {
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.BlastingRecipe;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.item.crafting.SmokingRecipe;
import net.minecraft.world.item.crafting.StonecutterRecipe;
//?}

class RecipeTabsTest
{
    @BeforeAll
    static void bootstrap()
    {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    //? if >=1.21.4 {
    /*@Test
    void craftingCategoriesMapOntoTheirOwnRibbon()
    {
        assertEquals(BrowseTab.BUILDING, RecipeTabs.tabOf(RecipeBookCategories.CRAFTING_BUILDING_BLOCKS));
        assertEquals(BrowseTab.REDSTONE, RecipeTabs.tabOf(RecipeBookCategories.CRAFTING_REDSTONE));
        assertEquals(BrowseTab.EQUIPMENT, RecipeTabs.tabOf(RecipeBookCategories.CRAFTING_EQUIPMENT));
        assertEquals(BrowseTab.MISC, RecipeTabs.tabOf(RecipeBookCategories.CRAFTING_MISC));
    }

    @Test
    void everyCookingStationFoldsItsBlocksOntoBuilding()
    {
        assertEquals(BrowseTab.BUILDING, RecipeTabs.tabOf(RecipeBookCategories.FURNACE_BLOCKS));
        assertEquals(BrowseTab.BUILDING, RecipeTabs.tabOf(RecipeBookCategories.BLAST_FURNACE_BLOCKS));
        assertEquals(BrowseTab.MISC, RecipeTabs.tabOf(RecipeBookCategories.FURNACE_FOOD));
        assertEquals(BrowseTab.MISC, RecipeTabs.tabOf(RecipeBookCategories.FURNACE_MISC));
        assertEquals(BrowseTab.MISC, RecipeTabs.tabOf(RecipeBookCategories.BLAST_FURNACE_MISC));
        assertEquals(BrowseTab.MISC, RecipeTabs.tabOf(RecipeBookCategories.SMOKER_FOOD));
        assertEquals(BrowseTab.MISC, RecipeTabs.tabOf(RecipeBookCategories.CAMPFIRE));
    }

    @Test
    void stationsWithoutACategoryLandNowhere()
    {
        assertNull(RecipeTabs.tabOf(RecipeBookCategories.STONECUTTER));
        assertNull(RecipeTabs.tabOf(RecipeBookCategories.SMITHING));
    }
    *///?} else {
    @Test
    void craftingRecipesLandOnTheirOwnRibbon()
    {
        assertEquals(BrowseTab.REDSTONE, RecipeTabs.tabOf(new ShapelessRecipe("", CraftingBookCategory.REDSTONE,
                new ItemStack(Items.LEVER), NonNullList.of(Ingredient.EMPTY, Ingredient.of(Items.STICK)))));
    }

    @Test
    void everyCookingStationFoldsItsBlocksOntoBuilding()
    {
        Ingredient sand = Ingredient.of(Items.SAND);
        ItemStack glass = new ItemStack(Items.GLASS);
        assertEquals(BrowseTab.BUILDING,
                RecipeTabs.tabOf(new SmeltingRecipe("", CookingBookCategory.BLOCKS, sand, glass, 0, 200)));
        assertEquals(BrowseTab.BUILDING,
                RecipeTabs.tabOf(new BlastingRecipe("", CookingBookCategory.BLOCKS, sand, glass, 0, 100)));
        assertEquals(BrowseTab.MISC,
                RecipeTabs.tabOf(new BlastingRecipe("", CookingBookCategory.MISC, sand, glass, 0, 100)));
        assertEquals(BrowseTab.MISC,
                RecipeTabs.tabOf(new SmokingRecipe("", CookingBookCategory.FOOD, sand, glass, 0, 100)));
        assertEquals(BrowseTab.MISC,
                RecipeTabs.tabOf(new CampfireCookingRecipe("", CookingBookCategory.FOOD, sand, glass, 0, 600)));
    }

    @Test
    void stationsWithoutACategoryLandNowhere()
    {
        assertNull(RecipeTabs.tabOf(new StonecutterRecipe("", Ingredient.of(Items.STONE), new ItemStack(Items.STONE_SLAB))));
    }
    //?}
}
