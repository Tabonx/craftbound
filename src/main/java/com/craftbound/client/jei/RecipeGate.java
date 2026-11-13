package com.craftbound.client.jei;

import java.util.Optional;

import com.craftbound.progression.UnlockKey;

import net.minecraft.resources.ResourceLocation;

// What the book may show of the recipes JEI knows. Progression answers it; JEI only asks.
public interface RecipeGate
{
    RecipeGate OPEN = new RecipeGate()
    {
        @Override
        public boolean isCategoryUnlocked(String categoryUid)
        {
            return true;
        }

        @Override
        public boolean isRecipeUnlocked(String categoryUid, Object recipe)
        {
            return true;
        }

        @Override
        public boolean narrowsSlots()
        {
            return false;
        }

        @Override
        public boolean hasInput(UnlockKey unlockKey, Optional<ResourceLocation> itemId)
        {
            return true;
        }
    };

    boolean isCategoryUnlocked(String categoryUid);

    boolean isRecipeUnlocked(String categoryUid, Object recipe);

    // Whether a slot offering alternatives shows only the ones the player has.
    boolean narrowsSlots();

    boolean hasInput(UnlockKey unlockKey, Optional<ResourceLocation> itemId);
}
