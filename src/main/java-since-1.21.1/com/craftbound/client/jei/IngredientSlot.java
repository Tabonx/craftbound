package com.craftbound.client.jei;

import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotTooltipCallback;
import mezz.jei.api.helpers.IPlatformFluidHelper;
import mezz.jei.api.runtime.IIngredientManager;

// The slot as JEI asked for it before it moved to displays. Only the tooltip callback it has since
// removed is left; everything else is shared.
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
}
