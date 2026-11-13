package com.craftbound.progression;

import net.minecraft.resources.ResourceLocation;

// Item and fluid keys written the way they print, so a test reads like the keys it compares.
public final class Keys
{
    public static UnlockKey key(String text)
    {
        for (UnlockKey.Kind kind : new UnlockKey.Kind[] {UnlockKey.Kind.ITEM, UnlockKey.Kind.FLUID})
        {
            String prefix = kind.name().toLowerCase() + "|";
            if (text.startsWith(prefix))
            {
                String rest = text.substring(prefix.length());
                int split = rest.indexOf('|');
                String id = split < 0 ? rest : rest.substring(0, split);
                String subtype = split < 0 ? "" : rest.substring(split + 1);
                return new UnlockKey(kind, ResourceLocation.parse(id).toString(), subtype);
            }
        }
        throw new IllegalArgumentException("not an item or fluid key: " + text);
    }

    private Keys() {}
}
