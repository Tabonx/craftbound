package com.craftbound.client.jei;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.craftbound.progression.InputSlot;
import com.craftbound.progression.UnlockKey;

import mezz.jei.api.gui.builder.IIngredientAcceptor;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.helpers.IPlatformFluidHelper;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IIngredientManager;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;

// Runs a category's setRecipe without building anything drawable, keeping the slots apart so
// "any one of these planks" stays one slot rather than dissolving into a flat ingredient list.
// JEI's own IngredientSupplierBuilder merges every slot of a role, which would turn alternatives
// into separate requirements and lock recipes that are perfectly makeable.
final class SlotIngredientCollector implements IRecipeLayoutBuilder
{
    private final IIngredientManager manager;
    private final IPlatformFluidHelper<?> fluids;
    private final List<IngredientSlot> inputs = new ArrayList<>();
    private final List<IngredientSlot> outputs = new ArrayList<>();
    private final List<IngredientSlot> invisibleOutputs = new ArrayList<>();

    SlotIngredientCollector(IIngredientManager manager, IPlatformFluidHelper<?> fluids)
    {
        this.manager = manager;
        this.fluids = fluids;
    }

    List<InputSlot> inputSlots()
    {
        return inputs.stream().map(this::toInputSlot).toList();
    }

    // Fluids are recorded by identity so progression can ask whether they are reachable yet, and
    // their bucket is folded in with the items so having one in hand also satisfies the slot.
    private InputSlot toInputSlot(IngredientSlot slot)
    {
        Set<ResourceLocation> items = new HashSet<>();
        Set<UnlockKey> fluids = new HashSet<>();
        Set<UnlockKey> variants = new HashSet<>();

        for (ITypedIngredient<?> ingredient : slot.getAllIngredients())
        {
            BookIngredient.itemIdOf(ingredient).ifPresent(id ->
            {
                UnlockKey key = BookIngredient.unlockKey(manager, ingredient);
                if (key.hasSubtype())
                    variants.add(key);
                else
                    items.add(id);
            });

            BookIngredient.fluidOf(ingredient).ifPresent(fluid ->
            {
                fluids.add(BookIngredient.unlockKey(manager, ingredient));
                bucketId(fluid).ifPresent(items::add);
            });
        }
        return new InputSlot(Set.copyOf(items), Set.copyOf(fluids), Set.copyOf(variants));
    }

    private static Optional<ResourceLocation> bucketId(Fluid fluid)
    {
        Item bucket = fluid.getBucket();
        return bucket == null || bucket == Items.AIR
                ? Optional.empty()
                : Optional.of(BuiltInRegistries.ITEM.getKey(bucket));
    }

    // Keyed by unlock key, keeping the ingredient itself: the unlock toast draws it with JEI's own
    // renderer so a fluid looks the same there as it does in the book.
    Map<UnlockKey, ITypedIngredient<?>> outputs()
    {
        Map<UnlockKey, ITypedIngredient<?>> byKey = new HashMap<>();
        for (IngredientSlot slot : outputs)
            for (ITypedIngredient<?> ingredient : slot.getAllIngredients())
                byKey.putIfAbsent(BookIngredient.unlockKey(manager, ingredient), ingredient);
        return byKey;
    }

    @Override
    public IRecipeSlotBuilder addSlot(RecipeIngredientRole role)
    {
        IngredientSlot slot = new IngredientSlot(manager, fluids);
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

    //? if <1.21.4 {
    @SuppressWarnings("removal")
    @Override
    public IRecipeSlotBuilder addSlotToWidget(RecipeIngredientRole role,
            mezz.jei.api.gui.widgets.ISlottedWidgetFactory<?> widgetFactory)
    {
        return addSlot(role);
    }
    //?}

    // Invisible inputs are dropped rather than treated as another required slot: a category that
    // declares one the player can never hold would lock the recipe forever, and showing a recipe
    // early is the far cheaper mistake. Invisible outputs are kept apart from the real ones: JEI
    // finds the recipe by them, so the book lists them, but they unlock nothing.
    @Override
    public IIngredientAcceptor<?> addInvisibleIngredients(RecipeIngredientRole role)
    {
        IngredientSlot slot = new IngredientSlot(manager, fluids);
        if (role == RecipeIngredientRole.OUTPUT)
            invisibleOutputs.add(slot);
        return slot;
    }

    Set<UnlockKey> invisibleOutputKeys()
    {
        Set<UnlockKey> keys = new HashSet<>();
        for (IngredientSlot slot : invisibleOutputs)
            for (ITypedIngredient<?> ingredient : slot.getAllIngredients())
                keys.add(BookIngredient.unlockKey(manager, ingredient));
        return keys;
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
