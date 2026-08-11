package com.craftbound.client;

// Pure paging math for the book's left tab rail: how many tabs fit, how far it can scroll, and
// where the scroll window has to sit to keep a given tab visible. Kept free of Minecraft types so
// it can be unit-tested.
public final class RailLayout
{
    // Tabs that fit the rail at once; past this the rail pages, with ▲/▼ claiming the two ends.
    public static final int MAX_TABS = 6;
    public static final int PAGED_TABS = 5;

    public static boolean paged(int count)
    {
        return count > MAX_TABS;
    }

    public static int visibleTabs(int count)
    {
        return paged(count) ? PAGED_TABS : count;
    }

    public static int maxOffset(int count)
    {
        return Math.max(0, count - visibleTabs(count));
    }

    public static int clampOffset(int offset, int count)
    {
        return Math.max(0, Math.min(maxOffset(count), offset));
    }

    // The offset that brings tab index into the visible window, moving as little as possible.
    public static int offsetShowing(int index, int offset, int count)
    {
        int visible = visibleTabs(count);
        if (index < offset)
            offset = index;
        else if (index >= offset + visible)
            offset = index - visible + 1;
        return clampOffset(offset, count);
    }

    private RailLayout() {}
}
