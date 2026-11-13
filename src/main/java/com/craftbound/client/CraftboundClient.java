package com.craftbound.client;

import com.craftbound.Craftbound;
import com.craftbound.CraftboundNetwork;
//? if create {
import com.craftbound.client.ponder.CraftboundPonderPlugin;
//?}
import com.mojang.logging.LogUtils;

import org.slf4j.Logger;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;

// Entry point for the client alone, so common code never names a client class.
@Mod(value = Craftbound.MODID, dist = Dist.CLIENT)
public final class CraftboundClient
{
    private static final Logger LOGGER = LogUtils.getLogger();

    public CraftboundClient()
    {
        CraftboundNetwork.receiveOnClient(new ClientPackets());
        //? if create {
        // Resolving CraftboundPonderPlugin loads Ponder's classes, so it stays behind the check.
        if (ModList.get().isLoaded("ponder"))
            registerPonderPlugin();
        //?}
    }

    // Create is optional and its version range has no upper bound, so a Create that moved its
    // Ponder API turns the first touch of these classes into a link error. The book itself does not
    // need Ponder, so the integration is dropped and the mod loads on.
    //? if create {
    private static void registerPonderPlugin()
    {
        try
        {
            CraftboundPonderPlugin.register();
        }
        catch (LinkageError e)
        {
            LOGGER.error("Ponder integration disabled: this version of Create no longer fits it", e);
        }
    }
    //?}
}
