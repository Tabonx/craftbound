package com.craftbound;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

import com.mojang.serialization.Codec;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

// Per-player data the book reads: what the player has obtained (see ObtainedItems) and whether
// their book carries the Bookbinder's Lens upgrade. Both are persisted to disk here and sent to the
// owning client by PlayerState, since the book filters and draws client-side.
public final class CraftboundAttachments
{
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Craftbound.MODID);

    public static final Supplier<AttachmentType<Set<ResourceLocation>>> OBTAINED_ITEMS =
            ATTACHMENT_TYPES.register("obtained_items", () -> AttachmentType.<Set<ResourceLocation>>builder(() -> new HashSet<>())
                    //? if >=1.21.8 {
                    /*.serialize(ObtainedItems.CODEC.fieldOf("items"))
                    *///?} else {
                    .serialize(ObtainedItems.CODEC)
                    //?}
                    .copyOnDeath()
                    .build());

    // Whether a Bookbinder's Lens has been bound into the player's book. Deliberately not copied on
    // death: BookUpgradeEvents decides, since the lens drops with the player's other things unless
    // keepInventory is on.
    public static final Supplier<AttachmentType<Boolean>> BOOK_UPGRADED =
            ATTACHMENT_TYPES.register("book_upgraded", () -> AttachmentType.builder(() -> false)
                    //? if >=1.21.8 {
                    /*.serialize(Codec.BOOL.fieldOf("upgraded"))
                    *///?} else {
                    .serialize(Codec.BOOL)
                    //?}
                    .build());

    private CraftboundAttachments() {}
}
