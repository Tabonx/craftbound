package com.craftbound.client.book;

import java.util.Objects;

// Pages the browse grid. A taller book shows another row for every grid row it grows. A new query
// starts on the first page, as vanilla's does; the same query filled again, after an unlock or a
// change in what can be made, keeps the page as near as it still exists.
public final class GridPager
{
    public static final int COLUMNS = 5;
    public static final int ROWS = 4;
    public static final int CELL = RecipeBookLayout.ROW;

    private int rows = ROWS;
    private int count = 0;
    private int page = 0;
    private Object query = null;

    public void fit(int bookHeight)
    {
        rows = ROWS + (bookHeight - RecipeBookLayout.BOOK_HEIGHT) / CELL;
        page = clamp(page);
    }

    public void fill(int count, Object query)
    {
        this.count = count;
        page = Objects.equals(query, this.query) ? clamp(page) : 0;
        this.query = query;
    }

    public void step(int delta)
    {
        page = clamp(page + delta);
    }

    public int page()
    {
        return page;
    }

    public int pageCount()
    {
        return Math.max(1, (count + perPage() - 1) / perPage());
    }

    public int perPage()
    {
        return COLUMNS * rows;
    }

    // The index of the first entry on the page, and how many entries it shows.
    public int first()
    {
        return page * perPage();
    }

    public int shown()
    {
        return Math.max(0, Math.min(perPage(), count - first()));
    }

    // The page-relative cell at a point measured from the grid's corner, or -1.
    public int cellAt(double dx, double dy)
    {
        if (dx < 0 || dy < 0)
            return -1;
        int column = (int) (dx / CELL);
        int row = (int) (dy / CELL);
        int index = row * COLUMNS + column;
        return column < COLUMNS && index < shown() ? index : -1;
    }

    public static int cellX(int index)
    {
        return CELL * (index % COLUMNS);
    }

    public static int cellY(int index)
    {
        return CELL * (index / COLUMNS);
    }

    private int clamp(int target)
    {
        return Math.max(0, Math.min(pageCount() - 1, target));
    }
}
