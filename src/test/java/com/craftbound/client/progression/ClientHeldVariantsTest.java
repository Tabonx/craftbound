package com.craftbound.client.progression;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.craftbound.client.JsonFile;
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
        JsonFile<Map<String, List<ItemStack>>> file =
                new JsonFile<>(dir.resolve("held.json"), ClientHeldVariants.CODEC, Map::of);
        ItemStack strength = PotionContents.createItemStack(Items.POTION, Potions.STRENGTH);

        file.write(ops, Map.of("world/test", List.of(strength)));

        ItemStack read = file.read(ops).get("world/test").get(0);
        assertTrue(ItemStack.isSameItemSameComponents(strength, read));
    }
    //?}
}
