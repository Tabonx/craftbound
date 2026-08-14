package com.craftbound;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

// The item ids a player currently has on them. Sided the same way on both ends: the server sweeps
// its own players, and on a server without Craftbound the client sweeps its synced copy instead.
public final class HeldItems
{
    public static List<ResourceLocation> of(Player player)
    {
        List<ResourceLocation> ids = new ArrayList<>();
        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++)
        {
            ItemStack stack = inventory.getItem(slot);
            if (!stack.isEmpty())
                ids.add(idOf(stack));
        }

        ItemStack carried = player.containerMenu.getCarried();
        if (!carried.isEmpty())
            ids.add(idOf(carried));

        return ids;
    }

    public static ResourceLocation idOf(ItemStack stack)
    {
        return BuiltInRegistries.ITEM.getKey(stack.getItem());
    }

    private HeldItems() {}
}
