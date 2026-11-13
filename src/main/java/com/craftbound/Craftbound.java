package com.craftbound;

import com.craftbound.platform.CraftboundAttachments;
import com.craftbound.platform.CraftboundConfig;
import com.craftbound.platform.CraftboundItems;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

// Entry point for both sides. The MODID must match the [[mods]] id in META-INF/neoforge.mods.toml.
// The client's own entry point is CraftboundClient.
@Mod(Craftbound.MODID)
public class Craftbound
{
    public static final String MODID = "craftbound";

    public Craftbound(IEventBus modEventBus, ModContainer modContainer)
    {
        CraftboundAttachments.ATTACHMENT_TYPES.register(modEventBus);
        CraftboundItems.ITEMS.register(modEventBus);
        modEventBus.addListener(CraftboundItems::addToCreativeTabs);
        modContainer.registerConfig(ModConfig.Type.SERVER, CraftboundConfig.SPEC);
    }
}
