package com.craftbound;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

import com.mojang.serialization.Codec;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

// Per-player data the book reads: what the player has obtained (see ObtainedItems) and whether
// their book carries the Bookbinder's Lens upgrade. Both are persisted to disk and auto-synced to
// the owning client, since the book filters and draws client-side.
public final class CraftboundAttachments
{
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Craftbound.MODID);

    public static final Supplier<AttachmentType<Set<ResourceLocation>>> OBTAINED_ITEMS =
            ATTACHMENT_TYPES.register("obtained_items", () -> AttachmentType.<Set<ResourceLocation>>builder(() -> new HashSet<>())
                    .serialize(ObtainedItems.CODEC)
                    .copyOnDeath()
                    .sync(ObtainedItems.STREAM_CODEC)
                    .build());

    // Whether a Bookbinder's Lens has been bound into the player's book. Deliberately not copied on
    // death: BookUpgradeEvents decides, since the lens drops with the player's other things unless
    // keepInventory is on.
    public static final Supplier<AttachmentType<Boolean>> BOOK_UPGRADED =
            ATTACHMENT_TYPES.register("book_upgraded", () -> AttachmentType.builder(() -> false)
                    .serialize(Codec.BOOL)
                    .sync(ByteBufCodecs.BOOL)
                    .build());

    private CraftboundAttachments() {}
}
