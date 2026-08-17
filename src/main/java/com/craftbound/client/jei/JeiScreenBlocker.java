package com.craftbound.client.jei;

import com.craftbound.Craftbound;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;

// Keeps JEI's full-screen recipe view from opening. Hiding JEI's overlays leaves one way back in:
// its show-recipe and show-uses keys work off whatever the cursor is over, inventory slots
// included, and would answer with every recipe in the pack rather than the ones the book has
// revealed. The book is the only recipe view Craftbound wants on screen.
//
// The screen is matched by class name so JEI's internals stay off the compile classpath. If JEI
// ever renames it the view simply comes back, which is a far better failure than a crash.
@EventBusSubscriber(modid = Craftbound.MODID, value = Dist.CLIENT)
public final class JeiScreenBlocker
{
    private static final String RECIPES_GUI = "mezz.jei.gui.recipes.RecipesGui";

    @SubscribeEvent
    public static void onScreenOpening(final ScreenEvent.Opening event)
    {
        if (RECIPES_GUI.equals(event.getNewScreen().getClass().getName()))
            event.setCanceled(true);
    }

    private JeiScreenBlocker() {}
}
