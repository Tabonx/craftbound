package com.craftbound.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// Which recipe slots the player cannot fill from what they have. Slots are matched as a whole
// rather than one by one, so a slot taking oak or birch planks does not use up the only oak plank
// that another slot needs. A slot needing several items is filled one item at a time, and any of
// its alternatives may fill each one, the way Create's basin accepts mixed planks.
public final class MissingInputs<K>
{
    public record Slot<K>(List<K> accepts, int count) {}

    private final List<List<K>> units = new ArrayList<>();
    private final Map<K, Integer> have;
    private final Map<K, List<Integer>> holders = new HashMap<>();

    private MissingInputs(Map<K, Integer> have)
    {
        this.have = have;
    }

    // The indexes of the slots left short.
    public static <K> Set<Integer> of(List<Slot<K>> slots, Map<K, Integer> have)
    {
        MissingInputs<K> matching = new MissingInputs<>(have);
        Set<Integer> missing = new HashSet<>();
        for (int slot = 0; slot < slots.size(); slot++)
        {
            for (int i = 0; i < slots.get(slot).count(); i++)
            {
                matching.units.add(slots.get(slot).accepts());
                if (!matching.place(matching.units.size() - 1, new HashSet<>()))
                    missing.add(slot);
            }
        }
        return missing;
    }

    // Takes a free item, or frees one by moving an earlier slot onto another alternative.
    private boolean place(int unit, Set<K> visited)
    {
        for (K item : units.get(unit))
        {
            if (!visited.add(item))
                continue;

            List<Integer> users = holders.computeIfAbsent(item, unused -> new ArrayList<>());
            if (users.size() < have.getOrDefault(item, 0))
            {
                users.add(unit);
                return true;
            }
            for (int i = 0; i < users.size(); i++)
            {
                if (place(users.get(i), visited))
                {
                    users.set(i, unit);
                    return true;
                }
            }
        }
        return false;
    }
}
