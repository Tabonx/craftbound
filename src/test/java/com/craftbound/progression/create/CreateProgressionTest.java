package com.craftbound.progression.create;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.craftbound.progression.InputSlot;
import com.craftbound.progression.ProgressionRules;
import com.craftbound.progression.RecipeIndex;
import com.craftbound.progression.RecipeNode;
import com.craftbound.progression.UnlockRule;
import com.craftbound.progression.Unlocks;

import org.junit.jupiter.api.Test;

import net.minecraft.resources.ResourceLocation;

// Progression walked over a model of Create's actual early game, since Create is what this mod is
// aimed at. The recipes are fixtures rather than the real datapack — real recipes need a running
// game — but they are shaped exactly as RecipeIndexBuilder produces them, heat requirements
// included, and they go through the real HeatRequirement mapping.
class CreateProgressionTest
{
    private static final String CRAFTING = "minecraft:crafting";
    private static final String MIXING = "create:mixing";
    private static final String CRUSHING = "create:crushing";
    private static final String PRESSING = "create:pressing";
    private static final String BREWING = "create:automatic_brewing";

    private static final ProgressionRules RULES =
            new ProgressionRules(true, UnlockRule.ALL_INPUTS, true, Set.of(CRAFTING));

    private static final String ANDESITE_ALLOY = "item|create:andesite_alloy";
    private static final String BRASS_INGOT = "item|create:brass_ingot";
    private static final String COPPER_SHEET = "item|create:copper_sheet";
    private static final String COGWHEEL = "item|create:cogwheel";
    private static final String CRUSHED_IRON = "item|create:crushed_raw_iron";
    private static final String LAVA = "fluid|minecraft:lava";
    private static final String CHOCOLATE = "fluid|create:chocolate";
    private static final String CHOCOLATE_BAR = "item|create:bar_of_chocolate";
    private static final String POTION_FLUID = "fluid|create:potion";
    private static final String POTION_AWKWARD = "fluid|create:potion|awkward";
    private static final String POTION_STRONG = "fluid|create:potion|strength";

    // Create's early tree, as the index would hold it: andesite alloy by hand or in a basin, brass
    // needing a heated basin, lava needing a superheated one, plus a couple of neighbouring
    // categories to prove they gate independently.
    private static RecipeIndex createIndex()
    {
        return new Index()
                .catalysts(CRAFTING, "minecraft:crafting_table")
                .catalysts(MIXING, "create:mechanical_mixer", "create:basin")
                .catalysts(CRUSHING, "create:crushing_wheel")
                .catalysts(PRESSING, "create:mechanical_press")
                .recipe("andesite_alloy_by_hand", CRAFTING, ANDESITE_ALLOY,
                        slots(any("minecraft:andesite"), any("minecraft:iron_nugget", "create:zinc_nugget")))
                .recipe("cogwheel", CRAFTING, COGWHEEL,
                        slots(any("create:andesite_alloy"), any("minecraft:oak_planks")))
                .recipe("andesite_alloy_mixing", MIXING, ANDESITE_ALLOY,
                        basin(HeatRequirement.NONE,
                                any("minecraft:andesite"), any("minecraft:iron_nugget", "create:zinc_nugget")))
                .recipe("brass_ingot", MIXING, BRASS_INGOT,
                        basin(HeatRequirement.HEATED,
                                any("minecraft:copper_ingot"), any("create:zinc_ingot")))
                .recipe("lava", MIXING, LAVA,
                        basin(HeatRequirement.SUPERHEATED, any("minecraft:cobblestone")))
                .recipe("crushed_iron", CRUSHING, CRUSHED_IRON, slots(any("minecraft:raw_iron")))
                .recipe("copper_sheet", PRESSING, COPPER_SHEET, slots(any("minecraft:copper_ingot")))
                .build();
    }

    private static Set<String> unlocked(String... obtainedIds)
    {
        return Unlocks.unlockedOutputs(RULES, createIndex(), obtained(obtainedIds));
    }

