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

// A single browsable entry, decoupled from its JEI ingredient type. Rendering and tooltips are
// captured as type-bound closures at construction so the widget can treat items, fluids and any
// other registered ingredient type uniformly without touching generics.
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

    // JEI's uid for the ingredient, namespaced by its type. Only good for one session: for a fluid
    // or a variant JEI's uid holds objects whose text is an identity hash.
    static <V> String sessionUidOf(IIngredientType<V> type, IIngredientHelper<V> helper, V ingredient)
    {
        return type.getUid() + "|" + helper.getUid(ingredient, UidContext.Ingredient);
    }

    // Progression identity: the registry id and JEI's subtype, see UnlockKey. Other ingredient types
    // fall back to JEI's own uid, which is as fine-grained as we can be for them.
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

        return UnlockKey.ofIngredient(typed.getType().getUid(),
                String.valueOf(helper.getUid(typed.getIngredient(), UidContext.Ingredient)));
    }

    // The item an ingredient stands for, when it is an item stack.
    public static Optional<ResourceLocation> itemIdOf(ITypedIngredient<?> typed)
    {
        return typed.getItemStack().map(stack -> BuiltInRegistries.ITEM.getKey(stack.getItem()));
    }

    // The fluid an ingredient stands for, read through JEI's ingredient type rather than through the
    // loader's own fluid stack.
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

    // What tells two potions apart. It has to come from getUid: that is the only path reaching JEI's
    // modern subtype data, which is where Create's PotionFluidSubtypeInterpreter puts the potion
    // type. Its legacy string counterpart, the one getUniqueId reads, returns "", so keying on
    // that collapsed every potion onto one key and one unlocked brewing step revealed the lot.
    //
    // getUid hands back either the item or fluid alone or List.of(it, subtypeData); only the subtype
    // half is stringified, never the Item or Fluid, whose toString is an identity hash. An
    // ingredient type that does not follow that shape yields no subtype, which is merely the coarse
    // behaviour rather than a crash.
    private static <V> String subtypeOf(ITypedIngredient<V> typed, IIngredientHelper<V> helper)
    {
        Object uid = helper.getUid(typed.getIngredient(), UidContext.Ingredient);
        return uid instanceof List<?> parts && parts.size() == 2 ? String.valueOf(parts.get(1)) : "";
    }

    public UnlockKey unlockKey()
    {
        return unlockKey;
    }

    // The key without its subtype, the level recipe inputs are read at.
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
