package com.craftbound.progression;

import java.util.Optional;

import net.minecraft.resources.ResourceLocation;

// How progression names the things a recipe produces. Registry-level for items on purpose, ignoring
// counts, damage and components, so a recipe outputting a damaged tool still unlocks the plain grid
// entry.
//
// Fluids carry a subtype as well, because collapsing them to the registry id is wrong where one
// fluid stands for many things: every Create potion is `create:potion`, so a single key would let
// one unlocked brewing recipe reveal every potion in the game while their own recipes stayed
// locked. The subtype is JEI's, which is how Create itself distinguishes them.
//
// Kept as a parsed record rather than raw string handling so the format lives in one place and can
// be tested without a registry: reading a key back is how the unlock toast finds an icon.
public record UnlockKey(Kind kind, ResourceLocation id, String subtype)
{
    public enum Kind
    {
        ITEM,
        FLUID
    }

    private static final String ITEM_PREFIX = "item|";
    private static final String FLUID_PREFIX = "fluid|";
    private static final String SUBTYPE_SEPARATOR = "|";

    public static String ofItem(ResourceLocation id)
    {
        return ITEM_PREFIX + id;
    }

    public static String ofFluid(ResourceLocation id, String subtype)
    {
        return subtype.isEmpty()
                ? FLUID_PREFIX + id
                : FLUID_PREFIX + id + SUBTYPE_SEPARATOR + subtype;
    }

    // Empty for keys naming something other than an item or fluid: ingredient types we have no
    // registry-level identity for fall back to JEI's own uid, which this does not try to read.
    public static Optional<UnlockKey> parse(String key)
    {
        if (key.startsWith(ITEM_PREFIX))
            return of(Kind.ITEM, key.substring(ITEM_PREFIX.length()), "");

        if (!key.startsWith(FLUID_PREFIX))
            return Optional.empty();

        // Ids never contain the separator, so the first one ends the id and starts the subtype.
        String rest = key.substring(FLUID_PREFIX.length());
        int split = rest.indexOf(SUBTYPE_SEPARATOR);
        return split < 0
                ? of(Kind.FLUID, rest, "")
                : of(Kind.FLUID, rest.substring(0, split), rest.substring(split + 1));
    }

    private static Optional<UnlockKey> of(Kind kind, String id, String subtype)
    {
        return Optional.ofNullable(ResourceLocation.tryParse(id))
                .map(parsed -> new UnlockKey(kind, parsed, subtype));
    }
}
