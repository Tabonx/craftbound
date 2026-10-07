package com.craftbound.client;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.craftbound.client.jei.RecipeGroup;

import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

// Moves a recipe the book shows into the open screen: into the grid on a recipe-book screen, or
// wherever a mod's JEI transfer handler takes it, such as the stock keeper's order.
public interface RecipePlacer
{
    // Whether the place button shows for the recipe at all.
    boolean offers(IRecipeLayoutDrawable<?> layout);

    // Whether placing it would work now. The button greys out otherwise.
    boolean canPlace(IRecipeLayoutDrawable<?> layout);

    void place(IRecipeLayoutDrawable<?> layout, boolean placeAll);

    // The inputs to mark after a click on the greyed button.
    default Set<IRecipeSlotView> missing(IRecipeLayoutDrawable<?> layout)
    {
        return Set.of();
    }

    // Why placing would not work now, shown on the greyed button. Empty leaves it unexplained.
    default List<Component> refusal(IRecipeLayoutDrawable<?> layout)
    {
        return List.of();
    }

    // Drawn over the shown recipe, in its own coordinates, after a click on the greyed button.
    default void showMissing(GuiGraphics graphics, IRecipeLayoutDrawable<?> layout, int mouseX, int mouseY)
    {
    }

    // The tab a recipe opens on.
    int menuGroup(List<RecipeGroup> groups);

    // What shift-clicking an item places: the first recipe that works now, in the order the book
    // lists them, so it is the one the player would see on opening.
    default Optional<IRecipeLayoutDrawable<?>> firstPlaceable(List<RecipeGroup> groups)
    {
        return groups.stream()
                .flatMap(group -> group.recipes().stream())
                .filter(layout -> offers(layout) && canPlace(layout))
                .findFirst();
    }
}
