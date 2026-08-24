package com.craftbound.upgrade;

import com.craftbound.Craftbound;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

// Server -> client: whether the player's book carries the Bookbinder's Lens. The toggle button and
// the unlock marks both read it, so the client is told every time it changes.
public record BookUpgradePayload(boolean bound) implements CustomPacketPayload
{
    public static final Type<BookUpgradePayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Craftbound.MODID, "book_upgrade"));

    public static final StreamCodec<RegistryFriendlyByteBuf, BookUpgradePayload> STREAM_CODEC =
            StreamCodec.composite(ByteBufCodecs.BOOL, BookUpgradePayload::bound, BookUpgradePayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type()
    {
        return TYPE;
    }
}
