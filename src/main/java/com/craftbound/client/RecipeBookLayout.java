package com.craftbound.client;

// Pure layout math for docking the book left of the inventory. The open book shifts the inventory
// right so the browsing book and the inventory sit centered as a pair, as vanilla does. The recipe
// panel widens leftward from there, so the inventory moves only when the book opens or closes. Kept
// free of Minecraft types so it can be unit-tested.
public final class RecipeBookLayout
{
    public static final int BOOK_WIDTH = 147;
    // The width the book widens to while showing a recipe, growing leftward from its right edge.
    public static final int RECIPE_WIDTH = 200;
    public static final int GAP = 8;

    private RecipeBookLayout()
    {
    }

    // The inventory's left edge (leftPos). Never closer to the screen edge than the widened recipe
    // panel needs, so a narrow window pushes the pair off center rather than the panel off screen.
    public static int inventoryLeftPos(int screenWidth, int imageWidth, boolean bookOpen)
    {
        if (!bookOpen)
            return (screenWidth - imageWidth) / 2;

        int browsing = BOOK_WIDTH + GAP;
        int centered = (screenWidth - (browsing + imageWidth)) / 2 + browsing;
        return Math.max(RECIPE_WIDTH + GAP, centered);
    }

    // Right edge that book content is aligned to: just left of the inventory, across the gap.
    public static int bookRight(int inventoryLeftPos)
    {
        return inventoryLeftPos - GAP;
    }
}
