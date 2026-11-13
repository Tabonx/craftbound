package com.craftbound.client.jei;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.runtime.IJeiRuntime;

import net.minecraft.world.inventory.AbstractContainerMenu;

// The transfer handlers mods give JEI, which move a recipe into their screens.
public final class TransferHandlers
{
    // Asked on every screen init, and answering means walking every recipe category.
    private static final RuntimeScoped<Map<Object, Boolean>> ACCEPTS = new RuntimeScoped<>(runtime -> new HashMap<>());

    // Whether a mod lets JEI move recipes into this menu: a storage terminal, a modded crafting
    // table, Create's stock keeper. A screen like that is somewhere a player looks recipes up.
    public static boolean acceptsRecipes(AbstractContainerMenu menu)
    {
        Optional<IJeiRuntime> runtime = JeiSession.runtime();
        Optional<Map<Object, Boolean>> accepts = ACCEPTS.get();
        if (runtime.isEmpty() || accepts.isEmpty())
            return false;
        return accepts.get().computeIfAbsent(menuKey(menu), key -> runtime.get().getRecipeManager()
                .createRecipeCategoryLookup().get()
                .anyMatch(category -> handlerFor(menu, category).isPresent()));
    }

    static <R> Optional<IRecipeTransferHandler<AbstractContainerMenu, R>> handlerFor(AbstractContainerMenu menu,
            IRecipeCategory<R> category)
    {
        return JeiSession.runtime()
                .flatMap(runtime -> runtime.getRecipeTransferManager().getRecipeTransferHandler(menu, category));
    }

    // JEI finds a transfer handler by both. The player's own menu has no type and says so by throwing.
    private static Object menuKey(AbstractContainerMenu menu)
    {
        try
        {
            return List.of(menu.getClass(), menu.getType());
        }
        catch (UnsupportedOperationException noType)
        {
            return menu.getClass();
        }
    }

    private TransferHandlers() {}
}
