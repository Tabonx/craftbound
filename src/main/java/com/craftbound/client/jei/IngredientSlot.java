package com.craftbound.client.jei;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotRichTooltipCallback;
import mezz.jei.api.helpers.IPlatformFluidHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.IIngredientTypeWithSubtypes;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IIngredientManager;

import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.level.material.Fluid;

// One slot of a recipe, keeping only what went into it. A category lays its recipe out by calling
// these setters; everything about where and how the slot would be drawn is dropped on the floor,
// since the index only ever asks what the slot accepts.
//
// Ingredients go through createTypedIngredient, which is also what drops the invalid ones, so a
// category that offers an unregistered item contributes nothing rather than a broken entry.
final class IngredientSlot implements IRecipeSlotBuilder
{
    private static final int SLOT_SIZE = 16;

    private final IIngredientManager manager;
    private final IPlatformFluidHelper<?> fluids;
    private final List<ITypedIngredient<?>> ingredients = new ArrayList<>();

    IngredientSlot(IIngredientManager manager, IPlatformFluidHelper<?> fluids)
    {
        this.manager = manager;
        this.fluids = fluids;
    }

    List<ITypedIngredient<?>> getAllIngredients()
    {
        return List.copyOf(ingredients);
    }

    @Override
    public <I> IRecipeSlotBuilder addIngredients(IIngredientType<I> type, List<I> ingredients)
    {
        for (I ingredient : ingredients)
            add(type, ingredient);
        return this;
    }

    @Override
    public <I> IRecipeSlotBuilder addIngredient(IIngredientType<I> type, I ingredient)
    {
        return add(type, ingredient);
    }

    @Override
    public IRecipeSlotBuilder addIngredientsUnsafe(List<?> ingredients)
    {
        for (Object ingredient : ingredients)
            if (ingredient != null)
                manager.createTypedIngredient(ingredient).ifPresent(this.ingredients::add);
        return this;
    }

    // Re-created through the manager rather than taken as given, the same way JEI does it: a
    // category is free to hand over an ingredient that is no longer valid, and one of those in an
    // input slot is a requirement nothing can satisfy.
    @Override
    public IRecipeSlotBuilder addTypedIngredients(List<ITypedIngredient<?>> ingredients)
    {
        for (ITypedIngredient<?> ingredient : ingredients)
            addTyped(ingredient);
        return this;
    }

    private <I> void addTyped(ITypedIngredient<I> ingredient)
    {
        add(ingredient.getType(), ingredient.getIngredient());
    }

    @Override
    public IRecipeSlotBuilder addOptionalTypedIngredients(List<Optional<ITypedIngredient<?>>> ingredients)
    {
        for (Optional<ITypedIngredient<?>> ingredient : ingredients)
            ingredient.ifPresent(this.ingredients::add);
        return this;
    }

    @Override
    public IRecipeSlotBuilder addFluidStack(Fluid fluid)
    {
        return addFluidStack(fluid, fluids.bucketVolume());
    }

    @Override
    public IRecipeSlotBuilder addFluidStack(Fluid fluid, long amount)
    {
        return addFluidStack(fluid, amount, DataComponentPatch.EMPTY);
    }

    @Override
    public IRecipeSlotBuilder addFluidStack(Fluid fluid, long amount, DataComponentPatch components)
    {
        return addFluid(fluids, fluid, amount, components);
    }

    @SuppressWarnings("deprecation")
    private <T> IRecipeSlotBuilder addFluid(IPlatformFluidHelper<T> helper, Fluid fluid, long amount,
            DataComponentPatch components)
    {
        IIngredientTypeWithSubtypes<Fluid, T> type = helper.getFluidIngredientType();
        return add(type, helper.create(fluid.builtInRegistryHolder(), amount, components));
    }

    private <I> IRecipeSlotBuilder add(IIngredientType<I> type, I ingredient)
    {
        if (ingredient != null)
            manager.createTypedIngredient(type, ingredient).ifPresent(ingredients::add);
        return this;
    }

    @Override
    public int getWidth()
    {
        return SLOT_SIZE;
    }

    @Override
    public int getHeight()
    {
        return SLOT_SIZE;
    }

    @Override
    public IRecipeSlotBuilder setPosition(int x, int y)
    {
        return this;
    }

    @Override
    public IRecipeSlotBuilder setSlotName(String slotName)
    {
        return this;
    }

    @Override
    public IRecipeSlotBuilder setStandardSlotBackground()
    {
        return this;
    }

    @Override
    public IRecipeSlotBuilder setOutputSlotBackground()
    {
        return this;
    }

    @Override
    public IRecipeSlotBuilder setBackground(IDrawable background, int xOffset, int yOffset)
    {
        return this;
    }

    @Override
    public IRecipeSlotBuilder setOverlay(IDrawable overlay, int xOffset, int yOffset)
    {
        return this;
    }

    @Override
    public IRecipeSlotBuilder setFluidRenderer(long capacity, boolean showCapacity, int width, int height)
    {
        return this;
    }

    @Override
    public <T> IRecipeSlotBuilder setCustomRenderer(IIngredientType<T> type, IIngredientRenderer<T> renderer)
    {
        return this;
    }

    @SuppressWarnings("removal")
    @Override
    public IRecipeSlotBuilder addTooltipCallback(mezz.jei.api.gui.ingredient.IRecipeSlotTooltipCallback callback)
    {
        return this;
    }

    @Override
    public IRecipeSlotBuilder addRichTooltipCallback(IRecipeSlotRichTooltipCallback callback)
    {
        return this;
    }
}
