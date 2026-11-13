package com.craftbound;

import java.util.Set;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import net.minecraft.resources.ResourceLocation;

// How the per-player attachments are saved. Every Minecraft version writes each one as a compound
// with a named field, the only shape NeoForge reads from 1.21.8 on. Before 1.21.8, Craftbound wrote
// the bare value instead, which is still read wherever it can turn up.
public final class PlayerDataFormat
{
    public static final MapCodec<Set<ResourceLocation>> OBTAINED_ITEMS = ObtainedItems.CODEC.fieldOf("items");
    public static final MapCodec<Boolean> BOOK_UPGRADED = Codec.BOOL.fieldOf("upgraded");

    public static final Codec<Set<ResourceLocation>> LEGACY_OBTAINED_ITEMS = ObtainedItems.CODEC;
    public static final Codec<Boolean> LEGACY_BOOK_UPGRADED = Codec.BOOL;

    // Writes the current shape and reads either.
    public static final Codec<Set<ResourceLocation>> OBTAINED_ITEMS_ANY_SHAPE =
            Codec.withAlternative(OBTAINED_ITEMS.codec(), LEGACY_OBTAINED_ITEMS);
    public static final Codec<Boolean> BOOK_UPGRADED_ANY_SHAPE =
            Codec.withAlternative(BOOK_UPGRADED.codec(), LEGACY_BOOK_UPGRADED);

    private PlayerDataFormat() {}
}
