package com.craftbound.client.jei;

import java.util.Optional;

import com.craftbound.progression.UnlockKey;

import net.minecraft.resources.ResourceLocation;

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

    boolean narrowsSlots();

    boolean hasInput(UnlockKey unlockKey, Optional<ResourceLocation> itemId);
}
