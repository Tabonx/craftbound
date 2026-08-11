package com.craftbound;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

// Client -> server: fill the open menu's input slots from the recipe shown in the book.
// placeAll asks for as many crafts as the inventory allows, like shift-clicking a vanilla recipe.
public record PlaceRecipePayload(int containerId, ResourceLocation recipeId, boolean placeAll)
        implements CustomPacketPayload
{
    public static final Type<PlaceRecipePayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Craftbound.MODID, "place_recipe"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PlaceRecipePayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, PlaceRecipePayload::containerId,
                    ResourceLocation.STREAM_CODEC, PlaceRecipePayload::recipeId,
                    ByteBufCodecs.BOOL, PlaceRecipePayload::placeAll,
                    PlaceRecipePayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type()
    {
        return TYPE;
    }
}
