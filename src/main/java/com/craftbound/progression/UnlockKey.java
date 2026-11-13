package com.craftbound.progression;

import net.minecraft.resources.ResourceLocation;

// How progression names the things a recipe produces: the registry id, plus JEI's subtype where one
// id stands for many things. Every potion is `minecraft:potion` and every Create potion fluid is
// `create:potion`, so the id alone would let filling one water bottle reveal every potion in the game.
// JEI counts neither damage nor most components as a subtype, so a recipe outputting a damaged tool
// still unlocks the plain grid entry.
//
// An ingredient type with no registry identity is named by JEI's type uid and its own uid instead,
// which is as fine-grained as we can be for it and never has a subtype to drop.
public record UnlockKey(Kind kind, String id, String subtype)
{
    public enum Kind
    {
        ITEM("item|"),
        FLUID("fluid|"),
        INGREDIENT("");

        private final String prefix;

        Kind(String prefix)
        {
            this.prefix = prefix;
        }
    }

    private static final String SEPARATOR = "|";

    public static UnlockKey ofItem(ResourceLocation id)
    {
        return ofItem(id, "");
    }

    public static UnlockKey ofItem(ResourceLocation id, String subtype)
    {
        return new UnlockKey(Kind.ITEM, id.toString(), subtype);
    }

    public static UnlockKey ofFluid(ResourceLocation id, String subtype)
    {
        return new UnlockKey(Kind.FLUID, id.toString(), subtype);
    }

    public static UnlockKey ofIngredient(String typeUid, String uid)
    {
        return new UnlockKey(Kind.INGREDIENT, typeUid, uid);
    }

    // What the registry id alone names. Recipe inputs are read at that level, since a slot asks for
    // an item, not for one potion of it.
    public UnlockKey withoutSubtype()
    {
        return hasSubtype() ? new UnlockKey(kind, id, "") : this;
    }

    public boolean hasSubtype()
    {
        return kind != Kind.INGREDIENT && !subtype.isEmpty();
    }

    @Override
    public String toString()
    {
        return subtype.isEmpty() ? kind.prefix + id : kind.prefix + id + SEPARATOR + subtype;
    }
}
