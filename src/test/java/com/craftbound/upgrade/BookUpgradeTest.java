package com.craftbound.upgrade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.craftbound.upgrade.BookUpgrade.DeathOutcome;

class BookUpgradeTest
{
    @Test
    void hintsStayHiddenUntilTheLensIsBound()
    {
        assertFalse(BookUpgrade.hintsUnlocked(true, true, false));
        assertTrue(BookUpgrade.hintsUnlocked(true, true, true));
    }

    @Test
    void hintsShowOnAServerWithoutCraftbound()
    {
        assertTrue(BookUpgrade.hintsUnlocked(true, false, false));
    }

    @Test
    void hintsShowWhenThePackTurnsTheGateOff()
    {
        assertTrue(BookUpgrade.hintsUnlocked(false, true, false));
    }

    @Test
    void deathDropsTheLensAndUnbindsTheBook()
    {
        assertEquals(new DeathOutcome(false, true), BookUpgrade.onDeath(true, false));
    }

    @Test
    void keepInventoryKeepsTheBookBound()
    {
        assertEquals(new DeathOutcome(true, false), BookUpgrade.onDeath(true, true));
    }

    @Test
    void anUnupgradedBookDropsNothing()
    {
        assertEquals(new DeathOutcome(false, false), BookUpgrade.onDeath(false, false));
        assertEquals(new DeathOutcome(false, false), BookUpgrade.onDeath(false, true));
    }
}
