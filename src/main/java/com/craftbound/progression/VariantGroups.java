package com.craftbound.progression;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

// Which outputs are variants of one another, read off vanilla's recipe-book groups, the same ones
// its book uses to show every chest boat behind one button.
//
// An output made by recipes in several groups joins the largest, so a bed lands with the other beds
// rather than with the recipes that dye one. A group making a single output has nothing to merge.
public final class VariantGroups
{
    public static Map<String, String> of(RecipeIndex index)
    {
        Map<String, Set<String>> outputsByGroup = new HashMap<>();
        index.nodes()
                .filter(node -> !node.group().isEmpty())
                .forEach(node -> outputsByGroup.computeIfAbsent(node.group(), unused -> new HashSet<>())
                        .addAll(node.outputKeys()));

        Comparator<String> preferred = Comparator.<String>comparingInt(group -> outputsByGroup.get(group).size())
                .reversed()
                .thenComparing(Comparator.naturalOrder());
        Map<String, String> groupOf = new HashMap<>();
        outputsByGroup.forEach((group, outputs) ->
        {
            if (outputs.size() < 2)
                return;
            for (String output : outputs)
                groupOf.merge(output, group, (current, other) -> preferred.compare(current, other) <= 0 ? current : other);
        });
        return groupOf;
    }

    private VariantGroups() {}
}
