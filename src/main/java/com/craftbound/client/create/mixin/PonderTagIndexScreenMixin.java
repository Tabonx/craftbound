package com.craftbound.client.create.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.craftbound.client.create.PonderVisibility;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import net.createmod.ponder.foundation.PonderTag;
import net.createmod.ponder.foundation.ui.PonderTagIndexScreen;

// Drops categories whose entries are all hidden. A mod whose categories all empty out disappears
// too, since the screen groups what it is given by mod id.
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
