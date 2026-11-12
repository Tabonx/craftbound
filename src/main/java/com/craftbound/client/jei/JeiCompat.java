package com.craftbound.client.jei;

import java.util.List;
import java.util.Optional;

import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IIngredientManager;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
//? if >=1.21.5 {
/*import net.minecraft.client.Minecraft;
import net.minecraft.world.item.TooltipFlag;
*///?}

// The JEI calls whose shape changed between the versions the book supports, written once so the
// rest of the book calls JEI the same way everywhere.
public final class JeiCompat
{
    public static <T> Optional<ITypedIngredient<T>> typed(IIngredientManager manager, IIngredientType<T> type,
            T ingredient)
    {
        //? if >=1.21.5 {
        /*return manager.createTypedIngredient(type, ingredient, false);
        *///?} else {
        return manager.createTypedIngredient(type, ingredient);
        //?}
    }

    public static <V> Optional<ITypedIngredient<V>> typed(IIngredientManager manager, V ingredient)
    {
        //? if >=1.21.5 {
        /*return manager.createTypedIngredient(ingredient, false);
        *///?} else {
        return manager.createTypedIngredient(ingredient);
        //?}
    }

    // JEI's slot tooltip as plain lines. Newer JEI builds it into a rich tooltip instead, so the
    // lines come from the shown ingredient's renderer, which needs the running JEI to find.
    public static List<Component> tooltip(IRecipeSlotDrawable slot, IIngredientManager manager)
    {
        //? if >=1.21.5 {
        /*if (manager == null)
            return List.of();
        TooltipFlag flag = Minecraft.getInstance().options.advancedItemTooltips
                ? TooltipFlag.ADVANCED
                : TooltipFlag.NORMAL;
        return slot.getDisplayedIngredient()
                .map(ingredient -> tooltip(manager, ingredient, flag))
                .orElseGet(List::of);
        *///?} else {
        return slot.getTooltip();
        //?}
    }

    //? if >=1.21.5 {
    /*private static <V> List<Component> tooltip(IIngredientManager manager, ITypedIngredient<V> ingredient,
            TooltipFlag flag)
    {
        return manager.getIngredientRenderer(ingredient.getType()).getTooltip(ingredient.getIngredient(), flag);
    }
    *///?}

    // The highlight JEI draws over a slot under the cursor, without its tooltip.
    public static void drawHighlight(IRecipeSlotDrawable slot, GuiGraphics graphics)
    {
        //? if >=1.21.11 {
        /*slot.draw(graphics, true);
        *///?} else {
        slot.drawHoverOverlays(graphics);
        //?}
    }

    private JeiCompat() {}
}
