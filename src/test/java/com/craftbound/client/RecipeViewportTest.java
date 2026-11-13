package com.craftbound.client;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class RecipeViewportTest
{
    private static final Rect BODY = new Rect(100, 50, 120, 80);

    @Test
    void aSmallRecipeIsCenteredUnscaled()
    {
        RecipeViewport view = RecipeViewport.fit(BODY, -4, -4, 60, 40);
        assertEquals(new RecipeViewport(1f, 130, 70, -4, -4), view);
    }

    @Test
    void aWideRecipeShrinksToTheBodyWidth()
    {
        RecipeViewport view = RecipeViewport.fit(BODY, 0, 0, 240, 40);
        assertEquals(0.5f, view.scale());
        assertEquals(100, view.originX());
        assertEquals(80, view.originY());
    }

    @Test
    void anEmptyRecipeCountsAsOnePixel()
    {
        assertEquals(1f, RecipeViewport.fit(BODY, 0, 0, 0, 0).scale());
    }

    @Test
    void theMouseIsMappedIntoTheRecipesOwnCoordinates()
    {
        RecipeViewport view = RecipeViewport.fit(BODY, -4, -2, 240, 40);
        assertEquals(-4, view.localX(100));
        assertEquals(16, view.localX(110));
        assertEquals(-2 + 20, view.localY(view.originY() + 10));
    }
}
