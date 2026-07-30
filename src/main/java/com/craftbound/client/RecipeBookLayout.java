package com.craftbound.client;

// Pure layout math for docking the book left of the inventory. When the book is open we reserve
// a column on the left and shift the inventory right, keeping the book+inventory pair centered —
// the same idea as vanilla shifting its GUI to make room. Kept free of Minecraft types so it can
// be unit-tested.
public final class RecipeBookLayout
{
    public static final int BOOK_WIDTH = 176;
    public static final int GAP = 8;

    private RecipeBookLayout()
    {
    }

    // The inventory's left edge (leftPos). Centered normally; shifted right by the reserved book
    // column when the book is open.
    public static int inventoryLeftPos(int screenWidth, int imageWidth, boolean bookOpen)
    {
        if (!bookOpen)
            return (screenWidth - imageWidth) / 2;

        int reserved = BOOK_WIDTH + GAP;
        int clusterLeft = Math.max(0, (screenWidth - (reserved + imageWidth)) / 2);
        return clusterLeft + reserved;
    }

    // Right edge that book content is aligned to: just left of the inventory, across the gap.
    public static int bookRight(int inventoryLeftPos)
    {
        return inventoryLeftPos - GAP;
    }
}
