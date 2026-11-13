package com.craftbound.client;

import com.craftbound.Craftbound;

import net.minecraft.resources.ResourceLocation;

// The slot behind a grid entry. An entry with variants sits on vanilla's stacked slot, and one worth
// getting hold of, because obtaining it opens recipes the book still hides, on Craftbound's tinted copy.
public final class SlotSprites
{
    public static ResourceLocation of(boolean variants, boolean unlocksMore, boolean craftable)
    {
        String path = "recipe_book/slot_" + (variants ? "many_" : "") + (craftable ? "craftable" : "uncraftable");
        return unlocksMore
                ? ResourceLocation.fromNamespaceAndPath(Craftbound.MODID, path)
                : ResourceLocation.withDefaultNamespace(path);
    }

    private SlotSprites() {}
}
