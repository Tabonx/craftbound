package com.craftbound;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import com.mojang.serialization.Codec;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

// Data attachments registered by Craftbound. Attachments are arbitrary data bolted onto
// game objects (here: a player). This one is persisted to disk, copied across death, and
// auto-synced to the owning client so the recipe-book Mixin can read it.
public final class CraftboundAttachments
{
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Craftbound.MODID);

    // On-disk form: stored as a list of item ids, held in memory as a set.
    private static final Codec<Set<ResourceLocation>> CODEC =
            ResourceLocation.CODEC.listOf().xmap(list -> new HashSet<ResourceLocation>(list), List::copyOf);

    // Network form used by the auto-sync to the owning client.
    private static final StreamCodec<RegistryFriendlyByteBuf, Set<ResourceLocation>> STREAM_CODEC =
            ByteBufCodecs.<RegistryFriendlyByteBuf, ResourceLocation, Set<ResourceLocation>>collection(
                    size -> new HashSet<>(), ResourceLocation.STREAM_CODEC);

    // The set of result-item ids a player has ever crafted.
    public static final Supplier<AttachmentType<Set<ResourceLocation>>> CRAFTED_ITEMS =
            ATTACHMENT_TYPES.register("crafted_items", () -> AttachmentType.<Set<ResourceLocation>>builder(() -> new HashSet<>())
                    .serialize(CODEC)
                    .copyOnDeath()
                    .sync(STREAM_CODEC)
                    .build());

    private CraftboundAttachments() {}
}
