package com.craftbound.client;

import com.craftbound.ClientHooks;
import com.craftbound.Craftbound;
import com.craftbound.client.integration.Integrations;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;

// Entry point for the client alone, so common code never names a client class.
@Mod(value = Craftbound.MODID, dist = Dist.CLIENT)
public final class CraftboundClient
{
    public CraftboundClient()
    {
        ClientHooks.install(new ClientSideHooks());
        Integrations.load();
    }
}
