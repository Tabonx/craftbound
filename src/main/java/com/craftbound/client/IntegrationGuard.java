package com.craftbound.client;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

import com.mojang.logging.LogUtils;

import org.slf4j.Logger;

// Other mods are optional and their version ranges have no upper bound, so their classes can move
// under us. An integration wraps each call into the other mod's own classes in its guard, and only
// those: the first one that fails switches the integration off, and every later call gets the answer
// the book would give without that mod. A failing call is never repeated.
public final class IntegrationGuard
{
    private static final Logger LOGGER = LogUtils.getLogger();

    private final String name;
    private final BiConsumer<String, Throwable> log;
    private volatile boolean enabled = true;

    public IntegrationGuard(String name)
    {
        this(name, (message, error) -> LOGGER.error(message, error));
    }

    IntegrationGuard(String name, BiConsumer<String, Throwable> log)
    {
        this.name = name;
        this.log = log;
    }

    public boolean enabled()
    {
        return enabled;
    }

    public <T> T call(Supplier<T> call, Supplier<T> whenDisabled)
    {
        if (enabled)
        {
            try
            {
                return call.get();
            }
            catch (LinkageError | RuntimeException e)
            {
                enabled = false;
                log.accept("Craftbound: " + name + " integration disabled, this version of " + name
                        + " no longer fits it", e);
            }
        }
        return whenDisabled.get();
    }

    public void run(Runnable call)
    {
        call(() ->
        {
            call.run();
            return null;
        }, () -> null);
    }
}
