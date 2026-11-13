package com.craftbound.client.progression;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.craftbound.client.storage.WorldId;
import com.craftbound.client.storage.WorldScopedStore;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;

import net.minecraft.SharedConstants;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;

class ClientHeldVariantsTest
{
    @BeforeAll
    static void bootstrap()
    {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    // What the file holds is the stacks themselves, read back through the world's registries. From
    // 26.1 an item's components are bound only once a world loads, so no stack can be made here.
    //? if <26.1 {
    @Test
    void aPotionSurvivesTheFile(@TempDir Path dir)
    {
        HolderLookup.Provider registries = VanillaRegistries.createLookup();
        var ops = registries.createSerializationContext(JsonOps.INSTANCE);
        Path path = dir.resolve("held.json");
        WorldId world = WorldId.server("example.com");
        ItemStack strength = PotionContents.createItemStack(Items.POTION, Potions.STRENGTH);

        WorldScopedStore<List<Either<ItemStack, Dynamic<?>>>> written =
                new WorldScopedStore<>(path, ClientHeldVariants.CODEC, ArrayList::new);
        written.open(world, ops).add(Either.left(strength));
        written.markDirty();
        written.close();

        ItemStack read = new WorldScopedStore<>(path, ClientHeldVariants.CODEC, ArrayList::new)
                .open(world, ops).get(0).left().orElseThrow();
        assertTrue(ItemStack.isSameItemSameComponents(strength, read));
    }

    @Test
    void aStackFromAMissingModStaysInTheFile(@TempDir Path dir) throws Exception
    {
        var ops = VanillaRegistries.createLookup().createSerializationContext(JsonOps.INSTANCE);
        Path path = dir.resolve("held.json");
        WorldId world = WorldId.server("example.com");
        String gone = "{\"id\":\"gone_mod:widget\",\"count\":1}";
        Files.writeString(path, "{\"server/example.com\":[" + gone + "]}");

        WorldScopedStore<List<Either<ItemStack, Dynamic<?>>>> store =
                new WorldScopedStore<>(path, ClientHeldVariants.CODEC, ArrayList::new);
        store.open(world, ops).add(Either.left(new ItemStack(Items.STONE)));
        store.markDirty();
        store.close();

        assertTrue(Files.readString(path).contains("gone_mod:widget"));
        assertEquals(2, new WorldScopedStore<>(path, ClientHeldVariants.CODEC, ArrayList::new).open(world, ops).size());
    }
    //?}
}
