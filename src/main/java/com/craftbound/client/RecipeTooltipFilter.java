package com.craftbound.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

// The lines of JEI's slot tooltip that talk about mods and tags rather than the thing under the
// cursor: the mod a recipe came from, the recipe id shown while shift is held, the heading of newer
// JEI's ingredient summary, and the tag hint, whose tag name follows on the next line.
public final class RecipeTooltipFilter
{
    private static final Set<String> DROPPED_KEYS = Set.of("jei.tooltip.recipe.by", "jei.tooltip.recipe.id",
            "jei.tooltip.recipe.tooltips.craft.ingredients");
    private static final String TAG_KEY = "jei.tooltip.recipe.tag";

    public static <T> List<T> filter(List<T> lines, Function<? super T, String> translationKey)
    {
        List<T> kept = new ArrayList<>(lines.size());
        for (int i = 0; i < lines.size(); i++)
        {
            String key = translationKey.apply(lines.get(i));
            if (TAG_KEY.equals(key))
                i++;
            else if (!DROPPED_KEYS.contains(key))
                kept.add(lines.get(i));
        }
        return kept;
    }

    private RecipeTooltipFilter() {}
}
