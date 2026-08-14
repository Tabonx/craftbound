package com.craftbound;

import com.craftbound.client.jei.CraftboundRecipeRuntime;
import com.craftbound.client.ponder.CraftboundPonderPlugin;
import com.craftbound.progression.ProgressionConfig;
import com.mojang.logging.LogUtils;

import org.slf4j.Logger;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

// Entry point. The MODID must match the [[mods]] id in META-INF/neoforge.mods.toml.
@Mod(Craftbound.MODID)
public class Craftbound
{
    public static final String MODID = "craftbound";

    private static final Logger LOGGER = LogUtils.getLogger();

    public Craftbound(IEventBus modEventBus, ModContainer modContainer, Dist dist)
    {
        CraftboundAttachments.ATTACHMENT_TYPES.register(modEventBus);
        modContainer.registerConfig(ModConfig.Type.SERVER, ProgressionConfig.SPEC);

        if (dist.isClient())
        {
            CraftboundRecipeRuntime.register(modEventBus);
            // Resolving CraftboundPonderPlugin loads Ponder's classes, so it stays behind the check.
            if (ModList.get().isLoaded("ponder"))
                registerPonderPlugin();
        }
    }

    // Create is optional and its version range has no upper bound, so a Create that moved its
    // Ponder API turns the first touch of these classes into a link error. The book itself does not
    // need Ponder, so the integration is dropped and the mod loads on.
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
}
