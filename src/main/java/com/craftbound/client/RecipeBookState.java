package com.craftbound.client;

// Client-side open/closed state for Craftbound's recipe book. Static so it persists while the
// game runs, the way vanilla remembers its book being open. Starts closed.
public final class RecipeBookState
{
    private static boolean open = false;

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
}
