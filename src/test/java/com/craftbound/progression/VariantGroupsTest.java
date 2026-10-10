package com.craftbound.progression;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

class VariantGroupsTest
{
    private static final String CRAFTING = "minecraft:crafting";

    @Test
    void outputsSharingAGroupAreVariants()
    {
        Map<String, String> groups = VariantGroups.of(index(
                node("oak", "item|minecraft:oak_chest_boat", "chest_boat"),
                node("birch", "item|minecraft:birch_chest_boat", "chest_boat")));

        assertEquals("chest_boat", groups.get("item|minecraft:oak_chest_boat"));
        assertEquals("chest_boat", groups.get("item|minecraft:birch_chest_boat"));
    }

    // Two recipes for red dye share a group, but there is only one dye to fold.
    @Test
    void aGroupMakingOneOutputFoldsNothing()
    {
        Map<String, String> groups = VariantGroups.of(index(
                node("poppy", "item|minecraft:red_dye", "red_dye"),
                node("rose", "item|minecraft:red_dye", "red_dye")));

        assertFalse(groups.containsKey("item|minecraft:red_dye"));
    }

    // A red bed is both crafted and dyed; it joins the larger group, where every bed is.
    @Test
    void anOutputInSeveralGroupsJoinsTheLargest()
    {
        Map<String, String> groups = VariantGroups.of(index(
                node("white_bed", "item|minecraft:white_bed", "bed"),
                node("red_bed", "item|minecraft:red_bed", "bed"),
                node("blue_bed", "item|minecraft:blue_bed", "bed"),
                node("dye_red_bed", "item|minecraft:red_bed", "bed_dye"),
                node("dye_blue_bed", "item|minecraft:blue_bed", "bed_dye")));

        assertEquals("bed", groups.get("item|minecraft:red_bed"));
        assertEquals("bed", groups.get("item|minecraft:blue_bed"));
    }

    @Test
    void recipesWithoutAGroupStayApart()
    {
        assertEquals(Map.of(), VariantGroups.of(index(
                node("a", "item|minecraft:stick", ""),
                node("b", "item|minecraft:torch", ""))));
    }

    private record Named(String name, RecipeNode node) {}

    private static Named node(String name, String output, String group)
    {
        return new Named(name, new RecipeNode(CRAFTING, List.of(), Set.of(output), group));
    }

    private static RecipeIndex index(Named... nodes)
    {
        Map<Object, RecipeNode> byRecipe = new java.util.HashMap<>();
        for (Named named : nodes)
            byRecipe.put(named.name(), named.node());
        return RecipeIndex.of(Map.of(CRAFTING, byRecipe), Map.of());
    }
}
