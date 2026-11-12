package com.craftbound;

import com.craftbound.client.progression.ClientObtainedItems;
import com.craftbound.client.upgrade.ClientBookUpgrade;
import com.craftbound.placement.PlaceRecipeHandler;
import com.craftbound.placement.PlaceRecipePayload;
import com.craftbound.upgrade.BookUpgradePayload;
import com.craftbound.upgrade.UnbindLensPayload;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
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
                .playToServer(UnbindLensPayload.TYPE, UnbindLensPayload.STREAM_CODEC, CraftboundNetwork::unbindLens)
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

    // The lens is not held while bound, so it is handed back here rather than taken from a slot.
    private static void unbindLens(UnbindLensPayload payload, IPayloadContext context)
    {
        if (!(context.player() instanceof ServerPlayer player) || player.isSpectator())
            return;

        player.resetLastActionTime();
        if (!player.getData(CraftboundAttachments.BOOK_UPGRADED))
            return;

        player.setData(CraftboundAttachments.BOOK_UPGRADED, false);
        PlayerState.sendBookUpgrade(player);
        player.getInventory().placeItemBackInInventory(
                new ItemStack(CraftboundItems.BOOKBINDERS_LENS.get()));
    }

    private CraftboundNetwork() {}
}
