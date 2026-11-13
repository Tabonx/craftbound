package com.craftbound.client;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import net.minecraft.resources.ResourceLocation;

class SlotSpritesTest
{
    @Test
    void plainEntriesUseVanillasSlots()
    {
        assertEquals(ResourceLocation.withDefaultNamespace("recipe_book/slot_craftable"),
                SlotSprites.of(false, false, true));
        assertEquals(ResourceLocation.withDefaultNamespace("recipe_book/slot_uncraftable"),
                SlotSprites.of(false, false, false));
        assertEquals(ResourceLocation.withDefaultNamespace("recipe_book/slot_many_craftable"),
                SlotSprites.of(true, false, true));
        assertEquals(ResourceLocation.withDefaultNamespace("recipe_book/slot_many_uncraftable"),
                SlotSprites.of(true, false, false));
    }

    @Test
    void entriesThatUnlockMoreUseTheTintedCopies()
    {
        assertEquals(ResourceLocation.fromNamespaceAndPath("craftbound", "recipe_book/slot_craftable"),
                SlotSprites.of(false, true, true));
        assertEquals(ResourceLocation.fromNamespaceAndPath("craftbound", "recipe_book/slot_many_uncraftable"),
                SlotSprites.of(true, true, false));
    }
}
