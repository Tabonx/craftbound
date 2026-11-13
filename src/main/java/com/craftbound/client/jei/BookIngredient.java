package com.craftbound.client.jei;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.IIngredientTypeWithSubtypes;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.ingredients.subtypes.UidContext;
import com.craftbound.progression.UnlockKey;

import mezz.jei.api.runtime.IIngredientManager;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.material.Fluid;

// Rendering and tooltips are captured as type-bound closures so callers can treat every ingredient
// type alike without generics.
public final class BookIngredient
{
    private final ITypedIngredient<?> typed;
    private final String sessionUid;
    private final UnlockKey unlockKey;
    private final String displayName;
    private final Renderer renderer;
    private final Function<TooltipFlag, List<Component>> tooltip;

    private BookIngredient(ITypedIngredient<?> typed, String sessionUid, UnlockKey unlockKey, String displayName,
            Renderer renderer, Function<TooltipFlag, List<Component>> tooltip)
    {
        this.typed = typed;
        this.sessionUid = sessionUid;
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
                sessionUidOf(typed.getType(), helper, ingredient),
                keyOf(typed, helper),
                helper.getDisplayName(ingredient),
                (graphics, x, y) -> renderer.render(graphics, ingredient, x, y),
                flag -> renderer.getTooltip(ingredient, flag));
    }

    // Only good for one session: for a fluid or a variant JEI's uid holds objects whose text is an
    // identity hash.
    static <V> String sessionUidOf(IIngredientType<V> type, IIngredientHelper<V> helper, V ingredient)
    {
        return type.getUid() + "|" + helper.getUid(ingredient, UidContext.Ingredient);
    }

    public static <V> UnlockKey unlockKey(IIngredientManager manager, ITypedIngredient<V> typed)
    {
        return keyOf(typed, manager.getIngredientHelper(typed.getType()));
    }

    private static <V> UnlockKey keyOf(ITypedIngredient<V> typed, IIngredientHelper<V> helper)
    {
        Optional<UnlockKey> itemKey = itemIdOf(typed).map(id -> UnlockKey.ofItem(id, subtypeOf(typed, helper)));
        if (itemKey.isPresent())
            return itemKey.get();

        Optional<UnlockKey> fluidKey = fluidOf(typed)
                .map(fluid -> UnlockKey.ofFluid(BuiltInRegistries.FLUID.getKey(fluid), subtypeOf(typed, helper)));
        if (fluidKey.isPresent())
            return fluidKey.get();

        // Other ingredient types fall back to JEI's own uid, as fine-grained as they can be keyed.
        return UnlockKey.ofIngredient(typed.getType().getUid(),
                String.valueOf(helper.getUid(typed.getIngredient(), UidContext.Ingredient)));
    }

    public static Optional<ResourceLocation> itemIdOf(ITypedIngredient<?> typed)
    {
        return typed.getItemStack().map(stack -> BuiltInRegistries.ITEM.getKey(stack.getItem()));
    }

    // Read through JEI's ingredient type rather than the loader's own fluid stack.
    static Optional<Fluid> fluidOf(ITypedIngredient<?> typed)
    {
        if (!(typed.getType() instanceof IIngredientTypeWithSubtypes<?, ?> type)
                || !Fluid.class.isAssignableFrom(type.getIngredientBaseClass()))
            return Optional.empty();
        return baseOf(type, typed).map(Fluid.class::cast);
    }

    private static <B, V> Optional<B> baseOf(IIngredientTypeWithSubtypes<B, V> type, ITypedIngredient<?> typed)
    {
        return typed.getIngredient(type).map(type::getBase);
    }

    // Only getUid reaches JEI's subtype data, where Create's PotionFluidSubtypeInterpreter puts the
    // potion type; the legacy getUniqueId returns "" and would put every potion on one key.
    // getUid returns the item or fluid alone or List.of(it, subtypeData). Only the subtype half is
    // stringified, since an Item or Fluid prints as an identity hash.
    private static <V> String subtypeOf(ITypedIngredient<V> typed, IIngredientHelper<V> helper)
    {
        Object uid = helper.getUid(typed.getIngredient(), UidContext.Ingredient);
        return uid instanceof List<?> parts && parts.size() == 2 ? String.valueOf(parts.get(1)) : "";
    }

    public UnlockKey unlockKey()
    {
        return unlockKey;
    }

    public UnlockKey registryKey()
    {
        return unlockKey.withoutSubtype();
    }

    public ITypedIngredient<?> typed()
    {
        return typed;
    }

    public String sessionUid()
    {
        return sessionUid;
    }

    public String displayName()
    {
        return displayName;
    }

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
