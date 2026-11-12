package com.craftbound.client.jei;

import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotTooltipCallback;
import mezz.jei.api.helpers.IPlatformFluidHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.runtime.IIngredientManager;

import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.level.material.Fluid;

// The slot as JEI asked for it before it moved to displays.
final class IngredientSlot extends IngredientSlotBase
{
    IngredientSlot(IIngredientManager manager, IPlatformFluidHelper<?> fluids)
    {
        super(manager, fluids);
    }

    @SuppressWarnings("removal")
    @Override
    public IRecipeSlotBuilder addTooltipCallback(IRecipeSlotTooltipCallback callback)
    {
        return this;
    }

    @Override
    public <I> IRecipeSlotBuilder addIngredient(IIngredientType<I> type, I ingredient)
    {
        return add(type, ingredient);
    }

    @Override
    public IRecipeSlotBuilder addFluidStack(Fluid fluid)
    {
        return addFluidIngredient(fluid);
    }

    @Override
    public IRecipeSlotBuilder addFluidStack(Fluid fluid, long amount)
    {
        return addFluidIngredient(fluid, amount, DataComponentPatch.EMPTY);
    }

    @Override
    public IRecipeSlotBuilder addFluidStack(Fluid fluid, long amount, DataComponentPatch components)
    {
        return addFluidIngredient(fluid, amount, components);
    }
}
