package com.craftbound.client.jei;

// Whether the stretch of work currently running is the book drawing a recipe of its own.
//
// The book borrows JEI's category drawables, so anything Craftbound wants to leave out of a recipe
// has to be suppressed while JEI draws it. JEI now runs as its own mod with its own screens, and
// those must keep looking the way JEI drew them, so the suppression is scoped to this flag rather
// than applied wherever the drawable happens to be used.
public final class BookRecipeRender
{
    private static boolean drawing = false;

    public static void whileDrawing(Runnable recipe)
    {
        drawing = true;
        try
        {
            recipe.run();
        }
        finally
        {
            drawing = false;
        }
    }

    public static boolean active()
    {
        return drawing;
    }

    private BookRecipeRender() {}
}
