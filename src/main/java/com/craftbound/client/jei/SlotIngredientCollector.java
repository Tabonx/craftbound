package com.craftbound.client.jei;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mezz.jei.api.gui.builder.IIngredientAcceptor;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.library.gui.recipes.supplier.builder.IngredientSlotBuilder;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

// Runs a category's setRecipe without building anything drawable, keeping the slots apart so
// "any one of these planks" stays one slot rather than dissolving into a flat ingredient list.
// JEI's own IngredientSupplierBuilder merges every slot of a role, which would turn alternatives
// into separate requirements and lock recipes that are perfectly makeable.
final class SlotIngredientCollector implements IRecipeLayoutBuilder
{
    private final IIngredientManager manager;
    private final List<IngredientSlotBuilder> inputs = new ArrayList<>();
    private final List<IngredientSlotBuilder> outputs = new ArrayList<>();

    SlotIngredientCollector(IIngredientManager manager)
    {
        this.manager = manager;
    }

    List<Set<ResourceLocation>> inputSlots()
    {
        return inputs.stream().map(SlotIngredientCollector::itemIds).toList();
    }

    Set<String> outputKeys()
    {
        Set<String> keys = new HashSet<>();
        for (IngredientSlotBuilder slot : outputs)
            for (ITypedIngredient<?> ingredient : slot.getAllIngredients())
                keys.add(BookIngredient.unlockKey(manager, ingredient));
        return keys;
    }

    private static Set<ResourceLocation> itemIds(IngredientSlotBuilder slot)
    {
        Set<ResourceLocation> ids = new HashSet<>();
        for (ITypedIngredient<?> ingredient : slot.getAllIngredients())
            ingredient.getItemStack()
                    .map(ItemStack::getItem)
                    .map(BuiltInRegistries.ITEM::getKey)
                    .ifPresent(ids::add);
        return ids;
    }

    @Override
    public IRecipeSlotBuilder addSlot(RecipeIngredientRole role)
    {
        IngredientSlotBuilder slot = new IngredientSlotBuilder(manager);
        switch (role)
        {
            case INPUT -> inputs.add(slot);
            case OUTPUT -> outputs.add(slot);
            default -> { } // catalysts are gated per category, render-only slots are decoration
        }
        return slot;
    }

    @Override
    public IRecipeSlotBuilder addSlot(RecipeIngredientRole role, int x, int y)
    {
        return addSlot(role);
    }

    @SuppressWarnings("removal")
    @Override
    public IRecipeSlotBuilder addSlotToWidget(RecipeIngredientRole role,
            mezz.jei.api.gui.widgets.ISlottedWidgetFactory<?> widgetFactory)
    {
        return addSlot(role);
    }

    // Invisible ingredients are dropped rather than treated as another required slot: a category
    // that declares one the player can never hold would lock the recipe forever, and showing a
    // recipe early is the far cheaper mistake.
    @Override
    public IIngredientAcceptor<?> addInvisibleIngredients(RecipeIngredientRole role)
    {
        return new IngredientSlotBuilder(manager);
    }

    @Override
    public void moveRecipeTransferButton(int posX, int posY)
    {
    }

    @Override
    public void setShapeless()
    {
    }

    @Override
    public void setShapeless(int posX, int posY)
    {
    }

    @Override
    public void createFocusLink(IIngredientAcceptor<?>... slots)
    {
    }
}
