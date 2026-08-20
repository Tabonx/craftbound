package com.craftbound.upgrade;

import com.craftbound.CraftboundAttachments;
import com.craftbound.PlayerState;
import com.craftbound.client.upgrade.BookUpgradeToast;
import com.craftbound.client.upgrade.ClientBookUpgrade;
import com.mojang.logging.LogUtils;

import org.slf4j.Logger;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

// Used once to bind the upgrade into the player's recipe book, which consumes the lens. A second
// lens does nothing while one is already bound, so it is never spent for free.
//
// What using it does is here; what a use reports back is on BookbindersLensItem, since the type
// that carries the answer changed.
public abstract class BookbindersLensItemBase extends Item
{
    private static final Logger LOGGER = LogUtils.getLogger();

    protected BookbindersLensItemBase(Properties properties)
    {
        super(properties);
    }

    // The attachment only exists on the server, so the client answers from what it was last told.
    protected static boolean alreadyBound(Player player)
    {
        return player.level().isClientSide()
                ? boundOnClient()
                : player.getData(CraftboundAttachments.BOOK_UPGRADED);
    }

    private static boolean boundOnClient()
    {
        return ClientBookUpgrade.bound();
    }

    protected static void bind(Level level, Player player, ItemStack stack)
    {
        if (level.isClientSide())
            announce();
        else
        {
            player.setData(CraftboundAttachments.BOOK_UPGRADED, true);
            stack.consume(1, player);
            if (player instanceof ServerPlayer serverPlayer)
                PlayerState.sendBookUpgrade(serverPlayer);
        }

        level.playSound(player, player.blockPosition(), SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    // Kept in its own method so the client-only toast class is loaded on the client alone. Binding
    // the lens is the point of the item, and the toast only says so out loud, so a toast that
    // cannot be loaded is dropped rather than taking the game down with it.
    private static void announce()
    {
        try
        {
            BookUpgradeToast.show();
        }
        catch (LinkageError e)
        {
            LOGGER.error("Book upgrade toast unavailable", e);
        }
    }
}
