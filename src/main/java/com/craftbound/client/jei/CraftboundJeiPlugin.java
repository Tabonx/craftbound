package com.craftbound.client.jei;

import java.util.ArrayList;
import java.util.List;

import com.craftbound.Craftbound;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.RecipeType;
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
}
