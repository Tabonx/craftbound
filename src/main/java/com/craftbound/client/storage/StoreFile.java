package com.craftbound.client.storage;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;

import org.slf4j.Logger;

// A JSON object the client keeps in a file of its own. Never throws: a file that cannot be read or
// written costs the player some client-side history, which is worth a log line, not a crash.
//
// A file that does not read cleanly is moved aside before anything is written over it, so the
// player can still recover it. If it cannot be moved, the file is left alone for the session.
public final class StoreFile
{
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH.mm.ss.SSS");

    private final Path path;
    private boolean locked = false;

    public StoreFile(Path path)
    {
        this.path = path;
    }

    public Map<String, JsonElement> read()
    {
        Map<String, JsonElement> entries = new LinkedHashMap<>();
        if (!Files.exists(path))
            return entries;

        try
        {
            JsonElement json = JsonParser.parseString(Files.readString(path));
            if (json.isJsonObject())
                json.getAsJsonObject().entrySet().forEach(entry -> entries.put(entry.getKey(), entry.getValue()));
            else
                quarantine("it does not hold a JSON object");
        }
        catch (IOException | RuntimeException e)
        {
            quarantine(e.toString());
        }
        return entries;
    }

    public void quarantine(String reason)
    {
        if (!Files.exists(path))
            return;

        Path aside = path.resolveSibling(path.getFileName() + ".corrupt-" + LocalDateTime.now().format(STAMP));
        try
        {
            move(path, aside);
            LOGGER.warn("Moved {} aside to {} because {}", path, aside.getFileName(), reason);
        }
        catch (IOException e)
        {
            locked = true;
            LOGGER.error("Could not move {} aside ({}), so it stays untouched until restart", path, reason, e);
        }
    }

    // Written beside the real file and moved into place, so a crash midway leaves the previous
    // contents intact rather than a truncated file.
    public void write(Map<String, JsonElement> entries)
    {
        if (locked)
            return;

        JsonObject json = new JsonObject();
        entries.forEach(json::add);
        Path temp = path.resolveSibling(path.getFileName() + ".tmp");
        try
        {
            Files.writeString(temp, GSON.toJson(json));
            move(temp, path);
        }
        catch (IOException e)
        {
            LOGGER.error("Could not write {}", path, e);
            deleteQuietly(temp);
        }
    }

    private static void move(Path from, Path to) throws IOException
    {
        try
        {
            Files.move(from, to, StandardCopyOption.ATOMIC_MOVE);
        }
        catch (AtomicMoveNotSupportedException e)
        {
            Files.move(from, to, StandardCopyOption.REPLACE_EXISTING);
        }
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
