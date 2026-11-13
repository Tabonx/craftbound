package com.craftbound.client;

import java.util.function.Supplier;

// Whether the tooltip being built right now is for one of the book's own entries. Mods that work off
// item tooltips, such as Ponder's hold-to-ponder shortcut, follow an item anywhere it is drawn, and
// this is the only way to tell the book's entries apart from the inventory slots on the same screen.
public final class BookTooltips
{
    private static boolean building = false;

    public static <T> T whileBuilding(Supplier<T> tooltip)
    {
        building = true;
        try
        {
            return tooltip.get();
        }
        finally
        {
            building = false;
        }
    }

    public static boolean building()
    {
        return building;
    }

    private BookTooltips() {}
}
