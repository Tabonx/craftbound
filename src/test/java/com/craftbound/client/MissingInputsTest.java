package com.craftbound.client;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

class MissingInputsTest
{
    @Test
    void marksOnlyTheSlotsNothingFills()
    {
        List<MissingInputs.Slot<String>> torch = List.of(slot("coal"), slot("stick"));

        assertEquals(Set.of(1), MissingInputs.of(torch, Map.of("coal", 1)));
        assertEquals(Set.of(), MissingInputs.of(torch, Map.of("coal", 1, "stick", 1)));
    }

    // Two planks for two slots: having one leaves exactly one slot short.
    @Test
    void countsHowManyOfAnItemThereAre()
    {
        List<MissingInputs.Slot<String>> stick = List.of(slot("planks"), slot("planks"));

        assertEquals(Set.of(1), MissingInputs.of(stick, Map.of("planks", 1)));
    }

    // Filled one slot at a time, the first slot would take the only oak plank and leave the
    // oak-only slot short while a birch plank sat unused.
    @Test
    void movesAnEarlierSlotOntoItsOtherAlternative()
    {
        List<MissingInputs.Slot<String>> slots = List.of(slot("oak", "birch"), slot("oak"));

        assertEquals(Set.of(), MissingInputs.of(slots, Map.of("oak", 1, "birch", 1)));
    }

    @Test
    void aSlotNeedingSeveralTakesMixedAlternatives()
    {
        List<MissingInputs.Slot<String>> slots = List.of(new MissingInputs.Slot<>(List.of("oak", "birch"), 3));

        assertEquals(Set.of(), MissingInputs.of(slots, Map.of("oak", 2, "birch", 1)));
        assertEquals(Set.of(0), MissingInputs.of(slots, Map.of("oak", 2)));
    }

    private static MissingInputs.Slot<String> slot(String... accepts)
    {
        return new MissingInputs.Slot<>(List.of(accepts), 1);
    }
}
