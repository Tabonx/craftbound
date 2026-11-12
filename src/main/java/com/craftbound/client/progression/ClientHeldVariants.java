package com.craftbound.client.progression;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import com.craftbound.Craftbound;
import com.craftbound.HeldItems;
import com.craftbound.client.jei.CraftboundJeiPlugin;
import com.craftbound.client.storage.ClientStores;
import com.craftbound.client.storage.WorldKey;
import com.craftbound.client.storage.WorldScopedStore;
import com.craftbound.progression.UnlockKey;
import com.mojang.serialization.Codec;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
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
    static final Codec<List<ItemStack>> CODEC =
            ItemStack.CODEC.listOf().xmap(ArrayList::new, List::copyOf);
    private static final Supplier<WorldScopedStore<List<ItemStack>>> STORE =
            ClientStores.lazy("craftbound-held-variants.json", CODEC, ArrayList::new);

    private static List<ItemStack> keysSource = null;
    private static Set<String> keys = Set.of();

    public static Set<String> current()
    {
        if (Minecraft.getInstance().player == null || !CraftboundJeiPlugin.hasRuntime())
            return Set.of();

        List<ItemStack> stacks = stacks();
        if (stacks != keysSource)
        {
            keys = new HashSet<>();
            for (ItemStack stack : stacks)
                variantKey(stack).ifPresent(keys::add);
            keysSource = stacks;
        }
        return keys;
    }

    @SubscribeEvent
    public static void onClientTick(final ClientTickEvent.Post event)
    {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || player.tickCount % HeldItems.SCAN_INTERVAL_TICKS != 0 || !CraftboundJeiPlugin.hasRuntime())
            return;

        Set<String> known = current();
        for (ItemStack stack : held(player))
        {
            Optional<String> key = variantKey(stack);
            if (key.isPresent() && known.add(key.get()))
            {
                stacks().add(stack.copyWithCount(1));
                STORE.get().markDirty();
            }
        }
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

    private static List<ItemStack> stacks()
    {
        return STORE.get().open(WorldKey.current(), ClientStores.registryOps());
    }

    private ClientHeldVariants() {}
}
