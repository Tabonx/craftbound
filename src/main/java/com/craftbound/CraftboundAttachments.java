package com.craftbound;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

// Wires the obtained-items data (see ObtainedItems) onto the player as an attachment that is
// persisted to disk, copied across death, and auto-synced to the owning client.
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

    private CraftboundAttachments() {}
}
