package com.craftbound.client;

import java.util.Optional;

import com.craftbound.PlaceRecipePayload;
import com.craftbound.RecipePlacement;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.RecipeBookMenu;
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

    // The vanilla recipe behind a JEI layout, if this menu can place it. Create's machine recipes
    // and recipes for another menu (smelting while a crafting table is open) yield empty.
    public Optional<RecipeHolder<?>> placeable(Object shownRecipe)
    {
        return shownRecipe instanceof RecipeHolder<?> recipe && RecipePlacement.canPlace(menu, recipe)
                ? Optional.of(recipe)
                : Optional.empty();
    }

    public boolean hasIngredients(RecipeHolder<?> recipe)
    {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null)
            return false;

        StackedContents contents = new StackedContents();
        player.getInventory().fillStackedContents(contents);
        menu.fillCraftSlotsStackedContents(contents);
        return contents.canCraft(recipe.value(), null);
    }

    public void place(RecipeHolder<?> recipe, boolean placeAll)
    {
        PacketDistributor.sendToServer(new PlaceRecipePayload(menu.containerId, recipe.id(), placeAll));
    }
}
