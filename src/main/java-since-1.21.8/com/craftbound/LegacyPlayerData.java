package com.craftbound;

import com.mojang.serialization.Codec;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.ValueInput;
import net.neoforged.neoforge.attachment.AttachmentHolder;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

// NeoForge hands an attachment's serializer only a compound, so the bare values that Craftbound
// saved before 1.21.8 never reach it and fail to load. This reads them from the player's saved
// attachments directly, once NeoForge has loaded what it could.
public final class LegacyPlayerData
{
    public static void restore(ServerPlayer player, ValueInput input)
    {
        input.child(AttachmentHolder.ATTACHMENTS_NBT_KEY).ifPresent(saved -> {
            restore(player, saved, CraftboundAttachments.OBTAINED_ITEMS.get(), PlayerDataFormat.LEGACY_OBTAINED_ITEMS);
            restore(player, saved, CraftboundAttachments.BOOK_UPGRADED.get(), PlayerDataFormat.LEGACY_BOOK_UPGRADED);
        });
    }

    private static <T> void restore(ServerPlayer player, ValueInput saved, AttachmentType<T> type, Codec<T> legacy)
    {
        if (player.hasData(type))
            return;

        String key = NeoForgeRegistries.ATTACHMENT_TYPES.getKey(type).toString();
        saved.read(key, legacy).ifPresent(value -> player.setData(type, value));
    }

    private LegacyPlayerData() {}
}
