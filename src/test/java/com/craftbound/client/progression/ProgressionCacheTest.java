package com.craftbound.client.progression;

import static com.craftbound.progression.Keys.key;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.craftbound.progression.InputSlot;
import com.craftbound.progression.ProgressionRules;
import com.craftbound.progression.RecipeIndex;
import com.craftbound.progression.RecipeNode;
import com.craftbound.progression.UnlockKey;
import com.craftbound.progression.UnlockRule;

import net.minecraft.resources.ResourceLocation;

class ProgressionCacheTest
{
    private static final String CRAFTING = "minecraft:crafting";
    private static final ProgressionRules RULES =
            new ProgressionRules(true, UnlockRule.ALL_INPUTS, true, Set.of(CRAFTING));
    private static final UnlockKey PLANKS = key("item|minecraft:oak_planks");
    private static final UnlockKey STICK = key("item|minecraft:stick");

    private static final RecipeIndex INDEX = RecipeIndex.of(Map.of(CRAFTING, Map.of(
            "planks", new RecipeNode(CRAFTING, List.of(InputSlot.ofItems(Set.of(id("oak_log")))), Set.of(PLANKS)),
            "stick", new RecipeNode(CRAFTING, List.of(InputSlot.ofItems(Set.of(id("oak_planks")))), Set.of(STICK)))),
            Map.of());

    private Optional<Set<ResourceLocation>> obtained = Optional.empty();
    private Optional<RecipeIndex> index = Optional.empty();
    private final ProgressionCache cache =
            new ProgressionCache(() -> obtained, Set::of, () -> RULES, () -> index);

    @Test
    void theBaselineWaitsForTheObtainedSet()
    {
        index = Optional.of(INDEX);
        cache.refresh();
        assertFalse(cache.ready());

        obtained = Optional.of(Set.of(id("oak_log")));
        cache.refresh();

        assertTrue(cache.ready());
        assertTrue(cache.isUnlocked(PLANKS));
        assertEquals(List.of(), cache.drainNewlyUnlocked());
    }

    @Test
    void theBaselineWaitsForTheIndex()
    {
        obtained = Optional.of(Set.of(id("oak_log")));
        cache.refresh();
        assertFalse(cache.ready());

        index = Optional.of(INDEX);
        cache.refresh();

        assertTrue(cache.ready());
        assertEquals(List.of(), cache.drainNewlyUnlocked());
    }

    @Test
    void whatUnlocksAfterTheBaselineIsNews()
    {
        index = Optional.of(INDEX);
        obtained = Optional.of(Set.of(id("oak_log")));
        cache.refresh();

        obtained = Optional.of(Set.of(id("oak_log"), id("oak_planks")));
        cache.refresh();

        assertEquals(List.of(STICK), cache.drainNewlyUnlocked());
        assertEquals(List.of(), cache.drainNewlyUnlocked());
    }

    @Test
    void aSetGrownInPlaceIsNoticed()
    {
        Set<ResourceLocation> tracked = new HashSet<>(Set.of(id("oak_log")));
        index = Optional.of(INDEX);
        obtained = Optional.of(tracked);
        cache.refresh();

        tracked.add(id("oak_planks"));
        cache.refresh();

        assertTrue(cache.isUnlocked(STICK));
    }

    @Test
    void anotherSetOfTheSameSizeIsNoticed()
    {
        index = Optional.of(INDEX);
        obtained = Optional.of(Set.of(id("dirt")));
        cache.refresh();
        int before = cache.version();

        obtained = Optional.of(Set.of(id("oak_log")));
        cache.refresh();

        assertTrue(cache.version() > before);
        assertTrue(cache.isUnlocked(PLANKS));
    }

    @Test
    void leavingAWorldTakesANewBaseline()
    {
        index = Optional.of(INDEX);
        obtained = Optional.of(Set.of(id("dirt")));
        cache.refresh();

        obtained = Optional.empty();
        cache.refresh();
        assertFalse(cache.ready());

        obtained = Optional.of(Set.of(id("oak_log"), id("oak_planks")));
        cache.refresh();

        assertEquals(List.of(), cache.drainNewlyUnlocked());
    }

    @Test
    void aNewIndexTakesANewBaseline()
    {
        index = Optional.of(INDEX);
        obtained = Optional.of(Set.of(id("oak_log"), id("oak_planks")));
        cache.refresh();

        index = Optional.of(RecipeIndex.of(INDEX.byCategory(), Map.of()));
        cache.refresh();

        assertEquals(List.of(), cache.drainNewlyUnlocked());
    }

    @Test
    void theVersionMovesOnEveryChangeAndOnlyThen()
    {
        index = Optional.of(INDEX);
        obtained = Optional.of(Set.of(id("oak_log")));
        cache.refresh();
        int first = cache.version();

        cache.refresh();
        obtained = Optional.of(Set.of(id("oak_log")));
        cache.refresh();
        assertEquals(first, cache.version());

        obtained = Optional.of(Set.of(id("oak_log"), id("oak_planks")));
        cache.refresh();
        assertEquals(first + 1, cache.version());
    }

    // The toast tick and the open book both poll. Whichever refreshes first, the other still sees it.
    @Test
    void everyReaderSeesAChange()
    {
        index = Optional.of(INDEX);
        obtained = Optional.of(Set.of(id("oak_log")));
        cache.refresh();
        int toastSeen = cache.version();
        int bookSeen = cache.version();

        obtained = Optional.of(Set.of(id("oak_log"), id("oak_planks")));
        cache.refresh();
        boolean toastNoticed = cache.version() != toastSeen;
        cache.refresh();
        boolean bookNoticed = cache.version() != bookSeen;

        assertTrue(toastNoticed);
        assertTrue(bookNoticed);
        assertTrue(cache.takeHighlight(STICK));
    }

    // Ponder asks before JEI has started, when nothing has refreshed yet.
    @Test
    void ponderReadsTheObtainedSetLive()
    {
        ProgressionCache gated = new ProgressionCache(() -> obtained, Set::of, () -> RULES, () -> index);
        gated.refresh();
        obtained = Optional.of(Set.of(id("oak_log")));

        assertTrue(gated.canPonder(id("oak_log")));
        assertFalse(gated.canPonder(id("dirt")));
    }

    private static ResourceLocation id(String path)
    {
        return ResourceLocation.withDefaultNamespace(path);
    }
}
