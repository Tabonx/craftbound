package com.craftbound.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;

// Exposes the container GUI's position and size so Craftbound can place its book beside it.
@Mixin(AbstractContainerScreen.class)
public interface ContainerScreenAccessor
{
    @Accessor("leftPos")
    void craftbound$setLeftPos(int leftPos);

    @Accessor("imageWidth")
    int craftbound$getImageWidth();

    @Accessor("imageHeight")
    int craftbound$getImageHeight();

    // The screen finds its hovered slot while drawing, so one it does not draw keeps the last.
    @Accessor("hoveredSlot")
    void craftbound$setHoveredSlot(Slot slot);
}
