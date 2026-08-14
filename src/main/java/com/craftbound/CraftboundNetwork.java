package com.craftbound;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

@EventBusSubscriber(modid = Craftbound.MODID)
public final class CraftboundNetwork
{
    @SubscribeEvent
    public static void onRegisterPayloads(final RegisterPayloadHandlersEvent event)
    {
        // Optional, so a server without Craftbound still accepts the client: NeoForge refuses any
        // connection that is missing a required payload. RecipePlacer falls back to the vanilla
        // packet when the channel is absent.
        event.registrar("1").optional().playToServer(
                PlaceRecipePayload.TYPE, PlaceRecipePayload.STREAM_CODEC, CraftboundNetwork::placeRecipe);
    }

    private static void placeRecipe(PlaceRecipePayload payload, IPayloadContext context)
    {
        if (!(context.player() instanceof ServerPlayer player) || player.isSpectator())
            return;

        player.resetLastActionTime();
        if (!(player.containerMenu instanceof RecipeBookMenu<?, ?> menu)
                || menu.containerId != payload.containerId()
                || !menu.stillValid(player))
            return;

        RecipeHolder<?> recipe = player.level().getRecipeManager().byKey(payload.recipeId()).orElse(null);
        if (recipe == null || !RecipePlacement.canPlace(menu, recipe))
            return;

        // Vanilla placement refuses recipes the player's vanilla recipe book has not unlocked, but
        // Craftbound offers every recipe, so placing one counts as learning it.
        player.getRecipeBook().add(recipe);
        menu.handlePlacement(payload.placeAll(), recipe, player);
    }

    private CraftboundNetwork() {}
}
