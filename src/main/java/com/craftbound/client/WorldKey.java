package com.craftbound.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.server.MinecraftServer;

// Identifies the world the client is in, so client-side data does not leak between saves.
public final class WorldKey
{
    // Singleplayer keys off the save's name, multiplayer off the server address. Anything else
    // (Realms, an unusual connection) shares one bucket rather than losing its data.
    public static String current()
    {
        Minecraft minecraft = Minecraft.getInstance();
        MinecraftServer server = minecraft.getSingleplayerServer();
        if (server != null)
            return "world/" + server.getWorldData().getLevelName();

        ServerData serverData = minecraft.getCurrentServer();
        return serverData != null ? "server/" + serverData.ip : "unknown";
    }

    private WorldKey() {}
}
