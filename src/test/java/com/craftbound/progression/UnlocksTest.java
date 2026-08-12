package com.craftbound.progression;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import net.minecraft.resources.ResourceLocation;

class UnlocksTest
{
    private static final String CRAFTING = "minecraft:crafting";
    private static final String MIXING = "create:mixing";
    private static final String AUTOMATIC = "create:automatic_shaped";

    private static final ProgressionRules STRICT =
            new ProgressionRules(true, UnlockRule.ALL_INPUTS, true, Set.of(CRAFTING));
    private static final ProgressionRules LOOSE =
            new ProgressionRules(true, UnlockRule.ANY_INPUT, true, Set.of(CRAFTING));

    private static ResourceLocation rl(String id)
    {
        return ResourceLocation.parse(id);
    }

    private static Set<ResourceLocation> obtained(String... ids)
    {
        return java.util.Arrays.stream(ids).map(UnlocksTest::rl).collect(java.util.stream.Collectors.toSet());
    }

    private static Set<ResourceLocation> slot(String... ids)
    {
        return obtained(ids);
    }

    @Test
    void allInputs_needsEverySlotSatisfied()
    {
        List<Set<ResourceLocation>> inputs = List.of(slot("minecraft:oak_planks"), slot("minecraft:stick"));

        assertFalse(Unlocks.inputsSatisfied(STRICT, inputs, obtained("minecraft:oak_planks")));
        assertTrue(Unlocks.inputsSatisfied(STRICT, inputs, obtained("minecraft:oak_planks", "minecraft:stick")));
    }

    @Test
    void allInputs_slotWithAlternativesNeedsOnlyOne()
    {
        List<Set<ResourceLocation>> inputs =
                List.of(slot("minecraft:oak_planks", "minecraft:birch_planks", "minecraft:spruce_planks"));

        assertTrue(Unlocks.inputsSatisfied(STRICT, inputs, obtained("minecraft:birch_planks")));
    }

    @Test
    void anyInput_needsOneSlotSatisfied()
    {
        List<Set<ResourceLocation>> inputs = List.of(slot("minecraft:oak_planks"), slot("minecraft:stick"));

        assertTrue(Unlocks.inputsSatisfied(LOOSE, inputs, obtained("minecraft:stick")));
        assertFalse(Unlocks.inputsSatisfied(LOOSE, inputs, obtained("minecraft:diamond")));
    }

    @Test
    void unjudgeableSlotsNeverLockARecipe()
    {
        List<Set<ResourceLocation>> fluidOnly = List.of(Set.of());
        assertTrue(Unlocks.inputsSatisfied(STRICT, fluidOnly, Set.of()));

        List<Set<ResourceLocation>> mixed = List.of(Set.of(), slot("minecraft:sugar"));
        assertFalse(Unlocks.inputsSatisfied(STRICT, mixed, Set.of()));
        assertTrue(Unlocks.inputsSatisfied(STRICT, mixed, obtained("minecraft:sugar")));
    }

    @Test
    void recipeWithNoInputsIsUnlocked()
    {
        assertTrue(Unlocks.inputsSatisfied(STRICT, List.of(), Set.of()));
    }

    @Test
    void categoryIsGatedOnItsCatalyst()
    {
        Set<ResourceLocation> mixer = obtained("create:mechanical_mixer");

        assertFalse(Unlocks.categoryUnlocked(STRICT, MIXING, mixer, obtained("minecraft:stick")));
        assertTrue(Unlocks.categoryUnlocked(STRICT, MIXING, mixer, obtained("create:mechanical_mixer")));
    }

    @Test
    void exemptCategoryIsNeverGated()
    {
        assertTrue(Unlocks.categoryUnlocked(STRICT, CRAFTING, obtained("minecraft:crafting_table"), Set.of()));
    }

    @Test
    void categoryWithoutCatalystsIsNeverGated()
    {
        assertTrue(Unlocks.categoryUnlocked(STRICT, MIXING, Set.of(), Set.of()));
    }

