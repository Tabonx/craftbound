package com.craftbound.client.screen;

// A press the book took, whose drag and release the screen must not see either: with an item on the
// cursor, a release outside the screen's own bounds throws the item out.
final class ClickCapture
{
    private boolean taken = false;

    void take()
    {
        taken = true;
    }

    boolean holding()
    {
        return taken;
    }

    // Whether the release ends a press the book took.
    boolean release()
    {
        boolean wasTaken = taken;
        taken = false;
        return wasTaken;
    }

    // A screen that closes under a click never sees its release.
    void reset()
    {
        taken = false;
    }
}
