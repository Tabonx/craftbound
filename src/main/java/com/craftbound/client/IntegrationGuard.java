package com.craftbound.client;

import java.util.function.Supplier;

import com.mojang.logging.LogUtils;

import org.slf4j.Logger;

// Other mods are optional and their version ranges have no upper bound, so their classes can move
// under us. The first call that fails switches the whole integration off, and every later call gets
// the answer the book would give without that mod.
public final class IntegrationGuard
{
    private static final Logger LOGGER = LogUtils.getLogger();

    private final String name;
    private volatile boolean enabled = true;

    public IntegrationGuard(String name)
    {
        this.name = name;
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
                LOGGER.error("Craftbound: {} integration disabled, this version of {} no longer fits it", name, name, e);
            }
        }
        return whenDisabled.get();
    }

    // Whether the call went through.
    public boolean run(Runnable call)
    {
        return call(() ->
        {
            call.run();
            return true;
        }, () -> false);
    }
}
