package com.craftbound.client.compat;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

// Drawing calls whose shape, not just their name, changed between Minecraft versions.
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

    // The font is unused here and stays so callers read the same on every version.
    public static void tooltip(GuiGraphics graphics, Font font, Component text, int mouseX, int mouseY)
    {
        graphics.setTooltipForNextFrame(text, mouseX, mouseY);
    }

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

    // Lasts until pop().
    public static void scaleAbout(GuiGraphics graphics, float centerX, float centerY, float scale)
    {
        push(graphics);
        translate(graphics, centerX, centerY);
        scale(graphics, scale);
        translate(graphics, -centerX, -centerY);
    }

    // Draws over everything already drawn. This generation layers by stratum rather than depth, and
    // tooltips still come after.
    public static void inFront(GuiGraphics graphics, Runnable draw)
    {
        graphics.nextStratum();
        draw.run();
    }

    // Dims the world behind a screen the book hides, without the screen's own panel, which a
    // container draws as part of its background.
    public static void backdrop(Screen screen, GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
    {
        graphics.nextStratum();
        //? if >=26.1 {
        /*screen.extractTransparentBackground(graphics);
        *///?} else {
        screen.renderTransparentBackground(graphics);
        //?}
        graphics.nextStratum();
    }

    // This generation collects tooltips and draws them once the screen is done, which a screen the
    // book hides never gets to.
    public static void drawTooltips(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
    {
        //? if >=26.1 {
        /*graphics.extractDeferredElements(mouseX, mouseY, partialTick);
        *///?} else if >=1.21.10 {
        /*graphics.renderDeferredElements();
        *///?} else {
        graphics.renderDeferredTooltip();
        //?}
    }

    private Canvas() {}
}
