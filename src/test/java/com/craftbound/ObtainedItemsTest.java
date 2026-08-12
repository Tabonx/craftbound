package com.craftbound;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

class ObtainedItemsTest
{
    private static ResourceLocation rl(String id)
    {
        return ResourceLocation.parse(id);
    }

    @Test
    void recordAll_marksItemsHeldWithoutCrafting()
    {
        Set<ResourceLocation> obtained = new HashSet<>();
        assertTrue(ObtainedItems.recordAll(obtained, List.of(rl("minecraft:obsidian"))));
        assertEquals(Set.of(rl("minecraft:obsidian")), obtained);
    }

    @Test
    void recordAll_falseWhenNothingNew()
    {
        Set<ResourceLocation> obtained = new HashSet<>(List.of(rl("minecraft:obsidian")));
        assertFalse(ObtainedItems.recordAll(obtained, List.of(rl("minecraft:obsidian"))));
    }

    @Test
    void codec_roundTripsThroughNbt()
    {
        Set<ResourceLocation> original = Set.of(rl("minecraft:stick"), rl("create:cogwheel"));
        Tag encoded = ObtainedItems.CODEC.encodeStart(NbtOps.INSTANCE, original).getOrThrow();
        Set<ResourceLocation> decoded = ObtainedItems.CODEC.parse(NbtOps.INSTANCE, encoded).getOrThrow();
        assertEquals(original, decoded);
    }
}
