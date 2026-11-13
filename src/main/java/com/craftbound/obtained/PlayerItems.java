package com.craftbound.obtained;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

// What a player has, in the two senses the book asks about. Common, since the server sweeps its
// own players for obtained items and the client does the same on a server without Craftbound.
public final class PlayerItems
{
    // How often the inventory is swept, on either side, and so how often the obtained set can change.
    public static final int SCAN_INTERVAL_TICKS = 20;

    // Everything on the player: the whole inventory, worn armor and the offhand included, and the
    // stack on the cursor. Holding an item anywhere here obtains it.
    public static List<ItemStack> carried(Player player)
    {
        List<ItemStack> stacks = new ArrayList<>();
        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++)
            add(stacks, inventory.getItem(slot));
        add(stacks, player.containerMenu.getCarried());
        return stacks;
    }

    // What a recipe can be made from in this menu, the stacks vanilla's placement counts: the main
    // inventory and a crafting grid, which goes back to the player when cleared. Worn armor, the
    // offhand and the cursor stay out, and so does a furnace's contents, which RecipePlacement
    // explains.
    public static List<ItemStack> craftingPool(Player player, AbstractContainerMenu menu)
    {
        List<ItemStack> stacks = new ArrayList<>();
        for (int slot = 0; slot < Inventory.INVENTORY_SIZE; slot++)
            add(stacks, player.getInventory().getItem(slot));
        for (Slot slot : menu.slots)
            if (slot.container instanceof CraftingContainer)
                add(stacks, slot.getItem());
        return stacks;
    }

    public static List<ResourceLocation> ids(List<ItemStack> stacks)
    {
        return stacks.stream().map(PlayerItems::idOf).toList();
    }

    public static Map<Item, Integer> counts(List<ItemStack> stacks)
    {
        Map<Item, Integer> counts = new HashMap<>();
        for (ItemStack stack : stacks)
            counts.merge(stack.getItem(), stack.getCount(), Integer::sum);
        return counts;
    }

    public static ResourceLocation idOf(ItemStack stack)
    {
        return BuiltInRegistries.ITEM.getKey(stack.getItem());
    }

    private static void add(List<ItemStack> stacks, ItemStack stack)
    {
        if (!stack.isEmpty())
            stacks.add(stack);
    }

    private PlayerItems() {}
}
