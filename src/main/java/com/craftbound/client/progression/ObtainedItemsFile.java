package com.craftbound.client.progression;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;

import org.slf4j.Logger;

// Reading and writing the client-tracked obtained items. Takes the path rather than resolving it,
// so the whole read/write path can be exercised against a temp directory.
//
// Never throws: a file that cannot be read or written costs the player their client-side unlock
// history, which is worth a log line, not a crash mid-game.
public final class ObtainedItemsFile
{
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final Path path;

    public ObtainedItemsFile(Path path)
    {
        this.path = path;
    }

    public ObtainedItemsByWorld read()
    {
        if (!Files.exists(path))
            return new ObtainedItemsByWorld();

        try
        {
            JsonElement json = JsonParser.parseString(Files.readString(path));
            return ObtainedItemsByWorld.CODEC.parse(JsonOps.INSTANCE, json)
                    .resultOrPartial(error -> LOGGER.warn("Ignoring malformed obtained items in {}: {}", path, error))
                    .map(ObtainedItemsByWorld::fromMap)
                    .orElseGet(ObtainedItemsByWorld::new);
        }
        catch (IOException | RuntimeException e)
        {
            LOGGER.warn("Could not read {}, starting with nothing obtained", path, e);
            return new ObtainedItemsByWorld();
        }
    }

    // Written beside the real file and moved into place, so a crash midway leaves the previous
    // contents intact rather than a truncated file, which reads back as no progression at all.
    public void write(ObtainedItemsByWorld obtained)
    {
        ObtainedItemsByWorld.CODEC.encodeStart(JsonOps.INSTANCE, obtained.asMap())
                .resultOrPartial(error -> LOGGER.error("Could not encode obtained items: {}", error))
                .ifPresent(json ->
                {
                    Path temp = path.resolveSibling(path.getFileName() + ".tmp");
                    try
                    {
                        Files.writeString(temp, GSON.toJson(json));
                        Files.move(temp, path, StandardCopyOption.REPLACE_EXISTING);
                    }
                    catch (IOException e)
                    {
                        LOGGER.error("Could not write obtained items to {}", path, e);
                        deleteQuietly(temp);
                    }
                });
    }

    private static void deleteQuietly(Path path)
    {
        try
        {
            Files.deleteIfExists(path);
        }
        catch (IOException e)
        {
            LOGGER.warn("Could not remove leftover {}", path, e);
        }
    }
}
