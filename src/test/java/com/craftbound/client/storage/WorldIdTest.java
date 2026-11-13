package com.craftbound.client.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.nio.file.Path;
import java.util.Optional;

import org.junit.jupiter.api.Test;

class WorldIdTest
{
    @Test
    void saveIsKnownByItsFolder()
    {
        WorldId id = WorldId.save(Path.of("saves", "New World (1)", "."), "New World");

        assertEquals("save/New World (1)", id.key());
        assertEquals(Optional.of("world/New World"), id.legacyKey());
    }

    @Test
    void savesWithTheSameNameAreApart()
    {
        assertNotEquals(WorldId.save(Path.of("saves/New World"), "New World").key(),
                WorldId.save(Path.of("saves/New World (1)"), "New World").key());
    }

    @Test
    void serverHasNoLegacyKey()
    {
        assertEquals(new WorldId("server/play.example.net", Optional.empty()), WorldId.server("play.example.net"));
    }
}
