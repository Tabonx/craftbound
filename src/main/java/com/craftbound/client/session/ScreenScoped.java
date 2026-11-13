package com.craftbound.client.session;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.function.Supplier;

// State that belongs to one open screen: kept while that screen is laid out again, made anew for
// another screen, and dropped once the screen closes. The state may reach the screen, its menu and
// the world freely, since nothing holds it past the screen.
final class ScreenScoped<S>
{
    private final Supplier<S> fresh;
    private Reference<Object> screen = new WeakReference<>(null);
    private S state = null;

    ScreenScoped(Supplier<S> fresh)
    {
        this.fresh = fresh;
    }

    S of(Object screen)
    {
        if (state == null || this.screen.get() != screen)
        {
            this.screen = new WeakReference<>(screen);
            state = fresh.get();
        }
        return state;
    }

    void closed(Object screen)
    {
        if (this.screen.get() == screen)
        {
            this.screen = new WeakReference<>(null);
            state = null;
        }
    }
}
