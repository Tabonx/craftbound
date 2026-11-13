package com.craftbound.client;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.function.Supplier;

// What the book remembers. Whether it is open, and whether it lists only what can be made, lasts the
// whole game. What it shows lasts as long as the screen it sits on: that screen is laid out again
// whenever the book opens or closes, the window is resized or a mod's side panel needs room, and
// none of that should lose the ribbon, the page or the open recipe. A screen opened anew starts
// from the top, as vanilla's book does.
public final class BookSession
{
    private static final BookSession INSTANCE = new BookSession(BookState::new);

    private final Supplier<BookState> fresh;
    private boolean open = false;
    private boolean filtering = false;
    private Reference<Object> screen = new WeakReference<>(null);
    private BookState state;

    BookSession(Supplier<BookState> fresh)
    {
        this.fresh = fresh;
    }

    public static BookSession get()
    {
        return INSTANCE;
    }

    public boolean isOpen()
    {
        return open;
    }

    public void toggleOpen()
    {
        open = !open;
    }

    public boolean isFiltering()
    {
        return filtering;
    }

    public void toggleFiltering()
    {
        filtering = !filtering;
    }

    BookState stateFor(Object screen)
    {
        if (state == null || this.screen.get() != screen)
        {
            this.screen = new WeakReference<>(screen);
            state = fresh.get();
        }
        return state;
    }
}
