package com.craftbound.client;

// Pure layout math for docking the book left of the inventory. The recipe panel widens leftward
// from the book's right edge, so the inventory moves only when the book opens or closes. Kept free
// of Minecraft types so it can be unit-tested.
public final class RecipeBookLayout
{
    public static final int BOOK_WIDTH = 147;
    // The width the book widens to while showing a recipe, growing leftward from its right edge.
    public static final int RECIPE_WIDTH = 200;
    public static final int GAP = 8;
    // The book toggle's width and the gap between it and the screen it sits beside.
    public static final int TOGGLE_SPACE = 22;
    // Between the book and that toggle.
    public static final int TOGGLE_GAP = 2;
    // How far the rail's ribbons reach left of the book.
    public static final int RAIL_WIDTH = -BookRail.TAB_X;
    // The least room kept at either screen edge.
    public static final int EDGE = 4;

    private RecipeBookLayout()
    {
    }

    // The inventory's left edge (leftPos).
    public static int inventoryLeftPos(int screenWidth, int imageWidth, boolean bookOpen)
    {
        return bookOpen ? docked(screenWidth, imageWidth, GAP) : centered(screenWidth, imageWidth);
    }

    // A screen with no recipe book of its own carries the toggle just outside its left edge, and the
    // book docks left of the toggle. Closed, the screen stays where it put itself.
    public static int besideLeftPos(int screenWidth, int imageWidth, boolean bookOpen)
    {
        return bookOpen
                ? docked(screenWidth, imageWidth, TOGGLE_SPACE + TOGGLE_GAP)
                : centered(screenWidth, imageWidth);
    }

    // The width to tell such a screen it has, so that centering itself puts it at besideLeftPos.
    // Screens center themselves from their width more often than from leftPos: vanilla's brewing
    // stand draws its background that way. Moving the center moves both.
    public static int besideScreenWidth(int screenWidth, int imageWidth, boolean bookOpen)
    {
        return screenWidth + 2 * (besideLeftPos(screenWidth, imageWidth, bookOpen) - centered(screenWidth, imageWidth));
    }

    // Right edge that book content is aligned to: just left of the inventory, across the gap.
    public static int bookRight(int inventoryLeftPos)
    {
        return inventoryLeftPos - GAP;
    }

    public static int besideBookRight(int leftPos)
    {
        return leftPos - TOGGLE_SPACE - TOGGLE_GAP;
    }

    // The widest the recipe panel may grow left from its right edge with the rail still on screen.
    public static int recipeWidth(int bookRight)
    {
        return Math.max(BOOK_WIDTH, Math.min(RECIPE_WIDTH, bookRight - RAIL_WIDTH - EDGE));
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
