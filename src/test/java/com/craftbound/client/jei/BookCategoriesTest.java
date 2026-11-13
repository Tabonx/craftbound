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
        assertFalse(BookCategories.isBrowsable(uid("minecraft", "tag_recipes/item")));
        assertFalse(BookCategories.isBrowsable(uid("minecraft", "tag_recipes/block")));
        assertFalse(BookCategories.isBrowsable(uid("minecraft", "tag_recipes/fluid")));
        assertFalse(BookCategories.isBrowsable(uid("somemod", "tag_recipes/gas")));
    }

    @Test
    void realCategoriesAreBrowsable()
    {
        assertTrue(BookCategories.isBrowsable(uid("minecraft", "crafting")));
        assertTrue(BookCategories.isBrowsable(uid("minecraft", "smelting")));
        assertTrue(BookCategories.isBrowsable(uid("create", "milling")));
        assertTrue(BookCategories.isBrowsable(uid("create", "sequenced_assembly")));
    }

    private static ResourceLocation uid(String namespace, String path)
    {
        return ResourceLocation.fromNamespaceAndPath(namespace, path);
    }
}