    // Chocolate: sugar and cocoa mix into the fluid, and the fluid is pressed into a bar. The bar
    // must not appear before the fluid is reachable, and both should arrive together the moment the
    // fluid's own ingredients are in hand.
    private static RecipeIndex chocolateIndex()
    {
        return new Index()
                .catalysts(MIXING, "create:mechanical_mixer", "create:basin")
                .catalysts(PRESSING, "create:mechanical_press")
                .recipe("chocolate_fluid", MIXING, CHOCOLATE,
                        basin(HeatRequirement.HEATED, any("minecraft:sugar"), any("minecraft:cocoa_beans")))
                .recipe("chocolate_bar", PRESSING, CHOCOLATE_BAR,
                        slots(fluid("create:chocolate", "create:chocolate_bucket")))
                .build();
    }

    private static Set<String> chocolateUnlocked(String... obtainedIds)
    {
        return Unlocks.unlockedOutputs(RULES, chocolateIndex(), obtained(obtainedIds));
    }

    @Test
    void theChocolateBarWaitsForTheChocolateItself()
    {
        Set<String> withMachinesOnly = chocolateUnlocked("create:basin", "create:mechanical_press",
                "create:blaze_burner");

        assertFalse(withMachinesOnly.contains(CHOCOLATE), "the fluid needs its ingredients");
        assertFalse(withMachinesOnly.contains(CHOCOLATE_BAR), "the bar must not precede the fluid");
    }

    @Test
    void sugarAndCocoaUnlockTheFluidAndTheBarTogether()
    {
        Set<String> unlocked = chocolateUnlocked("create:basin", "create:mechanical_press",
                "create:blaze_burner", "minecraft:sugar", "minecraft:cocoa_beans");

        assertTrue(unlocked.contains(CHOCOLATE));
        assertTrue(unlocked.contains(CHOCOLATE_BAR));
    }

    @Test
    void halfTheChocolateIngredientsUnlockNeither()
    {
        Set<String> unlocked = chocolateUnlocked("create:basin", "create:mechanical_press",
                "create:blaze_burner", "minecraft:sugar");

        assertFalse(unlocked.contains(CHOCOLATE));
        assertFalse(unlocked.contains(CHOCOLATE_BAR));
    }

    // The fluid's own gate still applies to the bar transitively: no burner, no chocolate, no bar.
    @Test
    void theBarInheritsTheFluidsHeatRequirement()
    {
        Set<String> unlocked = chocolateUnlocked("create:basin", "create:mechanical_press",
                "minecraft:sugar", "minecraft:cocoa_beans");

        assertFalse(unlocked.contains(CHOCOLATE));
        assertFalse(unlocked.contains(CHOCOLATE_BAR));
    }

    // Create's brewing both consumes and produces potion fluid. Gating on a fluid only its own
    // recipes make would lock every brewing recipe forever, so that fluid must not become a gate.
    @Test
    void aFluidOnlyReachableFromItselfDoesNotLockItsRecipes()
    {
        InputSlot potion = fluid("create:potion", "create:potion_bucket");
        RecipeIndex brewing = new Index()
                .catalysts(MIXING, "create:basin")
                .recipe("awkward", MIXING, POTION_FLUID,
                        basin(HeatRequirement.HEATED, potion, any("minecraft:nether_wart")))
                .build();

        Set<String> unlocked = Unlocks.unlockedOutputs(RULES, brewing,
                obtained("create:basin", "create:blaze_burner", "minecraft:nether_wart"));

        assertTrue(unlocked.contains(POTION_FLUID));
    }

    // ...but the ingredient gate still applies: no nether wart, no brewing.
    @Test
    void aSelfReferentialFluidStillLeavesTheItemGatesInPlace()
    {
        InputSlot potion = fluid("create:potion", "create:potion_bucket");
        RecipeIndex brewing = new Index()
                .catalysts(MIXING, "create:basin")
                .recipe("awkward", MIXING, POTION_FLUID,
                        basin(HeatRequirement.HEATED, potion, any("minecraft:nether_wart")))
                .build();

        assertFalse(Unlocks.unlockedOutputs(RULES, brewing, obtained("create:basin", "create:blaze_burner"))
                .contains(POTION_FLUID));
    }

