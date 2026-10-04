package com.craftbound.progression;

import java.util.Optional;

import net.minecraft.resources.ResourceLocation;

// How progression names the things a recipe produces: the registry id, plus JEI's subtype where one
// id stands for many things. Every potion is `minecraft:potion` and every Create potion fluid is
// `create:potion`, so the id alone let filling one water bottle reveal every potion in the game.
// JEI counts neither damage nor most components as a subtype, so a recipe outputting a damaged tool
// still unlocks the plain grid entry.
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
        return ofItem(id, "");
    }

    public static String ofItem(ResourceLocation id, String subtype)
    {
        return withSubtype(ITEM_PREFIX + id, subtype);
    }

    public static String ofFluid(ResourceLocation id, String subtype)
    {
        return withSubtype(FLUID_PREFIX + id, subtype);
    }

    private static String withSubtype(String key, String subtype)
    {
        return subtype.isEmpty() ? key : key + SUBTYPE_SEPARATOR + subtype;
    }

    // Empty for keys naming something other than an item or fluid: ingredient types we have no
    // registry-level identity for fall back to JEI's own uid, which this does not try to read.
    public static Optional<UnlockKey> parse(String key)
    {
        if (key.startsWith(ITEM_PREFIX))
            return parse(Kind.ITEM, key.substring(ITEM_PREFIX.length()));
        if (key.startsWith(FLUID_PREFIX))
            return parse(Kind.FLUID, key.substring(FLUID_PREFIX.length()));
        return Optional.empty();
    }

    // The key with its subtype dropped: what the registry id alone names. Recipe inputs are read at
    // that level, since a slot asks for an item, not for one potion of it.
    public static String withoutSubtype(String key)
    {
        int prefixEnd = key.indexOf(SUBTYPE_SEPARATOR);
        int subtypeStart = key.indexOf(SUBTYPE_SEPARATOR, prefixEnd + 1);
        return parse(key).isEmpty() || subtypeStart < 0 ? key : key.substring(0, subtypeStart);
    }

    // Ids never contain the separator, so the first one ends the id and starts the subtype.
    private static Optional<UnlockKey> parse(Kind kind, String rest)
    {
        int split = rest.indexOf(SUBTYPE_SEPARATOR);
        return split < 0
                ? of(kind, rest, "")
                : of(kind, rest.substring(0, split), rest.substring(split + 1));
    }

    private static Optional<UnlockKey> of(Kind kind, String id, String subtype)
    {
        return Optional.ofNullable(ResourceLocation.tryParse(id))
                .map(parsed -> new UnlockKey(kind, parsed, subtype));
    }
}
