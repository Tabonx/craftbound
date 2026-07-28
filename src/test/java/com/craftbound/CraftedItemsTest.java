package com.craftbound;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

class CraftedItemsTest
{
    private static ResourceLocation rl(String id)
    {
        return ResourceLocation.parse(id);
    }

    @Test
    void hasUncrafted_trueWhenAnyResultMissing()
    {
        Set<ResourceLocation> crafted = Set.of(rl("minecraft:stick"));
        assertTrue(CraftedItems.hasUncrafted(crafted, List.of(rl("minecraft:torch"))));
        assertTrue(CraftedItems.hasUncrafted(crafted, List.of(rl("minecraft:stick"), rl("minecraft:torch"))));
    }

    @Test
    void hasUncrafted_falseWhenAllResultsCrafted()
    {
        Set<ResourceLocation> crafted = Set.of(rl("minecraft:stick"), rl("minecraft:torch"));
        assertFalse(CraftedItems.hasUncrafted(crafted, List.of(rl("minecraft:stick"))));
        assertFalse(CraftedItems.hasUncrafted(crafted, List.of(rl("minecraft:stick"), rl("minecraft:torch"))));
    }

    @Test
    void hasUncrafted_falseWhenNoRecipes()
    {
        assertFalse(CraftedItems.hasUncrafted(Set.of(), List.of()));
    }

    @Test
    void codec_roundTripsThroughNbt()
    {
        Set<ResourceLocation> original = Set.of(rl("minecraft:stick"), rl("create:cogwheel"));
        Tag encoded = CraftedItems.CODEC.encodeStart(NbtOps.INSTANCE, original).getOrThrow();
        Set<ResourceLocation> decoded = CraftedItems.CODEC.parse(NbtOps.INSTANCE, encoded).getOrThrow();
        assertEquals(original, decoded);
    }
}
