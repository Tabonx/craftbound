package com.craftbound.client;

// Pure paging math for the book's left tab rail: how many tabs fit, how far it can scroll, and
// where the scroll window has to sit to keep a given tab visible. A taller book fits more tabs.
// Kept free of Minecraft types so it can be unit-tested.
public record RailLayout(int maxTabs, int pagedTabs)
{
    // Past maxTabs the rail pages, with ▲/▼ claiming the two ends.
    public static RailLayout forHeight(int height)
    {
        int room = height - BookRail.TAB_TOP;
        return new RailLayout(room / BookRail.TAB_H, (room - 2 * BookRail.ARROW_H) / BookRail.TAB_H);
    }

    public boolean paged(int count)
    {
        return count > maxTabs;
    }

    public int visibleTabs(int count)
    {
        return paged(count) ? pagedTabs : count;
    }

    public int maxOffset(int count)
    {
        return Math.max(0, count - visibleTabs(count));
    }

    public int clampOffset(int offset, int count)
    {
        return Math.max(0, Math.min(maxOffset(count), offset));
    }

    // The offset that brings tab index into the visible window, moving as little as possible.
    public int offsetShowing(int index, int offset, int count)
    {
        int visible = visibleTabs(count);
        if (index < offset)
            offset = index;
        else if (index >= offset + visible)
            offset = index - visible + 1;
        return clampOffset(offset, count);
    }
}
