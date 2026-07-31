package com.craftbound.client;

// Client-side open/closed state for Craftbound's recipe book. Static so it persists while the
// game runs, the way vanilla remembers its book being open. Starts closed.
public final class RecipeBookState
{
    private static boolean open = false;
    private static boolean filtering = false;

    private RecipeBookState()
    {
    }

    public static boolean isOpen()
    {
        return open;
    }

    public static void toggle()
    {
        open = !open;
    }

    // Whether the browse grid is limited to items craftable right now (the filter button's state).
    public static boolean isFiltering()
    {
        return filtering;
    }

    public static void toggleFiltering()
    {
        filtering = !filtering;
    }
}
