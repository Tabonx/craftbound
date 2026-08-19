package com.craftbound.client.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import com.craftbound.Craftbound;
import com.craftbound.client.progression.Progression;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.recipebook.RecipeButton;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;

// For recipes whose result would open up recipes the book is still hiding, swap the slot-background
// sprite vanilla is about to draw for our marked copy under the craftbound namespace. Because our
// textures share the vanilla paths (recipe_book/slot_*), we just re-namespace the sprite the game
// already chose, which preserves the craftable / "many" variants while recoloring them.
@Mixin(RecipeButton.class)
public abstract class RecipeButtonMixin
{
    // Shadow of RecipeButton's private helper. We use this instead of getRecipe() because,
    // at the blitSprite call, the button's currentIndex has not been recomputed yet and may
    // point past the end of a smaller, just-swapped-in collection (crash when paging).
    @Shadow
    private List<RecipeHolder<?>> getOrderedRecipes()
    {
        throw new AssertionError();
    }

    @ModifyArg(
            method = "renderWidget",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lnet/minecraft/resources/ResourceLocation;IIII)V"),
            index = 0)
    private ResourceLocation craftbound$swapUnlockingSprite(ResourceLocation original)
    {
        if (craftbound$resultUnlocksMore())
            return ResourceLocation.fromNamespaceAndPath(Craftbound.MODID, original.getPath());
        return original;
    }

    private boolean craftbound$resultUnlocksMore()
    {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null)
            return false;

        for (RecipeHolder<?> holder : getOrderedRecipes())
        {
            var result = holder.value().getResultItem(mc.level.registryAccess());
            if (Progression.unlocksMore(BuiltInRegistries.ITEM.getKey(result.getItem())))
                return true;
        }
        return false;
    }
}
