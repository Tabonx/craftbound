package com.craftbound.client.jei;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.craftbound.Craftbound;
import com.craftbound.client.progression.Progression;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
//? if >=1.21.5 {
/*import mezz.jei.api.gui.ingredient.IRecipeSlotView;
*///?}
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IFocusFactory;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
//? if >=1.21.5 {
/*import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;
*///?}

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

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration)
    {
        registration.addGlobalGuiHandler(new JeiOverlayHider());
    }

    // JEI hands the runtime over on every reload, and takes it back before rebuilding, so these two
    // are also the moments the recipe index behind them stops being true.
    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime)
    {
        runtime = jeiRuntime;
        Progression.invalidate();
    }

    @Override
    public void onRuntimeUnavailable()
    {
        runtime = null;
        Progression.invalidate();
    }

    public static boolean hasRuntime()
    {
        return runtime != null;
    }

    // JEI's search text is shared state: mods with a "sync with JEI" search, like Create's stock
    // keeper, read and write it. The book uses it as its own, so those mods stay in sync with the book.
    public static String searchText()
    {
        return runtime == null ? "" : runtime.getIngredientFilter().getFilterText();
    }

    public static void setSearchText(String text)
    {
        if (runtime != null)
            runtime.getIngredientFilter().setFilterText(text);
    }

    // Whether a mod lets JEI move recipes into this menu: a storage terminal, a modded crafting
    // table, Create's stock keeper. A screen like that is somewhere a player looks recipes up.
    public static boolean acceptsRecipes(AbstractContainerMenu menu)
    {
        if (runtime == null)
            return false;
        return runtime.getRecipeManager().createRecipeCategoryLookup().get()
                .anyMatch(category -> transferHandler(menu, category).isPresent());
    }

    static <R> Optional<IRecipeTransferHandler<AbstractContainerMenu, R>> transferHandler(AbstractContainerMenu menu,
            IRecipeCategory<R> category)
    {
        return runtime == null
                ? Optional.empty()
                : runtime.getRecipeTransferManager().getRecipeTransferHandler(menu, category);
    }

    public static RecipeIndexSnapshot buildRecipeIndex()
    {
        return runtime == null ? RecipeIndexSnapshot.EMPTY : RecipeIndexBuilder.build(runtime);
    }

    // The ingredient types a player browses as craftable results. Deliberately not "all registered
    // types": mods register exotic types (tag-like pseudo-ingredients, etc.) that are noise here.
    private static final List<IIngredientType<?>> BROWSABLE_TYPES =
            List.of(VanillaTypes.ITEM_STACK, NeoForgeTypes.FLUID_STACK);

    public static List<BookIngredient> getAllIngredients()
    {
        if (runtime == null)
            return List.of();

        IIngredientManager manager = runtime.getIngredientManager();
        List<BookIngredient> result = new ArrayList<>();
        for (IIngredientType<?> type : BROWSABLE_TYPES)
            collect(manager, type, result);
        return result;
    }

    // Only ingredients the player can actually make: an item like an oak log is a valid ingredient
    // but has no recipe producing it, so it would just be dead weight in the browse grid.
    public static List<BookIngredient> producible(List<BookIngredient> ingredients)
    {
        if (runtime == null)
            return List.of();

        IRecipeManager recipes = runtime.getRecipeManager();
        IFocusFactory focusFactory = runtime.getJeiHelpers().getFocusFactory();
        return ingredients.stream()
                .filter(item -> isProducible(recipes, focusFactory, item))
                .toList();
    }

    private static <V> void collect(IIngredientManager manager, IIngredientType<V> type,
            List<BookIngredient> out)
    {
        var renderer = manager.getIngredientRenderer(type);
        var helper = manager.getIngredientHelper(type);
        for (V ingredient : manager.getAllIngredients(type))
            //? if >=1.21.5 {
            /*manager.createTypedIngredient(type, ingredient, false)
            *///?} else {
            manager.createTypedIngredient(type, ingredient)
            //?}
                    .ifPresent(typed -> out.add(BookIngredient.of(typed, renderer, helper)));
    }

    //? if >=1.21.5 {
    /*public static List<Component> getTooltip(IRecipeSlotView slot, TooltipFlag flag)
    {
        if (runtime == null)
            return List.of();
        return slot.getDisplayedIngredient()
                .map(ingredient -> getTooltip(runtime.getIngredientManager(), ingredient, flag))
                .orElseGet(List::of);
    }

    private static <V> List<Component> getTooltip(IIngredientManager manager,
            ITypedIngredient<V> ingredient, TooltipFlag flag)
    {
        return manager.getIngredientRenderer(ingredient.getType()).getTooltip(ingredient.getIngredient(), flag);
    }
    *///?}

    // Wrap an ingredient (e.g. one clicked inside a shown recipe) so its own recipe can be opened.
    public static Optional<BookIngredient> toBookIngredient(ITypedIngredient<?> typed)
    {
        if (runtime == null)
            return Optional.empty();
        return Optional.of(build(runtime.getIngredientManager(), typed));
    }

    // The unlock key of a held stack, the same one a recipe slot asking for it would carry.
    public static Optional<String> unlockKeyOf(ItemStack stack)
    {
        if (runtime == null || stack.isEmpty())
            return Optional.empty();
        IIngredientManager manager = runtime.getIngredientManager();
        //? if >=1.21.5 {
        /*return manager.createTypedIngredient(VanillaTypes.ITEM_STACK, stack, false)
        *///?} else {
        return manager.createTypedIngredient(VanillaTypes.ITEM_STACK, stack)
        //?}
                .map(typed -> BookIngredient.unlockKey(manager, typed));
    }

    private static <V> BookIngredient build(IIngredientManager manager, ITypedIngredient<V> typed)
    {
        return BookIngredient.of(typed, manager.getIngredientRenderer(typed.getType()),
                manager.getIngredientHelper(typed.getType()));
    }

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
        categoriesFor(recipes, focuses)
                .filter(category -> Progression.isCategoryUnlocked(
                        category.getRecipeType().getUid().toString()))
                .forEach(category ->
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
        String categoryUid = category.getRecipeType().getUid().toString();
        return recipes.createRecipeLookup(category.getRecipeType())
                .get()
                .filter(recipe -> Progression.isRecipeUnlocked(categoryUid, recipe))
                .<Supplier<IRecipeLayoutDrawable<?>>>map(recipe -> () -> layout(recipes, category, recipe, noFocus))
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

        //? if >=1.21.4 {
        /*return recipes.createCraftingStationLookup(category.getRecipeType())
        *///?} else {
        return recipes.createRecipeCatalystLookup(category.getRecipeType())
        //?}
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

    // The non-tag recipe categories matching the given focus (by role and ingredient). Deliberately
    // free of progression: this also answers "is this producible at all?" for the browse grid, whose
    // ingredient list is built once and then filtered per frame. Gating here would bake whatever was
    // locked at load time into that list, and nothing unlocked later could ever appear.
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

    private static <T> void addLayouts(IRecipeManager recipes, IRecipeCategory<T> category,
            List<IFocus<?>> focuses, IFocusGroup group, List<IRecipeLayoutDrawable<?>> out)
    {
        String categoryUid = category.getRecipeType().getUid().toString();
        recipes.createRecipeLookup(category.getRecipeType())
                .limitFocus(focuses)
                .get()
                .filter(recipe -> Progression.isRecipeUnlocked(categoryUid, recipe))
                .forEach(recipe -> out.add(layout(recipes, category, recipe, group)));
    }

    // A slot offering alternatives shows only the ones the player has held, as long as they hold any:
    // mundane potion brews from a dozen ingredients, and cycling a breeze rod they never saw gives it
    // away. JEI already narrows a slot to whatever a focus names, so each held alternative becomes an
    // input focus. A slot with none held keeps showing all of them, or the recipe would not say what
    // it needs.
    private static <T> IRecipeLayoutDrawable<T> layout(IRecipeManager recipes, IRecipeCategory<T> category,
            T recipe, IFocusGroup group)
    {
        IRecipeLayoutDrawable<T> layout = recipes.createRecipeLayoutDrawableOrShowError(category, recipe, group);
        if (!Progression.isGating())
            return layout;

        IIngredientManager manager = runtime.getIngredientManager();
        List<IFocus<?>> focuses = new ArrayList<>(group.getAllFocuses());
        boolean narrowed = false;
        for (IRecipeSlotView slot : layout.getRecipeSlotsView().getSlotViews(RecipeIngredientRole.INPUT))
        {
            List<ITypedIngredient<?>> alternatives = slot.getAllIngredientsList();
            List<ITypedIngredient<?>> held = alternatives.stream().filter(typed -> hasInput(manager, typed)).toList();
            narrowed |= !held.isEmpty() && held.size() < alternatives.size();
            held.forEach(typed -> focuses.add(focus(runtime.getJeiHelpers().getFocusFactory(),
                    RecipeIngredientRole.INPUT, typed)));
        }
        return narrowed
                ? recipes.createRecipeLayoutDrawableOrShowError(category, recipe,
                        runtime.getJeiHelpers().getFocusFactory().createFocusGroup(focuses))
                : layout;
    }

    private static boolean hasInput(IIngredientManager manager, ITypedIngredient<?> typed)
    {
        return Progression.hasInput(BookIngredient.unlockKey(manager, typed),
                typed.getItemStack().map(stack -> BuiltInRegistries.ITEM.getKey(stack.getItem())));
    }
}