    // Brewing: each potion is a separate fluid subtype, so unlocking one recipe must not put every
    // other potion in the grid. That mismatch showed up as fluids you could see but whose recipes
    // were all still locked.
    // Potions are subtypes of one fluid. Keyed only by that fluid, unlocking the first brewing step
    // put every potion in the grid while their own recipes stayed locked — visible fluids with
    // nothing behind them.
    @Test
    void unlockingOneBrewingStepDoesNotRevealEveryPotion()
    {
        RecipeIndex brewing = brewingIndex();

        Set<String> withWart = Unlocks.unlockedOutputs(RULES, brewing,
                obtained("create:basin", "create:blaze_burner", "minecraft:nether_wart"));

        assertTrue(withWart.contains(POTION_AWKWARD), "the step whose ingredient is in hand");
        assertFalse(withWart.contains(POTION_STRONG), "the step beyond it is still out of reach");
    }

    // The second step brews from the first one's output, so it waits for both that and its own
    // ingredient — the same chaining as items, carried through a fluid.
    @Test
    void aLaterBrewingStepWaitsForTheOneItBrewsFrom()
    {
        RecipeIndex brewing = brewingIndex();

        Set<String> glowstoneOnly = Unlocks.unlockedOutputs(RULES, brewing,
                obtained("create:basin", "create:blaze_burner", "minecraft:glowstone_dust"));
        assertFalse(glowstoneOnly.contains(POTION_STRONG), "awkward potion is not reachable yet");
        assertFalse(glowstoneOnly.contains(POTION_AWKWARD));

        Set<String> both = Unlocks.unlockedOutputs(RULES, brewing,
                obtained("create:basin", "create:blaze_burner", "minecraft:nether_wart",
                        "minecraft:glowstone_dust"));
        assertTrue(both.contains(POTION_AWKWARD));
        assertTrue(both.contains(POTION_STRONG));
    }

    @Test
    void waterAloneUnlocksNoBrewingAtAll()
    {
        RecipeIndex brewing = brewingIndex();

        assertEquals(Set.of(), Unlocks.unlockedOutputs(RULES, brewing,
                obtained("create:basin", "create:blaze_burner", "minecraft:water_bucket")));
    }

    // Two brewing steps over the same base fluid, each keyed by its own potion subtype.
    private static RecipeIndex brewingIndex()
    {
        return new Index()
                .catalysts(BREWING, "create:mechanical_mixer", "create:basin")
                .recipe("awkward", BREWING, POTION_AWKWARD,
                        basin(HeatRequirement.HEATED, potion(""), any("minecraft:nether_wart")))
                .recipe("strong", BREWING, POTION_STRONG,
                        basin(HeatRequirement.HEATED, potion("awkward"), any("minecraft:glowstone_dust")))
                .build();
    }

    // Having the fluid in a bucket is its own way in, without ever unlocking the mixing recipe.
    @Test
    void aBucketOfChocolateUnlocksTheBarOnItsOwn()
    {
        Set<String> unlocked = chocolateUnlocked("create:mechanical_press", "create:chocolate_bucket");

        assertTrue(unlocked.contains(CHOCOLATE_BAR));
        assertFalse(unlocked.contains(CHOCOLATE));
    }

    @Test
    void nothingIsUnlockedBeforeAnythingIsObtained()
    {
        assertEquals(Set.of(), unlocked());
    }

    @Test
    void handCraftingNeedsOnlyItsIngredients()
    {
        assertEquals(Set.of(ANDESITE_ALLOY), unlocked("minecraft:andesite", "minecraft:iron_nugget"));
    }

    @Test
    void eitherNuggetSatisfiesTheSameSlot()
    {
        assertTrue(unlocked("minecraft:andesite", "create:zinc_nugget").contains(ANDESITE_ALLOY));
    }

    @Test
    void halfTheIngredientsUnlockNothing()
    {
        assertFalse(unlocked("minecraft:andesite").contains(ANDESITE_ALLOY));
        assertFalse(unlocked("minecraft:iron_nugget").contains(ANDESITE_ALLOY));
    }

