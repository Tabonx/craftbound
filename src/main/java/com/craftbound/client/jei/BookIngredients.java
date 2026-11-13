package com.craftbound.client.jei;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.craftbound.client.bookmarks.BookmarkKeys;
import com.google.gson.JsonElement;
import com.mojang.serialization.DynamicOps;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IJeiRuntime;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

// The ingredients JEI knows, as the book's own entries.
public final class BookIngredients
{
    // Every ingredient a player can browse, raw ones included, as JEI shows them.
    public static List<BookIngredient> all()
    {
        return JeiSession.runtime().map(BookIngredients::all).orElse(List.of());
    }

    private static List<BookIngredient> all(IJeiRuntime runtime)
    {
        List<BookIngredient> result = new ArrayList<>();
        for (IIngredientType<?> type : browsableTypes(runtime))
            collect(runtime, type, result);
        return result;
    }

    // The ingredient types a player browses as craftable results. Deliberately not "all registered
    // types": mods register exotic types (tag-like pseudo-ingredients, etc.) that are noise here.
    static List<IIngredientType<?>> browsableTypes(IJeiRuntime runtime)
    {
        return List.of(VanillaTypes.ITEM_STACK,
                runtime.getJeiHelpers().getPlatformFluidHelper().getFluidIngredientType());
    }

    private static <V> void collect(IJeiRuntime runtime, IIngredientType<V> type, List<BookIngredient> out)
    {
        IIngredientManager manager = runtime.getIngredientManager();
        var renderer = manager.getIngredientRenderer(type);
        var helper = manager.getIngredientHelper(type);
        var visibility = runtime.getJeiHelpers().getIngredientVisibility();
        for (V ingredient : manager.getAllIngredients(type))
            JeiCompat.typed(manager, type, ingredient)
                    .filter(visibility::isIngredientVisible)
                    .ifPresent(typed -> out.add(BookIngredient.of(typed, renderer, helper)));
    }

    // Wrap an ingredient (e.g. one clicked inside a shown recipe) so its own recipe can be opened.
    public static Optional<BookIngredient> of(ITypedIngredient<?> typed)
    {
        return JeiSession.runtime().map(runtime -> build(runtime.getIngredientManager(), typed));
    }

    private static <V> BookIngredient build(IIngredientManager manager, ITypedIngredient<V> typed)
    {
        return BookIngredient.of(typed, manager.getIngredientRenderer(typed.getType()),
                manager.getIngredientHelper(typed.getType()));
    }

    // The unlock key of a held stack, the same one a recipe slot asking for it would carry.
    public static Optional<String> unlockKeyOf(ItemStack stack)
    {
        if (stack.isEmpty())
            return Optional.empty();
        return JeiSession.runtime().map(IJeiRuntime::getIngredientManager).flatMap(manager ->
                JeiCompat.typed(manager, VanillaTypes.ITEM_STACK, stack)
                        .map(typed -> BookIngredient.unlockKey(manager, typed)));
    }

    public static List<Component> tooltip(IRecipeSlotDrawable slot)
    {
        return JeiCompat.tooltip(slot, JeiSession.runtime().map(IJeiRuntime::getIngredientManager).orElse(null));
    }

    public static Optional<BookmarkKeys<BookIngredient>> bookmarkKeys(DynamicOps<JsonElement> ops)
    {
        return JeiSession.runtime().map(runtime -> new IngredientBookmarkKeys(runtime.getIngredientManager(), ops));
    }

    private BookIngredients() {}
}
