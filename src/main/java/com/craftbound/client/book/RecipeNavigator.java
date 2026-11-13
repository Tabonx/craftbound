package com.craftbound.client.book;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

// Which recipe the book shows: the groups an item's recipes fall into, one of them chosen, and one
// recipe of it. A group's recipes come either for the focused item or for the whole group, and are
// built only once shown, since a whole crafting category runs to thousands.
public final class RecipeNavigator<G, R>
{
    private final Function<G, List<Supplier<R>>> focused;
    private final Function<G, List<Supplier<R>>> whole;

    private List<G> groups = List.of();
    private int group = 0;
    private List<Supplier<R>> body = List.of();
    private List<R> built = List.of();
    private int index = 0;

    public RecipeNavigator(Function<G, List<Supplier<R>>> focused, Function<G, List<Supplier<R>>> whole)
    {
        this.focused = focused;
        this.whole = whole;
    }

    public boolean isOpen()
    {
        return !groups.isEmpty();
    }

    public void open(List<G> groups, int start)
    {
        this.groups = groups;
        selectGroup(start);
    }

    public void close()
    {
        groups = List.of();
        group = 0;
        show(List.of());
    }

    public void selectGroup(int target)
    {
        group = clampGroup(target);
        show(focused.apply(groups.get(group)));
    }

    // Stays on the group shown when the chosen one has nothing to show.
    public void showAll(int target)
    {
        int chosen = clampGroup(target);
        List<Supplier<R>> all = whole.apply(groups.get(chosen));
        if (all.isEmpty())
            return;
        group = chosen;
        show(all);
    }

    public void step(int delta)
    {
        index = Math.max(0, Math.min(body.size() - 1, index + delta));
    }

    public R current()
    {
        R recipe = built.get(index);
        if (recipe == null)
        {
            recipe = body.get(index).get();
            built.set(index, recipe);
        }
        return recipe;
    }

    public List<G> groups()
    {
        return groups;
    }

    public int group()
    {
        return group;
    }

    public int index()
    {
        return index;
    }

    public int size()
    {
        return body.size();
    }

    private void show(List<Supplier<R>> recipes)
    {
        body = recipes;
        built = new ArrayList<>(Collections.nCopies(recipes.size(), null));
        index = 0;
    }

    private int clampGroup(int target)
    {
        return Math.max(0, Math.min(groups.size() - 1, target));
    }
}
