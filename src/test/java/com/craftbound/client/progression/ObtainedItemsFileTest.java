package com.craftbound.client.progression;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import net.minecraft.resources.ResourceLocation;

class ObtainedItemsFileTest
{
    private static ResourceLocation rl(String id)
    {
        return ResourceLocation.parse(id);
    }

    @Test
    void roundTripsThroughDisk(@TempDir Path dir)
    {
        ObtainedItemsFile file = new ObtainedItemsFile(dir.resolve("obtained.json"));
        ObtainedItemsByWorld obtained = new ObtainedItemsByWorld();
        obtained.record("server/example.com", List.of(rl("minecraft:stick"), rl("create:cogwheel")));

        file.write(obtained);

        assertEquals(obtained.asMap(), file.read().asMap());
    }

    @Test
    void readsNothingWhenFileIsMissing(@TempDir Path dir)
    {
        assertEquals(Set.of(), new ObtainedItemsFile(dir.resolve("absent.json")).read().of("server/example.com"));
    }

    @Test
    void malformedFileIsIgnoredRatherThanThrowing(@TempDir Path dir) throws IOException
    {
        Path path = dir.resolve("obtained.json");
        Files.writeString(path, "{not json at all");

        assertEquals(Set.of(), new ObtainedItemsFile(path).read().of("server/example.com"));
    }

    @Test
    void entriesThatAreNotItemIdsAreIgnoredRatherThanThrowing(@TempDir Path dir) throws IOException
    {
        Path path = dir.resolve("obtained.json");
        Files.writeString(path, "{\"server/example.com\": [\"NOT AN ID\"]}");

        assertEquals(Set.of(), new ObtainedItemsFile(path).read().of("server/example.com"));
    }

    @Test
    void unwritablePathIsLoggedRatherThanThrowing(@TempDir Path dir)
    {
        // A directory where the file should be: writing must fail, and the game must not notice.
        Path path = dir.resolve("obtained.json");
        assertTrue(path.toFile().mkdir());

        ObtainedItemsByWorld obtained = new ObtainedItemsByWorld();
        obtained.record("server/example.com", List.of(rl("minecraft:stick")));

        new ObtainedItemsFile(path).write(obtained);

        assertFalse(Files.exists(dir.resolve("obtained.json.tmp")), "half-written file left behind");
    }

    @Test
    void writeLeavesNoTemporaryFileBehind(@TempDir Path dir)
    {
        Path path = dir.resolve("obtained.json");
        ObtainedItemsByWorld obtained = new ObtainedItemsByWorld();
        obtained.record("server/example.com", List.of(rl("minecraft:stick")));

        new ObtainedItemsFile(path).write(obtained);

        assertTrue(Files.exists(path));
        assertFalse(Files.exists(dir.resolve("obtained.json.tmp")));
    }
}
