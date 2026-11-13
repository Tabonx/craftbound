package com.craftbound.progression;

// Unlock keys written the way they print, so a test reads like the keys it compares.
public final class Keys
{
    public static UnlockKey key(String text)
    {
        return UnlockKey.parse(text).orElseThrow();
    }

    private Keys() {}
}
