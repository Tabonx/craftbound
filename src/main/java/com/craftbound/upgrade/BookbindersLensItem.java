package com.craftbound.upgrade;

import com.craftbound.CraftboundAttachments;
import com.craftbound.client.upgrade.BookUpgradeToast;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
//? if >=1.21.5 {
/*import net.minecraft.world.InteractionResult;
*///?} else {
import net.minecraft.world.InteractionResultHolder;
//?}
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

// Used once to bind the upgrade into the player's recipe book, which consumes the lens. A second
// lens does nothing while one is already bound, so it is never spent for free.
public class BookbindersLensItem extends Item
{
    public BookbindersLensItem(Properties properties)
    {
        super(properties);
    }

    //? if >=1.21.5 {
    /*@Override
    public InteractionResult use(Level level, Player player, InteractionHand hand)
    {
        ItemStack stack = player.getItemInHand(hand);
        if (player.getData(CraftboundAttachments.BOOK_UPGRADED))
            return InteractionResult.PASS;

        if (level.isClientSide())
            announce();
        else
        {
            player.setData(CraftboundAttachments.BOOK_UPGRADED, true);
            stack.consume(1, player);
        }

        level.playSound(player, player.blockPosition(), SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 1.0F, 1.0F);
        return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER;
    }
    *///?} else {
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand)
    {
        ItemStack stack = player.getItemInHand(hand);
        if (player.getData(CraftboundAttachments.BOOK_UPGRADED))
            return InteractionResultHolder.pass(stack);

        if (level.isClientSide())
            announce();
        else
        {
            player.setData(CraftboundAttachments.BOOK_UPGRADED, true);
            stack.consume(1, player);
        }

        level.playSound(player, player.blockPosition(), SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 1.0F, 1.0F);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
    //?}

    // Kept in its own method so the client-only toast class is loaded on the client alone.
    private static void announce()
    {
        BookUpgradeToast.show();
    }
}
