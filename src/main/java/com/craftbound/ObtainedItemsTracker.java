package com.craftbound;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

// Server-side: records into the player's OBTAINED_ITEMS attachment every item they have held,
// however they got it. Mining, loot and trades count just as much as crafting, so an item the
// player already owns is never flagged as undiscovered in the recipe book.
// Note: "obtained" is keyed by item rather than by exact recipe id, which matches how the recipe
// book groups recipes.
@EventBusSubscriber(modid = Craftbound.MODID)
public class ObtainedItemsTracker
{
    private static final int SCAN_INTERVAL_TICKS = 20;

    @SubscribeEvent
    public static void onItemCrafted(final PlayerEvent.ItemCraftedEvent event)
    {
        if (!(event.getEntity() instanceof ServerPlayer player))
            return;

        ItemStack result = event.getCrafting();
        if (result.isEmpty())
            return;

        record(player, List.of(HeldItems.idOf(result)));
    }

    // There is no single event for every way an item can reach a player, so sweep the inventory
    // periodically instead of chasing pickups, container moves, commands and creative grabs.
    @SubscribeEvent
    public static void onPlayerTick(final PlayerTickEvent.Post event)
    {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.tickCount % SCAN_INTERVAL_TICKS != 0)
            return;

        record(player, HeldItems.of(player));
    }

    private static void record(ServerPlayer player, Collection<ResourceLocation> ids)
    {
        Set<ResourceLocation> obtained = player.getData(CraftboundAttachments.OBTAINED_ITEMS);
        if (ObtainedItems.recordAll(obtained, ids))
        {
            // Re-set the attachment so NeoForge marks it dirty and syncs it to the client.
            player.setData(CraftboundAttachments.OBTAINED_ITEMS, obtained);
        }
    }
}
