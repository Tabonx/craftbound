package com.craftbound.client.mixin.jei;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.craftbound.client.RecipeBookScreenTweaks;
import com.craftbound.client.Screens;
import com.craftbound.client.jei.CraftboundJeiPlugin;

// Create's stock keeper takes JEI's search text only while JEI's own search field has focus, and
// that field is never on screen. The book's search is the one the player types in, so it counts.
//
// Named as a string so Create stays off the compile classpath; without Create it is skipped.
@Pseudo
@Mixin(targets = "com.simibubi.create.content.logistics.stockTicker.StockKeeperRequestScreen", remap = false)
public class StockKeeperSearchMixin
{
    @Shadow
    String previousJEISearchText;

    @Inject(method = "shouldSyncFromJEI", at = @At("HEAD"), cancellable = true)
    private void craftbound$syncFromBook(CallbackInfoReturnable<Boolean> cir)
    {
        if (RecipeBookScreenTweaks.isSearching(Screens.current())
                && !previousJEISearchText.equals(CraftboundJeiPlugin.searchText()))
            cir.setReturnValue(true);
    }
}
