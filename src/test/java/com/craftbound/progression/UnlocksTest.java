package com.craftbound.progression;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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

    // Nothing unlocked yet and nothing produced anywhere: the baseline for slot-level assertions.
    private static boolean satisfied(ProgressionRules rules, List<InputSlot> slots,
            Set<ResourceLocation> obtained)
    {
        return Unlocks.inputsSatisfied(rules, slots, obtained, Set.of(), Set.of());
    }

    @Test
    void allInputs_needsEverySlotSatisfied()
    {
        List<InputSlot> inputs = List.of(items("minecraft:oak_planks"), items("minecraft:stick"));

        assertFalse(satisfied(STRICT, inputs, obtained("minecraft:oak_planks")));
        assertTrue(satisfied(STRICT, inputs, obtained("minecraft:oak_planks", "minecraft:stick")));
    }

    @Test
    void allInputs_slotWithAlternativesNeedsOnlyOne()
    {
        List<InputSlot> inputs =
                List.of(items("minecraft:oak_planks", "minecraft:birch_planks", "minecraft:spruce_planks"));

        assertTrue(satisfied(STRICT, inputs, obtained("minecraft:birch_planks")));
    }

    @Test
    void anyInput_needsOneSlotSatisfied()
    {
        List<InputSlot> inputs = List.of(items("minecraft:oak_planks"), items("minecraft:stick"));

        assertTrue(satisfied(LOOSE, inputs, obtained("minecraft:stick")));
        assertFalse(satisfied(LOOSE, inputs, obtained("minecraft:diamond")));
    }

    @Test
    void unjudgeableSlotsNeverLockARecipe()
    {
        List<InputSlot> unreadable = List.of(InputSlot.ofItems(Set.of()));
        assertTrue(satisfied(STRICT, unreadable, Set.of()));

        List<InputSlot> mixed = List.of(InputSlot.ofItems(Set.of()), items("minecraft:sugar"));
        assertFalse(satisfied(STRICT, mixed, Set.of()));
        assertTrue(satisfied(STRICT, mixed, obtained("minecraft:sugar")));
    }

    @Test
    void recipeWithNoInputsIsUnlocked()
    {
        assertTrue(satisfied(STRICT, List.of(), Set.of()));
    }

    // Water is in no recipe's output, so gating on it would be a gate that never opens.
    @Test
    void aFluidNothingProducesIsNotAGate()
    {
        List<InputSlot> waterSlot = List.of(fluidSlot("minecraft:water", "minecraft:water_bucket"));

        assertTrue(Unlocks.inputsSatisfied(STRICT, waterSlot, Set.of(), Set.of(), Set.of()));
    }

    @Test
    void aProducedFluidGatesUntilItIsUnlocked()
    {
        List<InputSlot> chocolate = List.of(fluidSlot("create:chocolate", "create:chocolate_bucket"));
        Set<String> produced = Set.of("fluid|create:chocolate");

        assertFalse(Unlocks.inputsSatisfied(STRICT, chocolate, Set.of(), Set.of(), produced));
        assertTrue(Unlocks.inputsSatisfied(STRICT, chocolate, Set.of(),
                Set.of("fluid|create:chocolate"), produced));
    }

    @Test
    void aFluidsBucketAlsoSatisfiesTheSlot()
    {
        List<InputSlot> chocolate = List.of(fluidSlot("create:chocolate", "create:chocolate_bucket"));

        assertTrue(Unlocks.inputsSatisfied(STRICT, chocolate, obtained("create:chocolate_bucket"),
                Set.of(), Set.of("fluid|create:chocolate")));
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

        assertTrue(Unlocks.recipeUnlocked(ProgressionRules.OPEN, index,
                node(MIXING, List.of(items("create:brass_ingot")), "item|create:brass_sheet"),
                Set.of(), Set.of()));
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

        assertFalse(Unlocks.unlockedOutputs(STRICT, index, hasIngredientsOnly)
                .contains("item|create:brass_sheet"));

        Set<ResourceLocation> hasMixer = obtained("minecraft:oak_log", "minecraft:oak_planks",
                "create:brass_ingot", "create:mechanical_mixer");
        assertTrue(Unlocks.unlockedOutputs(STRICT, index, hasMixer).contains("item|create:brass_sheet"));
    }

    // The same recipe object is listed by several categories — Create re-lists ordinary shaped
    // recipes under its own mechanical-crafter category. Being locked there must not hide it from
    // the crafting table, which is what a single recipe-keyed map did.
    @Test
    void aRecipeListedByTwoCategoriesUnlocksThroughTheOpenOne()
    {
        Object shared = "planks";
        RecipeIndex index = RecipeIndex.of(
                Map.of(
                        CRAFTING, Map.of(shared, node(CRAFTING, List.of(items("minecraft:oak_log")),
                                "item|minecraft:oak_planks")),
                        AUTOMATIC, Map.of(shared, node(AUTOMATIC, List.of(items("minecraft:oak_log")),
                                "item|minecraft:oak_planks"))),
                Map.of(AUTOMATIC, Set.of(rl("create:mechanical_crafter"))));

        assertEquals(Set.of("item|minecraft:oak_planks"),
                Unlocks.unlockedOutputs(STRICT, index, obtained("minecraft:oak_log")));
        assertTrue(Unlocks.recipeUnlocked(STRICT, index, index.node(CRAFTING, shared),
                obtained("minecraft:oak_log"), Set.of()));
        assertFalse(Unlocks.recipeUnlocked(STRICT, index, index.node(AUTOMATIC, shared),
                obtained("minecraft:oak_log"), Set.of()));
    }

    @Test
    void unlockingItemsAreTheOnesThatOpenSomething()
    {
        RecipeIndex index = index();

        // The log opens planks and the planks open sticks; brass is no use while the mixer that
        // consumes it is still locked away.
        assertEquals(obtained("minecraft:oak_log", "minecraft:oak_planks"),
                Unlocks.unlockingItems(STRICT, index, Set.of(), Set.of()));

        Set<ResourceLocation> hasLog = obtained("minecraft:oak_log");
        assertEquals(obtained("minecraft:oak_planks"),
                Unlocks.unlockingItems(STRICT, index, hasLog, Unlocks.unlockedOutputs(STRICT, index, hasLog)));
    }

    // A machine is worth marking too: obtaining it is what opens its category.
    @Test
    void aCatalystIsMarkedWhenItIsTheMissingPiece()
    {
        RecipeIndex index = index();
        Set<ResourceLocation> hasBrass = obtained("create:brass_ingot");

        assertTrue(Unlocks.unlockingItems(STRICT, index, hasBrass,
                Unlocks.unlockedOutputs(STRICT, index, hasBrass)).contains(rl("create:mechanical_mixer")));
    }

    // What the marks are for: one plank satisfying the shared recipe clears the mark from all of
    // them, because none of the others opens anything the first has not already opened.
    @Test
    void obtainingOneAlternativeClearsTheMarkFromTheRest()
    {
        RecipeIndex index = RecipeIndex.of(
                Map.of(CRAFTING, Map.of("stick", node(CRAFTING,
                        List.of(items("minecraft:oak_planks", "minecraft:birch_planks")),
                        "item|minecraft:stick"))),
                Map.of());

        assertEquals(obtained("minecraft:oak_planks", "minecraft:birch_planks"),
                Unlocks.unlockingItems(STRICT, index, Set.of(), Set.of()));

        Set<ResourceLocation> hasOak = obtained("minecraft:oak_planks");
        assertEquals(Set.of(),
                Unlocks.unlockingItems(STRICT, index, hasOak, Unlocks.unlockedOutputs(STRICT, index, hasOak)));
    }

    // Under ALL_INPUTS a single item is only worth marking once it is the last one missing.
    @Test
    void allInputs_marksOnlyTheFinalMissingInput()
    {
        RecipeIndex index = RecipeIndex.of(
                Map.of(CRAFTING, Map.of("torch", node(CRAFTING,
                        List.of(items("minecraft:stick"), items("minecraft:coal")), "item|minecraft:torch"))),
                Map.of());

        assertEquals(Set.of(), Unlocks.unlockingItems(STRICT, index, Set.of(), Set.of()));
        assertEquals(obtained("minecraft:coal"),
                Unlocks.unlockingItems(STRICT, index, obtained("minecraft:stick"), Set.of()));
    }

    // An output already reachable another way is nothing new, so nothing points at it.
    @Test
    void anAlreadyUnlockedOutputIsNotWorthMarking()
    {
        RecipeIndex index = RecipeIndex.of(
                Map.of(CRAFTING, Map.of(
                        "torch", node(CRAFTING, List.of(items("minecraft:coal")), "item|minecraft:torch"),
                        "torch_alt", node(CRAFTING, List.of(items("minecraft:charcoal")), "item|minecraft:torch"))),
                Map.of());

        Set<ResourceLocation> hasCoal = obtained("minecraft:coal");
        assertEquals(Set.of(),
                Unlocks.unlockingItems(STRICT, index, hasCoal, Unlocks.unlockedOutputs(STRICT, index, hasCoal)));
    }

    @Test
    void disabledRulesMarkNothing()
    {
        assertEquals(Set.of(), Unlocks.unlockingItems(ProgressionRules.OPEN, index(), Set.of(), Set.of()));
    }

    private static ResourceLocation rl(String id)
    {
        return ResourceLocation.parse(id);
    }

    private static Set<ResourceLocation> obtained(String... ids)
    {
        return Arrays.stream(ids).map(UnlocksTest::rl).collect(Collectors.toSet());
    }

    private static InputSlot items(String... ids)
    {
        return InputSlot.ofItems(obtained(ids));
    }

    @Test
    void discovered_needsTheRecipeUnlockedOrTheItemInHand()
    {
        ResourceLocation planks = rl("minecraft:oak_planks");

        assertFalse(Unlocks.discovered(Set.of(), Set.of(), planks));
        assertTrue(Unlocks.discovered(Set.of("item|minecraft:oak_planks"), Set.of(), planks));
        assertTrue(Unlocks.discovered(Set.of(), Set.of(planks), planks));
    }

    // A Bell and Create's creative-only blocks have no recipe at all, so nothing ever unlocks them;
    // picking one up is the only way they should be able to appear.
    @Test
    void discovered_hidesUncraftableThingsUntilObtained()
    {
        ResourceLocation bell = rl("minecraft:bell");

        assertFalse(Unlocks.discovered(Set.of(), Set.of(), bell));
        assertTrue(Unlocks.discovered(Set.of(), Set.of(bell), bell));
    }

    private static InputSlot fluidSlot(String fluidId, String bucketId)
    {
        return new InputSlot(Set.of(rl(bucketId)), Set.of("fluid|" + fluidId));
    }

    private static RecipeNode node(String category, List<InputSlot> inputs, String output)
    {
        return new RecipeNode(category, inputs, Set.of(output));
    }

    private static RecipeIndex index()
    {
        return RecipeIndex.of(
                Map.of(
                        CRAFTING, Map.of(
                                "planks", node(CRAFTING, List.of(items("minecraft:oak_log")),
                                        "item|minecraft:oak_planks"),
                                "stick", node(CRAFTING, List.of(items("minecraft:oak_planks")),
                                        "item|minecraft:stick")),
                        MIXING, Map.of(
                                "brass_sheet", node(MIXING, List.of(items("create:brass_ingot")),
                                        "item|create:brass_sheet"))),
                Map.of(
                        CRAFTING, Set.of(rl("minecraft:crafting_table")),
                        MIXING, Set.of(rl("create:mechanical_mixer"))));
    }
}
