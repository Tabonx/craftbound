package com.craftbound.progression;

import java.io.IOException;
import java.io.Reader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import org.slf4j.Logger;

import com.craftbound.Craftbound;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

// Blocks a machine works on, which a mod lists as catalysts beside the machine: Create puts a basin
// under its mixer and press, a depot and a belt under its deployer. Catalysts are alternatives, so a
// basin alone would open mixing, packing and brewing before the player has either machine. These
// companions are dropped, so a category is gated on the machine that does the work.
//
// The list is assets/craftbound/catalyst_companions.json, read from the client's resource packs
// rather than from a tag, since a server without Craftbound sends no tags of ours. Packs stack the
// way tags do: each adds its values, and one that sets replace starts the list over.
public record CatalystCompanions(Set<ResourceLocation> companions)
{
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ResourceLocation FILE =
            ResourceLocation.fromNamespaceAndPath(Craftbound.MODID, "catalyst_companions.json");

    private record Entry(boolean replace, List<ResourceLocation> values)
    {
        static final Codec<Entry> CODEC = RecordCodecBuilder.create(entry -> entry.group(
                Codec.BOOL.optionalFieldOf("replace", false).forGetter(Entry::replace),
                ResourceLocation.CODEC.listOf().fieldOf("values").forGetter(Entry::values))
                .apply(entry, Entry::new));
    }

    public static CatalystCompanions load(ResourceManager resources)
    {
        return of(resources.getResourceStack(FILE).stream().map(CatalystCompanions::read).toList());
    }

    private static JsonElement read(Resource resource)
    {
        try (Reader reader = resource.openAsReader())
        {
            return JsonParser.parseReader(reader);
        }
        catch (IOException | RuntimeException e)
        {
            LOGGER.error("Craftbound: could not read {} from {}", FILE, resource.sourcePackId(), e);
            return null;
        }
    }

    // From the lowest pack to the highest. A file that does not parse is skipped.
    public static CatalystCompanions of(List<JsonElement> files)
    {
        Set<ResourceLocation> companions = new HashSet<>();
        for (JsonElement file : files)
        {
            if (file == null)
                continue;
            Entry entry = Entry.CODEC.parse(JsonOps.INSTANCE, file).resultOrPartial(LOGGER::error).orElse(null);
            if (entry == null)
                continue;
            if (entry.replace())
                companions.clear();
            companions.addAll(entry.values());
        }
        return new CatalystCompanions(Set.copyOf(companions));
    }

    // A category whose only catalysts are companions keeps them, since nothing else could open it.
    public Set<ResourceLocation> machinesOf(Set<ResourceLocation> catalysts)
    {
        Set<ResourceLocation> machines = new HashSet<>(catalysts);
        machines.removeAll(companions);
        return machines.isEmpty() ? catalysts : Set.copyOf(machines);
    }
}
