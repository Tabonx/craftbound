package com.craftbound.client;

import java.util.Set;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

// What the open screen can make right now, read again only when its version moves. A screen with no
// grid of its own, like a storage terminal, binds nothing and has nothing to filter against.
public final class CraftableTracker<T>
{
    private Supplier<Set<T>> source = null;
    private IntSupplier version = () -> 0;
    private Set<T> craftable = Set.of();
    private int seen = -1;

    public void bind(Supplier<Set<T>> source, IntSupplier version)
    {
        this.source = source;
        this.version = version;
        seen = -1;
    }

    public boolean bound()
    {
        return source != null;
    }

    // Whether what can be made changed. A stock keeper is sent its stock every second, mostly
    // unchanged, and refiltering on each would shuffle the grid for nothing.
    public boolean refresh()
    {
        if (source == null)
            return false;
        int current = version.getAsInt();
        if (current == seen)
            return false;
        seen = current;
        Set<T> fresh = source.get();
        if (fresh.equals(craftable))
            return false;
        craftable = fresh;
        return true;
    }

    public void reload()
    {
        if (source == null)
            return;
        craftable = source.get();
        seen = version.getAsInt();
    }

    public boolean contains(T item)
    {
        return craftable.contains(item);
    }
}
