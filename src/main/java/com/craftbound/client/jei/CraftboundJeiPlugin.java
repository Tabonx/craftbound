package com.craftbound.client.jei;

import java.util.List;
import java.util.Optional;

import com.craftbound.Craftbound;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

@JeiPlugin
public final class CraftboundJeiPlugin implements IModPlugin
{
    private static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(Craftbound.MODID, "jei");

    private static IJeiRuntime runtime;

    @Override
    public ResourceLocation getPluginUid()
    {
        return ID;
    }

    // Report "no GUI here" for the inventory so JEI does not draw its ingredient-list overlay
    // next to it. Craftbound's own book takes over that role. Scoped to the inventory for now;
    // other screens keep JEI's overlay until our book covers them.
    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration)
    {
        registration.addGuiScreenHandler(InventoryScreen.class, screen -> null);
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime)
    {
        runtime = jeiRuntime;
    }

    @Override
    public void onRuntimeUnavailable()
    {
        runtime = null;
    }

    public static boolean hasRuntime()
    {
        return runtime != null;
    }

    public static List<ItemStack> getAllItemStacks()
    {
        if (runtime == null)
            return List.of();
        return List.copyOf(runtime.getIngredientManager().getAllItemStacks());
    }

    // Spike: build a drawable layout for one Create recipe so we can render it inside our own
    // rectangle (proving we can reuse JEI's category renderers without its full-screen GUI).
    // Prefers the mixing category, falls back to any Create category that has a recipe.
    public static Optional<IRecipeLayoutDrawable<?>> createCreateRecipeLayout()
    {
        if (runtime == null)
            return Optional.empty();

        IRecipeManager recipes = runtime.getRecipeManager();
        IFocusGroup noFocus = runtime.getJeiHelpers().getFocusFactory().getEmptyFocusGroup();

        List<IRecipeCategory<?>> createCategories = recipes.createRecipeCategoryLookup()
                .get()
                .filter(category -> category.getRecipeType().getUid().getNamespace().equals("create"))
                .toList();

        IRecipeCategory<?> category = createCategories.stream()
                .filter(c -> c.getRecipeType().getUid().getPath().equals("mixing"))
                .findFirst()
                .orElse(createCategories.isEmpty() ? null : createCategories.get(0));

        if (category == null)
            return Optional.empty();

        return firstLayout(recipes, category, noFocus);
    }

    private static <T> Optional<IRecipeLayoutDrawable<?>> firstLayout(
            IRecipeManager recipes, IRecipeCategory<T> category, IFocusGroup focus)
    {
        return recipes.createRecipeLookup(category.getRecipeType())
                .get()
                .findFirst()
                .map(recipe -> recipes.createRecipeLayoutDrawableOrShowError(category, recipe, focus));
    }
}
