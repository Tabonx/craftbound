package com.craftbound.client.jei;

import java.util.Optional;
import java.util.function.Function;

import mezz.jei.api.runtime.IJeiRuntime;

// A value built from the running JEI, built again once JEI has restarted.
final class RuntimeScoped<T>
{
    private final Function<IJeiRuntime, T> build;
    private int generation = -1;
    private T value = null;

    RuntimeScoped(Function<IJeiRuntime, T> build)
    {
        this.build = build;
    }

    Optional<T> get()
    {
        if (generation != JeiSession.generation())
        {
            value = null;
            generation = JeiSession.generation();
            value = JeiSession.runtime().map(build).orElse(null);
        }
        return Optional.ofNullable(value);
    }
}
