package com.craftbound.client;

import com.craftbound.PlaceRecipePayload;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;

// Whether the server on the other end runs Craftbound. It is optional there, so the client must
// degrade gracefully: place recipes with the vanilla packet and track obtained items itself.
// Singleplayer always counts as supported, since the client is the server.
public final class ServerSupport
{
    public static boolean installed()
    {
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        return connection != null && connection.hasChannel(PlaceRecipePayload.TYPE);
    }

    private ServerSupport() {}
}
