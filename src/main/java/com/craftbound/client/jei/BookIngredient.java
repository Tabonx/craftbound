package com.craftbound.client.jei;

import java.util.List;
import java.util.function.Function;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.ITypedIngredient;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;

// A single browsable entry, decoupled from its JEI ingredient type. Rendering and tooltips are
// captured as type-bound closures at construction so the widget can treat items, fluids and any
// other registered ingredient type uniformly without touching generics.
public final class BookIngredient
{
    private final ITypedIngredient<?> typed;
    private final String displayName;
    private final Renderer renderer;
    private final Function<TooltipFlag, List<Component>> tooltip;

    private BookIngredient(ITypedIngredient<?> typed, String displayName, Renderer renderer,
            Function<TooltipFlag, List<Component>> tooltip)
    {
        this.typed = typed;
        this.displayName = displayName;
        this.renderer = renderer;
        this.tooltip = tooltip;
    }

    @SuppressWarnings({"deprecation", "removal"}) // getTooltip(V, TooltipFlag): the stable List-returning path
    public static <V> BookIngredient of(ITypedIngredient<V> typed,
            IIngredientRenderer<V> renderer, IIngredientHelper<V> helper)
    {
        V ingredient = typed.getIngredient();
        return new BookIngredient(
                typed,
                helper.getDisplayName(ingredient),
                (graphics, x, y) -> renderer.render(graphics, ingredient, x, y),
                flag -> renderer.getTooltip(ingredient, flag));
    }

    public ITypedIngredient<?> typed()
    {
        return typed;
    }

    public String displayName()
    {
        return displayName;
    }

    public void render(GuiGraphics graphics, int x, int y)
    {
        renderer.render(graphics, x, y);
    }

    public List<Component> tooltip(TooltipFlag flag)
    {
        return tooltip.apply(flag);
    }

    @FunctionalInterface
    private interface Renderer
    {
        void render(GuiGraphics graphics, int x, int y);
    }
}
