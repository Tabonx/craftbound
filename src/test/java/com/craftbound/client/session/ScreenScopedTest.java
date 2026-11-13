package com.craftbound.client.session;

import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.lang.ref.WeakReference;

import org.junit.jupiter.api.Test;

class ScreenScopedTest
{
    // A screen's menu points back at the screen, as Create's stock keeper menu does.
    static final class Menu
    {
        Object screen;
    }

    static final class Screen
    {
        final Menu menu = new Menu();

        Screen()
        {
            menu.screen = this;
        }
    }

    // State that reaches its screen through the menu, as a craftable source bound to it would.
    static final class State
    {
        Menu menu;
    }

    private final ScreenScoped<State> states = new ScreenScoped<>(State::new);

    @Test
    void layingTheSameScreenOutAgainKeepsItsState()
    {
        Screen screen = new Screen();
        assertSame(states.of(screen), states.of(screen));
    }

    @Test
    void anotherScreenStartsAfresh()
    {
        State first = states.of(new Screen());
        assertNotSame(first, states.of(new Screen()));
    }

    @Test
    void aClosedScreenStartsAfreshWhenShownAgain()
    {
        Screen screen = new Screen();
        State first = states.of(screen);
        states.closed(screen);
        assertNotSame(first, states.of(screen));
    }

    @Test
    void closingAnotherScreenKeepsTheState()
    {
        Screen screen = new Screen();
        State first = states.of(screen);
        states.closed(new Screen());
        assertSame(first, states.of(screen));
    }

    @Test
    void aClosedScreenIsLetGoEvenWhenItsStateReachesIt() throws InterruptedException
    {
        Screen screen = new Screen();
        states.of(screen).menu = screen.menu;
        states.closed(screen);
        WeakReference<Screen> ref = new WeakReference<>(screen);
        screen = null;
        for (int i = 0; i < 20 && ref.get() != null; i++)
        {
            System.gc();
            Thread.sleep(20);
        }
        assertNull(ref.get());
    }
}
