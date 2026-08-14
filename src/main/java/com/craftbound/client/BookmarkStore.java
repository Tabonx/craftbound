package com.craftbound.client;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;

import org.slf4j.Logger;

import net.neoforged.fml.loading.FMLPaths;

// The bookmark file, kept apart from Bookmarks so the data stays testable. Bookmarks are a client
// convenience, so nothing here talks to the server.
public final class BookmarkStore
{
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FILE_NAME = "craftbound-bookmarks.json";

    private static Bookmarks bookmarks = null;

    public static List<String> current()
    {
        return load().of(WorldKey.current());
    }

    public static boolean contains(String uid)
    {
        return load().contains(WorldKey.current(), uid);
    }

    public static boolean toggle(String uid)
    {
        boolean added = load().toggle(WorldKey.current(), uid);
        save();
        return added;
    }

    public static void remove(String uid)
    {
        load().remove(WorldKey.current(), uid);
        save();
    }

    private static Path file()
    {
        return FMLPaths.CONFIGDIR.get().resolve(FILE_NAME);
    }

    private static Bookmarks load()
    {
        if (bookmarks != null)
            return bookmarks;

        bookmarks = new Bookmarks();
        Path path = file();
        if (Files.exists(path))
        {
            try
            {
                JsonElement json = JsonParser.parseString(Files.readString(path));
                Bookmarks.CODEC.parse(JsonOps.INSTANCE, json)
                        .resultOrPartial(error -> LOGGER.warn("Ignoring malformed bookmarks: {}", error))
                        .ifPresent(map -> bookmarks = Bookmarks.fromMap(map));
            }
            catch (IOException | RuntimeException e)
            {
                LOGGER.warn("Could not read {}, starting with no bookmarks", path, e);
            }
        }
        return bookmarks;
    }

    private static void save()
    {
        Bookmarks.CODEC.encodeStart(JsonOps.INSTANCE, load().asMap())
                .resultOrPartial(error -> LOGGER.error("Could not encode bookmarks: {}", error))
                .ifPresent(json ->
                {
                    try
                    {
                        Files.writeString(file(), GSON.toJson(json));
                    }
                    catch (IOException e)
                    {
                        LOGGER.error("Could not write bookmarks to {}", file(), e);
                    }
                });
    }

    private BookmarkStore() {}
}