    @Test
    void disabledRulesUnlockEverything()
    {
        RecipeIndex index = index();
        assertTrue(Unlocks.recipeUnlocked(ProgressionRules.OPEN, index, node(MIXING,
                List.of(slot("create:brass_ingot")), "item|create:brass_sheet"), Set.of()));
        assertEquals(Set.of("item|minecraft:oak_planks", "item|minecraft:stick", "item|create:brass_sheet"),
                Unlocks.unlockedOutputs(ProgressionRules.OPEN, index, Set.of()));
    }

    @Test
    void unlockedOutputsFollowTheObtainedChain()
    {
        RecipeIndex index = index();

        assertEquals(Set.of(), Unlocks.unlockedOutputs(STRICT, index, Set.of()));
        assertEquals(Set.of("item|minecraft:oak_planks"),
                Unlocks.unlockedOutputs(STRICT, index, obtained("minecraft:oak_log")));
        assertEquals(Set.of("item|minecraft:oak_planks", "item|minecraft:stick"),
                Unlocks.unlockedOutputs(STRICT, index, obtained("minecraft:oak_log", "minecraft:oak_planks")));
    }

    @Test
    void machineOutputsStayHiddenUntilTheMachineIsObtained()
    {
        RecipeIndex index = index();
        Set<ResourceLocation> hasIngredientsOnly = obtained("minecraft:oak_log", "minecraft:oak_planks",
                "create:brass_ingot");

        assertFalse(Unlocks.unlockedOutputs(STRICT, index, hasIngredientsOnly).contains("item|create:brass_sheet"));

        Set<ResourceLocation> hasMixer = obtained("minecraft:oak_log", "minecraft:oak_planks",
                "create:brass_ingot", "create:mechanical_mixer");
        assertTrue(Unlocks.unlockedOutputs(STRICT, index, hasMixer).contains("item|create:brass_sheet"));
    }

    private static RecipeNode node(String category, List<Set<ResourceLocation>> inputs, String output)
    {
        return new RecipeNode(category, inputs, Set.of(output));
    }

    private static RecipeIndex index()
    {
        return new RecipeIndex(
                Map.of(
                        CRAFTING, Map.of(
                                "planks", node(CRAFTING, List.of(slot("minecraft:oak_log")),
                                        "item|minecraft:oak_planks"),
                                "stick", node(CRAFTING, List.of(slot("minecraft:oak_planks")),
                                        "item|minecraft:stick")),
                        MIXING, Map.of(
                                "brass_sheet", node(MIXING, List.of(slot("create:brass_ingot")),
                                        "item|create:brass_sheet"))),
                Map.of(
                        CRAFTING, Set.of(rl("minecraft:crafting_table")),
                        MIXING, Set.of(rl("create:mechanical_mixer"))));
    }

    // The same recipe object is listed by several categories — Create re-lists ordinary shaped
    // recipes under its own mechanical-crafter category. Being locked there must not hide it from
    // the crafting table, which is what a single recipe-keyed map did.
    @Test
    void aRecipeListedByTwoCategoriesUnlocksThroughTheOpenOne()
    {
        Object shared = "planks";
        RecipeIndex index = new RecipeIndex(
                Map.of(
                        CRAFTING, Map.of(shared, node(CRAFTING, List.of(slot("minecraft:oak_log")),
                                "item|minecraft:oak_planks")),
                        AUTOMATIC, Map.of(shared, node(AUTOMATIC, List.of(slot("minecraft:oak_log")),
                                "item|minecraft:oak_planks"))),
                Map.of(AUTOMATIC, Set.of(rl("create:mechanical_crafter"))));

        assertEquals(Set.of("item|minecraft:oak_planks"),
                Unlocks.unlockedOutputs(STRICT, index, obtained("minecraft:oak_log")));
        assertTrue(Unlocks.recipeUnlocked(STRICT, index, index.node(CRAFTING, shared),
                obtained("minecraft:oak_log")));
        assertFalse(Unlocks.recipeUnlocked(STRICT, index, index.node(AUTOMATIC, shared),
                obtained("minecraft:oak_log")));
    }
}
