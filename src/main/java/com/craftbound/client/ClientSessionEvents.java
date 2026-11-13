package com.craftbound.client;

import com.craftbound.Craftbound;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = Craftbound.MODID, value = Dist.CLIENT)
public final class ClientSessionEvents
{
    @SubscribeEvent
    public static void onLoggingIn(final ClientPlayerNetworkEvent.LoggingIn event)
    {
        ClientSession.begin();
    }

    @SubscribeEvent
    public static void onLoggingOut(final ClientPlayerNetworkEvent.LoggingOut event)
    {
        ClientSession.end();
    }

    @SubscribeEvent
    public static void onClientTick(final ClientTickEvent.Post event)
    {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null)
            ClientSession.current().tick(player);
    }

    private ClientSessionEvents() {}
}
