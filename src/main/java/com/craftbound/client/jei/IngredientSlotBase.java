package com.craftbound.client.jei;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
//? if >=26.1.2 {
/*import mezz.jei.api.gui.drawable.TilingDirection;
*///?}
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
//
// Everything JEI has asked of a slot on every version lives here. The methods newer JEI added, and
// the one it removed, are on IngredientSlot, which each generation supplies its own.
abstract class IngredientSlotBase implements IRecipeSlotBuilder
{
    private static final int SLOT_SIZE = 16;

    private final IIngredientManager manager;
    private final IPlatformFluidHelper<?> fluids;
    protected final List<ITypedIngredient<?>> ingredients = new ArrayList<>();

    IngredientSlotBase(IIngredientManager manager, IPlatformFluidHelper<?> fluids)
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

    //? if <1.21.4 {
    @Override
    public <I> IRecipeSlotBuilder addIngredient(IIngredientType<I> type, I ingredient)
    {
        return add(type, ingredient);
    }
    //?}

    @Override
    public IRecipeSlotBuilder addIngredientsUnsafe(List<?> ingredients)
    {
        for (Object ingredient : ingredients)
            if (ingredient != null)
                //? if >=1.21.5 {
                /*manager.createTypedIngredient(ingredient, false).ifPresent(this.ingredients::add);
                *///?} else {
                manager.createTypedIngredient(ingredient).ifPresent(this.ingredients::add);
                //?}
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

    //? if <1.21.4 {
    @Override
    public IRecipeSlotBuilder addFluidStack(Fluid fluid)
    {
        return addFluidIngredient(fluid, fluids.bucketVolume(), DataComponentPatch.EMPTY);
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
    //?}

    protected final IRecipeSlotBuilder addFluidIngredient(Fluid fluid, long amount, DataComponentPatch components)
    {
        return addFluid(fluids, fluid, amount, components);
    }

    protected final IRecipeSlotBuilder addFluidIngredient(Fluid fluid)
    {
        return addFluidIngredient(fluid, fluids.bucketVolume(), DataComponentPatch.EMPTY);
    }

    @SuppressWarnings("deprecation")
    private <T> IRecipeSlotBuilder addFluid(IPlatformFluidHelper<T> helper, Fluid fluid, long amount,
            DataComponentPatch components)
    {
        IIngredientTypeWithSubtypes<Fluid, T> type = helper.getFluidIngredientType();
        return add(type, helper.create(fluid.builtInRegistryHolder(), amount, components));
    }

    //? if >=1.21.4 {
    /*@Override
    *///?}
    public <I> IRecipeSlotBuilder add(IIngredientType<I> type, I ingredient)
    {
        if (ingredient != null)
            //? if >=1.21.5 {
            /*manager.createTypedIngredient(type, ingredient, false).ifPresent(ingredients::add);
            *///?} else {
            manager.createTypedIngredient(type, ingredient).ifPresent(ingredients::add);
            //?}
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

    //? if >=26.1.2 {
    /*@Override
    public IRecipeSlotBuilder setFluidRenderer(long capacity, boolean showCapacity, int width, int height,
            TilingDirection tilingDirection)
    {
        return this;
    }

    *///?}
    @Override
    public <T> IRecipeSlotBuilder setCustomRenderer(IIngredientType<T> type, IIngredientRenderer<T> renderer)
    {
        return this;
    }

    @Override
    public IRecipeSlotBuilder addRichTooltipCallback(IRecipeSlotRichTooltipCallback callback)
    {
        return this;
    }
}
