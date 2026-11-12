package com.craftbound;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.Test;

import com.mojang.serialization.Codec;

import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

class PlayerDataFormatTest
{
    private static final Set<ResourceLocation> ITEMS =
            Set.of(ResourceLocation.parse("minecraft:stick"), ResourceLocation.parse("create:cogwheel"));

    private static ListTag bareItems()
    {
        ListTag list = new ListTag();
        ITEMS.forEach(id -> list.add(StringTag.valueOf(id.toString())));
        return list;
    }

    private static <T> T read(Codec<T> codec, Tag tag)
    {
        return codec.parse(NbtOps.INSTANCE, tag).getOrThrow();
    }

    @Test
    void obtainedItemsAreWrittenAsACompound()
    {
        Tag written = PlayerDataFormat.OBTAINED_ITEMS_ANY_SHAPE.encodeStart(NbtOps.INSTANCE, ITEMS).getOrThrow();

        CompoundTag compound = assertInstanceOf(CompoundTag.class, written);
        assertEquals(ITEMS, read(PlayerDataFormat.LEGACY_OBTAINED_ITEMS, compound.get("items")));
        assertEquals(ITEMS, read(PlayerDataFormat.OBTAINED_ITEMS.codec(), written));
    }

    @Test
    void obtainedItemsReadEveryReleasedShape()
    {
        CompoundTag compound = new CompoundTag();
        compound.put("items", bareItems());

        assertEquals(ITEMS, read(PlayerDataFormat.OBTAINED_ITEMS_ANY_SHAPE, compound));
        assertEquals(ITEMS, read(PlayerDataFormat.OBTAINED_ITEMS_ANY_SHAPE, bareItems()));
        assertEquals(ITEMS, read(PlayerDataFormat.LEGACY_OBTAINED_ITEMS, bareItems()));
    }

    @Test
    void readObtainedItemsCanStillGrow()
    {
        Set<ResourceLocation> read = read(PlayerDataFormat.OBTAINED_ITEMS_ANY_SHAPE, bareItems());
        assertDoesNotThrow(() -> read.add(ResourceLocation.parse("minecraft:dirt")));
    }

    @Test
    void bookUpgradeIsWrittenAsACompound()
    {
        Tag written = PlayerDataFormat.BOOK_UPGRADED_ANY_SHAPE.encodeStart(NbtOps.INSTANCE, true).getOrThrow();

        CompoundTag expected = new CompoundTag();
        expected.putBoolean("upgraded", true);
        assertEquals(expected, written);
        assertTrue(read(PlayerDataFormat.BOOK_UPGRADED.codec(), written));
    }

    @Test
    void bookUpgradeReadsEveryReleasedShape()
    {
        CompoundTag compound = new CompoundTag();
        compound.putBoolean("upgraded", true);

        assertTrue(read(PlayerDataFormat.BOOK_UPGRADED_ANY_SHAPE, compound));
        assertTrue(read(PlayerDataFormat.BOOK_UPGRADED_ANY_SHAPE, ByteTag.valueOf(true)));
        assertTrue(read(PlayerDataFormat.LEGACY_BOOK_UPGRADED, ByteTag.valueOf(true)));
    }
}
