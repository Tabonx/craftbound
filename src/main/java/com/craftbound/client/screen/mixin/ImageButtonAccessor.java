package com.craftbound.client.screen.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.WidgetSprites;

@Mixin(ImageButton.class)
public interface ImageButtonAccessor
{
    @Accessor("sprites")
    WidgetSprites craftbound$getSprites();
}
