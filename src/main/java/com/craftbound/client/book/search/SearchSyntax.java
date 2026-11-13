package com.craftbound.client.book.search;

public final class SearchSyntax
{
    // JEI's search prefixes: mod, tag, tooltip, creative tab, colour, resource id, and exclusion.
    private static final String OPERATORS = "@#$%^&-";

    private SearchSyntax()
    {
    }

    // Whether the text only names items, with none of JEI's operators filtering by something else.
    public static boolean isPlainName(String text)
    {
        if (text.contains("|"))
            return false;
        for (String token : text.trim().split("\\s+"))
            if (!token.isEmpty() && OPERATORS.indexOf(token.charAt(0)) >= 0)
                return false;
        return true;
    }
}
