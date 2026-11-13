package com.craftbound.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

class IntegrationGuardTest
{
    private final List<String> logged = new ArrayList<>();
    private final IntegrationGuard guard = new IntegrationGuard("Mod", (message, error) -> logged.add(message));

    @Test
    void answersFromTheCallWhileItWorks()
    {
        assertEquals("mod", guard.call(() -> "mod", () -> "fallback"));
        assertTrue(guard.enabled());
        assertTrue(logged.isEmpty());
    }

    @Test
    void aMissingClassSwitchesItOff()
    {
        assertEquals("fallback", guard.call(() -> { throw new NoClassDefFoundError("Moved"); }, () -> "fallback"));
        assertFalse(guard.enabled());
    }

    @Test
    void aRuntimeExceptionSwitchesItOff()
    {
        assertEquals("fallback", guard.call(() -> { throw new IllegalStateException(); }, () -> "fallback"));
        assertFalse(guard.enabled());
    }

    @Test
    void itLogsOnceAndNeverCallsTheModAgain()
    {
        AtomicInteger calls = new AtomicInteger();
        for (int i = 0; i < 3; i++)
            guard.run(() ->
            {
                calls.incrementAndGet();
                throw new IllegalStateException();
            });

        assertEquals(1, calls.get());
        assertEquals(1, logged.size());
    }

    @Test
    void aFailingSideEffectRunsOnce()
    {
        AtomicInteger sent = new AtomicInteger();
        guard.run(() ->
        {
            sent.incrementAndGet();
            throw new IllegalStateException("after sending");
        });

        assertEquals(1, sent.get());
    }

    @Test
    void theFallbackIsOnlyAskedForOnceTheModIsOff()
    {
        AtomicInteger fallbacks = new AtomicInteger();
        guard.call(() -> "mod", () -> "fallback" + fallbacks.incrementAndGet());

        assertEquals(0, fallbacks.get());
    }
}
