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

        if (!RecipeBookState.isOpen())
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

        // Measure the layout's bordered bounds at origin, then right-align the box to the book
        // area (just left of the shifted inventory) and align its top to the inventory top.
        layout.setPosition(0, 0);
        Rect2i border = layout.getRectWithBorder();
        int boxW = border.getWidth();
        int boxH = border.getHeight();
        int boxX = Math.max(2, RecipeBookLayout.bookRight(inventory.getGuiLeft()) - boxW);
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
