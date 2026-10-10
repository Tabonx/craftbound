package com.craftbound.client;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

// How many of each item the player has to make a recipe from: the main inventory, the cursor, and
// a crafting grid, which goes back to them when cleared. Worn armor and the offhand stay out, as in
// vanilla's recipe book, and so does a furnace's contents, which RecipePlacement explains.
public final class OwnedItems
{
    public static Map<Item, Integer> of(Player player)
    {
        Map<Item, Integer> counts = new HashMap<>();
        for (int slot = 0; slot < Inventory.INVENTORY_SIZE; slot++)
            add(counts, player.getInventory().getItem(slot));
        add(counts, player.containerMenu.getCarried());
        for (Slot slot : player.containerMenu.slots)
            if (slot.container instanceof CraftingContainer)
                add(counts, slot.getItem());
        return counts;
    }

    private static void add(Map<Item, Integer> counts, ItemStack stack)
    {
        if (!stack.isEmpty())
            counts.merge(stack.getItem(), stack.getCount(), Integer::sum);
    }

    private OwnedItems() {}
}
