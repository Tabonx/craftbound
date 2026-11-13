package com.craftbound.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.ref.WeakReference;

import org.junit.jupiter.api.Test;

class ScreenSidesTest
{
    private static final SideAreas TOOLBAR = new SideAreas(20, Integer.MAX_VALUE);

    private final ScreenSides sides = new ScreenSides();

    private static final class Terminal
    {
    }

    @Test
    void aKindOfScreenKeepsWhatWasLearnedFromAnyOfIt()
    {
        sides.learn(new Terminal(), TOOLBAR, true);

        assertEquals(TOOLBAR, sides.of(new Terminal()));
        assertEquals(RecipeBookLayout.GAP + 20, sides.gap(new Terminal()));
        assertEquals(SideAreas.NONE, sides.of(new Object()));
    }

    @Test
    void aNewAnswerLaysTheOpenScreenOutAgainOnTheNextTick()
    {
        Terminal screen = new Terminal();
        sides.learn(screen, TOOLBAR, true);

        assertTrue(sides.relayoutDue(screen));
        assertFalse(sides.relayoutDue(screen));
    }

    @Test
    void theSameAnswerOrAClosedBookLaysNothingOut()
    {
        Terminal screen = new Terminal();
        sides.learn(screen, SideAreas.NONE, true);
        assertFalse(sides.relayoutDue(screen));

        sides.learn(screen, TOOLBAR, false);
        assertFalse(sides.relayoutDue(screen));
    }

    @Test
    void aScreenIsLaidOutAgainOnlyOnce()
    {
        Terminal screen = new Terminal();
        sides.learn(screen, TOOLBAR, true);
        sides.relayoutDue(screen);

        sides.learn(screen, SideAreas.NONE, true);

        assertFalse(sides.relayoutDue(screen));
    }

    @Test
    void aClosedScreenIsNotKeptAlive()
    {
        WeakReference<Terminal> closed = laidOutTwice();

        for (int i = 0; i < 50 && closed.get() != null; i++)
            System.gc();

        assertNull(closed.get());
    }

    private WeakReference<Terminal> laidOutTwice()
    {
        Terminal screen = new Terminal();
        sides.learn(screen, TOOLBAR, true);
        sides.relayoutDue(screen);
        return new WeakReference<>(screen);
    }

    @Test
    void aScreenClosedBeforeTheTickMissesItsLayout()
    {
        Terminal screen = new Terminal();
        sides.learn(screen, TOOLBAR, true);

        assertFalse(sides.relayoutDue(new Terminal()));
        assertFalse(sides.relayoutDue(screen));
    }
}
