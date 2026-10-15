package com.craftbound.client;

// Pure layout math for the book beside a container screen. The recipe panel widens leftward from
// the book's right edge, so the screen moves only when the book opens or closes. Kept free of
// Minecraft types so it can be unit-tested.
public final class RecipeBookLayout
{
    public static final int BOOK_WIDTH = 147;
    public static final int BOOK_HEIGHT = 166;
    // A grid row: the book shows another one for every this much it grows.
    public static final int ROW = 25;
    // The width the book widens to while showing a recipe, growing leftward from its right edge.
    public static final int RECIPE_WIDTH = 200;
    // Between the book and the screen.
    public static final int GAP = 8;
    // The closed book's toggle beside a screen with no place for it: its width and the gap to the
    // screen.
    public static final int TOGGLE_SPACE = 22;
    // How far the rail's ribbons reach left of the book.
    public static final int RAIL_WIDTH = -BookRail.TAB_X;
    // The least room kept at either screen edge.
    public static final int EDGE = 4;

    // Where the book goes. Docked, it sits left of the screen. Overlaid, the window is too narrow for
    // both, so the book is centered and the screen is hidden behind it, as vanilla does.
    public record Placement(boolean overlay, int bookRight, int bookTop, int bookHeight)
    {
    }

    private RecipeBookLayout()
    {
    }

    // `gap` is everything between the book and the screen's own left edge, whatever the screen draws
    // out there included.
    public static boolean fits(int screenWidth, int imageWidth, int gap)
    {
        return EDGE + RAIL_WIDTH + BOOK_WIDTH + gap + imageWidth + EDGE <= screenWidth;
    }

    public static boolean overlays(int screenWidth, int imageWidth, int gap, boolean open)
    {
        return open && !fits(screenWidth, imageWidth, gap);
    }

    // The screen's left edge (leftPos).
    public static int leftPos(int screenWidth, int imageWidth, int gap, boolean open)
    {
        return open && fits(screenWidth, imageWidth, gap)
                ? docked(screenWidth, imageWidth, gap)
                : centered(screenWidth, imageWidth);
    }

    // The width to tell a screen that centers itself, so that centering puts it at leftPos. Screens
    // center themselves from their width more often than from leftPos: vanilla's brewing stand draws
    // its background that way. Moving the center moves both.
    public static int screenWidthFor(int screenWidth, int imageWidth, int gap, boolean open)
    {
        return screenWidth + 2 * (leftPos(screenWidth, imageWidth, gap, open) - centered(screenWidth, imageWidth));
    }

    // `floor` is how far below topPos the left side is taken by the screen, or MAX_VALUE.
    public static Placement place(int screenWidth, int screenHeight, int leftPos, int topPos, int imageWidth,
            int imageHeight, int gap, int floor)
    {
        boolean overlay = !fits(screenWidth, imageWidth, gap);
        int bookRight = overlay ? (screenWidth - RAIL_WIDTH - BOOK_WIDTH) / 2 + RAIL_WIDTH + BOOK_WIDTH : leftPos - gap;
        int height = bookHeight(screenHeight, imageHeight, overlay ? Integer.MAX_VALUE : floor);
        return new Placement(overlay, bookRight, bookTop(screenHeight, topPos, height), height);
    }

    // The widest the recipe panel may grow left from its right edge with the rail still on screen.
    public static int recipeWidth(int bookRight)
    {
        return Math.max(BOOK_WIDTH, Math.min(RECIPE_WIDTH, bookRight - RAIL_WIDTH - EDGE));
    }

    // As tall as the screen beside it, where the window allows, so their edges line up. Never
    // shorter than vanilla's book.
    static int bookHeight(int screenHeight, int imageHeight, int floor)
    {
        return Math.max(BOOK_HEIGHT, Math.min(imageHeight, Math.min(screenHeight - 2 * EDGE, floor)));
    }

    // Level with the screen's top, unless that would push the book off the window.
    static int bookTop(int screenHeight, int topPos, int bookHeight)
    {
        return Math.max(0, Math.min(topPos, screenHeight - EDGE - bookHeight));
    }

    private static int centered(int screenWidth, int imageWidth)
    {
        return (screenWidth - imageWidth) / 2;
    }

    // The browsing book and the screen sit centered as a pair, as vanilla does, and the screen moves
    // right as far as the full recipe panel and its rail need. The screen itself never leaves the
    // right edge: on a narrow window the recipe panel gives way instead.
    private static int docked(int screenWidth, int imageWidth, int gap)
    {
        int browsing = BOOK_WIDTH + gap;
        int pair = centered(screenWidth, browsing + imageWidth) + browsing;
        int recipe = EDGE + RAIL_WIDTH + RECIPE_WIDTH + gap;
        return Math.min(screenWidth - EDGE - imageWidth, Math.max(recipe, pair));
    }
}
