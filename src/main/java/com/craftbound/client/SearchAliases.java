package com.craftbound.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.craftbound.progression.UnlockKey;

import net.minecraft.resources.ResourceLocation;

// Extra search terms an entry answers to, on top of its display name. Aliases are keyed by
// progression unlock key so they name a registry entry rather than a localized string.
//
// To add one, drop a line in DEFAULTS below: ids without a namespace mean minecraft, and an entry
// may take any number of aliases. Repeating the same id adds to its list rather than replacing it.
public final class SearchAliases
{
    private static final SearchAliases DEFAULTS = builder()
            .item("hopper", "tom holland")
            .build();

    private final Map<String, List<String>> byKey;

    private SearchAliases(Map<String, List<String>> byKey)
    {
        this.byKey = byKey;
    }

    public static boolean matches(String unlockKey, String needle)
    {
        return DEFAULTS.hasMatch(unlockKey, needle);
    }

    public boolean hasMatch(String unlockKey, String needle)
    {
        String lowered = needle.toLowerCase(Locale.ROOT);
        return byKey.getOrDefault(unlockKey, List.of()).stream()
                .anyMatch(alias -> alias.contains(lowered));
    }

    public static Builder builder()
    {
        return new Builder();
    }

    public static final class Builder
    {
        private final Map<String, List<String>> byKey = new HashMap<>();

        public Builder item(String id, String... aliases)
        {
            return add(UnlockKey.ofItem(ResourceLocation.parse(id)), aliases);
        }

        public Builder fluid(String id, String... aliases)
        {
            return add(UnlockKey.ofFluid(ResourceLocation.parse(id), ""), aliases);
        }

        private Builder add(String key, String... aliases)
        {
            List<String> target = byKey.computeIfAbsent(key, unused -> new ArrayList<>());
            for (String alias : aliases)
                target.add(alias.toLowerCase(Locale.ROOT));
            return this;
        }

        public SearchAliases build()
        {
            return new SearchAliases(Map.copyOf(byKey));
        }
    }
}
