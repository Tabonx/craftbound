package com.craftbound.client.create.mixin;

import java.util.Set;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.craftbound.client.create.PonderVisibility;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import net.createmod.ponder.foundation.ui.PonderTagScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

// Filters the category's items as they are read, before the screen counts rows, so the grid closes
// up instead of leaving holes.
@Mixin(PonderTagScreen.class)
public class PonderTagScreenMixin
{
    @ModifyExpressionValue(
            method = "init",
            at = @At(value = "INVOKE",
                    target = "Lnet/createmod/ponder/api/registration/TagRegistryAccess;getItems(Lnet/createmod/ponder/foundation/PonderTag;)Ljava/util/Set;"))
    private Set<ResourceLocation> craftbound$hideUnponderable(Set<ResourceLocation> items)
    {
        return PonderVisibility.visible(items);
    }

    // The main item has its own button beside the grid. Reporting it as absent takes Ponder's branch
    // for categories without one, which drops the button and keeps the rest of the page.
    @ModifyExpressionValue(
            method = "init",
            at = @At(value = "INVOKE",
                    target = "Lnet/createmod/ponder/foundation/PonderTag;getMainItem()Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack craftbound$hideUnponderableMainItem(ItemStack mainItem)
    {
        return mainItem.isEmpty() || !PonderVisibility.isHidden(mainItem.getItem())
                ? mainItem
                : ItemStack.EMPTY;
    }
}
