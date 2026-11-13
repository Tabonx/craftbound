package com.craftbound.client.jei;

import com.craftbound.Craftbound;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;

// JEI's show-recipe and show-uses keys work off any slot under the cursor and would reveal every
// recipe in the pack. The screen is matched by class name so JEI's internals stay off the compile
// classpath; if JEI renames it the view comes back rather than crashing.
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
