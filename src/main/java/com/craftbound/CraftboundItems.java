package com.craftbound;

import com.craftbound.upgrade.BookbindersLensItem;

import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

// The mod's items. Only the book upgrade so far, which is why it sits beside the tools and
// utilities the player already carries rather than in a tab of its own.
public final class CraftboundItems
{
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Craftbound.MODID);

    public static final DeferredItem<Item> BOOKBINDERS_LENS = ITEMS.register("bookbinders_lens",
            () -> new BookbindersLensItem(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));

    public static void addToCreativeTabs(final BuildCreativeModeTabContentsEvent event)
    {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES)
            event.accept(BOOKBINDERS_LENS);
    }

    private CraftboundItems() {}
}
