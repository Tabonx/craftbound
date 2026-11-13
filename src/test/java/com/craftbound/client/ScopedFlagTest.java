package com.craftbound.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ScopedFlagTest
{
    private final ScopedFlag flag = new ScopedFlag();

    @Test
    void upOnlyWhileTheWorkRuns()
    {
        assertFalse(flag.active());
        assertTrue(flag.during(flag::active));
        assertFalse(flag.active());
    }

    @Test
    void anInnerScopeLeavesTheOuterOneUp()
    {
        boolean afterInner = flag.during(() ->
        {
            flag.during(() -> { });
            return flag.active();
        });

        assertTrue(afterInner);
        assertFalse(flag.active());
    }

    @Test
    void comesDownWhenTheWorkThrows()
    {
        assertThrows(IllegalStateException.class, () -> flag.during(() -> { throw new IllegalStateException(); }));
        assertFalse(flag.active());
    }

    @Test
    void handsBackWhatTheWorkReturns()
    {
        assertEquals("tooltip", flag.during(() -> "tooltip"));
    }
}
