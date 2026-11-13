package com.craftbound.client.create;

import java.util.Set;

import com.craftbound.client.integration.IntegrationGuard;
import com.craftbound.client.placement.CraftableItems;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.packager.InventorySummary;
import com.simibubi.create.content.logistics.stockTicker.StockKeeperRequestMenu;

import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.Item;

// The keeper orders crafts from its stock, not the player's inventory. The client replaces the stock
// whole on every update, so a new one is a new version.
final class StockKeeperStock
{
    private final IntegrationGuard guard;
    private final StockKeeperRequestMenu menu;
    private InventorySummary seen = null;
    private int version = 0;

    StockKeeperStock(IntegrationGuard guard, StockKeeperRequestMenu menu)
    {
        this.guard = guard;
        this.menu = menu;
    }

    Set<Item> craftable()
    {
        StackedContents contents = guard.call(this::contents, () -> null);
        return contents == null ? Set.of() : CraftableItems.craftableFrom(contents);
    }

    int version()
    {
        return guard.call(() ->
        {
            InventorySummary stock = stock();
            if (stock != seen)
            {
                seen = stock;
                version++;
            }
            return version;
        }, () -> version);
    }

    private StackedContents contents()
    {
        InventorySummary stock = stock();
        if (stock == null)
            return null;

        StackedContents contents = new StackedContents();
        for (BigItemStack entry : stock.getStacks())
            contents.accountStack(entry.stack.copyWithCount(entry.count), entry.count);
        return contents;
    }

    private InventorySummary stock()
    {
        return menu.contentHolder.getLastClientsideStockSnapshotAsSummary();
    }
}
