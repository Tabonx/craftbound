package com.craftbound;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

// Wires the crafted-items data (see CraftedItems) onto the player as an attachment that is
// persisted to disk, copied across death, and auto-synced to the owning client.
public final class CraftboundAttachments
{
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Craftbound.MODID);

    public static final Supplier<AttachmentType<Set<ResourceLocation>>> CRAFTED_ITEMS =
            ATTACHMENT_TYPES.register("crafted_items", () -> AttachmentType.<Set<ResourceLocation>>builder(() -> new HashSet<>())
                    .serialize(CraftedItems.CODEC)
                    .copyOnDeath()
                    .sync(CraftedItems.STREAM_CODEC)
                    .build());

    private CraftboundAttachments() {}
}
