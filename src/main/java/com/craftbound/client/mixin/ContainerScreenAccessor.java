package com.craftbound.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

// Exposes the container GUI's position/size so Craftbound can shift the inventory aside to make
// room for its book.
@Mixin(AbstractContainerScreen.class)
public interface ContainerScreenAccessor
{
    @Accessor("leftPos")
    void craftbound$setLeftPos(int leftPos);

    @Accessor("imageWidth")
    int craftbound$getImageWidth();
}
