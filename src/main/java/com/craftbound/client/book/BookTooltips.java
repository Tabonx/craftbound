package com.craftbound.client.book;

import java.util.function.Supplier;

import com.craftbound.client.ScopedFlag;

// Whether the tooltip being built right now is for one of the book's own entries. Mods that work off
// item tooltips, such as Ponder's hold-to-ponder shortcut, follow an item anywhere it is drawn, and
// this is the only way to tell the book's entries apart from the inventory slots on the same screen.
public final class BookTooltips
{
    private static final ScopedFlag BUILDING = new ScopedFlag();

    public static <T> T whileBuilding(Supplier<T> tooltip)
    {
        return BUILDING.during(tooltip);
    }

    public static boolean building()
    {
        return BUILDING.active();
    }

    private BookTooltips() {}
}
