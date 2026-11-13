package com.craftbound.client.storage;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;

class WorldScopedStoreTest
{
    private static final Codec<List<String>> CODEC = Codec.STRING.listOf().xmap(ArrayList::new, List::copyOf);
    private static final WorldId SERVER = WorldId.server("example.com");
    private static final WorldId OTHER = WorldId.server("other.net");

    private static WorldScopedStore<List<String>> store(Path path)
    {
        return new WorldScopedStore<>(path, CODEC, ArrayList::new);
    }

    private static void add(WorldScopedStore<List<String>> store, WorldId world, String value)
    {
        store.open(world, JsonOps.INSTANCE).add(value);
        store.markDirty();
    }

    private static JsonObject onDisk(Path path) throws IOException
    {
        return JsonParser.parseString(Files.readString(path)).getAsJsonObject();
    }

    private static List<Path> quarantined(Path dir) throws IOException
    {
        try (Stream<Path> files = Files.list(dir))
        {
            return files.filter(file -> file.getFileName().toString().contains(".corrupt-")).toList();
        }
    }

    @Test
    void roundTripsThroughDisk(@TempDir Path dir)
    {
        Path path = dir.resolve("store.json");
        WorldScopedStore<List<String>> store = store(path);
        add(store, SERVER, "torch");
        store.close();

        assertEquals(List.of("torch"), store(path).open(SERVER, JsonOps.INSTANCE));
    }

    @Test
    void worldsDoNotShareEntries(@TempDir Path dir)
    {
        Path path = dir.resolve("store.json");
        WorldScopedStore<List<String>> store = store(path);
        add(store, SERVER, "torch");
        store.close();

        assertEquals(List.of(), store(path).open(OTHER, JsonOps.INSTANCE));
    }

    @Test
    void openingAnotherWorldSavesTheFirst(@TempDir Path dir)
    {
        Path path = dir.resolve("store.json");
        WorldScopedStore<List<String>> store = store(path);
        add(store, SERVER, "torch");
        add(store, OTHER, "cog");
        store.close();

        assertEquals(List.of("torch"), store(path).open(SERVER, JsonOps.INSTANCE));
        assertEquals(List.of("cog"), store(path).open(OTHER, JsonOps.INSTANCE));
    }

    @Test
    void nothingIsWrittenUntilSomethingChanges(@TempDir Path dir)
    {
        Path path = dir.resolve("store.json");
        WorldScopedStore<List<String>> store = store(path);
        store.open(SERVER, JsonOps.INSTANCE);
        store.close();

        assertFalse(Files.exists(path));
    }

    @Test
    void otherWorldsStayAsTheyWereEvenWhenTheyDoNotDecode(@TempDir Path dir) throws IOException
    {
        Path path = dir.resolve("store.json");
        Files.writeString(path, "{\"server/other.net\": [1, {\"from\": \"another version\"}], \"server/example.com\": [\"torch\"]}");
        JsonObject before = onDisk(path);

        WorldScopedStore<List<String>> store = store(path);
        add(store, SERVER, "cog");
        store.close();

        JsonObject after = onDisk(path);
        assertEquals(before.get("server/other.net"), after.get("server/other.net"));
        assertEquals(JsonParser.parseString("[\"torch\", \"cog\"]"), after.get("server/example.com"));
        assertEquals(List.of(), quarantined(dir));
    }

    @Test
    void malformedFileIsMovedAsideAndReadsEmpty(@TempDir Path dir) throws IOException
    {
        Path path = dir.resolve("store.json");
        Files.writeString(path, "{not json at all");

        assertEquals(List.of(), store(path).open(SERVER, JsonOps.INSTANCE));

        List<Path> aside = quarantined(dir);
        assertEquals(1, aside.size());
        assertEquals("{not json at all", Files.readString(aside.get(0)));
        assertFalse(Files.exists(path));
    }

    @Test
    void fileThatIsNotAnObjectIsMovedAside(@TempDir Path dir) throws IOException
    {
        Path path = dir.resolve("store.json");
        Files.writeString(path, "[\"torch\"]");

        WorldScopedStore<List<String>> store = store(path);
        add(store, SERVER, "cog");
        store.close();

        assertEquals(1, quarantined(dir).size());
        assertEquals(List.of("cog"), store(path).open(SERVER, JsonOps.INSTANCE));
    }

