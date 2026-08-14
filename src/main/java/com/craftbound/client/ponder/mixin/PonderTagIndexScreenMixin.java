package com.craftbound.client.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.craftbound.client.ponder.PonderVisibility;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import net.createmod.ponder.foundation.PonderTag;
import net.createmod.ponder.foundation.ui.PonderTagIndexScreen;

// Drops categories with nothing left to show. Once every entry in a category is hidden, listing it
// only offers the player an empty page to click into; a mod whose categories all empty out
// disappears from the list with them, since the screen groups what it is given by mod id.
@Mixin(PonderTagIndexScreen.class)
public class PonderTagIndexScreenMixin
{
    @ModifyExpressionValue(
            method = "init",
            at = @At(value = "INVOKE",
                    target = "Lnet/createmod/ponder/api/registration/TagRegistryAccess;getListedTags()Ljava/util/List;"))
    private List<PonderTag> craftbound$hideEmptyCategories(List<PonderTag> tags)
    {
        return tags.stream().filter(PonderVisibility::hasVisibleItems).toList();
    }
}
