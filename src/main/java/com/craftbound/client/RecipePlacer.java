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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.item.crafting.RecipeHolder;

// Places a shown recipe into the open menu's input slots. The slots are server-owned, so the click
// only asks; CraftboundNetwork does the moving.
public final class RecipePlacer
{
    private final RecipeBookMenu menu;

    public RecipePlacer(RecipeBookMenu menu)
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

    // Typed as Object because it is only ever compared for identity, and JEI renamed the interface
    // it returns between the versions the book supports.
    private static Object categoryFor(RecipeBookType bookType)
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

        //? if >=1.21.4 {
        /*// The client is only told about recipe displays now, and vanilla's placement packet names
        // a display rather than a recipe, so there is nothing the book can ask a plain server to
        // place. Placement needs a server running Craftbound, and the button greys out otherwise.
        if (!ServerSupport.installed())
            return false;
        *///?} else {
        if (!ServerSupport.installed() && !player.getRecipeBook().contains(recipe))
            return false;
        //?}

        StackedContents contents = new StackedContents();
        player.getInventory().fillStackedContents(contents);
        menu.fillCraftSlotsStackedContents(contents);
        return contents.canCraft(recipe.value(), null);
    }

    // A recipe's own id, which newer versions wrap in a registry key.
    private static ResourceLocation recipeId(RecipeHolder<?> recipe)
    {
        //? if >=1.21.4 {
        /*return recipe.id().location();
        *///?} else {
        return recipe.id();
        //?}
    }

    // A server without Craftbound cannot take our packet, so ask with vanilla's. That one only
    // places recipes the player's vanilla recipe book already holds, which is as far as the client
    // can get on its own.
    public void place(RecipeHolder<?> recipe, boolean placeAll)
    {
        if (ServerSupport.installed())
        {
            Net.toServer(new PlaceRecipePayload(menu.containerId, recipeId(recipe), placeAll));
            return;
        }

        // Newer versions have nothing to fall back to, and canPlace already refuses that case.
        //? if <1.21.4 {
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        if (connection != null)
            connection.send(new ServerboundPlaceRecipePacket(menu.containerId, recipe, placeAll));
        //?}
    }
}
