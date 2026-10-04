package com.craftbound.client;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.function.Supplier;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;

import org.slf4j.Logger;

// A value the client keeps in a JSON file of its own. Takes the path rather than resolving it, so
// the read/write path can be exercised against a temp directory, and the ops per call, since a value
// holding item stacks needs the world's registries to be read at all.
//
// Never throws: a file that cannot be read or written costs the player some client-side history,
// which is worth a log line, not a crash mid-game.
public final class JsonFile<T>
{
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final Path path;
    private final Codec<T> codec;
    private final Supplier<T> empty;

    public JsonFile(Path path, Codec<T> codec, Supplier<T> empty)
    {
        this.path = path;
        this.codec = codec;
        this.empty = empty;
    }

    public T read(DynamicOps<JsonElement> ops)
    {
        if (!Files.exists(path))
            return empty.get();

        try
        {
            JsonElement json = JsonParser.parseString(Files.readString(path));
            return codec.parse(ops, json)
                    .resultOrPartial(error -> LOGGER.warn("Ignoring malformed entries in {}: {}", path, error))
                    .orElseGet(empty);
        }
        catch (IOException | RuntimeException e)
        {
            LOGGER.warn("Could not read {}, starting empty", path, e);
            return empty.get();
        }
    }

    // Written beside the real file and moved into place, so a crash midway leaves the previous
    // contents intact rather than a truncated file, which reads back as nothing at all.
    public void write(DynamicOps<JsonElement> ops, T value)
    {
        codec.encodeStart(ops, value)
                .resultOrPartial(error -> LOGGER.error("Could not encode {}: {}", path, error))
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
                        LOGGER.error("Could not write {}", path, e);
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
