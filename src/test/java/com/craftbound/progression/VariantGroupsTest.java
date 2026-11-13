package com.craftbound.progression;

import static com.craftbound.progression.Keys.key;
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
        Map<UnlockKey, String> groups = VariantGroups.of(index(
                node("oak", key("item|minecraft:oak_chest_boat"), "chest_boat"),
                node("birch", key("item|minecraft:birch_chest_boat"), "chest_boat")));

        assertEquals("chest_boat", groups.get(key("item|minecraft:oak_chest_boat")));
        assertEquals("chest_boat", groups.get(key("item|minecraft:birch_chest_boat")));
    }

    // Two recipes for red dye share a group, but there is only one dye to fold.
    @Test
    void aGroupMakingOneOutputFoldsNothing()
    {
        Map<UnlockKey, String> groups = VariantGroups.of(index(
                node("poppy", key("item|minecraft:red_dye"), "red_dye"),
                node("rose", key("item|minecraft:red_dye"), "red_dye")));

        assertFalse(groups.containsKey(key("item|minecraft:red_dye")));
    }

    // A red bed is both crafted and dyed; it joins the larger group, where every bed is.
    @Test
    void anOutputInSeveralGroupsJoinsTheLargest()
    {
        Map<UnlockKey, String> groups = VariantGroups.of(index(
                node("white_bed", key("item|minecraft:white_bed"), "bed"),
                node("red_bed", key("item|minecraft:red_bed"), "bed"),
                node("blue_bed", key("item|minecraft:blue_bed"), "bed"),
                node("dye_red_bed", key("item|minecraft:red_bed"), "bed_dye"),
                node("dye_blue_bed", key("item|minecraft:blue_bed"), "bed_dye")));

        assertEquals("bed", groups.get(key("item|minecraft:red_bed")));
        assertEquals("bed", groups.get(key("item|minecraft:blue_bed")));
    }

    @Test
    void recipesWithoutAGroupStayApart()
    {
        assertEquals(Map.of(), VariantGroups.of(index(
                node("a", key("item|minecraft:stick"), ""),
                node("b", key("item|minecraft:torch"), ""))));
    }

    private record Named(String name, RecipeNode node) {}

    private static Named node(String name, UnlockKey output, String group)
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
