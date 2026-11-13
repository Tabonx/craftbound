package com.craftbound.client;

import java.util.Set;

import com.craftbound.ClientHooks;
import com.craftbound.client.progression.ClientObtainedItems;
import com.craftbound.client.upgrade.ClientBookUpgrade;

import net.minecraft.resources.ResourceLocation;

final class ClientSideHooks implements ClientHooks
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

    @Override
    public boolean bookUpgraded()
    {
        return ClientBookUpgrade.bound();
    }
}
