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

import com.craftbound.client.integration.Integrations;
import com.craftbound.progression.CatalystCompanions;
import com.craftbound.progression.InputSlot;
import com.craftbound.progression.RecipeIndex;
import com.craftbound.progression.RecipeNode;
import com.craftbound.progression.UnlockKey;

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

// Recipes are keyed by identity: they come straight out of JEI's lookups, and only some of them are
// records with usable equality.
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
    private final Map<UnlockKey, ITypedIngredient<?>> representatives = new HashMap<>();
    private final Set<UnlockKey> unjudged = new HashSet<>();
    private final Set<UnlockKey> invisibleOutputs = new HashSet<>();

    private RecipeIndexBuilder(IJeiRuntime runtime)
    {
        this.recipes = runtime.getRecipeManager();
        this.manager = runtime.getIngredientManager();
        this.fluids = runtime.getJeiHelpers().getPlatformFluidHelper();
        this.noFocus = runtime.getJeiHelpers().getFocusFactory().getEmptyFocusGroup();
    }

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
        Map<UnlockKey, ITypedIngredient<?>> outputs = collector.outputs();
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

    // An unreadable recipe counts as unlocked, but its outputs still come from JEI's own layout and
    // are never hidden: otherwise an item whose only recipe this is would stay out of the book for
    // good. JEI logs a failed layout as an error, so one that failed is not tried again.
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
        Map<UnlockKey, ITypedIngredient<?>> outputs = new HashMap<>();
        layout.get().getRecipeSlotsView().getSlotViews(RecipeIngredientRole.OUTPUT)
                .forEach(slot -> slot.getAllIngredientsList().forEach(output ->
                        outputs.putIfAbsent(BookIngredient.unlockKey(manager, output), output)));
        unjudged.addAll(outputs.keySet());
        represent(outputs);
    }

    // Normalized so the stack size of whichever recipe is read first does not leak into the unlock:
    // otherwise the toast would announce a nugget wearing a 9.
    private void represent(Map<UnlockKey, ITypedIngredient<?>> outputs)
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
