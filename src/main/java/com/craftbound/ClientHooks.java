package com.craftbound;

import java.util.Set;

import net.minecraft.resources.ResourceLocation;

// What common code needs from the client: somewhere to put the server's packets, and the synced state
// they carried. Common code runs on both sides, so it reaches the client through this, and the client
// entry point installs the real one. A dedicated server keeps the defaults.
public interface ClientHooks
{
    static ClientHooks get()
    {
        return Installed.hooks;
    }

    static void install(ClientHooks hooks)
    {
        Installed.hooks = hooks;
    }

    default void obtainedItems(Set<ResourceLocation> items)
    {
    }

    default void bookUpgrade(boolean bound)
    {
    }

    // Whether the server last said the player's book carries the lens.
    default boolean bookUpgraded()
    {
        return false;
    }

    final class Installed
    {
        private static ClientHooks hooks = new ClientHooks() {};

        private Installed() {}
    }
}
