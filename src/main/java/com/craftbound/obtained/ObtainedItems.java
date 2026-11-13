package com.craftbound.obtained;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.mojang.serialization.Codec;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

// The item ids a player has ever had.
public final class ObtainedItems
{
    public static final Codec<Set<ResourceLocation>> CODEC =
            ResourceLocation.CODEC.listOf().xmap(list -> new HashSet<ResourceLocation>(list), List::copyOf);

    public static final StreamCodec<RegistryFriendlyByteBuf, Set<ResourceLocation>> STREAM_CODEC =
            ByteBufCodecs.<RegistryFriendlyByteBuf, ResourceLocation, Set<ResourceLocation>>collection(
                    size -> new HashSet<>(), ResourceLocation.STREAM_CODEC);

    // Whether anything was added, so callers can skip a redundant sync.
    public static boolean recordAll(Set<ResourceLocation> obtained, Collection<ResourceLocation> ids)
    {
        return obtained.addAll(ids);
    }

    private ObtainedItems() {}
}
