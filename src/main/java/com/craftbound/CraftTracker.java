package com.craftbound;

import java.util.Set;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

// Server-side: records the result item of every craft into the player's CRAFTED_ITEMS attachment.
// Note: the craft event carries the result ItemStack, not the recipe, so "crafted" is keyed by
// result item rather than by exact recipe id. That matches how the recipe book groups recipes.
@EventBusSubscriber(modid = Craftbound.MODID)
public class CraftTracker
{
    @SubscribeEvent
    public static void onItemCrafted(final PlayerEvent.ItemCraftedEvent event)
    {
        if (!(event.getEntity() instanceof ServerPlayer player))
            return;

        ItemStack result = event.getCrafting();
        if (result.isEmpty())
            return;

        ResourceLocation id = BuiltInRegistries.ITEM.getKey(result.getItem());
        Set<ResourceLocation> crafted = player.getData(CraftboundAttachments.CRAFTED_ITEMS);
        if (crafted.add(id))
        {
            // Re-set the attachment so NeoForge marks it dirty and syncs it to the client.
            player.setData(CraftboundAttachments.CRAFTED_ITEMS, crafted);
        }
    }
}
