package com.craftbound.client.progression;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.mojang.serialization.JsonOps;

import com.google.gson.JsonElement;

import net.minecraft.resources.ResourceLocation;

class ObtainedItemsByWorldTest
{
    private static ResourceLocation rl(String id)
    {
        return ResourceLocation.parse(id);
    }

    @Test
    void record_keepsWorldsApart()
    {
        ObtainedItemsByWorld obtained = new ObtainedItemsByWorld();
        assertTrue(obtained.record("server/one", List.of(rl("minecraft:stick"))));

        assertEquals(Set.of(rl("minecraft:stick")), obtained.of("server/one"));
        assertEquals(Set.of(), obtained.of("server/two"));
    }

    @Test
    void record_falseWhenNothingNew()
    {
        ObtainedItemsByWorld obtained = new ObtainedItemsByWorld();
        obtained.record("server/one", List.of(rl("minecraft:stick")));
        assertFalse(obtained.record("server/one", List.of(rl("minecraft:stick"))));
    }

    @Test
    void codec_roundTripsThroughJson()
    {
        ObtainedItemsByWorld obtained = new ObtainedItemsByWorld();
        obtained.record("server/one", List.of(rl("minecraft:stick"), rl("create:cogwheel")));
        obtained.record("world/save", List.of(rl("minecraft:obsidian")));

        JsonElement json = ObtainedItemsByWorld.CODEC.encodeStart(JsonOps.INSTANCE, obtained.asMap()).getOrThrow();
        Map<String, Set<ResourceLocation>> decoded =
                ObtainedItemsByWorld.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow();

        assertEquals(obtained.asMap(), decoded);
    }
}
