package com.craftbound.client.progression;

import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

import com.craftbound.client.JsonFile;
import com.mojang.serialization.JsonOps;

import net.minecraft.resources.ResourceLocation;

// The client-tracked obtained items on disk.
public final class ObtainedItemsFile
{
    private final JsonFile<Map<String, Set<ResourceLocation>>> file;

    public ObtainedItemsFile(Path path)
    {
        this.file = new JsonFile<>(path, ObtainedItemsByWorld.CODEC, Map::of);
    }

    public ObtainedItemsByWorld read()
    {
        return ObtainedItemsByWorld.fromMap(file.read(JsonOps.INSTANCE));
    }

    public void write(ObtainedItemsByWorld obtained)
    {
        file.write(JsonOps.INSTANCE, obtained.asMap());
    }
}
