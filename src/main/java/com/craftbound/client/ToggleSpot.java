package com.craftbound.client;

import java.util.List;
import java.util.function.BooleanSupplier;

// Where the book's toggle stands on a screen. Kept free of Minecraft types so it can be unit-tested.
enum ToggleSpot
{
    // Where the screen's own recipe-book button was.
    VANILLA,
    // In the screen's top-left corner, where a crafting table keeps its button.
    INSIDE,
    // In the open book's bottom-left corner.
    CARRIED,
    // Left of the closed book's screen.
    OUTSIDE;

    static final int INSIDE_X = 5;
    // The crafting table's button stands this far above the middle of the screen. The inside spot is
    // a little lower, to clear the anvil's hammer.
    private static final int VANILLA_ABOVE_MIDDLE = 49;
    private static final int ANVIL_CLEARANCE = 3;
    private static final int SLOT_SIZE = 16;
    // A slot's frame is drawn a pixel out from the item on every side.
    private static final int SLOT_FRAME = 1;

    // Over a hidden screen the toggle cannot stand on it, so the open book carries it.
    static ToggleSpot of(boolean vanillaSpot, BooleanSupplier insideFree, boolean overlay, boolean open)
    {
        if (!overlay && vanillaSpot)
            return VANILLA;
        if (!overlay && insideFree.getAsBoolean())
            return INSIDE;
        return open ? CARRIED : OUTSIDE;
    }

    static int insideY(int imageHeight)
    {
        return imageHeight / 2 - VANILLA_ABOVE_MIDDLE + ANVIL_CLEARANCE;
    }

    // A vanilla screen with the corner free of slots, such as the brewing stand. A modded screen's
    // art is unknown, so its toggle stays outside where it covers nothing. Slots are given relative
    // to the screen, by where their item is drawn.
    static boolean insideFree(String screenClass, int imageHeight, List<Rect> slots)
    {
        if (!screenClass.startsWith("net.minecraft."))
            return false;
        Rect toggle = new Rect(INSIDE_X, insideY(imageHeight), RecipeBookToggleButton.WIDTH,
                RecipeBookToggleButton.HEIGHT);
        return slots.stream().map(ToggleSpot::frame).noneMatch(toggle::overlaps);
    }

    static Rect slot(int x, int y)
    {
        return new Rect(x, y, SLOT_SIZE, SLOT_SIZE);
    }

    private static Rect frame(Rect slot)
    {
        return new Rect(slot.x() - SLOT_FRAME, slot.y() - SLOT_FRAME, slot.width() + 2 * SLOT_FRAME,
                slot.height() + 2 * SLOT_FRAME);
    }
}
