package com.craftbound.client.jei;

import static com.craftbound.client.jei.RecipeRuntimeReadiness.Action.NONE;
import static com.craftbound.client.jei.RecipeRuntimeReadiness.Action.RESTART;
import static com.craftbound.client.jei.RecipeRuntimeReadiness.Action.START;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class RecipeRuntimeReadinessTest
{
    @Test
    void startsOnlyAfterTagsAndRecipesAreReady()
    {
        RecipeRuntimeReadiness readiness = new RecipeRuntimeReadiness();

        assertEquals(NONE, readiness.recipesReady());
        assertEquals(START, readiness.tagsReady());
    }

    @Test
    void restartsAfterAnotherCompleteReload()
    {
        RecipeRuntimeReadiness readiness = new RecipeRuntimeReadiness();
        readiness.tagsReady();
        readiness.recipesReady();

        assertEquals(NONE, readiness.tagsReady());
        assertEquals(RESTART, readiness.recipesReady());
    }

    @Test
    void resetReportsWhetherRuntimeWasRunning()
    {
        RecipeRuntimeReadiness readiness = new RecipeRuntimeReadiness();
        assertFalse(readiness.reset());

        readiness.tagsReady();
        readiness.recipesReady();
        assertTrue(readiness.reset());
        assertEquals(NONE, readiness.recipesReady());
        assertEquals(START, readiness.tagsReady());
    }
}
