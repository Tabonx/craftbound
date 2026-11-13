package com.craftbound;

import com.craftbound.client.progression.ClientObtainedItems;
import com.craftbound.client.upgrade.ClientBookUpgrade;
import com.craftbound.placement.PlaceRecipeHandler;
import com.craftbound.placement.PlaceRecipePayload;
import com.craftbound.upgrade.BookUpgradePayload;
import com.craftbound.upgrade.UnbindLensHandler;
import com.craftbound.upgrade.UnbindLensPayload;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

@EventBusSubscriber(modid = Craftbound.MODID)
public final class CraftboundNetwork
{
    @SubscribeEvent
    public static void onRegisterPayloads(final RegisterPayloadHandlersEvent event)
    {
        // Optional, so a server without Craftbound still accepts the client: NeoForge refuses any
        // connection that is missing a required payload. RecipePlacer falls back to the vanilla
        // packet when the channel is absent.
        event.registrar("1").optional()
                .playToServer(PlaceRecipePayload.TYPE, PlaceRecipePayload.STREAM_CODEC, PlaceRecipeHandler::handle)
                .playToServer(UnbindLensPayload.TYPE, UnbindLensPayload.STREAM_CODEC, UnbindLensHandler::handle)
                .playToClient(ObtainedItemsPayload.TYPE, ObtainedItemsPayload.STREAM_CODEC,
                        CraftboundNetwork::receiveObtainedItems)
                .playToClient(BookUpgradePayload.TYPE, BookUpgradePayload.STREAM_CODEC,
                        CraftboundNetwork::receiveBookUpgrade);
    }

    // The client classes are named inside a method rather than by the registration itself, so a
    // dedicated server never loads them.
    private static void receiveObtainedItems(ObtainedItemsPayload payload, IPayloadContext context)
    {
        ClientObtainedItems.accept(payload.items());
    }

    private static void receiveBookUpgrade(BookUpgradePayload payload, IPayloadContext context)
    {
        ClientBookUpgrade.accept(payload.bound());
    }

    private CraftboundNetwork() {}
}
