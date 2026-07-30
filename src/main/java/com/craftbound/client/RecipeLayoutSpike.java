package com.craftbound.client;

import com.craftbound.Craftbound;
import com.craftbound.client.jei.CraftboundJeiPlugin;

import mezz.jei.api.gui.IRecipeLayoutDrawable;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.Rect2i;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;

// Spike: render a single Create recipe drawable into our own rectangle to the left of the
// inventory, proving we can host JEI's category renderers inside our own UI. Throwaway once
// the real recipe-book panel exists.
@EventBusSubscriber(modid = Craftbound.MODID, value = Dist.CLIENT)
public final class RecipeLayoutSpike
{
    private static IRecipeLayoutDrawable<?> layout;

    private RecipeLayoutSpike()
    {
    }

    @SubscribeEvent
    public static void render(ScreenEvent.Render.Post event)
    {
        if (!(event.getScreen() instanceof InventoryScreen inventory))
            return;

        if (!CraftboundJeiPlugin.hasRuntime())
        {
            layout = null;
            return;
        }

        if (layout == null)
        {
            var built = CraftboundJeiPlugin.createCreateRecipeLayout();
            if (built.isEmpty())
                return;
            layout = built.get();
        }

        // Measure the layout's bordered bounds at origin, then place it so that bordered box
        // sits with its top-left at (boxX, boxY) — aligned to the inventory's top edge.
        layout.setPosition(0, 0);
        Rect2i border = layout.getRectWithBorder();
        int boxW = border.getWidth();
        int boxH = border.getHeight();
        int boxX = Math.max(4, inventory.getGuiLeft() - boxW - 12);
        int boxY = inventory.getGuiTop();
        layout.setPosition(boxX - border.getX(), boxY - border.getY());

        GuiGraphics graphics = event.getGuiGraphics();
        int mouseX = event.getMouseX();
        int mouseY = event.getMouseY();

        graphics.fill(boxX, boxY, boxX + boxW, boxY + boxH, 0xF0202020);
        layout.drawRecipe(graphics, mouseX, mouseY);
        layout.drawOverlays(graphics, mouseX, mouseY);
    }
}
