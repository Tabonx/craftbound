package com.craftbound.platform;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.Test;

import com.craftbound.progression.ProgressionRules;
import com.craftbound.progression.UnlockRule;

// The client asks for rules before any server config has arrived, so the unloaded config has to
// answer with its defaults rather than throw.
class CraftboundConfigTest
{
    @Test
    void anUnloadedConfigHandsOutItsDefaults()
    {
        assertEquals(new ProgressionRules(true, UnlockRule.ALL_INPUTS, true, Set.of("minecraft:crafting")),
                CraftboundConfig.progression());
        assertTrue(CraftboundConfig.gateHintsBehindLens());
    }
}
