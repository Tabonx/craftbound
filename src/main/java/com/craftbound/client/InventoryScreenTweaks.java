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

// Swap the vanilla recipe-book toggle (which drives the now-suppressed vanilla book) for our own
// button in the same spot, reusing the vanilla sprite. Ours toggles Craftbound's book and shifts
// the inventory aside to make room when the book is open.
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

        ImageButton button = new ImageButton(
                0, 0, 20, 18,
                RecipeBookComponent.RECIPE_BUTTON_SPRITES,
                b -> {
                    RecipeBookState.toggle();
                    applyLayout(inventory, b);
                });
        event.addListener(button);
        applyLayout(inventory, button);
    }

    // Position the inventory (and our button) for the current open/closed state.
    private static void applyLayout(InventoryScreen inventory, Button button)
    {
        ContainerScreenAccessor accessor = (ContainerScreenAccessor) inventory;
        int leftPos = RecipeBookLayout.inventoryLeftPos(
                inventory.width, accessor.craftbound$getImageWidth(), RecipeBookState.isOpen());
        accessor.craftbound$setLeftPos(leftPos);
        button.setPosition(leftPos + 104, inventory.height / 2 - 22);
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
