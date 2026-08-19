package com.craftbound.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

// The handful of drawing calls whose shape, not just their name, changed between Minecraft
// versions. Everything else the book draws is a straight rename and is mapped by the build. Keeping
// these here means the book's own drawing code reads the same on every version.
public final class Canvas
{
    public static void sprite(GuiGraphics graphics, ResourceLocation sprite, int x, int y, int width, int height)
    {
        graphics.blitSprite(sprite, x, y, width, height);
    }

    // A region of a texture file, as opposed to a stitched gui sprite.
    public static void texture(GuiGraphics graphics, ResourceLocation texture, int x, int y, int width, int height,
            float u, float v, int regionWidth, int regionHeight, int textureWidth, int textureHeight)
    {
        graphics.blit(texture, x, y, width, height, u, v, regionWidth, regionHeight, textureWidth, textureHeight);
    }

    public static void widget(Renderable child, GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
    {
        child.render(graphics, mouseX, mouseY, partialTick);
    }

    public static void tooltip(GuiGraphics graphics, Font font, Component text, int mouseX, int mouseY)
    {
        graphics.renderTooltip(font, text, mouseX, mouseY);
    }

    // Transforms. This generation carries a full pose stack, and the book only ever moves and
    // scales in two dimensions.
    public static void push(GuiGraphics graphics)
    {
        graphics.pose().pushPose();
    }

    public static void pop(GuiGraphics graphics)
    {
        graphics.pose().popPose();
    }

    public static void translate(GuiGraphics graphics, float x, float y)
    {
        graphics.pose().translate(x, y, 0f);
    }

    public static void scale(GuiGraphics graphics, float scale)
    {
        graphics.pose().scale(scale, scale, 1f);
    }

    // Scales what follows about a point, until pop().
    public static void scaleAbout(GuiGraphics graphics, float centerX, float centerY, float scale)
    {
        push(graphics);
        translate(graphics, centerX, centerY);
        scale(graphics, scale);
        translate(graphics, -centerX, -centerY);
    }

    private Canvas() {}
}
