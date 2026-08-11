package com.craftbound.client;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.mojang.serialization.Codec;

// Bookmarked ingredient uids, kept per world so what you are working toward in one save does not
// follow you into another. Insertion-ordered: a new bookmark lands at the end, so adding and
// removing never reshuffles the rail. Pure data, unit-testable without launching the game.
public final class Bookmarks
{
    public static final Codec<Map<String, List<String>>> CODEC =
            Codec.unboundedMap(Codec.STRING, Codec.STRING.listOf());

    private final Map<String, List<String>> byWorld = new LinkedHashMap<>();

    public List<String> of(String world)
    {
        return List.copyOf(byWorld.getOrDefault(world, List.of()));
    }

    public boolean contains(String world, String uid)
    {
        return byWorld.getOrDefault(world, List.of()).contains(uid);
    }

    // Returns whether the uid is bookmarked after the toggle.
    public boolean toggle(String world, String uid)
    {
        List<String> uids = byWorld.computeIfAbsent(world, key -> new ArrayList<>());
        if (uids.remove(uid))
        {
            if (uids.isEmpty())
                byWorld.remove(world);
            return false;
        }
        uids.add(uid);
        return true;
    }

    public void remove(String world, String uid)
    {
        List<String> uids = byWorld.get(world);
        if (uids != null && uids.remove(uid) && uids.isEmpty())
            byWorld.remove(world);
    }

    public Map<String, List<String>> asMap()
    {
        Map<String, List<String>> copy = new LinkedHashMap<>();
        byWorld.forEach((world, uids) -> copy.put(world, List.copyOf(uids)));
        return copy;
    }

    public static Bookmarks fromMap(Map<String, List<String>> map)
    {
        Bookmarks bookmarks = new Bookmarks();
        map.forEach((world, uids) -> bookmarks.byWorld.put(world, new ArrayList<>(uids)));
        return bookmarks;
    }
}
