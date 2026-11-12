package com.craftbound.client.storage;

import java.nio.file.Path;
import java.util.Map;
import java.util.function.Supplier;

import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;

import org.slf4j.Logger;

// One value per world, all kept in one file. Only the world the player is in is decoded. The other
// worlds stay raw JSON and go back to disk untouched, so an entry that does not decode here, such
// as one naming another world's registries or a removed mod's items, is never lost.
//
// The value handed out is live: callers change it in place and call markDirty.
public final class WorldScopedStore<T>
{
    private static final Logger LOGGER = LogUtils.getLogger();

    private final StoreFile file;
    private final Codec<T> codec;
    private final Supplier<T> empty;

    private Map<String, JsonElement> entries = null;
    private String world = null;
    private DynamicOps<JsonElement> ops = null;
    private T value = null;
    private boolean dirty = false;

    public WorldScopedStore(Path path, Codec<T> codec, Supplier<T> empty)
    {
        this.file = new StoreFile(path);
        this.codec = codec;
        this.empty = empty;
    }

    public T open(WorldId id, DynamicOps<JsonElement> ops)
    {
        if (id.key().equals(world))
            return value;

        close();
        entries = file.read();
        world = id.key();
        this.ops = ops;
        JsonElement json = entries.get(world);
        if (json == null && id.legacyKey().isPresent())
        {
            json = entries.remove(id.legacyKey().get());
            dirty = json != null;
        }
        value = json == null ? empty.get() : decode(json);
        return value;
    }

    public void markDirty()
    {
        dirty = world != null;
    }

    public void save()
    {
        if (!dirty)
            return;

        codec.encodeStart(ops, value)
                .resultOrPartial(error -> LOGGER.error("Could not encode the entry for {}: {}", world, error))
                .ifPresent(json ->
                {
                    if (isEmpty(json))
                        entries.remove(world);
                    else
                        entries.put(world, json);
                    file.write(entries);
                });
        dirty = false;
    }

    public void close()
    {
        save();
        entries = null;
        world = null;
        ops = null;
        value = null;
    }

    // A partial result keeps what did decode, and the file is set aside first, since writing that
    // back loses the rest.
    private T decode(JsonElement json)
    {
        return codec.parse(ops, json)
                .resultOrPartial(error ->
                {
                    file.quarantine("the entry for " + world + " did not fully decode: " + error);
                    dirty = true;
                })
                .orElseGet(empty);
    }

    private static boolean isEmpty(JsonElement json)
    {
        return json.isJsonArray() && json.getAsJsonArray().isEmpty()
                || json.isJsonObject() && json.getAsJsonObject().isEmpty();
    }
}
