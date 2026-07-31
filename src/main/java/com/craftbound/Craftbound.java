package com.craftbound;

import com.craftbound.client.jei.CraftboundRecipeRuntime;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;

// Entry point. The MODID must match the [[mods]] id in META-INF/neoforge.mods.toml.
@Mod(Craftbound.MODID)
public class Craftbound
{
    public static final String MODID = "craftbound";

    public Craftbound(IEventBus modEventBus, Dist dist)
    {
        CraftboundAttachments.ATTACHMENT_TYPES.register(modEventBus);

        if (dist.isClient())
            CraftboundRecipeRuntime.register(modEventBus);
    }
}
