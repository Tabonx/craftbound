package com.craftbound.progression;

import java.util.Set;

import net.minecraft.resources.ResourceLocation;

// What one recipe slot demands, as alternatives: any one of them satisfies it.
//
// Items are satisfied by having obtained them. Fluids cannot be obtained, since they never enter an
// inventory, so they are satisfied by being unlockable: some unlocked recipe produces them. That
// is what stops a Bar of Chocolate appearing before the chocolate itself is reachable.
//
// `items` also carries a fluid's bucket, which only ever helps satisfy the slot; whether the slot
// can be judged at all is decided by the fluids (see Unlocks#judgeable), so a bucket never turns an
// unjudgeable water slot into a gate.
public record InputSlot(Set<ResourceLocation> items, Set<String> fluids)
{
    public static InputSlot ofItems(Set<ResourceLocation> items)
    {
        return new InputSlot(items, Set.of());
    }
}
