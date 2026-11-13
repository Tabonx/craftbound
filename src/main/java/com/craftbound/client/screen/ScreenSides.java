package com.craftbound.client.screen;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import com.craftbound.client.book.RecipeBookLayout;

// What each kind of screen draws past its left edge, learned the first time it is shown. The screen
// is laid out before any of that exists, so a sighting that changes the answer lays it out again,
// once per screen, in case what a mod reports moves with the screen itself. Screens are held
// weakly, so a closed one is let go.
final class ScreenSides
{
    private static final Reference<Object> NONE = new WeakReference<>(null);

    private final Map<Class<?>, SideAreas> learned = new HashMap<>();
    private Reference<Object> pending = NONE;
    private Reference<Object> relaidOut = NONE;

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
        if (!found.equals(used) && bookOpen && relaidOut.get() != screen)
            pending = new WeakReference<>(screen);
    }

    // Whether the open screen is due its second layout. Asked once a tick, and a screen that is no
    // longer open by then misses it.
    boolean relayoutDue(Object current)
    {
        Object due = pending.get();
        pending = NONE;
        if (due == null || due != current)
            return false;
        relaidOut = new WeakReference<>(due);
        return true;
    }
}
