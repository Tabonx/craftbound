package com.craftbound.client.bookmarks;

import java.util.Optional;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;

// One saved bookmark: {"type": "item_stack", "ingredient": {...}}, the ingredient as JEI's codec for
// its type writes it. Earlier versions saved a string, JEI's uid for the ingredient, which changes
// between launches for fluids and some variants; such an entry is matched by that string and
// replaced once it is found. Any other shape is kept as it is.
public record BookmarkEntry(JsonElement json)
{
    public static final Codec<BookmarkEntry> CODEC = Codec.PASSTHROUGH.xmap(
            dynamic -> new BookmarkEntry(dynamic.convert(JsonOps.INSTANCE).getValue()),
            entry -> new Dynamic<>(JsonOps.INSTANCE, entry.json));

    private static final String TYPE = "type";
    private static final String INGREDIENT = "ingredient";

    public static BookmarkEntry of(String typeUid, JsonElement ingredient)
    {
        JsonObject json = new JsonObject();
        json.addProperty(TYPE, typeUid);
        json.add(INGREDIENT, ingredient);
        return new BookmarkEntry(json);
    }

    public static BookmarkEntry legacy(String sessionUid)
    {
        return new BookmarkEntry(new JsonPrimitive(sessionUid));
    }

    public Optional<String> legacyUid()
    {
        return json.isJsonPrimitive() && json.getAsJsonPrimitive().isString()
                ? Optional.of(json.getAsString())
                : Optional.empty();
    }

    public <I> Optional<String> sessionUid(BookmarkKeys<I> keys)
    {
        if (legacyUid().isPresent())
            return legacyUid();
        if (json instanceof JsonObject object && object.get(TYPE) instanceof JsonPrimitive type
                && type.isString() && object.has(INGREDIENT))
            return keys.sessionUidOf(type.getAsString(), object.get(INGREDIENT));
        return Optional.empty();
    }
}
