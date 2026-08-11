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

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.server.MinecraftServer;
import net.neoforged.fml.loading.FMLPaths;

// The bookmark file and the "which world is this?" question, kept apart from Bookmarks so the
// data stays testable. Bookmarks are a client convenience, so nothing here talks to the server.
public final class BookmarkStore
{
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FILE_NAME = "craftbound-bookmarks.json";

    private static Bookmarks bookmarks = null;

    public static List<String> current()
    {
        return load().of(worldKey());
    }

    public static boolean contains(String uid)
    {
        return load().contains(worldKey(), uid);
    }

    public static boolean toggle(String uid)
    {
        boolean added = load().toggle(worldKey(), uid);
        save();
        return added;
    }

    public static void remove(String uid)
    {
        load().remove(worldKey(), uid);
        save();
    }

    // Singleplayer keys off the save's name, multiplayer off the server address. Anything else
    // (Realms, an unusual connection) shares one bucket rather than losing its bookmarks.
    private static String worldKey()
    {
        Minecraft minecraft = Minecraft.getInstance();
        MinecraftServer server = minecraft.getSingleplayerServer();
        if (server != null)
            return "world/" + server.getWorldData().getLevelName();

        ServerData serverData = minecraft.getCurrentServer();
        return serverData != null ? "server/" + serverData.ip : "unknown";
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
