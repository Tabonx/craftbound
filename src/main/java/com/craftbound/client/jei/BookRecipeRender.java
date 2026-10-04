package com.craftbound.client.jei;

import java.util.Set;

// Whether the stretch of work currently running is the book drawing a recipe of its own.
//
// The book borrows JEI's category drawables, so anything Craftbound wants to leave out of a recipe
// has to be suppressed while JEI draws it. JEI now runs as its own mod with its own screens, and
// those must keep looking the way JEI drew them, so the suppression is scoped to this flag rather
// than applied wherever the drawable happens to be used.
public final class BookRecipeRender
{
    private static boolean drawing = false;
    private static Set<?> missing = Set.of();

    public static void whileDrawing(Runnable recipe)
    {
        whileDrawing(Set.of(), recipe);
    }

    // The slots to tint as missing are passed in here, since only JEI knows where it draws them.
    public static void whileDrawing(Set<?> missingSlots, Runnable recipe)
    {
        drawing = true;
        missing = missingSlots;
        try
        {
            recipe.run();
        }
        finally
        {
            drawing = false;
            missing = Set.of();
        }
    }

    public static boolean active()
    {
        return drawing;
    }

    public static boolean isMissing(Object slot)
    {
        return drawing && missing.contains(slot);
    }

    private BookRecipeRender() {}
}
