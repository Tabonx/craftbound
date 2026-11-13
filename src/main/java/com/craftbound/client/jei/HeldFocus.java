package com.craftbound.client.jei;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

// A slot offering alternatives shows only the ones the player has held, as long as they hold any;
// with none held it shows all, or the recipe would not say what it needs. JEI already narrows a
// slot to whatever a focus names, so every held alternative becomes a focus, and the recipe is
// laid out again only when that hides something.
public final class HeldFocus
{
    public record Narrowing<K>(List<K> focuses, boolean narrowed) {}

    public static <K> Narrowing<K> of(List<List<K>> slots, Predicate<K> held)
    {
        List<K> focuses = new ArrayList<>();
        boolean narrowed = false;
        for (List<K> alternatives : slots)
        {
            List<K> kept = alternatives.stream().filter(held).toList();
            narrowed |= !kept.isEmpty() && kept.size() < alternatives.size();
            focuses.addAll(kept);
        }
        return new Narrowing<>(List.copyOf(focuses), narrowed);
    }

    private HeldFocus() {}
}
