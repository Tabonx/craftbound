package com.craftbound.client;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.craftbound.PlaceRecipePayload;
import com.craftbound.RecipePlacement;
import com.craftbound.client.jei.RecipeGroup;

import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundPlaceRecipePacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

// Places a shown recipe into the open menu's input slots. The slots are server-owned, so the click
// only asks; CraftboundNetwork does the moving.
public final class GridPlacer implements RecipePlacer
{
    private final RecipeBookMenu menu;

    public GridPlacer(RecipeBookMenu menu)
    {
        this.menu = menu;
    }

    @Override
    public boolean offers(IRecipeLayoutDrawable<?> layout)
    {
        return placeable(layout).isPresent();
    }

    @Override
    public boolean canPlace(IRecipeLayoutDrawable<?> layout)
    {
        return placeable(layout).filter(this::canPlace).isPresent();
    }

    @Override
    public void place(IRecipeLayoutDrawable<?> layout, boolean placeAll)
    {
        placeable(layout).ifPresent(recipe -> place(recipe, placeAll));
    }

    // The inputs the player cannot fill from what they have. Only asked of recipes the book can
    // place: elsewhere an input may be a tool the machine holds rather than one it consumes, and
    // JEI cannot tell the two apart.
    @Override
    public Set<IRecipeSlotView> missing(IRecipeLayoutDrawable<?> layout)
    {
        var player = Minecraft.getInstance().player;
        if (player == null)
            return Set.of();

        List<IRecipeSlotView> inputs = layout.getRecipeSlotsView().getSlotViews(RecipeIngredientRole.INPUT).stream()
                .filter(view -> view.getItemStacks().findAny().isPresent())
                .toList();
        List<MissingInputs.Slot<Item>> slots = inputs.stream()
                .map(view -> new MissingInputs.Slot<>(
                        view.getItemStacks().map(ItemStack::getItem).distinct().toList(),
                        view.getItemStacks().findFirst().map(ItemStack::getCount).orElse(1)))
                .toList();
        return MissingInputs.of(slots, OwnedItems.of(player)).stream()
                .map(inputs::get)
                .collect(Collectors.toSet());
    }

    // The vanilla recipe behind a shown layout, if this menu can place it. Gated on the layout's
    // own category, so only the plain crafting or furnace-family tab offers placement: Create shows
    // ordinary crafting recipes under its own "Automatic Shaped Crafting" tab as well, and there
    // the recipe is meant for a mechanical crafter, not for the grid.
    private Optional<RecipeHolder<?>> placeable(IRecipeLayoutDrawable<?> layout)
    {
        if (!isMenuCategory(layout.getRecipeCategory()))
            return Optional.empty();

        return layout.getRecipe() instanceof RecipeHolder<?> recipe && RecipePlacement.canPlace(menu, recipe)
                ? Optional.of(recipe)
                : Optional.empty();
    }

    // The open menu's own category, so a furnace shows how copper is smelted rather than crafted
    // from nuggets. The first tab when the item has none there.
    @Override
    public int menuGroup(List<RecipeGroup> groups)
    {
        for (int i = 0; i < groups.size(); i++)
            if (isMenuCategory(groups.get(i).category()))
                return i;
        return 0;
    }

    private boolean isMenuCategory(IRecipeCategory<?> category)
    {
        return category.getRecipeType() == categoryFor(menu.getRecipeBookType());
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
    private boolean canPlace(RecipeHolder<?> recipe)
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

        return RecipePlacement.available(player, menu).canCraft(recipe.value(), null);
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
    private void place(RecipeHolder<?> recipe, boolean placeAll)
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
