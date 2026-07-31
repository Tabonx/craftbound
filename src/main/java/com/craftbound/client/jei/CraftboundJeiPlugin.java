package com.craftbound.client.jei;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.craftbound.Craftbound;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.drawable.IDrawable;
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
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
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

    // Report "no GUI here" for every container screen so JEI never draws its ingredient-list
    // overlay: Craftbound's own book replaces it. JEI resolves screen handlers by isInstance, so a
    // single handler on AbstractContainerScreen covers all of them (inventory, furnace, chest, …).
    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration)
    {
        registration.addGuiScreenHandler(AbstractContainerScreen.class, screen -> null);
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
        List<IFocus<?>> focuses = List.of(focus(focusFactory, RecipeIngredientRole.OUTPUT, ingredient.typed()));
        return categoriesFor(recipes, focuses).findAny().isPresent();
    }

    // Recipes involving the ingredient in the given role, grouped by category so each becomes a tab
    // on the book's left rail. OUTPUT answers "how is this made?"; INPUT answers "where is it used?".
    public static List<RecipeGroup> recipeGroupsFor(BookIngredient ingredient, RecipeIngredientRole role)
    {
        if (runtime == null)
            return List.of();

        IRecipeManager recipes = runtime.getRecipeManager();
        IFocusFactory focusFactory = runtime.getJeiHelpers().getFocusFactory();
        IIngredientManager manager = runtime.getIngredientManager();
        List<IFocus<?>> focuses = List.of(focus(focusFactory, role, ingredient.typed()));
        IFocusGroup group = focusFactory.createFocusGroup(focuses);

        List<RecipeGroup> result = new ArrayList<>();
        categoriesFor(recipes, focuses).forEach(category ->
        {
            List<IRecipeLayoutDrawable<?>> layouts = new ArrayList<>();
            addLayouts(recipes, category, focuses, group, layouts);
            if (!layouts.isEmpty())
                result.add(new RecipeGroup(category, category.getTitle(), layouts,
                        iconFor(recipes, manager, category)));
        });
        return result;
    }

    // Every recipe in a category, built lazily: right-clicking a tab browses the whole category
    // (thousands of entries for crafting), so drawables are only created for the one being viewed.
    public static List<Supplier<IRecipeLayoutDrawable<?>>> allRecipesFor(RecipeGroup group)
    {
        if (runtime == null)
            return List.of();

        IRecipeManager recipes = runtime.getRecipeManager();
        IFocusGroup noFocus = runtime.getJeiHelpers().getFocusFactory().getEmptyFocusGroup();
        return recipeSuppliers(recipes, group.category(), noFocus);
    }

    private static <T> List<Supplier<IRecipeLayoutDrawable<?>>> recipeSuppliers(IRecipeManager recipes,
            IRecipeCategory<T> category, IFocusGroup noFocus)
    {
        return recipes.createRecipeLookup(category.getRecipeType())
                .get()
                .<Supplier<IRecipeLayoutDrawable<?>>>map(recipe ->
                        () -> recipes.createRecipeLayoutDrawableOrShowError(category, recipe, noFocus))
                .toList();
    }

    // Prefer the category's own tab icon; fall back to its first catalyst (the workstation block,
    // e.g. a furnace or a Create machine), matching how JEI itself icons a category.
    private static RecipeGroup.Icon iconFor(IRecipeManager recipes, IIngredientManager manager,
            IRecipeCategory<?> category)
    {
        IDrawable icon = category.getIcon();
        if (icon != null)
            return icon::draw;

        return recipes.createRecipeCatalystLookup(category.getRecipeType())
                .get()
                .findFirst()
                .map(catalyst -> catalystIcon(manager, catalyst))
                .orElse((graphics, x, y) -> { });
    }

    private static <V> RecipeGroup.Icon catalystIcon(IIngredientManager manager, ITypedIngredient<V> catalyst)
    {
        var renderer = manager.getIngredientRenderer(catalyst.getType());
        V ingredient = catalyst.getIngredient();
        return (graphics, x, y) -> renderer.render(graphics, ingredient, x, y);
    }

    // The non-tag recipe categories matching the given focus (by role and ingredient).
    private static Stream<IRecipeCategory<?>> categoriesFor(IRecipeManager recipes, List<IFocus<?>> focuses)
    {
        return recipes.createRecipeCategoryLookup()
                .limitFocus(focuses)
                .get()
                .filter(category -> !category.getRecipeType().getUid().getPath()
                        .startsWith(TAG_RECIPE_PATH_PREFIX));
    }

    private static <V> IFocus<V> focus(IFocusFactory focusFactory, RecipeIngredientRole role,
            ITypedIngredient<V> typed)
    {
        return focusFactory.createFocus(role, typed);
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
