package com.craftbound;

import java.util.Set;

import net.minecraft.resources.ResourceLocation;

// What the client does with the server's packets. Common code registers the payloads on both sides,
// so it reaches the client through this, and the client entry point installs the real one.
public interface ClientReceiver
{
    ClientReceiver NONE = new ClientReceiver() {};

    default void obtainedItems(Set<ResourceLocation> items)
    {
    }

    default void bookUpgrade(boolean bound)
    {
    }
}
