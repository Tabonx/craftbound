package com.craftbound;

import com.craftbound.upgrade.BookUpgradePayload;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

// Sends the per-player attachments to the client that owns them. NeoForge only offers to sync an
// attachment on some of the Minecraft versions the book supports, so the book sends its own
// packets everywhere rather than having two answers to the same question.
//
// The client is told whenever a value changes, and again on every moment that hands the player a
// fresh entity, since the attachments travel with the old one.
@EventBusSubscriber(modid = Craftbound.MODID)
public final class PlayerState
{
    public static void sendObtainedItems(ServerPlayer player)
    {
        send(player, new ObtainedItemsPayload(player.getData(CraftboundAttachments.OBTAINED_ITEMS)));
    }

    public static void sendBookUpgrade(ServerPlayer player)
    {
        send(player, new BookUpgradePayload(player.getData(CraftboundAttachments.BOOK_UPGRADED)));
    }

    // Craftbound is optional on both ends, so a player may be on a client that never registered
    // these. Sending one anyway is an error rather than a no-op, and that client tracks what it has
    // obtained by itself regardless.
    private static void send(ServerPlayer player, CustomPacketPayload payload)
    {
        if (player.connection.hasChannel(payload))
            PacketDistributor.sendToPlayer(player, payload);
    }

    private static void sendAll(ServerPlayer player)
    {
        sendObtainedItems(player);
        sendBookUpgrade(player);
    }

    @SubscribeEvent
    public static void onLoggedIn(final PlayerEvent.PlayerLoggedInEvent event)
    {
        if (event.getEntity() instanceof ServerPlayer player)
            sendAll(player);
    }

    @SubscribeEvent
    public static void onRespawn(final PlayerEvent.PlayerRespawnEvent event)
    {
        if (event.getEntity() instanceof ServerPlayer player)
            sendAll(player);
    }

    @SubscribeEvent
    public static void onChangedDimension(final PlayerEvent.PlayerChangedDimensionEvent event)
    {
        if (event.getEntity() instanceof ServerPlayer player)
            sendAll(player);
    }

    private PlayerState() {}
}
