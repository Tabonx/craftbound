package com.craftbound.client.create;

import java.util.Set;

import com.craftbound.client.CraftableItems;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.packager.InventorySummary;
import com.simibubi.create.content.logistics.stockTicker.StockKeeperRequestMenu;

import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;

// What the stock behind a stock keeper can be crafted into, for the book's craftable filter. The
// keeper orders crafts from its stock, not from the player's inventory. The client holds the last
// stock it was sent and replaces it whole on every update, so its identity is its version.
public final class StockKeeperStock
{
    public static boolean isStockKeeper(AbstractContainerMenu menu)
    {
        return menu instanceof StockKeeperRequestMenu;
    }

    public static Set<Item> craftable(AbstractContainerMenu menu)
    {
        InventorySummary stock = stock(menu);
        if (stock == null)
            return Set.of();

        StackedContents contents = new StackedContents();
        for (BigItemStack entry : stock.getStacks())
            contents.accountStack(entry.stack.copyWithCount(entry.count), entry.count);
        return CraftableItems.craftableFrom(contents);
    }

    public static int version(AbstractContainerMenu menu)
    {
        return System.identityHashCode(stock(menu));
    }

    private static InventorySummary stock(AbstractContainerMenu menu)
    {
        return ((StockKeeperRequestMenu) menu).contentHolder.getLastClientsideStockSnapshotAsSummary();
    }

    private StockKeeperStock() {}
}
