package com.craftbound.progression;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import net.minecraft.resources.ResourceLocation;

class UnlockKeyTest
{
    private static final ResourceLocation LAVA = ResourceLocation.parse("minecraft:lava");
    private static final ResourceLocation STICK = ResourceLocation.parse("minecraft:stick");
    private static final ResourceLocation POTION = ResourceLocation.parse("create:potion");
    private static final ResourceLocation WATER_BOTTLE = ResourceLocation.parse("minecraft:potion");

    @Test
    void keysPrintInTheirTextForm()
    {
        assertEquals("item|minecraft:stick", UnlockKey.ofItem(STICK).toString());
        assertEquals("item|minecraft:potion|water", UnlockKey.ofItem(WATER_BOTTLE, "water").toString());
        assertEquals("fluid|minecraft:lava", UnlockKey.ofFluid(LAVA, "").toString());
        assertEquals("fluid|create:potion|strength", UnlockKey.ofFluid(POTION, "strength").toString());
        assertEquals("mymod:weird_type|whatever", UnlockKey.ofIngredient("mymod:weird_type", "whatever").toString());
    }

    @Test
    void everyKindRoundTrips()
    {
        for (UnlockKey key : new UnlockKey[] {
                UnlockKey.ofItem(STICK),
                UnlockKey.ofItem(WATER_BOTTLE, "water"),
                UnlockKey.ofFluid(LAVA, ""),
                UnlockKey.ofFluid(POTION, "strength"),
                UnlockKey.ofIngredient("mymod:weird_type", "whatever|more")})
            assertEquals(Optional.of(key), UnlockKey.parse(key.toString()));
    }

    @Test
    void aSubtypeContainingTheSeparatorStillYieldsTheFluidId()
    {
        UnlockKey parsed = UnlockKey.parse("fluid|create:potion|long|strength").orElseThrow();

        assertEquals(UnlockKey.ofFluid(POTION, "long|strength"), parsed);
    }

    @Test
    void twoPotionsAreDifferentKeys()
    {
        assertNotEquals(UnlockKey.ofFluid(POTION, "strength"), UnlockKey.ofFluid(POTION, "healing"));
        assertNotEquals(UnlockKey.ofFluid(POTION, "strength"), UnlockKey.ofFluid(POTION, ""));
    }

    @Test
    void twoPotionItemsAreDifferentKeys()
    {
        assertNotEquals(UnlockKey.ofItem(WATER_BOTTLE, "water"), UnlockKey.ofItem(WATER_BOTTLE, "strength"));
    }

    @Test
    void droppingTheSubtypeLeavesTheRegistryKey()
    {
        assertEquals(UnlockKey.ofItem(WATER_BOTTLE), UnlockKey.ofItem(WATER_BOTTLE, "long|strength").withoutSubtype());
        assertEquals(UnlockKey.ofFluid(POTION, ""), UnlockKey.ofFluid(POTION, "strength").withoutSubtype());
        UnlockKey stick = UnlockKey.ofItem(STICK);
        assertSame(stick, stick.withoutSubtype());
    }

    @Test
    void anIngredientWithNoRegistryIdentityHasNoSubtypeToDrop()
    {
        UnlockKey weird = UnlockKey.ofIngredient("mymod:weird_type", "whatever|more");

        assertFalse(weird.hasSubtype());
        assertSame(weird, weird.withoutSubtype());
        assertTrue(UnlockKey.ofItem(WATER_BOTTLE, "water").hasSubtype());
    }

    @Test
    void itemsAndFluidsOfTheSameNameAreDifferentKeys()
    {
        assertNotEquals(UnlockKey.ofItem(LAVA), UnlockKey.ofFluid(LAVA, ""));
    }

    @Test
    void textWithoutASeparatorIsNotAKey()
    {
        assertTrue(UnlockKey.parse("minecraft:stick").isEmpty());
    }

    @Test
    void aMalformedIdIsNotParsed()
    {
        assertTrue(UnlockKey.parse("item|NOT AN ID").isEmpty());
    }
}
