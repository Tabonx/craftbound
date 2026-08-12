package com.craftbound.client.jei;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.runtime.IIngredientManager;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
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
    private final String unlockKey;
    private final String displayName;
    private final Renderer renderer;
    private final Function<TooltipFlag, List<Component>> tooltip;

    private BookIngredient(ITypedIngredient<?> typed, String uid, String unlockKey, String displayName,
            Renderer renderer, Function<TooltipFlag, List<Component>> tooltip)
    {
        this.typed = typed;
        this.uid = uid;
        this.unlockKey = unlockKey;
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
                keyOf(typed, helper),
                helper.getDisplayName(ingredient),
                (graphics, x, y) -> renderer.render(graphics, ingredient, x, y),
                flag -> renderer.getTooltip(ingredient, flag));
    }

    // Progression identity, deliberately coarser than uid(): items key on their registry id alone,
    // so a recipe outputting a damaged or enchanted stack still unlocks the plain grid entry. Other
    // ingredient types fall back to JEI's own uid, which is as fine-grained as we can be for them.
    public static <V> String unlockKey(IIngredientManager manager, ITypedIngredient<V> typed)
    {
        return keyOf(typed, manager.getIngredientHelper(typed.getType()));
    }

    private static final String ITEM_KEY_PREFIX = "item|";

    private static <V> String keyOf(ITypedIngredient<V> typed, IIngredientHelper<V> helper)
    {
        return typed.getItemStack()
                .map(stack -> ITEM_KEY_PREFIX + BuiltInRegistries.ITEM.getKey(stack.getItem()))
                .orElseGet(() -> typed.getType().getUid() + "|"
                        + helper.getUid(typed.getIngredient(), UidContext.Ingredient));
    }

    // Empty for keys that name something other than an item, and for an item whose mod has since
    // been removed.
    public static Optional<Item> itemFromUnlockKey(String key)
    {
        if (!key.startsWith(ITEM_KEY_PREFIX))
            return Optional.empty();

        ResourceLocation id = ResourceLocation.tryParse(key.substring(ITEM_KEY_PREFIX.length()));
        return id == null ? Optional.empty() : BuiltInRegistries.ITEM.getOptional(id);
    }

    public String unlockKey()
    {
        return unlockKey;
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
