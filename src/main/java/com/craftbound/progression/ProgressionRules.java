package com.craftbound.progression;

import java.util.Set;

// The knobs progression is evaluated against, snapshotted from config so the pure logic never
// reaches for a config singleton.
public record ProgressionRules(boolean enabled, UnlockRule rule, boolean gateCategories,
        Set<String> exemptCategories)
{
    // Everything visible, i.e. how the book behaved before progression existed.
    public static final ProgressionRules OPEN =
            new ProgressionRules(false, UnlockRule.ALL_INPUTS, false, Set.of());
}
