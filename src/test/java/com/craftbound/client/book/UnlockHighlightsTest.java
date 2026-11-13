package com.craftbound.client.book;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class UnlockHighlightsTest
{
    @Test
    void anEntrySwellsAndSettlesOverFifteenTicks()
    {
        UnlockHighlights<String> highlights = new UnlockHighlights<String>();
        highlights.start("oak");
        assertEquals(1f, highlights.scale("oak"), 1e-6);
        highlights.advance(UnlockHighlights.TICKS / 2);
        assertEquals(1.1f, highlights.scale("oak"), 1e-6);
        highlights.advance(UnlockHighlights.TICKS / 2);
        assertFalse(highlights.active("oak"));
        assertEquals(1f, highlights.scale("oak"));
    }

    @Test
    void readingTwiceInAFrameDoesNotSpeedItUp()
    {
        UnlockHighlights<String> highlights = new UnlockHighlights<String>();
        highlights.start("oak");
        highlights.advance(5f);
        float first = highlights.scale("oak");
        assertEquals(first, highlights.scale("oak"));
        assertTrue(highlights.active("oak"));
    }

    @Test
    void untouchedEntriesNeverSwell()
    {
        assertEquals(1f, new UnlockHighlights<String>().scale("stone"));
    }
}
