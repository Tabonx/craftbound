package com.craftbound.upgrade;

import com.craftbound.CraftboundAttachments;
import com.craftbound.CraftboundItems;
import com.craftbound.PlayerState;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

// The lens is not held while bound, so it is handed back here rather than taken from a slot.
public final class UnbindLensHandler
{
    public static void handle(UnbindLensPayload payload, IPayloadContext context)
    {
        if (!(context.player() instanceof ServerPlayer player) || player.isSpectator() || !player.isAlive())
            return;

        player.resetLastActionTime();
        if (!player.getData(CraftboundAttachments.BOOK_UPGRADED))
            return;

        player.setData(CraftboundAttachments.BOOK_UPGRADED, false);
        PlayerState.sendBookUpgrade(player);
        player.getInventory().placeItemBackInInventory(new ItemStack(CraftboundItems.BOOKBINDERS_LENS.get()));
    }

    private UnbindLensHandler() {}
}
