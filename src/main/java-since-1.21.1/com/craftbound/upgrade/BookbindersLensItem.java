package com.craftbound.upgrade;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class BookbindersLensItem extends BookbindersLensItemBase
{
    public BookbindersLensItem(Properties properties)
    {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand)
    {
        ItemStack stack = player.getItemInHand(hand);
        if (alreadyBound(player))
            return InteractionResultHolder.pass(stack);

        bind(level, player, stack);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
