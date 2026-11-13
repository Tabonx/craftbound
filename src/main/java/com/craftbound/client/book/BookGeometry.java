package com.craftbound.client.book;

// Where everything on the book's page sits, for a page at (x, y) of the given size. Mirrors vanilla's
// RecipeBookPage. While a recipe is shown the page is wider and its texture stretched, so insets
// from its edges stretch with it.
public record BookGeometry(int x, int y, int width, int height)
{
    public static final int WIDTH = RecipeBookLayout.BOOK_WIDTH;
    public static final int HEIGHT = RecipeBookLayout.BOOK_HEIGHT;

    static final int GRID_X = 11;
    static final int GRID_Y = 31;
    static final int SEARCH_X = 25;
    static final int SEARCH_Y = 13;
    static final int SEARCH_W = 81;
    static final int SEARCH_H = 14;
    // Vanilla's own filter spot right of the search field, where the texture leaves clean parchment.
    private static final int BUTTON_Y = 12;
    private static final int FILTER_X = 110;
    static final int BUTTON_W = 26;
    static final int BUTTON_H = 16;
    private static final int BUTTON_MARGIN = WIDTH - FILTER_X - BUTTON_W;
    private static final int BUTTON_GAP = 4;
    static final int ARROW_W = 12;
    static final int ARROW_H = 17;
    private static final int FORWARD_X = 93;
    private static final int BACKWARD_X = 38;
    private static final int ARROW_Y = 137;
    static final int BACK_X = 8;
    private static final int BACK_Y = SEARCH_Y - 2;
    private static final int BACK_W = 42;
    private static final int BODY_X = 8;
    private static final int BODY_Y = 30;

    // The browsing page keeps its right edge at `right`; a recipe page widens left from there.
    public static BookGeometry anchored(int right, int top, int height, boolean recipe)
    {
        int width = recipe ? RecipeBookLayout.recipeWidth(right) : WIDTH;
        return new BookGeometry(right - width, top, width, height);
    }

    public Rect bounds()
    {
        return new Rect(x, y, width, height);
    }

    public float stretch()
    {
        return (float) width / WIDTH;
    }

    private int inset(int offset)
    {
        return (int) Math.ceil(offset * stretch());
    }

    public Rect search()
    {
        return new Rect(x + SEARCH_X, y + SEARCH_Y, SEARCH_W, SEARCH_H);
    }

    public Rect filter()
    {
        return new Rect(x + FILTER_X, y + BUTTON_Y, BUTTON_W, BUTTON_H);
    }

    public Rect place()
    {
        return new Rect(x + width - inset(BUTTON_MARGIN) - BUTTON_W, y + BUTTON_Y, BUTTON_W, BUTTON_H);
    }

    // Left of the place button while it shows, in its slot otherwise.
    public Rect bookmark(boolean placeShown)
    {
        Rect place = place();
        return placeShown ? new Rect(place.x() - BUTTON_GAP - BUTTON_W, place.y(), BUTTON_W, BUTTON_H) : place;
    }

    public Rect back()
    {
        return new Rect(x + inset(BACK_X), y + BACK_Y, BACK_W, ARROW_H);
    }

    // The strip of the texture holding the search bar, covered while a recipe shows. Each edge is
    // rounded inward so the cover stops just inside the stretched bezel.
    public Rect searchCover()
    {
        int left = back().x();
        int right = x + (int) Math.floor((WIDTH - BACK_X) * stretch());
        return new Rect(left, y + SEARCH_Y - 3, right - left, SEARCH_H + 6);
    }

    public int pagerY()
    {
        return y + height - (HEIGHT - ARROW_Y);
    }

    public Rect backArrow()
    {
        return new Rect(x + width / 2 + BACKWARD_X - WIDTH / 2, pagerY(), ARROW_W, ARROW_H);
    }

    public Rect forwardArrow()
    {
        return new Rect(x + width / 2 + FORWARD_X - WIDTH / 2, pagerY(), ARROW_W, ARROW_H);
    }

    public Rect body()
    {
        return new Rect(x + BODY_X, y + BODY_Y, width - 2 * BODY_X, pagerY() - y - BODY_Y - 4);
    }

    public int gridX()
    {
        return x + GRID_X;
    }

    public int gridY()
    {
        return y + GRID_Y;
    }

    // Where a toggle the page carries goes: its bottom-left corner, level with the pager.
    public int carriedX()
    {
        return x + inset(GRID_X);
    }

    public int carriedY(int toggleHeight)
    {
        return pagerY() + (ARROW_H - toggleHeight) / 2;
    }
}