    @Test
    void partlyDecodableEntryKeepsWhatDecodedAndSetsTheOriginalAside(@TempDir Path dir) throws IOException
    {
        Path path = dir.resolve("store.json");
        String original = "{\"server/example.com\": [\"torch\", 5, \"cog\"], \"server/other.net\": [\"stick\"]}";
        Files.writeString(path, original);

        WorldScopedStore<List<String>> store = store(path);
        assertEquals(List.of("torch", "cog"), store.open(SERVER, JsonOps.INSTANCE));
        store.close();

        List<Path> aside = quarantined(dir);
        assertEquals(1, aside.size());
        assertEquals(original, Files.readString(aside.get(0)));
        assertEquals(List.of("stick"), store(path).open(OTHER, JsonOps.INSTANCE));
        assertEquals(List.of("torch", "cog"), store(path).open(SERVER, JsonOps.INSTANCE));
    }

    @Test
    void legacyEntryIsTakenOverByTheSaveFolder(@TempDir Path dir) throws IOException
    {
        Path path = dir.resolve("store.json");
        Files.writeString(path, "{\"world/New World\": [\"torch\"], \"world/Other\": [\"cog\"]}");
        WorldId save = WorldId.save(dir.resolve("saves/New World (1)"), "New World");

        WorldScopedStore<List<String>> store = store(path);
        assertEquals(List.of("torch"), store.open(save, JsonOps.INSTANCE));
        store.close();

        JsonObject after = onDisk(path);
        assertFalse(after.has("world/New World"));
        assertTrue(after.has("world/Other"));
        assertEquals(JsonParser.parseString("[\"torch\"]"), after.get("save/New World (1)"));
    }

    @Test
    void legacyEntryGoesToOneSaveOnly(@TempDir Path dir) throws IOException
    {
        Path path = dir.resolve("store.json");
        Files.writeString(path, "{\"world/New World\": [\"torch\"]}");

        WorldScopedStore<List<String>> store = store(path);
        store.open(WorldId.save(dir.resolve("saves/New World"), "New World"), JsonOps.INSTANCE);
        store.close();

        assertEquals(List.of(), store(path).open(WorldId.save(dir.resolve("saves/New World (1)"), "New World"),
                JsonOps.INSTANCE));
    }

    @Test
    void legacyEntryIsLeftAloneOnceTheFolderHasOne(@TempDir Path dir) throws IOException
    {
        Path path = dir.resolve("store.json");
        Files.writeString(path, "{\"save/Folder\": [\"cog\"], \"world/Name\": [\"torch\"]}");

        WorldScopedStore<List<String>> store = store(path);
        assertEquals(List.of("cog"), store.open(WorldId.save(dir.resolve("Folder"), "Name"), JsonOps.INSTANCE));
        store.markDirty();
        store.close();

        assertTrue(onDisk(path).has("world/Name"));
    }

    @Test
    void emptyEntriesAreDropped(@TempDir Path dir) throws IOException
    {
        Path path = dir.resolve("store.json");
        WorldScopedStore<List<String>> store = store(path);
        add(store, SERVER, "torch");
        add(store, OTHER, "cog");
        store.open(SERVER, JsonOps.INSTANCE).clear();
        store.markDirty();
        store.close();

        assertFalse(onDisk(path).has("server/example.com"));
    }

    @Test
    void unwritablePathIsLoggedRatherThanThrowing(@TempDir Path dir)
    {
        Path path = dir.resolve("store.json");
        assertTrue(path.toFile().mkdir());

        WorldScopedStore<List<String>> store = store(path);
        add(store, SERVER, "torch");

        assertDoesNotThrow(store::close);
        assertFalse(Files.exists(dir.resolve("store.json.tmp")), "half-written file left behind");
    }

    @Test
    void writeLeavesNoTemporaryFileBehind(@TempDir Path dir)
    {
        Path path = dir.resolve("store.json");
        WorldScopedStore<List<String>> store = store(path);
        add(store, SERVER, "torch");
        store.close();

        assertTrue(Files.exists(path));
        assertFalse(Files.exists(dir.resolve("store.json.tmp")));
    }
}
