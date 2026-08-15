package com.craftbound.client.ponder;

import java.util.function.Supplier;

import com.craftbound.client.progression.Progression;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;

// Whether Ponder may be reached from the item currently under the cursor.
//
// Ponder's shortcut works off item tooltips, so it follows an item anywhere it is drawn, the book
// included. An item lying in a chest is fair game: the player is looking at the real thing. An
// entry in the book is not, until they have actually held one, or the book would hand out a tour of
// every machine it lists.
//
// The book flags the stretch of work where it builds a tooltip, which is the only way to tell its
// entries apart from the inventory slots on the same screen. Nothing here touches Ponder, so it is
// harmless when Create is absent.
public final class PonderGate
{
    private static boolean inBookTooltip = false;

    public static <T> T whileBuildingBookTooltip(Supplier<T> tooltip)
    {
        inBookTooltip = true;
        try
        {
            return tooltip.get();
        }
        finally
        {
            inBookTooltip = false;
        }
    }

    public static boolean blocks(ItemStack stack)
    {
        return inBookTooltip && !stack.isEmpty()
                && !Progression.isObtained(BuiltInRegistries.ITEM.getKey(stack.getItem()));
    }

    private PonderGate() {}
}