    @Test
    void theChainAdvancesOneStepAtATime()
    {
        assertFalse(unlocked("minecraft:andesite", "minecraft:iron_nugget").contains(COGWHEEL));
        assertTrue(unlocked("minecraft:andesite", "minecraft:iron_nugget", "create:andesite_alloy",
                "minecraft:oak_planks").contains(COGWHEEL));
    }

    @Test
    void mixingStaysLockedWhileTheIngredientsAreHeldButTheMachineIsNot()
    {
        RecipeIndex index = createIndex();
        Set<ResourceLocation> holdings = obtained("minecraft:andesite", "minecraft:iron_nugget");

        assertFalse(Unlocks.categoryUnlocked(RULES, MIXING, index.catalystsFor(MIXING), holdings));
        assertFalse(Unlocks.recipeUnlocked(RULES, index, index.node(MIXING, "andesite_alloy_mixing"), holdings, Set.of()));
    }

    @Test
    void eitherTheBasinOrTheMixerOpensTheMixingCategory()
    {
        RecipeIndex index = createIndex();

        assertTrue(Unlocks.categoryUnlocked(RULES, MIXING, index.catalystsFor(MIXING),
                obtained("create:basin")));
        assertTrue(Unlocks.categoryUnlocked(RULES, MIXING, index.catalystsFor(MIXING),
                obtained("create:mechanical_mixer")));
    }

    @Test
    void obtainingTheBasinUnlocksTheMixingRecipesTheIngredientsAllow()
    {
        RecipeIndex index = createIndex();
        Set<ResourceLocation> holdings =
                obtained("minecraft:andesite", "minecraft:iron_nugget", "create:basin");

        assertTrue(Unlocks.recipeUnlocked(RULES, index, index.node(MIXING, "andesite_alloy_mixing"), holdings, Set.of()));
        // The heated and superheated recipes must not come along for the ride.
        assertFalse(Unlocks.recipeUnlocked(RULES, index, index.node(MIXING, "brass_ingot"), holdings, Set.of()));
        assertFalse(Unlocks.recipeUnlocked(RULES, index, index.node(MIXING, "lava"), holdings, Set.of()));
    }

    @Test
    void aHeatedRecipeNeedsABlazeBurnerOnTopOfItsIngredients()
    {
        assertFalse(unlocked("create:basin", "minecraft:copper_ingot", "create:zinc_ingot")
                .contains(BRASS_INGOT));
        assertTrue(unlocked("create:basin", "minecraft:copper_ingot", "create:zinc_ingot",
                "create:blaze_burner").contains(BRASS_INGOT));
    }

    // The case that prompted this: lava showed up as soon as its ingredients were in hand, with no
    // hint that a superheated basin is required.
    @Test
    void aSuperheatedRecipeNeedsBlazeCakeAsWellAsTheBurner()
    {
        assertFalse(unlocked("create:basin", "minecraft:cobblestone").contains(LAVA));
        assertFalse(unlocked("create:basin", "minecraft:cobblestone", "create:blaze_burner").contains(LAVA));
        assertTrue(unlocked("create:basin", "minecraft:cobblestone", "create:blaze_burner",
                "create:blaze_cake").contains(LAVA));
    }

    @Test
    void aBlazeBurnerDoesNotUnlockASuperheatedRecipeInAnotherCategory()
    {
        assertFalse(unlocked("minecraft:raw_iron", "create:blaze_burner", "create:blaze_cake")
                .contains(CRUSHED_IRON));
    }

    @Test
    void categoriesGateIndependentlyOfEachOther()
    {
        Set<String> withWheels = unlocked("minecraft:raw_iron", "create:crushing_wheel",
                "minecraft:copper_ingot");

        assertTrue(withWheels.contains(CRUSHED_IRON));
        assertFalse(withWheels.contains(COPPER_SHEET));

        Set<String> withPress = unlocked("minecraft:raw_iron", "create:mechanical_press",
                "minecraft:copper_ingot");

        assertFalse(withPress.contains(CRUSHED_IRON));
        assertTrue(withPress.contains(COPPER_SHEET));
    }

