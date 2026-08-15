package com.craftbound.upgrade;

import com.craftbound.Craftbound;
import com.craftbound.CraftboundAttachments;
import com.craftbound.CraftboundItems;
import com.craftbound.upgrade.BookUpgrade.DeathOutcome;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

// Death handling for the book upgrade. The lens is not carried in the inventory once bound, so it
// has to be dropped by hand; the attachment is not copied on death for the same reason, which
// leaves the surviving copy to be made here.
@EventBusSubscriber(modid = Craftbound.MODID)
public final class BookUpgradeEvents
{
    @SubscribeEvent
    public static void onDrops(final LivingDropsEvent event)
    {
        if (!(event.getEntity() instanceof ServerPlayer player))
            return;

        DeathOutcome outcome = outcomeFor(player);
        if (!outcome.dropLens())
            return;

        event.getDrops().add(new ItemEntity(player.level(), player.getX(), player.getY(), player.getZ(),
                new ItemStack(CraftboundItems.BOOKBINDERS_LENS.get())));
    }

    // Clone also fires on the way back from the End, where nothing was lost and the upgrade simply
    // carries over.
    @SubscribeEvent
    public static void onClone(final PlayerEvent.Clone event)
    {
        if (!(event.getOriginal() instanceof ServerPlayer original)
                || !(event.getEntity() instanceof ServerPlayer clone))
            return;

        boolean bound = original.getData(CraftboundAttachments.BOOK_UPGRADED);
        boolean keep = event.isWasDeath() ? outcomeFor(original).keepBound() : bound;
        if (keep)
            clone.setData(CraftboundAttachments.BOOK_UPGRADED, true);
    }

    private static DeathOutcome outcomeFor(ServerPlayer player)
    {
        return BookUpgrade.onDeath(player.getData(CraftboundAttachments.BOOK_UPGRADED),
                player.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY));
    }

    private BookUpgradeEvents() {}
}
