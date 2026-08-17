package com.craftbound.client.jei;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class BookCategoriesTest
{
    // JEI's tag entries hand a whole tag back out as the result of taking that tag in. Read as
    // recipes they make every member of a tag reachable from any other, so one dirt block reveals
    // everything sharing a tag with it.
    @Test
    void tagCategoriesAreNotBrowsable()
    {
        assertFalse(BookCategories.isBrowsable("tag_recipes/item"));
        assertFalse(BookCategories.isBrowsable("tag_recipes/block"));
        assertFalse(BookCategories.isBrowsable("tag_recipes/fluid"));
    }

    @Test
    void realCategoriesAreBrowsable()
    {
        assertTrue(BookCategories.isBrowsable("crafting"));
        assertTrue(BookCategories.isBrowsable("smelting"));
        assertTrue(BookCategories.isBrowsable("milling"));
        assertTrue(BookCategories.isBrowsable("sequenced_assembly"));
    }
}