    @Test
    void aFullyEquippedPlayerUnlocksTheWholeModelledTree()
    {
        Set<String> everything = unlocked("minecraft:andesite", "minecraft:iron_nugget",
                "create:zinc_nugget", "create:andesite_alloy", "minecraft:oak_planks",
                "minecraft:copper_ingot", "create:zinc_ingot", "minecraft:cobblestone",
                "minecraft:raw_iron", "create:basin", "create:mechanical_mixer",
                "create:crushing_wheel", "create:mechanical_press", "create:blaze_burner",
                "create:blaze_cake");

        assertEquals(Set.of(ANDESITE_ALLOY, COGWHEEL, BRASS_INGOT, LAVA, CRUSHED_IRON, COPPER_SHEET),
                everything);
    }

    @Test
    void unlockingIsMonotonicAsItemsAccumulate()
    {
        List<String> pickups = List.of("minecraft:andesite", "minecraft:iron_nugget", "create:basin",
                "minecraft:copper_ingot", "create:zinc_ingot", "create:blaze_burner",
                "minecraft:cobblestone", "create:blaze_cake");

        Set<String> previous = Set.of();
        for (int held = 0; held <= pickups.size(); held++)
        {
            Set<String> now = unlocked(pickups.subList(0, held).toArray(String[]::new));
            assertTrue(now.containsAll(previous), "unlocked set shrank after obtaining an item");
            previous = now;
        }
    }

    @Test
    void progressionDisabledShowsEverythingAtOnce()
    {
        Set<String> all = Unlocks.unlockedOutputs(ProgressionRules.OPEN, createIndex(), Set.of());

        assertEquals(Set.of(ANDESITE_ALLOY, COGWHEEL, BRASS_INGOT, LAVA, CRUSHED_IRON, COPPER_SHEET), all);
    }

    private static Set<ResourceLocation> obtained(String... ids)
    {
        return Arrays.stream(ids).map(ResourceLocation::parse).collect(Collectors.toSet());
    }

    private static InputSlot any(String... ids)
    {
        return InputSlot.ofItems(obtained(ids));
    }

    // A fluid slot as the collector builds one: the fluid's identity, plus its bucket folded in
    // with the items so having one in hand also satisfies the slot.
    // A potion fluid slot: one subtype of create:potion, with no bucket to shortcut it.
    private static InputSlot potion(String subtype)
    {
        String key = subtype.isEmpty() ? POTION_FLUID : POTION_FLUID + "|" + subtype;
        return new InputSlot(Set.of(), Set.of(key));
    }

    private static InputSlot fluid(String fluidId, String bucketId)
    {
        return new InputSlot(Set.of(ResourceLocation.parse(bucketId)), Set.of("fluid|" + fluidId));
    }

    @SafeVarargs
    private static List<InputSlot> slots(InputSlot... slots)
    {
        return List.of(slots);
    }

    // A basin recipe as the index records one: its own slots plus whatever the heat condition adds,
    // through the same mapping production uses.
    @SafeVarargs
    private static List<InputSlot> basin(String heat, InputSlot... slots)
    {
        List<InputSlot> all = new ArrayList<>(List.of(slots));
        all.addAll(HeatRequirement.slotsFor(heat));
        return List.copyOf(all);
    }

    private static final class Index
    {
        private final Map<String, Map<Object, RecipeNode>> byCategory = new HashMap<>();
        private final Map<String, Set<ResourceLocation>> catalysts = new HashMap<>();

        Index catalysts(String category, String... items)
        {
            catalysts.put(category, obtained(items));
            return this;
        }

        Index recipe(String id, String category, String output, List<InputSlot> inputs)
        {
            byCategory.computeIfAbsent(category, key -> new HashMap<>())
                    .put(id, new RecipeNode(category, inputs, Set.of(output)));
            return this;
        }

        RecipeIndex build()
        {
            return RecipeIndex.of(byCategory, catalysts);
        }
    }
}
