package com.craftbound.client;

import java.util.Optional;

import com.google.gson.JsonElement;

// How bookmarks name an ingredient: by a session uid while the game runs, and by a stable entry on
// disk. An interface so the bookmark rules can be tested without JEI.
public interface BookmarkKeys<I>
{
    String sessionUid(I ingredient);

    // Empty for an ingredient with no stable form, which is then saved by its session uid.
    Optional<BookmarkEntry> entryOf(I ingredient);

    // Empty when the saved ingredient no longer exists, such as an item from a removed mod.
    Optional<String> sessionUidOf(String typeUid, JsonElement ingredient);
}
