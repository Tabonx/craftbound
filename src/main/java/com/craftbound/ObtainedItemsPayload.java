package com.craftbound;

import java.util.Set;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

// Server -> client: everything the player has obtained. The book filters client-side, so the client
// needs the whole set rather than the change, and it is sent whenever the set grows.
public record ObtainedItemsPayload(Set<ResourceLocation> items) implements CustomPacketPayload
{
    public static final Type<ObtainedItemsPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Craftbound.MODID, "obtained_items"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ObtainedItemsPayload> STREAM_CODEC =
            StreamCodec.composite(ObtainedItems.STREAM_CODEC, ObtainedItemsPayload::items,
                    ObtainedItemsPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type()
    {
        return TYPE;
    }
}
