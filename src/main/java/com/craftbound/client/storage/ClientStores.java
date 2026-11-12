package com.craftbound.client.storage;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.craftbound.Craftbound;
import com.google.common.base.Suppliers;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;

// The client's stores in the config folder. Logging out is the reliable moment to save; a game
// killed outright loses at most the last interval.
@EventBusSubscriber(modid = Craftbound.MODID, value = Dist.CLIENT)
public final class ClientStores
{
    private static final int SAVE_INTERVAL_TICKS = 200;
    private static final List<WorldScopedStore<?>> STORES = new ArrayList<>();

    // Created on first use, so a store nobody asks for never touches the disk.
    public static <T> Supplier<WorldScopedStore<T>> lazy(String fileName, Codec<T> codec, Supplier<T> empty)
    {
        return Suppliers.memoize(() ->
        {
            WorldScopedStore<T> store = new WorldScopedStore<>(FMLPaths.CONFIGDIR.get().resolve(fileName), codec, empty);
            STORES.add(store);
            return store;
        });
    }

    // For values that name registry entries, such as item stacks with components. Needs a world.
    public static DynamicOps<JsonElement> registryOps()
    {
        return Minecraft.getInstance().level.registryAccess().createSerializationContext(JsonOps.INSTANCE);
    }

    @SubscribeEvent
    public static void onClientTick(final ClientTickEvent.Post event)
    {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null && player.tickCount % SAVE_INTERVAL_TICKS == 0)
            STORES.forEach(WorldScopedStore::save);
    }

    @SubscribeEvent
    public static void onLoggingOut(final ClientPlayerNetworkEvent.LoggingOut event)
    {
        STORES.forEach(WorldScopedStore::close);
    }

    private ClientStores() {}
}
