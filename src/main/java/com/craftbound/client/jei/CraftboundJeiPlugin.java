package com.craftbound.client.jei;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import com.craftbound.Craftbound;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IFocusFactory;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.resources.ResourceLocation;

@JeiPlugin
public final class CraftboundJeiPlugin implements IModPlugin
{
    private static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(Craftbound.MODID, "jei");

    private static IJeiRuntime runtime;

    @Override
    public ResourceLocation getPluginUid()
    {
        return ID;
    }

    // Report "no GUI here" for the inventory so JEI does not draw its ingredient-list overlay
    // next to it. Craftbound's own book takes over that role. Scoped to the inventory for now;
    // other screens keep JEI's overlay until our book covers them.
    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration)
    {
        registration.addGuiScreenHandler(InventoryScreen.class, screen -> null);
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime)
    {
        runtime = jeiRuntime;
    }

    @Override
    public void onRuntimeUnavailable()
    {
        runtime = null;
    }

    public static boolean hasRuntime()
    {
        return runtime != null;
    }

    // The ingredient types a player browses as craftable results. Deliberately not "all registered
    // types": mods register exotic types (tag-like pseudo-ingredients, etc.) that are noise here.
    private static final List<IIngredientType<?>> BROWSABLE_TYPES =
            List.of(VanillaTypes.ITEM_STACK, NeoForgeTypes.FLUID_STACK);

    // Only ingredients the player can actually make: an item like an oak log is a valid ingredient
    // but has no recipe producing it, so it would just be dead weight in the browse grid.
    public static List<BookIngredient> getAllIngredients()
    {
        if (runtime == null)
            return List.of();

        IIngredientManager manager = runtime.getIngredientManager();
        List<BookIngredient> result = new ArrayList<>();
        for (IIngredientType<?> type : BROWSABLE_TYPES)
            collect(manager, type, result);

        IRecipeManager recipes = runtime.getRecipeManager();
        IFocusFactory focusFactory = runtime.getJeiHelpers().getFocusFactory();
        result.removeIf(item -> !isProducible(recipes, focusFactory, item));
        return result;
    }

    private static <V> void collect(IIngredientManager manager, IIngredientType<V> type,
            List<BookIngredient> out)
    {
        var renderer = manager.getIngredientRenderer(type);
        var helper = manager.getIngredientHelper(type);
        for (V ingredient : manager.getAllIngredients(type))
            manager.createTypedIngredient(type, ingredient)
                    .ifPresent(typed -> out.add(BookIngredient.of(typed, renderer, helper)));
    }

    // Wrap an ingredient (e.g. one clicked inside a shown recipe) so its own recipe can be opened.
    public static Optional<BookIngredient> toBookIngredient(ITypedIngredient<?> typed)
    {
        if (runtime == null)
            return Optional.empty();
        return Optional.of(build(runtime.getIngredientManager(), typed));
    }

    private static <V> BookIngredient build(IIngredientManager manager, ITypedIngredient<V> typed)
    {
        return BookIngredient.of(typed, manager.getIngredientRenderer(typed.getType()),
                manager.getIngredientHelper(typed.getType()));
    }

    // JEI's internal "Tag Info" categories (on by default in dev) list an item's tag memberships
    // as pseudo-recipes. They are always registered under a "tag_recipes/" type path; the book
    // shows how to make things, not what tags they belong to, so drop them.
    private static final String TAG_RECIPE_PATH_PREFIX = "tag_recipes/";

    // Whether at least one real (non-tag) recipe produces this ingredient. JEI's category lookup
    // respects the focus role, so an OUTPUT focus yields exactly the categories that output it.
    private static boolean isProducible(IRecipeManager recipes, IFocusFactory focusFactory,
            BookIngredient ingredient)
    {
        return outputCategories(recipes, focusFactory, ingredient).findAny().isPresent();
    }

    // Every recipe that produces the given ingredient, each as a drawable ready to render inside
    // the book's body rect.
    public static List<IRecipeLayoutDrawable<?>> recipesFor(BookIngredient ingredient)
    {
        if (runtime == null)
            return List.of();

        IRecipeManager recipes = runtime.getRecipeManager();
        IFocusFactory focusFactory = runtime.getJeiHelpers().getFocusFactory();
        List<IFocus<?>> focuses = List.of(outputFocus(focusFactory, ingredient.typed()));
        IFocusGroup group = focusFactory.createFocusGroup(focuses);

        List<IRecipeLayoutDrawable<?>> result = new ArrayList<>();
        outputCategories(recipes, focusFactory, ingredient)
                .forEach(category -> addLayouts(recipes, category, focuses, group, result));
        return result;
    }

    // The non-tag recipe categories that produce the ingredient as an output.
    private static Stream<IRecipeCategory<?>> outputCategories(IRecipeManager recipes,
            IFocusFactory focusFactory, BookIngredient ingredient)
    {
        List<IFocus<?>> focuses = List.of(outputFocus(focusFactory, ingredient.typed()));
        return recipes.createRecipeCategoryLookup()
                .limitFocus(focuses)
                .get()
                .filter(category -> !category.getRecipeType().getUid().getPath()
                        .startsWith(TAG_RECIPE_PATH_PREFIX));
    }

    private static <V> IFocus<V> outputFocus(IFocusFactory focusFactory, ITypedIngredient<V> typed)
    {
        return focusFactory.createFocus(RecipeIngredientRole.OUTPUT, typed);
    }

    private static <T> void addLayouts(IRecipeManager recipes, IRecipeCategory<T> category,
            List<IFocus<?>> focuses, IFocusGroup group, List<IRecipeLayoutDrawable<?>> out)
    {
        recipes.createRecipeLookup(category.getRecipeType())
                .limitFocus(focuses)
                .get()
                .forEach(recipe -> out.add(
                        recipes.createRecipeLayoutDrawableOrShowError(category, recipe, group)));
    }
}
