package com.craftbound.progression;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import net.minecraft.resources.ResourceLocation;

class UnlockKeyTest
{
    private static final ResourceLocation LAVA = ResourceLocation.parse("minecraft:lava");
    private static final ResourceLocation STICK = ResourceLocation.parse("minecraft:stick");
    private static final ResourceLocation POTION = ResourceLocation.parse("create:potion");

    @Test
    void itemKeysRoundTrip()
    {
        assertEquals(Optional.of(new UnlockKey(UnlockKey.Kind.ITEM, STICK, "")),
                UnlockKey.parse(UnlockKey.ofItem(STICK)));
    }

    // The one that broke the unlock toast: a fluid-only output has to be readable back, or the
    // recipe unlocks with nothing to draw and the notification is dropped.
    @Test
    void fluidKeysRoundTrip()
    {
        assertEquals(Optional.of(new UnlockKey(UnlockKey.Kind.FLUID, LAVA, "")),
                UnlockKey.parse(UnlockKey.ofFluid(LAVA, "")));
    }

    @Test
    void fluidSubtypesRoundTrip()
    {
        assertEquals(Optional.of(new UnlockKey(UnlockKey.Kind.FLUID, POTION, "strength")),
                UnlockKey.parse(UnlockKey.ofFluid(POTION, "strength")));
    }

    // The one that let a single unlocked brewing recipe reveal every potion in the game.
    @Test
    void twoPotionsAreDifferentKeys()
    {
        assertNotEquals(UnlockKey.ofFluid(POTION, "strength"), UnlockKey.ofFluid(POTION, "healing"));
        assertNotEquals(UnlockKey.ofFluid(POTION, "strength"), UnlockKey.ofFluid(POTION, ""));
    }

    @Test
    void aSubtypeContainingTheSeparatorStillYieldsTheFluidId()
    {
        UnlockKey parsed = UnlockKey.parse(UnlockKey.ofFluid(POTION, "long|strength")).orElseThrow();

        assertEquals(POTION, parsed.id());
        assertEquals("long|strength", parsed.subtype());
    }

    @Test
    void itemsAndFluidsOfTheSameNameAreDifferentKeys()
    {
        assertNotEquals(UnlockKey.ofItem(LAVA), UnlockKey.ofFluid(LAVA, ""));
    }

    @Test
    void anIngredientTypeWithNoRegistryIdentityIsNotParsed()
    {
        assertTrue(UnlockKey.parse("mymod:weird_type|whatever").isEmpty());
    }

    @Test
    void aMalformedIdIsNotParsed()
    {
        assertTrue(UnlockKey.parse("item|NOT AN ID").isEmpty());
    }
}
