package com.craftbound.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import com.craftbound.Craftbound;
import com.craftbound.client.progression.Progression;

import net.minecraft.client.gui.screens.recipebook.RecipeButton;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

// For recipes whose result would open up recipes the book is still hiding, swap the slot-background
// sprite vanilla is about to draw for our marked copy under the craftbound namespace. Because our
// textures share the vanilla paths (recipe_book/slot_*), we just re-namespace the sprite the game
// already chose, which preserves the craftable / "many" variants while recoloring them.
//
// This generation names the render type on the blit rather than a pipeline, which the injection
// point has to spell out in full.
@Mixin(RecipeButton.class)
public abstract class RecipeButtonMixin
{
    // The button resolves its own displayed stack, which is exactly the result the mark is about,
    // so there is no need to walk the collection's recipes.
    @Shadow
    public ItemStack getDisplayStack()
    {
        throw new AssertionError();
    }

    @ModifyArg(
            method = "renderWidget",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Ljava/util/function/Function;Lnet/minecraft/resources/ResourceLocation;IIII)V"),
            index = 1)
    private ResourceLocation craftbound$swapUnlockingSprite(ResourceLocation original)
    {
        ItemStack result = getDisplayStack();
        if (!result.isEmpty() && Progression.unlocksMore(BuiltInRegistries.ITEM.getKey(result.getItem())))
            return ResourceLocation.fromNamespaceAndPath(Craftbound.MODID, original.getPath());
        return original;
    }
}
