package com.craftbound.client.jei;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mojang.logging.LogUtils;

import org.slf4j.Logger;

import com.craftbound.progression.InputSlot;
import com.craftbound.progression.RecipeIndex;
import com.craftbound.progression.RecipeNode;

import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IJeiRuntime;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

// Reduces every recipe JEI knows about to the plain data progression needs, in one pass. Doing it
// per browsable item instead would mean thousands of recipe lookups every time the obtained set
// grows; with the index in hand, re-evaluating unlocks is set arithmetic.
//
// Recipes are keyed by identity because that is what the rest of the book holds: recipe objects
// come straight out of JEI's lookups, and only some of them are records with usable equality.
final class RecipeIndexBuilder
{
    private static final Logger LOGGER = LogUtils.getLogger();

    static RecipeIndexSnapshot build(IJeiRuntime runtime)
    {
        IRecipeManager recipes = runtime.getRecipeManager();
        IIngredientManager manager = runtime.getIngredientManager();
        IFocusGroup noFocus = runtime.getJeiHelpers().getFocusFactory().getEmptyFocusGroup();

        Map<String, Map<Object, RecipeNode>> byCategory = new HashMap<>();
        Map<String, Set<ResourceLocation>> catalysts = new HashMap<>();
        Map<String, ITypedIngredient<?>> representatives = new HashMap<>();

        recipes.createRecipeCategoryLookup().get().forEach(category ->
        {
            String categoryUid = category.getRecipeType().getUid().toString();
            catalysts.put(categoryUid, catalystItems(recipes, category));
            Map<Object, RecipeNode> nodes = new IdentityHashMap<>();
            collect(recipes, manager, category, noFocus, categoryUid, nodes, representatives);
            byCategory.put(categoryUid, nodes);
            LOGGER.debug("Craftbound: indexed {} recipes in {} (catalysts: {})",
                    nodes.size(), categoryUid, catalysts.get(categoryUid));
        });

        return new RecipeIndexSnapshot(RecipeIndex.of(byCategory, catalysts), Map.copyOf(representatives));
    }

    private static <T> void collect(IRecipeManager recipes, IIngredientManager manager,
            IRecipeCategory<T> category, IFocusGroup noFocus, String categoryUid,
            Map<Object, RecipeNode> out, Map<String, ITypedIngredient<?>> representatives)
    {
        recipes.createRecipeLookup(category.getRecipeType()).get().forEach(recipe ->
        {
            SlotIngredientCollector collector = read(manager, category, recipe, noFocus, categoryUid);
            if (collector == null)
                return;

            Map<String, ITypedIngredient<?>> outputs = collector.outputs();
            outputs.forEach(representatives::putIfAbsent);

            List<InputSlot> inputs = new ArrayList<>(collector.inputSlots());
            inputs.addAll(RecipeRequirements.extraSlots(recipe));
            out.put(recipe, new RecipeNode(categoryUid, List.copyOf(inputs), outputs.keySet()));
        });
    }

    // A recipe whose category cannot lay it out is left out of the index entirely; the book treats
    // an unindexed recipe as unlocked, so a broken one stays visible rather than silently vanishing.
    private static <T> SlotIngredientCollector read(IIngredientManager manager,
            IRecipeCategory<T> category, T recipe, IFocusGroup noFocus, String categoryUid)
    {
        if (!category.isHandled(recipe))
            return null;

        SlotIngredientCollector collector = new SlotIngredientCollector(manager);
        try
        {
            category.setRecipe(collector, recipe, noFocus);
        }
        catch (RuntimeException | LinkageError e)
        {
            LOGGER.debug("Craftbound: could not read recipe in category {}", categoryUid, e);
            return null;
        }
        return collector;
    }

    private static Set<ResourceLocation> catalystItems(IRecipeManager recipes, IRecipeCategory<?> category)
    {
        Set<ResourceLocation> items = new HashSet<>();
        recipes.createRecipeCatalystLookup(category.getRecipeType()).get()
                .map(ITypedIngredient::getItemStack)
                .forEach(stack -> stack
                        .map(ItemStack::getItem)
                        .map(BuiltInRegistries.ITEM::getKey)
                        .ifPresent(items::add));
        return items;
    }

    private RecipeIndexBuilder() {}
}
