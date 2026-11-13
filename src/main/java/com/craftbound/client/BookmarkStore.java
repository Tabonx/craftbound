package com.craftbound.client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import com.craftbound.client.jei.BookIngredient;
import com.craftbound.client.jei.BookIngredients;
import com.craftbound.client.storage.ClientStores;
import com.craftbound.client.storage.WorldKey;
import com.craftbound.client.storage.WorldScopedStore;
import com.mojang.serialization.JsonOps;

// The bookmarks of the world the player is in. Bookmarks are a client convenience, so nothing here
// talks to the server.
public final class BookmarkStore
{
    private static final Supplier<WorldScopedStore<Bookmarks>> STORE =
            ClientStores.lazy("craftbound-bookmarks.json", Bookmarks.CODEC, Bookmarks::new);

    // contains runs every frame the bookmark button shows, and naming an entry decodes it.
    private static Bookmarks uidsSource = null;
    private static Set<String> uids = Set.of();

    public static List<BookIngredient> resolve(List<BookIngredient> browsable)
    {
        Optional<BookmarkKeys<BookIngredient>> keys = keys();
        if (keys.isEmpty())
            return List.of();

        Map<String, BookIngredient> bySessionUid = new HashMap<>();
        browsable.forEach(ingredient -> bySessionUid.putIfAbsent(ingredient.sessionUid(), ingredient));
        Bookmarks bookmarks = bookmarks();
        if (bookmarks.upgradeLegacy(bySessionUid, keys.get()))
            changed();
        return bookmarks.resolve(bySessionUid, keys.get());
    }

    public static boolean contains(BookIngredient ingredient)
    {
        Bookmarks bookmarks = bookmarks();
        if (bookmarks != uidsSource)
        {
            Optional<BookmarkKeys<BookIngredient>> keys = keys();
            if (keys.isEmpty())
                return false;
            uids = bookmarks.sessionUids(keys.get());
            uidsSource = bookmarks;
        }
        return uids.contains(ingredient.sessionUid());
    }

    public static void toggle(BookIngredient ingredient)
    {
        keys().ifPresent(keys ->
        {
            bookmarks().toggle(ingredient, keys);
            changed();
        });
    }

    private static void changed()
    {
        STORE.get().markDirty();
        uidsSource = null;
    }

    private static Bookmarks bookmarks()
    {
        return STORE.get().open(WorldKey.current(), JsonOps.INSTANCE);
    }

    private static Optional<BookmarkKeys<BookIngredient>> keys()
    {
        return BookIngredients.bookmarkKeys(ClientStores.registryOps());
    }

    private BookmarkStore() {}
}
