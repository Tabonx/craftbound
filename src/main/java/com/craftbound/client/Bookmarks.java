package com.craftbound.client;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import com.mojang.serialization.Codec;

// One world's bookmarks, in the order they were added: a new one lands at the end, so adding and
// removing never reshuffles the rail. An entry that names nothing this session, such as an item
// from a removed mod, stays in place, so reinstalling the mod brings it back.
public final class Bookmarks
{
    public static final Codec<Bookmarks> CODEC =
            BookmarkEntry.CODEC.listOf().xmap(Bookmarks::new, Bookmarks::entries);

    private final List<BookmarkEntry> entries;

    public Bookmarks()
    {
        this(List.of());
    }

    private Bookmarks(List<BookmarkEntry> entries)
    {
        this.entries = new ArrayList<>(entries);
    }

    public List<BookmarkEntry> entries()
    {
        return List.copyOf(entries);
    }

    public <I> Set<String> sessionUids(BookmarkKeys<I> keys)
    {
        Set<String> uids = new LinkedHashSet<>();
        entries.forEach(entry -> entry.sessionUid(keys).ifPresent(uids::add));
        return uids;
    }

    // Returns whether the ingredient is bookmarked after the toggle.
    public <I> boolean toggle(I ingredient, BookmarkKeys<I> keys)
    {
        String uid = keys.sessionUid(ingredient);
        if (entries.removeIf(entry -> entry.sessionUid(keys).filter(uid::equals).isPresent()))
            return false;
        entries.add(keys.entryOf(ingredient).orElseGet(() -> BookmarkEntry.legacy(uid)));
        return true;
    }

    public <I> List<I> resolve(Map<String, I> bySessionUid, BookmarkKeys<I> keys)
    {
        return entries.stream()
                .map(entry -> entry.sessionUid(keys))
                .flatMap(Optional::stream)
                .map(bySessionUid::get)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    // Replaces each legacy entry found among the given ingredients with its stable entry, in place.
    // Returns whether any was replaced.
    public <I> boolean upgradeLegacy(Map<String, I> bySessionUid, BookmarkKeys<I> keys)
    {
        boolean upgraded = false;
        for (ListIterator<BookmarkEntry> it = entries.listIterator(); it.hasNext(); )
        {
            Optional<BookmarkEntry> stable = it.next().legacyUid()
                    .map(bySessionUid::get)
                    .flatMap(keys::entryOf);
            if (stable.isPresent())
            {
                it.set(stable.get());
                upgraded = true;
            }
        }
        return upgraded;
    }
}
