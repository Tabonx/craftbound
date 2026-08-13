package com.craftbound.client.mixin;

import java.util.Set;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.craftbound.client.ponder.PonderVisibility;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import net.createmod.ponder.foundation.ui.PonderTagScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

// Hides undiscovered entries from a Ponder category's item grid. The category's own item set is
// filtered as it is read, before the screen counts rows from it, so the grid closes up instead of
// leaving holes where the hidden entries would have been.
@Mixin(PonderTagScreen.class)
public class PonderTagScreenMixin
{
    @ModifyExpressionValue(
            method = "init",
            at = @At(value = "INVOKE",
                    target = "Lnet/createmod/ponder/api/registration/TagRegistryAccess;getItems(Lnet/createmod/ponder/foundation/PonderTag;)Ljava/util/Set;"))
    private Set<ResourceLocation> craftbound$hideUndiscovered(Set<ResourceLocation> items)
    {
        return PonderVisibility.visible(items);
    }

    // The category's main item gets its own large button beside the grid, so filtering the grid
    // alone would still name it. Reporting it as absent takes the branch Ponder already has for
    // categories without one, which drops the button and leaves the rest of the page intact.
    @ModifyExpressionValue(
            method = "init",
            at = @At(value = "INVOKE",
                    target = "Lnet/createmod/ponder/foundation/PonderTag;getMainItem()Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack craftbound$hideUndiscoveredMainItem(ItemStack mainItem)
    {
        return mainItem.isEmpty() || !PonderVisibility.isHidden(mainItem.getItem())
                ? mainItem
                : ItemStack.EMPTY;
    }
}
