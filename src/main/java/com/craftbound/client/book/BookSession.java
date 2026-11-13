package com.craftbound.client.book;

// Whether the book is open, and whether it lists only what can be made. Both last the whole game,
// the way vanilla remembers its book, so they hold plain values only: what the book shows belongs to
// one screen and lives in the client session (see ScreenScoped).
public final class BookSession
{
    private static final BookSession INSTANCE = new BookSession();

    private boolean open = false;
    private boolean filtering = false;

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
}
