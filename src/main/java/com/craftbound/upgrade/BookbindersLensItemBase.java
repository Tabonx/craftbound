package com.craftbound.upgrade;

import com.craftbound.ClientHooks;
import com.craftbound.CraftboundAttachments;
import com.craftbound.PlayerState;
import com.craftbound.ServerLevels;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

// Used once to bind the upgrade into the player's recipe book, which consumes the lens. A second
// lens does nothing while one is already bound, so it is never spent for free.
//
// What using it does is here; what a use reports back is on BookbindersLensItem, since the type
// that carries the answer changed.
public abstract class BookbindersLensItemBase extends Item
{
    protected BookbindersLensItemBase(Properties properties)
    {
        super(properties);
    }

    // Whether the use went through. The client answers from what the server last told it, and
    // announces the bind once the server reports it.
    protected static boolean bind(Player player, ItemStack stack)
    {
        if (!(player instanceof ServerPlayer serverPlayer))
            return !ClientHooks.get().bookUpgraded();
        if (serverPlayer.getData(CraftboundAttachments.BOOK_UPGRADED))
            return false;

        serverPlayer.setData(CraftboundAttachments.BOOK_UPGRADED, true);
        stack.consume(1, serverPlayer);
        PlayerState.sendBookUpgrade(serverPlayer);
        ServerLevels.of(serverPlayer).playSound(null, serverPlayer.blockPosition(), SoundEvents.BOOK_PAGE_TURN,
                SoundSource.PLAYERS, 1.0F, 1.0F);
        return true;
    }
}
