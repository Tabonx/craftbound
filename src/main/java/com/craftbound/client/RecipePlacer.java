package com.craftbound.client;

import java.util.Optional;

import com.craftbound.PlaceRecipePayload;
import com.craftbound.RecipePlacement;

import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.recipe.RecipeType;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundPlaceRecipePacket;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.network.PacketDistributor;

// Places a shown recipe into the open menu's input slots. The slots are server-owned, so the click
// only asks; CraftboundNetwork does the moving.
public final class RecipePlacer
{
    private final RecipeBookMenu<?, ?> menu;

    public RecipePlacer(RecipeBookMenu<?, ?> menu)
    {
        this.menu = menu;
    }

    // The vanilla recipe behind a shown layout, if this menu can place it. Gated on the layout's
    // own category, so only the plain crafting or furnace-family tab offers placement: Create shows
    // ordinary crafting recipes under its own "Automatic Shaped Crafting" tab as well, and there
    // the recipe is meant for a mechanical crafter, not for the grid.
    public Optional<RecipeHolder<?>> placeable(IRecipeLayoutDrawable<?> layout)
    {
        if (layout.getRecipeCategory().getRecipeType() != categoryFor(menu.getRecipeBookType()))
            return Optional.empty();

        return layout.getRecipe() instanceof RecipeHolder<?> recipe && RecipePlacement.canPlace(menu, recipe)
                ? Optional.of(recipe)
                : Optional.empty();
    }

    private static RecipeType<?> categoryFor(RecipeBookType bookType)
    {
        return switch (bookType)
        {
            case CRAFTING -> RecipeTypes.CRAFTING;
            case FURNACE -> RecipeTypes.SMELTING;
            case BLAST_FURNACE -> RecipeTypes.BLASTING;
            case SMOKER -> RecipeTypes.SMOKING;
        };
    }

    // Whether asking would actually do something: the ingredients are there, and on a server
    // without Craftbound the vanilla recipe book has learned the recipe, since that server places
    // nothing else. The book is synced to the client, so this is the server's own answer rather
    // than a guess, and the button greys out instead of silently doing nothing.
    public boolean canPlace(RecipeHolder<?> recipe)
    {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null)
            return false;

        if (!ServerSupport.installed() && !player.getRecipeBook().contains(recipe))
            return false;

        StackedContents contents = new StackedContents();
        player.getInventory().fillStackedContents(contents);
        menu.fillCraftSlotsStackedContents(contents);
        return contents.canCraft(recipe.value(), null);
    }

    // A server without Craftbound cannot take our packet, so ask with vanilla's. That one only
    // places recipes the player's vanilla recipe book already holds, which is as far as the client
    // can get on its own.
    public void place(RecipeHolder<?> recipe, boolean placeAll)
    {
        if (ServerSupport.installed())
        {
            PacketDistributor.sendToServer(new PlaceRecipePayload(menu.containerId, recipe.id(), placeAll));
            return;
        }

        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        if (connection != null)
            connection.send(new ServerboundPlaceRecipePacket(menu.containerId, recipe, placeAll));
    }
}
