package com.craftbound.client.progression;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import com.craftbound.client.ServerSupport;
import com.craftbound.client.storage.ClientStores;
import com.craftbound.client.storage.WorldKey;
import com.craftbound.client.storage.WorldScopedStore;
import com.craftbound.obtained.ObtainedItems;
import com.craftbound.obtained.PlayerItems;
import com.mojang.serialization.JsonOps;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;

// With Craftbound on the server this is the synced attachment. Without it nothing syncs, so the
// client sweeps its own inventory on the server's cadence and stores the result per world.
public final class ClientObtainedItems
{
    private final Supplier<WorldScopedStore<Set<ResourceLocation>>> store;
    private Set<ResourceLocation> synced = null;

    public ClientObtainedItems(ClientStores stores)
    {
        this.store = stores.lazy("craftbound-obtained.json", ObtainedItems.CODEC, HashSet::new);
    }

    public void accept(Set<ResourceLocation> items)
    {
        synced = Set.copyOf(items);
    }

    // Empty until the server has sent its set, or while there is no world.
    public Optional<Set<ResourceLocation>> current()
    {
        if (Minecraft.getInstance().player == null)
            return Optional.empty();
        return ServerSupport.installed() ? Optional.ofNullable(synced) : Optional.of(tracked());
    }

    public void tick(LocalPlayer player)
    {
        if (player.tickCount % PlayerItems.SCAN_INTERVAL_TICKS != 0 || ServerSupport.installed())
            return;

        if (ObtainedItems.recordAll(tracked(), PlayerItems.ids(PlayerItems.carried(player))))
            store.get().markDirty();
    }

    private Set<ResourceLocation> tracked()
    {
        return store.get().open(WorldKey.current(), JsonOps.INSTANCE);
    }
}
