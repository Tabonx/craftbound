package com.craftbound.client.book;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

class RecipeTooltipFilterTest
{
    @Test
    void dropsTheLinesAboutModsAndRecipeIds()
    {
        List<String> lines = List.of("Oak Planks", "jei.tooltip.recipe.by", "jei.tooltip.recipe.id",
                "jei.tooltip.recipe.tooltips.craft.ingredients", "Count: 4");
        assertEquals(List.of("Oak Planks", "Count: 4"), RecipeTooltipFilter.filter(lines, Function.identity()));
    }

    @Test
    void dropsTheTagHintTogetherWithTheTagNameAfterIt()
    {
        List<String> lines = List.of("Any Planks", "jei.tooltip.recipe.tag", "#minecraft:planks", "Shift for more");
        assertEquals(List.of("Any Planks", "Shift for more"), RecipeTooltipFilter.filter(lines, Function.identity()));
    }

    @Test
    void aTagHintOnTheLastLineEndsTheTooltip()
    {
        assertEquals(List.of("Any Planks"),
                RecipeTooltipFilter.filter(List.of("Any Planks", "jei.tooltip.recipe.tag"), Function.identity()));
    }
}
