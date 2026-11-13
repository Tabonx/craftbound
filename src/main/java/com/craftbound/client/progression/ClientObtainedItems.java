package com.craftbound.client.progression;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

import com.craftbound.Craftbound;
import com.craftbound.PlayerItems;
import com.craftbound.ObtainedItems;
import com.craftbound.client.ServerSupport;
import com.craftbound.client.storage.ClientStores;
import com.craftbound.client.storage.WorldKey;
import com.craftbound.client.storage.WorldScopedStore;
import com.mojang.serialization.JsonOps;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;

// The client's view of what the player has obtained.
//
// With Craftbound on the server that is simply the synced attachment. Without it nothing syncs, so
// the client sweeps its own copy of the inventory on the same cadence the server would and keeps
// the result per world, since a client-side set would otherwise be lost on every rejoin. Only what
// passes through the inventory can be seen this way, which is the whole picture for anything the
// player actually picks up.
@EventBusSubscriber(modid = Craftbound.MODID, value = Dist.CLIENT)
public final class ClientObtainedItems
{
    private static final Supplier<WorldScopedStore<Set<ResourceLocation>>> STORE =
            ClientStores.lazy("craftbound-obtained.json", ObtainedItems.CODEC, HashSet::new);

    private static Set<ResourceLocation> synced = Set.of();

    public static void accept(Set<ResourceLocation> items)
    {
        synced = Set.copyOf(items);
    }

    public static Set<ResourceLocation> current()
    {
        if (Minecraft.getInstance().player == null)
            return Set.of();

        return ServerSupport.installed() ? synced : tracked();
    }

    @SubscribeEvent
    public static void onClientTick(final ClientTickEvent.Post event)
    {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || player.tickCount % PlayerItems.SCAN_INTERVAL_TICKS != 0 || ServerSupport.installed())
            return;

        if (ObtainedItems.recordAll(tracked(), PlayerItems.ids(PlayerItems.carried(player))))
            STORE.get().markDirty();
    }

    @SubscribeEvent
    public static void onLoggingOut(final ClientPlayerNetworkEvent.LoggingOut event)
    {
        synced = Set.of();
    }

    private static Set<ResourceLocation> tracked()
    {
        return STORE.get().open(WorldKey.current(), JsonOps.INSTANCE);
    }

    private ClientObtainedItems() {}
}
