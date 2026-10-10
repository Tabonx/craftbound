package com.craftbound.client.progression;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.craftbound.Craftbound;
import com.craftbound.client.JsonFile;
import com.craftbound.client.WorldKey;
import com.craftbound.client.jei.CraftboundJeiPlugin;
import com.craftbound.progression.UnlockKey;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;

// Which variants of an item the player has held, such as which potion. The server records item ids
// only, since it has no JEI to say which components make a variant, so the client tracks these
// itself, per world, from its own view of the inventory.
//
// A variant's key holds JEI's subtype, which is only stable within one session, so what is saved is
// one stack of each variant, and the keys are worked out again from those once JEI is up.
@EventBusSubscriber(modid = Craftbound.MODID, value = Dist.CLIENT)
public final class ClientHeldVariants
{
    private static final String FILE_NAME = "craftbound-held-variants.json";
    static final Codec<Map<String, List<ItemStack>>> CODEC =
            Codec.unboundedMap(Codec.STRING, ItemStack.CODEC.listOf());
    private static final int SCAN_INTERVAL_TICKS = 20;
    private static final int SAVE_INTERVAL_TICKS = 200;

    private static JsonFile<Map<String, List<ItemStack>>> file = null;
    private static Map<String, List<ItemStack>> stacksByWorld = null;
    private static String keysWorld = null;
    private static Set<String> keys = Set.of();
    private static boolean dirty = false;

    public static Set<String> current()
    {
        if (Minecraft.getInstance().player == null || !CraftboundJeiPlugin.hasRuntime())
            return Set.of();

        String world = WorldKey.current();
        if (!world.equals(keysWorld))
        {
            keys = new HashSet<>();
            for (ItemStack stack : stacks(world))
                variantKey(stack).ifPresent(keys::add);
            keysWorld = world;
        }
        return keys;
    }

    @SubscribeEvent
    public static void onClientTick(final ClientTickEvent.Post event)
    {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || player.tickCount % SCAN_INTERVAL_TICKS != 0 || !CraftboundJeiPlugin.hasRuntime())
            return;

        Set<String> known = current();
        List<ItemStack> stacks = stacks(keysWorld);
        for (ItemStack stack : held(player))
        {
            Optional<String> key = variantKey(stack);
            if (key.isPresent() && known.add(key.get()))
            {
                stacks.add(stack.copyWithCount(1));
                dirty = true;
            }
        }
        if (player.tickCount % SAVE_INTERVAL_TICKS == 0)
            flush();
    }

    @SubscribeEvent
    public static void onLoggingOut(final ClientPlayerNetworkEvent.LoggingOut event)
    {
        flush();
        stacksByWorld = null;
        keysWorld = null;
        keys = Set.of();
    }

    private static List<ItemStack> held(LocalPlayer player)
    {
        List<ItemStack> held = new ArrayList<>();
        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++)
            held.add(inventory.getItem(slot));
        held.add(player.containerMenu.getCarried());
        return held;
    }

    // Present only for stacks JEI tells apart from the rest of their item.
    private static Optional<String> variantKey(ItemStack stack)
    {
        return CraftboundJeiPlugin.unlockKeyOf(stack)
                .filter(key -> !key.equals(UnlockKey.withoutSubtype(key)));
    }

    private static List<ItemStack> stacks(String world)
    {
        if (stacksByWorld == null)
        {
            stacksByWorld = new HashMap<>();
            ops().ifPresent(ops -> file().read(ops)
                    .forEach((key, stacks) -> stacksByWorld.put(key, new ArrayList<>(stacks))));
        }
        return stacksByWorld.computeIfAbsent(world, unused -> new ArrayList<>());
    }

    private static void flush()
    {
        if (!dirty || stacksByWorld == null)
            return;
        ops().ifPresent(ops ->
        {
            file().write(ops, Map.copyOf(stacksByWorld));
            dirty = false;
        });
    }

    // Item stacks name their components through the world's registries.
    private static Optional<DynamicOps<JsonElement>> ops()
    {
        var level = Minecraft.getInstance().level;
        return level == null ? Optional.empty()
                : Optional.of(level.registryAccess().createSerializationContext(JsonOps.INSTANCE));
    }

    private static JsonFile<Map<String, List<ItemStack>>> file()
    {
        if (file == null)
            file = new JsonFile<>(FMLPaths.CONFIGDIR.get().resolve(FILE_NAME), CODEC, Map::of);
        return file;
    }

    private ClientHeldVariants() {}
}
