package com.craftbound.client.jei;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import net.minecraft.resources.ResourceLocation;

class BookCategoriesTest
{
    // JEI's tag entries hand a whole tag back out as the result of taking that tag in. Read as
    // recipes they make every member of a tag reachable from any other, so one dirt block reveals
    // everything sharing a tag with it.
    @Test
    void tagCategoriesAreNotBrowsable()
    {
        assertFalse(BookCategories.isBrowsable(uid("jei", "tag_recipes/item")));
        assertFalse(BookCategories.isBrowsable(uid("jei", "tag_recipes/block")));
        assertFalse(BookCategories.isBrowsable(uid("jei", "tag_recipes/fluid")));
    }

    @Test
    void realCategoriesAreBrowsable()
    {
        assertTrue(BookCategories.isBrowsable(uid("minecraft", "crafting")));
        assertTrue(BookCategories.isBrowsable(uid("minecraft", "smelting")));
        assertTrue(BookCategories.isBrowsable(uid("create", "milling")));
        assertTrue(BookCategories.isBrowsable(uid("create", "sequenced_assembly")));
    }

    // Only JEI's own tag entries are left out; another mod is free to name a real category this way.
    @Test
    void anotherModsTagPathIsBrowsable()
    {
        assertTrue(BookCategories.isBrowsable(uid("somemod", "tag_recipes/item")));
    }

    private static ResourceLocation uid(String namespace, String path)
    {
        return ResourceLocation.fromNamespaceAndPath(namespace, path);
    }
}
