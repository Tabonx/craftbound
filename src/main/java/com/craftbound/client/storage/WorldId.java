package com.craftbound.client.storage;

import java.nio.file.Path;
import java.util.Optional;

// Which save or server client-side data belongs to. A save is known by its folder, which no other
// save shares and which survives renaming the world. Earlier versions used the world's name, kept
// as the legacy key so a store can take that entry over.
public record WorldId(String key, Optional<String> legacyKey)
{
    public static final WorldId UNKNOWN = new WorldId("unknown", Optional.empty());

    public static WorldId save(Path folder, String levelName)
    {
        return new WorldId("save/" + folder.toAbsolutePath().normalize().getFileName(),
                Optional.of("world/" + levelName));
    }

    public static WorldId server(String address)
    {
        return new WorldId("server/" + address, Optional.empty());
    }
}
