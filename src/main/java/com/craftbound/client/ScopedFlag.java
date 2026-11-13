package com.craftbound.client;

import java.util.function.Supplier;

// A flag that is up while a stretch of work runs, for a mixin to ask about. Counted rather than
// set, so a scope opened inside another leaves the flag up for the rest of the outer one.
public final class ScopedFlag
{
    private int depth = 0;

    public <T> T during(Supplier<T> work)
    {
        depth++;
        try
        {
            return work.get();
        }
        finally
        {
            depth--;
        }
    }

    public void during(Runnable work)
    {
        during(() ->
        {
            work.run();
            return null;
        });
    }

    public boolean active()
    {
        return depth > 0;
    }
}
