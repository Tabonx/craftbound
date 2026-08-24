package com.craftbound.client.jei;

import java.util.List;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.helpers.IPlatformFluidHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IIngredientManager;

import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;

// Newer JEI names every one of these "add". They all funnel into the same place: whatever the slot
// accepts, recorded as a typed ingredient.
final class IngredientSlot extends IngredientSlotBase
{
    IngredientSlot(IIngredientManager manager, IPlatformFluidHelper<?> fluids)
    {
        super(manager, fluids);
    }

    @Override
    public IRecipeSlotBuilder addItemStacks(List<ItemStack> itemStacks)
    {
        for (ItemStack stack : itemStacks)
            add(stack);
        return this;
    }

    @Override
    public <I> IRecipeSlotBuilder add(ITypedIngredient<I> ingredient)
    {
        ingredients.add(ingredient);
        return this;
    }

    @Override
    public IRecipeSlotBuilder add(Ingredient ingredient)
    {
        ingredient.items().forEach(item -> add(new ItemStack(item)));
        return this;
    }

    @Override
    public <I> IRecipeSlotBuilder add(IIngredientType<I> type, Ingredient ingredient)
    {
        return add(ingredient);
    }

    @Override
    public IRecipeSlotBuilder add(ItemStack itemStack)
    {
        return add(VanillaTypes.ITEM_STACK, itemStack);
    }

    @Override
    public IRecipeSlotBuilder add(ItemLike itemLike)
    {
        return add(new ItemStack(itemLike));
    }

    //? if >=26.1.2 {
    /*@Override
    public IRecipeSlotBuilder add(net.minecraft.world.item.ItemStackTemplate itemStackTemplate)
    {
        return add(itemStackTemplate.create());
    }

    // Slot contents only resolve to actual ingredients against the level's context. Without it
    // every slot resolves to nothing and the index reads every recipe as requiring no inputs.
    @Override
    public ContextMap getContextMap()
    {
        return contextMap();
    }
    *///?}

    @Override
    public IRecipeSlotBuilder add(SlotDisplay slotDisplay)
    {
        for (ItemStack stack : slotDisplay.resolveForStacks(contextMap()))
            add(stack);
        return this;
    }

    @Override
    public <I> IRecipeSlotBuilder add(IIngredientType<I> type, SlotDisplay slotDisplay)
    {
        return add(slotDisplay);
    }

    @Override
    public IRecipeSlotBuilder add(Fluid fluid)
    {
        return addFluidIngredient(fluid);
    }

    @Override
    public IRecipeSlotBuilder add(Fluid fluid, long amount)
    {
        return addFluidIngredient(fluid, amount, DataComponentPatch.EMPTY);
    }

    @Override
    public IRecipeSlotBuilder add(Fluid fluid, long amount, DataComponentPatch components)
    {
        return addFluidIngredient(fluid, amount, components);
    }

    private static ContextMap contextMap()
    {
        Level level = Minecraft.getInstance().level;
        return level == null ? ContextMap.EMPTY : SlotDisplayContext.fromLevel(level);
    }
}
