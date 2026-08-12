package com.craftbound.progression.create;

import java.util.List;
import java.util.Set;

import com.craftbound.progression.InputSlot;

import net.minecraft.resources.ResourceLocation;

// Create's basin recipes need a heat source under the basin, but the JEI category draws that as
// decoration rather than as a recipe slot, so the generic slot walk cannot see it. Without this a
// superheated recipe looks makeable the moment its ingredients are in hand.
//
// Expressed as extra input slots, which is exactly what a heat source is: another thing you must
// have obtained. That keeps Unlocks free of any Create knowledge.
//
// Keyed by the heat condition's name rather than Create's enum so the mapping stays pure and
// testable without Create on the classpath.
public final class HeatRequirement
{
    public static final String NONE = "NONE";
    public static final String HEATED = "HEATED";
    public static final String SUPERHEATED = "SUPERHEATED";

    private static final ResourceLocation BLAZE_BURNER = create("blaze_burner");
    // Superheating needs the burner fed a blaze cake; a plain lit burner only ever reaches "heated".
    private static final ResourceLocation BLAZE_CAKE = create("blaze_cake");

    public static List<InputSlot> slotsFor(String heatCondition)
    {
        return switch (heatCondition)
        {
            case HEATED -> List.of(InputSlot.ofItems(Set.of(BLAZE_BURNER)));
            case SUPERHEATED -> List.of(InputSlot.ofItems(Set.of(BLAZE_BURNER)),
                    InputSlot.ofItems(Set.of(BLAZE_CAKE)));
            default -> List.of();
        };
    }

    private static ResourceLocation create(String path)
    {
        return ResourceLocation.fromNamespaceAndPath("create", path);
    }

    private HeatRequirement() {}
}
