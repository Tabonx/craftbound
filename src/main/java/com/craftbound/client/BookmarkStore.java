package com.craftbound.client;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.craftbound.client.storage.ClientStores;
import com.craftbound.client.storage.WorldKey;
import com.craftbound.client.storage.WorldScopedStore;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;

// The bookmarked ingredient uids of the world the player is in, in the order they were added, so
// adding and removing never reshuffles the rail. Bookmarks are a client convenience, so nothing
// here talks to the server.
public final class BookmarkStore
{
    private static final Supplier<WorldScopedStore<List<String>>> STORE = ClientStores.lazy(
            "craftbound-bookmarks.json", Codec.STRING.listOf().xmap(ArrayList::new, List::copyOf), ArrayList::new);

    public static List<String> current()
    {
        return List.copyOf(uids());
    }

    public static boolean contains(String uid)
    {
        return uids().contains(uid);
    }

    public static void toggle(String uid)
    {
        List<String> uids = uids();
        if (!uids.remove(uid))
            uids.add(uid);
        STORE.get().markDirty();
    }

    private static List<String> uids()
    {
        return STORE.get().open(WorldKey.current(), JsonOps.INSTANCE);
    }

    private BookmarkStore() {}
}
