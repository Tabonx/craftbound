package com.craftbound.client.storage;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.google.common.base.Suppliers;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;

import net.minecraft.client.Minecraft;

// One session's stores in the config folder. Logging out is the reliable moment to save; a game
// killed outright loses at most the last interval.
public final class ClientStores
{
    private static final int SAVE_INTERVAL_TICKS = 200;

    private final Path folder;
    private final List<WorldScopedStore<?>> stores = new ArrayList<>();

    public ClientStores(Path folder)
    {
        this.folder = folder;
    }

    // Created on first use, so a store nobody asks for never touches the disk.
    public <T> Supplier<WorldScopedStore<T>> lazy(String fileName, Codec<T> codec, Supplier<T> empty)
    {
        return Suppliers.memoize(() ->
        {
            WorldScopedStore<T> store = new WorldScopedStore<>(folder.resolve(fileName), codec, empty);
            stores.add(store);
            return store;
        });
    }

    public void tick(int tickCount)
    {
        if (tickCount % SAVE_INTERVAL_TICKS == 0)
            stores.forEach(WorldScopedStore::save);
    }

    public void close()
    {
        stores.forEach(WorldScopedStore::close);
    }

    // For values that name registry entries, such as item stacks with components. Needs a world.
    public static DynamicOps<JsonElement> registryOps()
    {
        return Minecraft.getInstance().level.registryAccess().createSerializationContext(JsonOps.INSTANCE);
    }
}
