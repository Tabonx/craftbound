package com.craftbound.client;

import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class BookSessionTest
{
    private final BookSession session = new BookSession(BookState::new);

    @Test
    void layingTheSameScreenOutAgainKeepsWhatTheBookShows()
    {
        Object screen = new Object();
        assertSame(session.stateFor(screen), session.stateFor(screen));
    }

    @Test
    void anotherScreenStartsFromTheTop()
    {
        BookState first = session.stateFor(new Object());
        assertNotSame(first, session.stateFor(new Object()));
    }

    @Test
    void openAndFilteringOutliveTheScreen()
    {
        session.toggleOpen();
        session.toggleFiltering();
        session.stateFor(new Object());
        session.stateFor(new Object());
        assertTrue(session.isOpen());
        assertTrue(session.isFiltering());
    }
}
