package com.craftbound.client;

import com.craftbound.Craftbound;
import com.craftbound.client.mixin.ContainerScreenAccessor;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;

// Swap the vanilla recipe-book toggle for our own (same sprite, same spot), add Craftbound's book
// widget beside the inventory, and shift the inventory aside to make room while the book is open.
@EventBusSubscriber(modid = Craftbound.MODID, value = Dist.CLIENT)
public final class InventoryScreenTweaks
{
    private InventoryScreenTweaks()
    {
    }

    @SubscribeEvent
    public static void onInit(ScreenEvent.Init.Post event)
    {
        if (!(event.getScreen() instanceof InventoryScreen inventory))
            return;

        removeVanillaRecipeButton(event);

        RecipeBookWidget book = new RecipeBookWidget();
        event.addListener(book);

        ImageButton button = new ImageButton(
                0, 0, 20, 18,
                RecipeBookComponent.RECIPE_BUTTON_SPRITES,
                b -> {
                    RecipeBookState.toggle();
                    applyLayout(inventory, b, book);
                })
        {
            // A mouse click otherwise leaves the button focused, so it stays highlighted until
            // focus moves elsewhere. Refuse focus so only a live hover highlights the toggle.
            @Override
            public void setFocused(boolean focused)
            {
                super.setFocused(false);
            }
        };
        event.addListener(button);

        applyLayout(inventory, button, book);
    }

    // Draw the book's item tooltip last, so it sits above the inventory's slot placeholders.
    @SubscribeEvent
    public static void onRender(ScreenEvent.Render.Post event)
    {
        if (!(event.getScreen() instanceof InventoryScreen inventory))
            return;

        for (GuiEventListener listener : inventory.children())
        {
            if (listener instanceof RecipeBookWidget book)
            {
                book.renderDeferredTooltip(event.getGuiGraphics(), event.getMouseX(), event.getMouseY());
                return;
            }
        }
    }

    // Position the inventory, our button and the book for the current open/closed state.
    private static void applyLayout(InventoryScreen inventory, Button button, RecipeBookWidget book)
    {
        boolean open = RecipeBookState.isOpen();
        ContainerScreenAccessor accessor = (ContainerScreenAccessor) inventory;
        int leftPos = RecipeBookLayout.inventoryLeftPos(
                inventory.width, accessor.craftbound$getImageWidth(), open);
        accessor.craftbound$setLeftPos(leftPos);

        button.setPosition(leftPos + 104, inventory.height / 2 - 22);

        book.visible = open;
        book.setPosition(RecipeBookLayout.bookRight(leftPos) - RecipeBookWidget.WIDTH, inventory.getGuiTop());
    }

    // The recipe-book toggle is the only 20x18 ImageButton the inventory adds.
    private static void removeVanillaRecipeButton(ScreenEvent.Init.Post event)
    {
        for (GuiEventListener listener : event.getListenersList())
        {
            if (listener instanceof ImageButton button
                    && button.getWidth() == 20 && button.getHeight() == 18)
            {
                event.removeListener(button);
                return;
            }
        }
    }
}
