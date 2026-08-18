package com.craftbound.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
//? if >=1.21.5 {
/*import net.minecraft.client.renderer.RenderPipelines;
*///?}

// The handful of drawing calls whose shape, not just their name, changed between Minecraft
// versions. Everything else the book draws is a straight rename and is mapped by the build. Keeping
// these here means the book's own drawing code reads the same on every version.
public final class Canvas
{
    public static void sprite(GuiGraphics graphics, ResourceLocation sprite, int x, int y, int width, int height)
    {
        //? if >=1.21.5 {
        /*graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x, y, width, height);
        *///?} else {
        graphics.blitSprite(sprite, x, y, width, height);
        //?}
    }

    public static void tooltip(GuiGraphics graphics, Font font, Component text, int mouseX, int mouseY)
    {
        //? if >=1.21.5 {
        /*graphics.setTooltipForNextFrame(text, mouseX, mouseY);
        *///?} else {
        graphics.renderTooltip(font, text, mouseX, mouseY);
        //?}
    }

    // Transforms. Newer versions carry a plain 2D matrix stack for these, older ones a full pose
    // stack, and the book only ever moves and scales in two dimensions.
    public static void push(GuiGraphics graphics)
    {
        //? if >=1.21.5 {
        /*graphics.pose().pushMatrix();
        *///?} else {
        graphics.pose().pushPose();
        //?}
    }

    public static void pop(GuiGraphics graphics)
    {
        //? if >=1.21.5 {
        /*graphics.pose().popMatrix();
        *///?} else {
        graphics.pose().popPose();
        //?}
    }

    public static void translate(GuiGraphics graphics, float x, float y)
    {
        //? if >=1.21.5 {
        /*graphics.pose().translate(x, y);
        *///?} else {
        graphics.pose().translate(x, y, 0f);
        //?}
    }

    public static void scale(GuiGraphics graphics, float scale)
    {
        //? if >=1.21.5 {
        /*graphics.pose().scale(scale, scale);
        *///?} else {
        graphics.pose().scale(scale, scale, 1f);
        //?}
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
