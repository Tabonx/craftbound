package com.craftbound.client.jei;

import java.util.Set;

import com.craftbound.client.ScopedFlag;

// Whether the stretch of work currently running is the book drawing a recipe of its own.
//
// The book borrows JEI's category drawables, so anything Craftbound wants to leave out of a recipe
// has to be suppressed while JEI draws it. JEI now runs as its own mod with its own screens, and
// those must keep looking the way JEI drew them, so the suppression is scoped to this flag rather
// than applied wherever the drawable happens to be used.
public final class BookRecipeRender
{
    private static final ScopedFlag DRAWING = new ScopedFlag();
    private static Set<?> missing = Set.of();

    // The slots to tint as missing are passed in here, since only JEI knows where it draws them.
    public static void whileDrawing(Set<?> missingSlots, Runnable recipe)
    {
        Set<?> outer = missing;
        missing = missingSlots;
        try
        {
            DRAWING.during(recipe);
        }
        finally
        {
            missing = outer;
        }
    }

    public static boolean active()
    {
        return DRAWING.active();
    }

    public static boolean isMissing(Object slot)
    {
        return active() && missing.contains(slot);
    }

    private BookRecipeRender() {}
}
