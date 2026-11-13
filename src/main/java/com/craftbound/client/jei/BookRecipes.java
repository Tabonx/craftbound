package com.craftbound.client.jei;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IFocusFactory;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IJeiRuntime;

public final class BookRecipes
{
    public static List<RecipeGroup> groupsFor(BookIngredient ingredient, RecipeIngredientRole role, RecipeGate gate)
    {
        return JeiSession.runtime().map(runtime -> groupsFor(runtime, ingredient, role, gate)).orElse(List.of());
    }

    private static List<RecipeGroup> groupsFor(IJeiRuntime runtime, BookIngredient ingredient,
            RecipeIngredientRole role, RecipeGate gate)
    {
        IRecipeManager recipes = runtime.getRecipeManager();
        IFocusFactory focusFactory = runtime.getJeiHelpers().getFocusFactory();
        List<IFocus<?>> focuses = List.of(focus(focusFactory, role, ingredient.typed()));
        IFocusGroup group = focusFactory.createFocusGroup(focuses);

        List<RecipeGroup> result = new ArrayList<>();
        categoriesFor(recipes, focuses)
                .filter(category -> gate.isCategoryUnlocked(category.getRecipeType().getUid().toString()))
                .forEach(category ->
        {
            List<IRecipeLayoutDrawable<?>> layouts = new ArrayList<>();
            addLayouts(runtime, category, focuses, group, gate, layouts);
            if (!layouts.isEmpty())
                result.add(new RecipeGroup(category, category.getTitle(), layouts,
                        iconFor(recipes, runtime.getIngredientManager(), category)));
        });
        return result;
    }

    // Lazy: a whole category can hold thousands of recipes, so only the one viewed is laid out.
    public static List<Supplier<IRecipeLayoutDrawable<?>>> allIn(RecipeGroup group, RecipeGate gate)
    {
        return JeiSession.runtime().map(runtime -> recipeSuppliers(runtime, group.category(), gate)).orElse(List.of());
    }

    private static <T> List<Supplier<IRecipeLayoutDrawable<?>>> recipeSuppliers(IJeiRuntime runtime,
            IRecipeCategory<T> category, RecipeGate gate)
    {
        String categoryUid = category.getRecipeType().getUid().toString();
        IFocusGroup noFocus = runtime.getJeiHelpers().getFocusFactory().getEmptyFocusGroup();
        return runtime.getRecipeManager().createRecipeLookup(category.getRecipeType())
                .get()
                .filter(recipe -> gate.isRecipeUnlocked(categoryUid, recipe))
                .<Supplier<IRecipeLayoutDrawable<?>>>map(recipe -> () -> layout(runtime, category, recipe, noFocus, gate))
                .toList();
    }

    // Falls back to the first catalyst, as JEI does.
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

    private static Stream<IRecipeCategory<?>> categoriesFor(IRecipeManager recipes, List<IFocus<?>> focuses)
    {
        return recipes.createRecipeCategoryLookup()
                .limitFocus(focuses)
                .get()
                .filter(BookCategories::isBrowsable);
    }

    private static <V> IFocus<V> focus(IFocusFactory focusFactory, RecipeIngredientRole role,
            ITypedIngredient<V> typed)
    {
        return focusFactory.createFocus(role, typed);
    }

    private static <T> void addLayouts(IJeiRuntime runtime, IRecipeCategory<T> category,
            List<IFocus<?>> focuses, IFocusGroup group, RecipeGate gate, List<IRecipeLayoutDrawable<?>> out)
    {
        String categoryUid = category.getRecipeType().getUid().toString();
        runtime.getRecipeManager().createRecipeLookup(category.getRecipeType())
                .limitFocus(focuses)
                .get()
                .filter(recipe -> gate.isRecipeUnlocked(categoryUid, recipe))
                .forEach(recipe -> out.add(layout(runtime, category, recipe, group, gate)));
    }

    private static <T> IRecipeLayoutDrawable<T> layout(IJeiRuntime runtime, IRecipeCategory<T> category,
            T recipe, IFocusGroup group, RecipeGate gate)
    {
        IRecipeManager recipes = runtime.getRecipeManager();
        IRecipeLayoutDrawable<T> layout = recipes.createRecipeLayoutDrawableOrShowError(category, recipe, group);
        if (!gate.narrowsSlots())
            return layout;

        IIngredientManager manager = runtime.getIngredientManager();
        List<List<ITypedIngredient<?>>> slots = layout.getRecipeSlotsView()
                .getSlotViews(RecipeIngredientRole.INPUT).stream()
                .map(IRecipeSlotView::getAllIngredientsList)
                .toList();
        HeldFocus.Narrowing<ITypedIngredient<?>> narrowing = HeldFocus.of(slots,
                typed -> gate.hasInput(BookIngredient.unlockKey(manager, typed), BookIngredient.itemIdOf(typed)));
        if (!narrowing.narrowed())
            return layout;

        IFocusFactory focusFactory = runtime.getJeiHelpers().getFocusFactory();
        List<IFocus<?>> focuses = new ArrayList<>(group.getAllFocuses());
        narrowing.focuses().forEach(typed -> focuses.add(focus(focusFactory, RecipeIngredientRole.INPUT, typed)));
        return recipes.createRecipeLayoutDrawableOrShowError(category, recipe, focusFactory.createFocusGroup(focuses));
    }

    private BookRecipes() {}
}
