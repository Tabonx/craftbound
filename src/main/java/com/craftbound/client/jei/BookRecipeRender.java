package com.craftbound.client.jei;

import java.util.Set;

import com.craftbound.client.ScopedFlag;

// The book borrows JEI's category drawables, and JEI's own screens must keep looking the way JEI
// drew them, so anything the book leaves out of a recipe is suppressed only while this flag is set.
public final class BookRecipeRender
{
    private static final ScopedFlag DRAWING = new ScopedFlag();
    private static Set<?> missing = Set.of();

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
