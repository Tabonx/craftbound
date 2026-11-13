package com.craftbound.progression;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import net.minecraft.resources.ResourceLocation;

class CatalystCompanionsTest
{
    @Test
    void packsAddToTheListBelowThem()
    {
        CatalystCompanions companions = CatalystCompanions.of(List.of(
                json("{\"values\": [\"a:basin\"]}"),
                json("{\"values\": [\"b:tray\"]}")));

        assertEquals(ids("a:basin", "b:tray"), companions.companions());
    }

    @Test
    void replaceStartsTheListOver()
    {
        CatalystCompanions companions = CatalystCompanions.of(List.of(
                json("{\"values\": [\"a:basin\"]}"),
                json("{\"replace\": true, \"values\": [\"b:tray\"]}")));

        assertEquals(ids("b:tray"), companions.companions());
    }

    @Test
    void aFileThatDoesNotParseIsSkipped()
    {
        CatalystCompanions companions = CatalystCompanions.of(List.of(
                json("{\"values\": [\"a:basin\"]}"),
                json("{\"values\": 3}")));

        assertEquals(ids("a:basin"), companions.companions());
    }

    @Test
    void companionsAreDroppedBesideAMachine()
    {
        CatalystCompanions companions = new CatalystCompanions(ids("a:basin"));

        assertEquals(ids("a:mixer"), companions.machinesOf(ids("a:mixer", "a:basin")));
        assertEquals(ids("a:basin"), companions.machinesOf(ids("a:basin")));
    }

    private static JsonElement json(String text)
    {
        return JsonParser.parseString(text);
    }

    private static Set<ResourceLocation> ids(String... ids)
    {
        return Arrays.stream(ids).map(ResourceLocation::parse).collect(Collectors.toSet());
    }
}
