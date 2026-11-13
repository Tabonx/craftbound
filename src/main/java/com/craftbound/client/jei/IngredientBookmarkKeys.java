package com.craftbound.client.jei;

import java.util.Optional;

import com.craftbound.client.bookmarks.BookmarkEntry;
import com.craftbound.client.bookmarks.BookmarkKeys;
import com.google.gson.JsonElement;
import com.mojang.serialization.DynamicOps;

import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IIngredientManager;

// Saves a bookmark through JEI's codec for its ingredient type, which reads back the same in any
// session, and finds it again by decoding it and taking the decoded ingredient's session uid.
final class IngredientBookmarkKeys implements BookmarkKeys<BookIngredient>
{
    private final IIngredientManager manager;
    private final DynamicOps<JsonElement> ops;

    IngredientBookmarkKeys(IIngredientManager manager, DynamicOps<JsonElement> ops)
    {
        this.manager = manager;
        this.ops = ops;
    }

    @Override
    public String sessionUid(BookIngredient ingredient)
    {
        return ingredient.sessionUid();
    }

    @Override
    public Optional<BookmarkEntry> entryOf(BookIngredient ingredient)
    {
        return entryOf(ingredient.typed());
    }

    @Override
    public Optional<String> sessionUidOf(String typeUid, JsonElement ingredient)
    {
        return manager.getIngredientTypeForUid(typeUid).flatMap(type -> sessionUidOf(type, ingredient));
    }

    // A type registered without a codec throws rather than failing the result.
    private <V> Optional<BookmarkEntry> entryOf(ITypedIngredient<V> typed)
    {
        IIngredientType<V> type = typed.getType();
        try
        {
            V normalized = manager.getIngredientHelper(type).normalizeIngredient(typed.getIngredient());
            return manager.getIngredientCodec(type).encodeStart(ops, normalized).result()
                    .map(json -> BookmarkEntry.of(type.getUid(), json));
        }
        catch (RuntimeException e)
        {
            return Optional.empty();
        }
    }

    private <V> Optional<String> sessionUidOf(IIngredientType<V> type, JsonElement json)
    {
        try
        {
            return manager.getIngredientCodec(type).parse(ops, json).result()
                    .map(ingredient -> BookIngredient.sessionUidOf(type, manager.getIngredientHelper(type), ingredient));
        }
        catch (RuntimeException e)
        {
            return Optional.empty();
        }
    }
}
