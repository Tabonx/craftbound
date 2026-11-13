package com.craftbound.client.book;

import java.util.HashMap;
import java.util.Map;

// Vanilla's recipe-book highlight: a newly unlocked entry swells and settles once, a half sine over
// fifteen ticks. Advanced once a frame, so an entry drawn twice in a frame, in the grid and in its
// variants panel, still swells at the pace of one.
public final class UnlockHighlights<K>
{
    static final float TICKS = 15f;

    private final Map<K, Float> remaining = new HashMap<>();

    public void start(K key)
    {
        remaining.put(key, TICKS);
    }

    public void advance(float ticks)
    {
        remaining.replaceAll((key, left) -> left - ticks);
        remaining.values().removeIf(left -> left <= 0f);
    }

    // 1 for an entry not swelling.
    public float scale(K key)
    {
        Float left = remaining.get(key);
        return left == null ? 1f : 1f + 0.1f * (float) Math.sin(left / TICKS * Math.PI);
    }

    public boolean active(K key)
    {
        return remaining.containsKey(key);
    }
}
