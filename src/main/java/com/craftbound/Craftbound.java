package com.craftbound;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

// Entry point. The MODID must match the [[mods]] id in META-INF/neoforge.mods.toml.
@Mod(Craftbound.MODID)
public class Craftbound
{
    public static final String MODID = "craftbound";

    public Craftbound(IEventBus modEventBus)
    {
        // Register Craftbound's data attachments (the per-player crafted-items set).
        CraftboundAttachments.ATTACHMENT_TYPES.register(modEventBus);
    }
}
