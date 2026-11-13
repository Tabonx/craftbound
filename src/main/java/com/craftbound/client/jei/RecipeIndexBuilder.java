package com.craftbound.client.jei;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.WeakHashMap;

import com.mojang.logging.LogUtils;

import org.slf4j.Logger;

import com.craftbound.client.Integrations;
import com.craftbound.progression.CatalystCompanions;
import com.craftbound.progression.InputSlot;
import com.craftbound.progression.RecipeIndex;
import com.craftbound.progression.RecipeNode;

import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.helpers.IPlatformFluidHelper;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IJeiRuntime;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;

// Reduces every recipe JEI knows about to the plain data progression needs, in one pass. Doing it
// per browsable item instead would mean thousands of recipe lookups every time the obtained set
// grows; with the index in hand, re-evaluating unlocks is set arithmetic.
//
// Recipes are keyed by identity because that is what the rest of the book holds: recipe objects
// come straight out of JEI's lookups, and only some of them are records with usable equality.
public final class RecipeIndexBuilder
{
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final RuntimeScoped<RecipeIndexSnapshot> CURRENT =
            new RuntimeScoped<>(runtime -> new RecipeIndexBuilder(runtime).build());

    // Recipe to the categories JEI could not lay it out in.
    private static final Map<Object, Set<String>> UNLAYABLE = new WeakHashMap<>();

    private final IRecipeManager recipes;
    private final IIngredientManager manager;
    private final IPlatformFluidHelper<?> fluids;
    private final IFocusGroup noFocus;

    private final Map<String, Map<Object, RecipeNode>> byCategory = new HashMap<>();
    private final Map<String, Set<ResourceLocation>> catalysts = new HashMap<>();
    private final Map<String, ITypedIngredient<?>> representatives = new HashMap<>();
    private final Set<String> unjudged = new HashSet<>();
    private final Set<String> invisibleOutputs = new HashSet<>();

    private RecipeIndexBuilder(IJeiRuntime runtime)
    {
        this.recipes = runtime.getRecipeManager();
        this.manager = runtime.getIngredientManager();
        this.fluids = runtime.getJeiHelpers().getPlatformFluidHelper();
        this.noFocus = runtime.getJeiHelpers().getFocusFactory().getEmptyFocusGroup();
    }

    // Built the first time it is asked for after JEI starts, and kept until JEI stops.
    public static Optional<RecipeIndexSnapshot> current()
    {
        return CURRENT.get();
    }

    private RecipeIndexSnapshot build()
    {
        CatalystCompanions companions = CatalystCompanions.load(Minecraft.getInstance().getResourceManager());
        recipes.createRecipeCategoryLookup().get()
                .filter(BookCategories::isBrowsable)
                .forEach(category ->
        {
            String categoryUid = category.getRecipeType().getUid().toString();
            catalysts.put(categoryUid, companions.machinesOf(catalystItems(category)));
            byCategory.put(categoryUid, nodesOf(category, categoryUid));
        });
        return new RecipeIndexSnapshot(RecipeIndex.of(byCategory, catalysts, unjudged), Map.copyOf(representatives),
                Set.copyOf(invisibleOutputs));
    }

    private <T> Map<Object, RecipeNode> nodesOf(IRecipeCategory<T> category, String categoryUid)
    {
        Map<Object, RecipeNode> nodes = new IdentityHashMap<>();
        recipes.createRecipeLookup(category.getRecipeType()).get()
                .filter(category::isHandled)
                .forEach(recipe -> read(category, recipe, categoryUid).ifPresentOrElse(
                        collector -> nodes.put(recipe, node(collector, recipe, categoryUid)),
                        () -> unjudged(category, recipe, categoryUid)));
        return nodes;
    }

    private RecipeNode node(SlotIngredientCollector collector, Object recipe, String categoryUid)
    {
        Map<String, ITypedIngredient<?>> outputs = collector.outputs();
        represent(outputs);
        invisibleOutputs.addAll(collector.invisibleOutputKeys());
        List<InputSlot> inputs = new ArrayList<>(collector.inputSlots());
        inputs.addAll(Integrations.hiddenRequirements(recipe));
        return new RecipeNode(categoryUid, List.copyOf(inputs), outputs.keySet(), groupOf(categoryUid, recipe));
    }

    private static String groupOf(String categoryUid, Object recipe)
    {
        if (!BookCategories.readsGroups(categoryUid))
            return "";
        String group = recipe instanceof RecipeHolder<?> holder ? holder.value().getGroup() : "";
        return group.isEmpty() ? "" : categoryUid + "|" + group;
    }

    private <T> Optional<SlotIngredientCollector> read(IRecipeCategory<T> category, T recipe, String categoryUid)
    {
        SlotIngredientCollector collector = new SlotIngredientCollector(manager, fluids);
        try
        {
            category.setRecipe(collector, recipe, noFocus);
        }
        catch (RuntimeException | LinkageError e)
        {
            LOGGER.debug("Craftbound: could not read recipe in category {}", categoryUid, e);
            return Optional.empty();
        }
        return Optional.of(collector);
    }

    // A recipe the index cannot read is left out of it, and the book treats a recipe it never
    // indexed as unlocked. JEI's own layout often still reads it, being what JEI's screens show, so
    // what it makes is taken from there and never hidden: otherwise an item whose only recipe this
    // is would stay out of the book for good. JEI logs a layout that fails as an error, so one that
    // failed is not tried again on the next build.
    private <T> void unjudged(IRecipeCategory<T> category, T recipe, String categoryUid)
    {
        if (UNLAYABLE.getOrDefault(recipe, Set.of()).contains(categoryUid))
            return;
        Optional<IRecipeLayoutDrawable<T>> layout = recipes.createRecipeLayoutDrawable(category, recipe, noFocus);
        if (layout.isEmpty())
        {
            UNLAYABLE.computeIfAbsent(recipe, unused -> new HashSet<>()).add(categoryUid);
            return;
        }
        Map<String, ITypedIngredient<?>> outputs = new HashMap<>();
        layout.get().getRecipeSlotsView().getSlotViews(RecipeIngredientRole.OUTPUT)
                .forEach(slot -> slot.getAllIngredientsList().forEach(output ->
                        outputs.putIfAbsent(BookIngredient.unlockKey(manager, output), output)));
        unjudged.addAll(outputs.keySet());
        represent(outputs);
    }

    // The stack a recipe happens to hand back is that recipe's business: nine nuggets from an ingot
    // says nothing about the nugget itself. Whichever recipe is read first would otherwise decide
    // how the item is drawn wherever the book stands for an unlock, and the toast would announce a
    // nugget wearing a 9. Normalizing makes that one item, whatever produced it.
    private void represent(Map<String, ITypedIngredient<?>> outputs)
    {
        outputs.forEach((key, output) -> representatives.putIfAbsent(key, manager.normalizeTypedIngredient(output)));
    }

    private Set<ResourceLocation> catalystItems(IRecipeCategory<?> category)
    {
        Set<ResourceLocation> items = new HashSet<>();
        recipes.createRecipeCatalystLookup(category.getRecipeType()).get()
                .forEach(catalyst -> BookIngredient.itemIdOf(catalyst).ifPresent(items::add));
        return items;
    }
}
