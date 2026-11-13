package com.craftbound.client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.craftbound.client.jei.BookIngredient;
import com.craftbound.client.jei.BookIngredients;
import com.craftbound.client.jei.JeiSession;
import com.craftbound.client.storage.ClientStores;
import com.craftbound.client.storage.WorldKey;
import com.mojang.serialization.JsonOps;

// The bookmarks of the world the player is in. Bookmarks are a client convenience, so nothing here
// talks to the server.
public final class BookmarkStore
{
    // contains runs every frame the bookmark button shows, and naming an entry decodes it. Session
    // uids last as long as JEI's run does.
    private static Bookmarks uidsSource = null;
    private static int uidsGeneration = -1;
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
        if (bookmarks != uidsSource || uidsGeneration != JeiSession.generation())
        {
            Optional<BookmarkKeys<BookIngredient>> keys = keys();
            if (keys.isEmpty())
                return false;
            uids = bookmarks.sessionUids(keys.get());
            uidsSource = bookmarks;
            uidsGeneration = JeiSession.generation();
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
        ClientSession.current().bookmarks().markDirty();
        uidsSource = null;
    }

    private static Bookmarks bookmarks()
    {
        return ClientSession.current().bookmarks().open(WorldKey.current(), JsonOps.INSTANCE);
    }

    private static Optional<BookmarkKeys<BookIngredient>> keys()
    {
        return BookIngredients.bookmarkKeys(ClientStores.registryOps());
    }

    private BookmarkStore() {}
}
