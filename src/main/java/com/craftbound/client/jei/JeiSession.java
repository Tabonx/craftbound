package com.craftbound.client.jei;

import java.util.Collection;
import java.util.Optional;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IJeiRuntime;

// The runtime JEI has handed over, while it has one. JEI hands it over on every reload and takes it
// back before rebuilding, so every start and stop opens a new generation, and whatever was built
// from the runtime holds for one generation only.
public final class JeiSession
{
    private static IJeiRuntime runtime;
    private static int generation = 0;
    private static int ingredientsVersion = 0;

    static void start(IJeiRuntime started)
    {
        runtime = started;
        changed();
        started.getIngredientManager().registerIngredientListener(new IIngredientManager.IIngredientListener()
        {
            @Override
            public <V> void onIngredientsAdded(IIngredientHelper<V> helper, Collection<ITypedIngredient<V>> ingredients)
            {
                ingredientsVersion++;
            }

            @Override
            public <V> void onIngredientsRemoved(IIngredientHelper<V> helper, Collection<ITypedIngredient<V>> ingredients)
            {
                ingredientsVersion++;
            }
        });
    }

    static void stop()
    {
        runtime = null;
        changed();
    }

    private static void changed()
    {
        generation++;
        ingredientsVersion++;
    }

    public static boolean running()
    {
        return runtime != null;
    }

    public static int generation()
    {
        return generation;
    }

    // Moves with the generation, and whenever a mod adds or removes ingredients while JEI runs.
    static int ingredientsVersion()
    {
        return ingredientsVersion;
    }

    static Optional<IJeiRuntime> runtime()
    {
        return Optional.ofNullable(runtime);
    }

    private JeiSession() {}
}
