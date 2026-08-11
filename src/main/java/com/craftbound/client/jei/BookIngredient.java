package com.craftbound.client.jei;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.ingredients.subtypes.UidContext;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

// A single browsable entry, decoupled from its JEI ingredient type. Rendering and tooltips are
// captured as type-bound closures at construction so the widget can treat items, fluids and any
// other registered ingredient type uniformly without touching generics.
public final class BookIngredient
{
    private final ITypedIngredient<?> typed;
    private final String uid;
    private final String displayName;
    private final Renderer renderer;
    private final Function<TooltipFlag, List<Component>> tooltip;

    private BookIngredient(ITypedIngredient<?> typed, String uid, String displayName, Renderer renderer,
            Function<TooltipFlag, List<Component>> tooltip)
    {
        this.typed = typed;
        this.uid = uid;
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
                typed.getType().getUid() + "|" + helper.getUid(ingredient, UidContext.Ingredient),
                helper.getDisplayName(ingredient),
                (graphics, x, y) -> renderer.render(graphics, ingredient, x, y),
                flag -> renderer.getTooltip(ingredient, flag));
    }

    public ITypedIngredient<?> typed()
    {
        return typed;
    }

    // Stable identity across sessions (JEI's own ingredient uid, namespaced by ingredient type),
    // so bookmarks can name an entry in a file and find it again.
    public String uid()
    {
        return uid;
    }

    public String displayName()
    {
        return displayName;
    }

    // The backing item, when this entry is an item stack (empty for fluids and other types).
    public Optional<Item> item()
    {
        return typed.getItemStack().map(ItemStack::getItem);
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
