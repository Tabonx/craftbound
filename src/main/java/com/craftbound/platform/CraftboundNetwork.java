package com.craftbound.platform;

import com.craftbound.ClientHooks;
import com.craftbound.Craftbound;
import com.craftbound.obtained.ObtainedItemsPayload;
import com.craftbound.placement.PlaceRecipeHandler;
import com.craftbound.placement.PlaceRecipePayload;
import com.craftbound.upgrade.BookUpgradePayload;
import com.craftbound.upgrade.UnbindLensHandler;
import com.craftbound.upgrade.UnbindLensPayload;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

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
                        (payload, context) -> ClientHooks.get().obtainedItems(payload.items()))
                .playToClient(BookUpgradePayload.TYPE, BookUpgradePayload.STREAM_CODEC,
                        (payload, context) -> ClientHooks.get().bookUpgrade(payload.bound()));
    }

    private CraftboundNetwork() {}
}
