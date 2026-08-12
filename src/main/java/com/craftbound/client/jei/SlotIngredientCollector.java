package com.craftbound.client.jei;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.craftbound.progression.InputSlot;

import mezz.jei.api.gui.builder.IIngredientAcceptor;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.library.gui.recipes.supplier.builder.IngredientSlotBuilder;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.fluids.FluidStack;

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

    List<InputSlot> inputSlots()
    {
        return inputs.stream().map(this::toInputSlot).toList();
    }

    // Fluids are recorded by identity so progression can ask whether they are reachable yet, and
    // their bucket is folded in with the items so having one in hand also satisfies the slot.
    private InputSlot toInputSlot(IngredientSlotBuilder slot)
    {
        Set<ResourceLocation> items = new HashSet<>();
        Set<String> fluids = new HashSet<>();

        for (ITypedIngredient<?> ingredient : slot.getAllIngredients())
        {
            ingredient.getItemStack()
                    .map(ItemStack::getItem)
                    .map(BuiltInRegistries.ITEM::getKey)
                    .ifPresent(items::add);

            if (ingredient.getIngredient() instanceof FluidStack fluid)
            {
                fluids.add(BookIngredient.unlockKey(manager, ingredient));
                bucketId(fluid).ifPresent(items::add);
            }
        }
        return new InputSlot(Set.copyOf(items), Set.copyOf(fluids));
    }

    private static Optional<ResourceLocation> bucketId(FluidStack fluid)
    {
        Item bucket = fluid.getFluid().getBucket();
        return bucket == null || bucket == Items.AIR
                ? Optional.empty()
                : Optional.of(BuiltInRegistries.ITEM.getKey(bucket));
    }

    // Keyed by unlock key, keeping the ingredient itself: the unlock toast draws it with JEI's own
    // renderer so a fluid looks the same there as it does in the book.
    Map<String, ITypedIngredient<?>> outputs()
    {
        Map<String, ITypedIngredient<?>> byKey = new HashMap<>();
        for (IngredientSlotBuilder slot : outputs)
            for (ITypedIngredient<?> ingredient : slot.getAllIngredients())
                byKey.putIfAbsent(BookIngredient.unlockKey(manager, ingredient), ingredient);
        return byKey;
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
