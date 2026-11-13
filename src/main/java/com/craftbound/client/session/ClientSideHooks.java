package com.craftbound.client.session;

import java.util.Set;

import com.craftbound.platform.ClientHooks;

import net.minecraft.resources.ResourceLocation;

public final class ClientSideHooks implements ClientHooks
{
    @Override
    public void obtainedItems(Set<ResourceLocation> items)
    {
        ClientSession.current().obtained().accept(items);
    }

    @Override
    public void bookUpgrade(boolean bound)
    {
        ClientSession.current().bookUpgrade().accept(bound);
    }

    @Override
    public boolean bookUpgraded()
    {
        return ClientSession.current().bookUpgrade().bound();
    }
}
