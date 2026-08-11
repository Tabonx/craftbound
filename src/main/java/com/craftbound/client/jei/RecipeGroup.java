package com.craftbound.client.jei;

import java.util.List;

import com.craftbound.client.BookRail;

import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.recipe.category.IRecipeCategory;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

// One recipe category (crafting, smelting, a Create machine, …) and all of its recipes that make
// the focused item. Rendered as a tab on the book's left rail; its icon is captured as a draw
// closure so items, fluids and JEI's own category drawables can all supply it uniformly. Keeps a
// reference to the category so the whole category's recipes can be listed on demand.
public final class RecipeGroup implements BookRail.Tab
{
    private final IRecipeCategory<?> category;
    private final Component title;
    private final List<IRecipeLayoutDrawable<?>> recipes;
    private final Icon icon;

    public RecipeGroup(IRecipeCategory<?> category, Component title,
            List<IRecipeLayoutDrawable<?>> recipes, Icon icon)
    {
        this.category = category;
        this.title = title;
        this.recipes = recipes;
        this.icon = icon;
    }

    public IRecipeCategory<?> category()
    {
        return category;
    }

    @Override
    public Component title()
    {
        return title;
    }

    public List<IRecipeLayoutDrawable<?>> recipes()
    {
        return recipes;
    }

    @Override
    public void drawIcon(GuiGraphics graphics, int x, int y)
    {
        icon.draw(graphics, x, y);
    }

    @FunctionalInterface
    public interface Icon
    {
        void draw(GuiGraphics graphics, int x, int y);
    }
}
