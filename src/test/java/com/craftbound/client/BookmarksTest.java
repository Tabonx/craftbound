package com.craftbound.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;

class BookmarksTest
{
    private static final String WORLD = "world/New World";
    private static final String SERVER = "server/play.example.net";
    private static final String TORCH = "item_stack|minecraft:torch";
    private static final String COG = "item_stack|create:cogwheel";

    @Test
    void toggle_addsThenRemoves()
    {
        Bookmarks bookmarks = new Bookmarks();

        assertTrue(bookmarks.toggle(WORLD, TORCH));
        assertTrue(bookmarks.contains(WORLD, TORCH));

        assertFalse(bookmarks.toggle(WORLD, TORCH));
        assertFalse(bookmarks.contains(WORLD, TORCH));
    }

    @Test
    void newBookmarksGoToTheEnd()
    {
        Bookmarks bookmarks = new Bookmarks();
        bookmarks.toggle(WORLD, TORCH);
        bookmarks.toggle(WORLD, COG);

        assertEquals(List.of(TORCH, COG), bookmarks.of(WORLD));
    }

    @Test
    void removingOneKeepsTheOrderOfTheRest()
    {
        Bookmarks bookmarks = new Bookmarks();
        bookmarks.toggle(WORLD, TORCH);
        bookmarks.toggle(WORLD, COG);
        bookmarks.remove(WORLD, TORCH);

        assertEquals(List.of(COG), bookmarks.of(WORLD));
    }

    @Test
    void worldsDoNotShareBookmarks()
    {
        Bookmarks bookmarks = new Bookmarks();
        bookmarks.toggle(WORLD, TORCH);

        assertFalse(bookmarks.contains(SERVER, TORCH));
        assertEquals(List.of(), bookmarks.of(SERVER));
    }

    @Test
    void emptyWorldsAreDropped()
    {
        Bookmarks bookmarks = new Bookmarks();
        bookmarks.toggle(WORLD, TORCH);
        bookmarks.toggle(WORLD, TORCH);

        assertEquals(Map.of(), bookmarks.asMap());
    }

    @Test
    void roundTripsThroughJson()
    {
        Bookmarks bookmarks = new Bookmarks();
        bookmarks.toggle(WORLD, TORCH);
        bookmarks.toggle(WORLD, COG);
        bookmarks.toggle(SERVER, TORCH);

        JsonElement json = Bookmarks.CODEC.encodeStart(JsonOps.INSTANCE, bookmarks.asMap()).getOrThrow();
        Map<String, List<String>> decoded = Bookmarks.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow();

        assertEquals(bookmarks.asMap(), Bookmarks.fromMap(decoded).asMap());
        assertEquals(List.of(TORCH, COG), Bookmarks.fromMap(decoded).of(WORLD));
    }
}
