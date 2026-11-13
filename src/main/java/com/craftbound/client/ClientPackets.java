package com.craftbound.client;

import java.util.Set;

import com.craftbound.ClientReceiver;
import com.craftbound.client.progression.ClientObtainedItems;
import com.craftbound.client.upgrade.ClientBookUpgrade;

import net.minecraft.resources.ResourceLocation;

final class ClientPackets implements ClientReceiver
{
    @Override
    public void obtainedItems(Set<ResourceLocation> items)
    {
        ClientObtainedItems.accept(items);
    }

    @Override
    public void bookUpgrade(boolean bound)
    {
        ClientBookUpgrade.accept(bound);
    }
}
