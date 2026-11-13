package com.craftbound.progression;

import java.util.Set;

public record ProgressionRules(boolean enabled, UnlockRule rule, boolean gateCategories,
        Set<String> exemptCategories)
{
    public static final ProgressionRules OPEN =
            new ProgressionRules(false, UnlockRule.ALL_INPUTS, false, Set.of());
}
