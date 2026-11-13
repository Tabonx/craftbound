package com.craftbound.client.create.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.craftbound.client.BookScreens;
import com.craftbound.client.Screens;
import com.craftbound.client.jei.JeiSearch;
import com.simibubi.create.content.logistics.stockTicker.StockKeeperRequestScreen;

// Create's stock keeper takes JEI's search text only while JEI's own search field has focus, and
// that field is never on screen. The book's search is the one the player types in, so it counts.
@Mixin(value = StockKeeperRequestScreen.class, remap = false)
public class StockKeeperSearchMixin
{
    @Shadow
    String previousJEISearchText;

    @Inject(method = "shouldSyncFromJEI", at = @At("HEAD"), cancellable = true)
    private void craftbound$syncFromBook(CallbackInfoReturnable<Boolean> cir)
    {
        if (BookScreens.isSearching(Screens.current())
                && !previousJEISearchText.equals(JeiSearch.query()))
            cir.setReturnValue(true);
    }
}
