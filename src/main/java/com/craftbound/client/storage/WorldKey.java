package com.craftbound.client.storage;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

public final class WorldKey
{
    // Anything that is neither a save nor a server with an address (Realms, an unusual connection)
    // shares one bucket rather than losing its data.
    public static WorldId current()
    {
        Minecraft minecraft = Minecraft.getInstance();
        MinecraftServer server = minecraft.getSingleplayerServer();
        if (server != null)
            return WorldId.save(server.getWorldPath(LevelResource.ROOT), server.getWorldData().getLevelName());

        ServerData serverData = minecraft.getCurrentServer();
        return serverData != null ? WorldId.server(serverData.ip) : WorldId.UNKNOWN;
    }

    private WorldKey() {}
}
