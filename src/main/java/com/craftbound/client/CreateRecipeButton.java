package com.craftbound.client;

import com.craftbound.Craftbound;
import com.craftbound.client.jei.CraftboundJeiPlugin;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;

@EventBusSubscriber(modid = Craftbound.MODID, value = Dist.CLIENT)
public final class CreateRecipeButton
{
    private CreateRecipeButton()
    {
    }

    @SubscribeEvent
    public static void addToInventory(ScreenEvent.Init.Post event)
    {
        if (!(event.getScreen() instanceof InventoryScreen inventory))
            return;

        int x = Math.min(inventory.getGuiLeft() + 180, inventory.width - 104);
        int y = inventory.getGuiTop();
        event.addListener(Button.builder(
                Component.literal("Create recipes"),
                button -> CraftboundJeiPlugin.showCreateRecipes())
                .bounds(x, y, 100, 20)
                .build());
    }
}
