package com.craftbound.client.progression;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.craftbound.ObtainedItems;
import com.mojang.serialization.Codec;

import net.minecraft.resources.ResourceLocation;

// The obtained item ids the client tracked itself, kept per world so a server without Craftbound
// does not share progression with the next one. Pure data, unit-testable without launching the game.
public final class ObtainedItemsByWorld
{
    public static final Codec<Map<String, Set<ResourceLocation>>> CODEC =
            Codec.unboundedMap(Codec.STRING, ObtainedItems.CODEC);

    private final Map<String, Set<ResourceLocation>> byWorld = new LinkedHashMap<>();

    public Set<ResourceLocation> of(String world)
    {
        return byWorld.getOrDefault(world, Set.of());
    }

    // Returns whether anything was actually added, so callers can skip a redundant save.
    public boolean record(String world, Collection<ResourceLocation> ids)
    {
        return ObtainedItems.recordAll(byWorld.computeIfAbsent(world, key -> new HashSet<>()), ids);
    }

    public Map<String, Set<ResourceLocation>> asMap()
    {
        return Map.copyOf(byWorld);
    }

    public static ObtainedItemsByWorld fromMap(Map<String, Set<ResourceLocation>> map)
    {
        ObtainedItemsByWorld obtained = new ObtainedItemsByWorld();
        map.forEach((world, ids) -> obtained.byWorld.put(world, new HashSet<>(ids)));
        return obtained;
    }
}
