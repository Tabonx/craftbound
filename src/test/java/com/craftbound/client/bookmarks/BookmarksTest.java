package com.craftbound.client.bookmarks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.JsonOps;

class BookmarksTest
{
    // Ingredients are their registry ids. The session uid carries a launch number, as a fluid's
    // does in JEI, so only the stable entry finds an ingredient again after a restart.
    private record Keys(int launch) implements BookmarkKeys<String>
    {
        @Override
        public String sessionUid(String ingredient)
        {
            return ingredient + "@" + launch;
        }

        @Override
        public Optional<BookmarkEntry> entryOf(String ingredient)
        {
            return ingredient.startsWith("nocodec:") ? Optional.empty()
                    : Optional.of(BookmarkEntry.of("test", new JsonPrimitive(ingredient)));
        }

        @Override
        public Optional<String> sessionUidOf(String typeUid, JsonElement ingredient)
        {
            String id = ingredient.getAsString();
            return typeUid.equals("test") && !id.startsWith("removed:")
                    ? Optional.of(sessionUid(id))
                    : Optional.empty();
        }

        Map<String, String> bySessionUid(String... ingredients)
        {
            Map<String, String> map = new LinkedHashMap<>();
            for (String ingredient : ingredients)
                map.put(sessionUid(ingredient), ingredient);
            return map;
        }
    }

    private static final Keys KEYS = new Keys(1);
    private static final String TORCH = "minecraft:torch";
    private static final String COG = "create:cogwheel";

    private static Bookmarks reload(Bookmarks bookmarks)
    {
        JsonElement json = Bookmarks.CODEC.encodeStart(JsonOps.INSTANCE, bookmarks).getOrThrow();
        return Bookmarks.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow();
    }

    private static Bookmarks read(String json)
    {
        return Bookmarks.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).getOrThrow();
    }

    @Test
    void toggle_addsThenRemoves()
    {
        Bookmarks bookmarks = new Bookmarks();

        assertTrue(bookmarks.toggle(TORCH, KEYS));
        assertEquals(Set.of(KEYS.sessionUid(TORCH)), bookmarks.sessionUids(KEYS));

        assertFalse(bookmarks.toggle(TORCH, KEYS));
        assertEquals(Set.of(), bookmarks.sessionUids(KEYS));
    }

    @Test
    void newBookmarksGoToTheEnd()
    {
        Bookmarks bookmarks = new Bookmarks();
        bookmarks.toggle(TORCH, KEYS);
        bookmarks.toggle(COG, KEYS);

        assertEquals(List.of(TORCH, COG), bookmarks.resolve(KEYS.bySessionUid(COG, TORCH), KEYS));
    }

    @Test
    void removingOneKeepsTheOrderOfTheRest()
    {
        Bookmarks bookmarks = new Bookmarks();
        bookmarks.toggle(TORCH, KEYS);
        bookmarks.toggle(COG, KEYS);
        bookmarks.toggle("minecraft:stick", KEYS);
        bookmarks.toggle(COG, KEYS);

        assertEquals(List.of(TORCH, "minecraft:stick"),
                bookmarks.resolve(KEYS.bySessionUid(TORCH, COG, "minecraft:stick"), KEYS));
    }

    @Test
    void survivesARestartThatChangesSessionUids()
    {
        Bookmarks bookmarks = new Bookmarks();
        bookmarks.toggle(TORCH, KEYS);

        Keys nextLaunch = new Keys(2);
        assertEquals(List.of(TORCH), reload(bookmarks).resolve(nextLaunch.bySessionUid(TORCH), nextLaunch));
    }

    @Test
    void ingredientWithoutAStableFormIsSavedByItsSessionUid()
    {
        Bookmarks bookmarks = new Bookmarks();
        bookmarks.toggle("nocodec:thing", KEYS);

        assertEquals(List.of(new JsonPrimitive(KEYS.sessionUid("nocodec:thing"))),
                bookmarks.entries().stream().map(BookmarkEntry::json).toList());
        assertEquals(List.of("nocodec:thing"), bookmarks.resolve(KEYS.bySessionUid("nocodec:thing"), KEYS));
    }

    @Test
    void readsTheOldStringFormat()
    {
        Bookmarks bookmarks = read("[\"" + KEYS.sessionUid(TORCH) + "\"]");

        assertEquals(List.of(TORCH), bookmarks.resolve(KEYS.bySessionUid(TORCH), KEYS));
        assertFalse(bookmarks.toggle(TORCH, KEYS), "an old entry toggles off like a new one");
    }

    @Test
    void oldEntriesAreUpgradedOnceFound()
    {
        String gone = "item_stack|removedmod:widget";
        Bookmarks bookmarks = read("[\"" + gone + "\", \"" + KEYS.sessionUid(TORCH) + "\"]");

        assertTrue(bookmarks.upgradeLegacy(KEYS.bySessionUid(TORCH), KEYS));
        assertFalse(bookmarks.upgradeLegacy(KEYS.bySessionUid(TORCH), KEYS));

        assertEquals(List.of(BookmarkEntry.legacy(gone), BookmarkEntry.of("test", new JsonPrimitive(TORCH))),
                bookmarks.entries());
    }

    @Test
    void entriesThatNameNothingStayInPlace()
    {
        Bookmarks bookmarks = read("[{\"type\": \"test\", \"ingredient\": \"removed:widget\"}, 7, "
                + "{\"type\": \"test\", \"ingredient\": \"" + COG + "\"}]");

        assertEquals(List.of(COG), bookmarks.resolve(KEYS.bySessionUid(COG), KEYS));
        bookmarks.toggle(TORCH, KEYS);

        assertEquals(4, reload(bookmarks).entries().size());
        assertEquals(new JsonPrimitive(7), bookmarks.entries().get(1).json());
    }
}
