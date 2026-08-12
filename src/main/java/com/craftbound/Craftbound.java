package com.craftbound;

import com.craftbound.client.jei.CraftboundRecipeRuntime;
import com.craftbound.progression.ProgressionConfig;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

// Entry point. The MODID must match the [[mods]] id in META-INF/neoforge.mods.toml.
@Mod(Craftbound.MODID)
public class Craftbound
{
    public static final String MODID = "craftbound";

    public Craftbound(IEventBus modEventBus, ModContainer modContainer, Dist dist)
    {
        CraftboundAttachments.ATTACHMENT_TYPES.register(modEventBus);
        modContainer.registerConfig(ModConfig.Type.SERVER, ProgressionConfig.SPEC);

        if (dist.isClient())
            CraftboundRecipeRuntime.register(modEventBus);
    }
}
