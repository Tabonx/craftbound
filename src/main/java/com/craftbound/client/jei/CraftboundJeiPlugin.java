package com.craftbound.client.jei;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.craftbound.Craftbound;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.ResourceLocation;

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

    public static void showCreateRecipes()
    {
        if (runtime == null)
            return;

        List<RecipeType<?>> createTypes = new ArrayList<>();
        runtime.getRecipeManager()
                .createRecipeCategoryLookup()
                .get()
                .map(category -> category.getRecipeType())
                .filter(type -> type.getUid().getNamespace().equals("create"))
                .forEach(type -> {
                    if (!createTypes.contains(type))
                        createTypes.add(type);
                });

        runtime.getRecipesGui().showTypes(createTypes);
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
