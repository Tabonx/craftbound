package com.craftbound.client;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SearchSyntaxTest
{
    @Test
    void wordsAreAName()
    {
        assertTrue(SearchSyntax.isPlainName("oak planks"));
        assertTrue(SearchSyntax.isPlainName("  brass "));
        assertTrue(SearchSyntax.isPlainName("tnt-minecart"));
    }

    @Test
    void anOperatorAnywhereMakesItAFilter()
    {
        assertFalse(SearchSyntax.isPlainName("@create"));
        assertFalse(SearchSyntax.isPlainName("sheet @create"));
        assertFalse(SearchSyntax.isPlainName("#logs"));
        assertFalse(SearchSyntax.isPlainName("$fuel"));
        assertFalse(SearchSyntax.isPlainName("planks -oak"));
        assertFalse(SearchSyntax.isPlainName("iron|gold"));
    }
}
