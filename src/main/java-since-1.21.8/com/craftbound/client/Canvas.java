package com.craftbound.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

// The handful of drawing calls whose shape, not just their name, changed between Minecraft
// versions. Everything else the book draws is a straight rename and is mapped by the build. Keeping
// these here means the book's own drawing code reads the same on every version.
public final class Canvas
{
    public static void sprite(GuiGraphics graphics, ResourceLocation sprite, int x, int y, int width, int height)
    {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x, y, width, height);
    }

    // A region of a texture file, as opposed to a stitched gui sprite.
    public static void texture(GuiGraphics graphics, ResourceLocation texture, int x, int y, int width, int height,
            float u, float v, int regionWidth, int regionHeight, int textureWidth, int textureHeight)
    {
        graphics.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, u, v, width, height,
                regionWidth, regionHeight, textureWidth, textureHeight);
    }

    public static void widget(Renderable child, GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
    {
        //? if >=26.1 {
        /*child.extractRenderState(graphics, mouseX, mouseY, partialTick);
        *///?} else {
        child.render(graphics, mouseX, mouseY, partialTick);
        //?}
    }

    // The font is no longer asked for, and stays in the signature so the book's drawing code reads
    // the same on every version.
    public static void tooltip(GuiGraphics graphics, Font font, Component text, int mouseX, int mouseY)
    {
        graphics.setTooltipForNextFrame(text, mouseX, mouseY);
    }

    // Transforms. This generation carries a plain 2D matrix stack, which is all the book needs.
    public static void push(GuiGraphics graphics)
    {
        graphics.pose().pushMatrix();
    }

    public static void pop(GuiGraphics graphics)
    {
        graphics.pose().popMatrix();
    }

    public static void translate(GuiGraphics graphics, float x, float y)
    {
        graphics.pose().translate(x, y);
    }

    public static void scale(GuiGraphics graphics, float scale)
    {
        graphics.pose().scale(scale, scale);
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
