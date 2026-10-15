package com.craftbound.client;

import java.util.List;

// What a screen draws past its own left edge, read from the areas mods report to recipe viewers so
// those keep clear of them: AE2's toolbar, Curios' slot panel, Mekanism's side tabs. Relative to the
// screen, so it holds wherever the screen moves. Kept free of Minecraft types so it can be
// unit-tested.
public record SideAreas(int reach, int floor)
{
    public static final SideAreas NONE = new SideAreas(0, Integer.MAX_VALUE);

    // An area ending this close to the screen's left edge belongs to the screen.
    static final int ATTACHED = 8;

    public record Area(int x, int y, int width, int height)
    {
    }

    // `reach`: how far left of leftPos the screen's own elements go. `floor`: how far below topPos
    // the whole left side is taken. An area that spans from the window's edge to the screen claims
    // those rows outright, as Create's stock keeper does for its keeper, rather than saying the screen
    // is that wide. The keeper is drawn taller than the rows claimed for it, so the floor keeps as far
    // above them again as they reach into the screen's height.
    public static SideAreas of(List<Area> areas, int leftPos, int topPos, int imageHeight)
    {
        int reach = 0;
        int floor = Integer.MAX_VALUE;
        for (Area area : areas)
        {
            int right = area.x() + area.width();
            boolean besideScreen = area.y() < topPos + imageHeight && area.y() + area.height() > topPos;
            if (area.x() >= leftPos || right < leftPos - ATTACHED || !besideScreen)
                continue;
            if (area.x() <= 0 && right >= leftPos - ATTACHED)
            {
                if (area.y() > topPos)
                    floor = Math.min(floor, 2 * (area.y() - topPos) - imageHeight);
            }
            else
                reach = Math.max(reach, leftPos - area.x());
        }
        return new SideAreas(reach, floor);
    }
}
