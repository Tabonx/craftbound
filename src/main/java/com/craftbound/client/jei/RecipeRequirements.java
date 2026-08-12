package com.craftbound.client.jei;

import java.util.List;

import com.craftbound.progression.InputSlot;
import com.craftbound.progression.create.HeatRequirement;

import com.simibubi.create.content.processing.recipe.ProcessingRecipe;

import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.fml.ModList;

// Requirements a recipe carries that its JEI category never puts in a slot, and which the generic
// walk therefore cannot see. Create's basin heat is the one that matters for this mod; anything
// else a mod hides from its layout stays invisible to progression, which errs towards showing.
final class RecipeRequirements
{
    private static final boolean CREATE_LOADED = ModList.get().isLoaded("create");

    static List<InputSlot> extraSlots(Object recipe)
    {
        // Guarded so the Create classes below are only ever resolved when Create is installed.
        return CREATE_LOADED ? createHeatSlots(recipe) : List.of();
    }

    private static List<InputSlot> createHeatSlots(Object recipe)
    {
        if (recipe instanceof RecipeHolder<?> holder && holder.value() instanceof ProcessingRecipe<?, ?> processing)
            return HeatRequirement.slotsFor(processing.getRequiredHeat().name());
        return List.of();
    }

    private RecipeRequirements() {}
}
