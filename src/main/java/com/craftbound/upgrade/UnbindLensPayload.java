package com.craftbound.upgrade;

import com.craftbound.Craftbound;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

// Client -> server: pry the lens back out of the book. Carries nothing; the sender is the player.
public record UnbindLensPayload() implements CustomPacketPayload
{
    public static final Type<UnbindLensPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Craftbound.MODID, "unbind_lens"));

    public static final StreamCodec<RegistryFriendlyByteBuf, UnbindLensPayload> STREAM_CODEC =
            StreamCodec.unit(new UnbindLensPayload());

    @Override
    public Type<? extends CustomPacketPayload> type()
    {
        return TYPE;
    }
}
