package com.craftbound.platform;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

import com.craftbound.Craftbound;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

// Saved here and sent to the owning client by PlayerState, since the book filters and draws
// client-side.
public final class CraftboundAttachments
{
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Craftbound.MODID);

    public static final Supplier<AttachmentType<Set<ResourceLocation>>> OBTAINED_ITEMS =
            ATTACHMENT_TYPES.register("obtained_items", () -> AttachmentType.<Set<ResourceLocation>>builder(() -> new HashSet<>())
                    //? if >=1.21.8 {
                    /*.serialize(PlayerDataFormat.OBTAINED_ITEMS)
                    *///?} else {
                    .serialize(PlayerDataFormat.OBTAINED_ITEMS_ANY_SHAPE)
                    //?}
                    .copyOnDeath()
                    .build());

    // Whether a Bookbinder's Lens has been bound into the player's book. Deliberately not copied on
    // death: BookUpgradeEvents decides, since the lens drops with the player's other things unless
    // keepInventory is on.
    public static final Supplier<AttachmentType<Boolean>> BOOK_UPGRADED =
            ATTACHMENT_TYPES.register("book_upgraded", () -> AttachmentType.builder(() -> false)
                    //? if >=1.21.8 {
                    /*.serialize(PlayerDataFormat.BOOK_UPGRADED)
                    *///?} else {
                    .serialize(PlayerDataFormat.BOOK_UPGRADED_ANY_SHAPE)
                    //?}
                    .build());

    private CraftboundAttachments() {}
}
