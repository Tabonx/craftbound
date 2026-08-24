package com.craftbound;

import com.craftbound.client.progression.ClientObtainedItems;
import com.craftbound.client.upgrade.ClientBookUpgrade;
import com.craftbound.upgrade.BookUpgradePayload;
import com.craftbound.upgrade.UnbindLensPayload;

//? if >=1.21.4 {
/*import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
*///?}
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.item.ItemStack;
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
        event.registrar("1").optional()
                .playToServer(PlaceRecipePayload.TYPE, PlaceRecipePayload.STREAM_CODEC,
                        CraftboundNetwork::placeRecipe)
                .playToServer(UnbindLensPayload.TYPE, UnbindLensPayload.STREAM_CODEC,
                        CraftboundNetwork::unbindLens)
                .playToClient(ObtainedItemsPayload.TYPE, ObtainedItemsPayload.STREAM_CODEC,
                        CraftboundNetwork::receiveObtainedItems)
                .playToClient(BookUpgradePayload.TYPE, BookUpgradePayload.STREAM_CODEC,
                        CraftboundNetwork::receiveBookUpgrade);
    }

    // The client classes are named inside a method rather than by the registration itself, so a
    // dedicated server never loads them.
    private static void receiveObtainedItems(ObtainedItemsPayload payload, IPayloadContext context)
    {
        ClientObtainedItems.accept(payload.items());
    }

    private static void receiveBookUpgrade(BookUpgradePayload payload, IPayloadContext context)
    {
        ClientBookUpgrade.accept(payload.bound());
    }

    // The lens is not held while bound, so it is handed back here rather than taken from a slot.
    private static void unbindLens(UnbindLensPayload payload, IPayloadContext context)
    {
        if (!(context.player() instanceof ServerPlayer player) || player.isSpectator())
            return;

        player.resetLastActionTime();
        if (!player.getData(CraftboundAttachments.BOOK_UPGRADED))
            return;

        player.setData(CraftboundAttachments.BOOK_UPGRADED, false);
        PlayerState.sendBookUpgrade(player);
        player.getInventory().placeItemBackInInventory(
                new ItemStack(CraftboundItems.BOOKBINDERS_LENS.get()));
    }

    private static void placeRecipe(PlaceRecipePayload payload, IPayloadContext context)
    {
        if (!(context.player() instanceof ServerPlayer player) || player.isSpectator())
            return;

        player.resetLastActionTime();
        if (!(player.containerMenu instanceof RecipeBookMenu menu)
                || menu.containerId != payload.containerId()
                || !menu.stillValid(player))
            return;

        //? if >=1.21.4 {
        /*// The recipe manager lives on the server alone now, and recipes are named by registry key.
        ResourceKey<Recipe<?>> key = ResourceKey.create(Registries.RECIPE, payload.recipeId());
        RecipeHolder<?> recipe = ServerLevels.of(player).getServer().getRecipeManager().byKey(key).orElse(null);
        if (recipe == null || !RecipePlacement.canPlace(menu, recipe))
            return;

        // Vanilla placement refuses recipes the player's vanilla recipe book has not unlocked, but
        // Craftbound offers every recipe, so placing one counts as learning it.
        player.getRecipeBook().add(key);
        menu.handlePlacement(payload.placeAll(), false, recipe, ServerLevels.of(player), player.getInventory());
        *///?} else {
        RecipeHolder<?> recipe = ServerLevels.of(player).getRecipeManager().byKey(payload.recipeId()).orElse(null);
        if (recipe == null || !RecipePlacement.canPlace(menu, recipe))
            return;

        // Vanilla placement refuses recipes the player's vanilla recipe book has not unlocked, but
        // Craftbound offers every recipe, so placing one counts as learning it.
        player.getRecipeBook().add(recipe);
        menu.handlePlacement(payload.placeAll(), recipe, player);
        //?}
    }

    private CraftboundNetwork() {}
}
