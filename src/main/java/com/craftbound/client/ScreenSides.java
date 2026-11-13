package com.craftbound.client;

import java.util.HashMap;
import java.util.Map;

// What each kind of screen draws past its left edge, learned the first time it is shown. The screen
// is laid out before any of that exists, so a sighting that changes the answer lays it out again,
// once per screen, in case what a mod reports moves with the screen itself. Kept free of Minecraft
// types so it can be unit-tested.
final class ScreenSides
{
    private final Map<Class<?>, SideAreas> learned = new HashMap<>();
    private Object pending = null;
    private Object relaidOut = null;

    SideAreas of(Object screen)
    {
        return learned.getOrDefault(screen.getClass(), SideAreas.NONE);
    }

    // Everything between the book and the screen's left edge.
    int gap(Object screen)
    {
        return RecipeBookLayout.GAP + of(screen).reach();
    }

    void learn(Object screen, SideAreas found, boolean bookOpen)
    {
        SideAreas used = of(screen);
        learned.put(screen.getClass(), found);
        if (!found.equals(used) && bookOpen && relaidOut != screen)
            pending = screen;
    }

    // Whether the open screen is due its second layout. Asked once a tick, and a screen that is no
    // longer open by then misses it.
    boolean relayoutDue(Object current)
    {
        Object due = pending;
        pending = null;
        if (due == null || due != current)
            return false;
        relaidOut = due;
        return true;
    }
}
