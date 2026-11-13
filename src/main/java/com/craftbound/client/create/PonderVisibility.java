package com.craftbound.client.create;

import java.util.LinkedHashSet;
import java.util.Set;

import com.craftbound.client.book.BookTooltips;
import com.craftbound.client.integration.IntegrationGuard;
import com.craftbound.client.progression.Progression;

import net.createmod.catnip.registry.RegisteredObjectsHelper;
import net.createmod.ponder.foundation.PonderIndex;
import net.createmod.ponder.foundation.PonderTag;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

// What Ponder is allowed to show: only what the player can ponder. Listing everything the book names
// would hand out a tour of every machine whose recipe is unlocked but never made.
//
// Every Ponder screen goes through this one class rather than asking Progression directly, so the
// index and the category screens cannot drift apart as Ponder's own screens change. Every answer is
// "show everything" once the Ponder integration is switched off: the filtering is cosmetic.
public final class PonderVisibility
{
    private static IntegrationGuard guard = new IntegrationGuard("Ponder");

    static void guardedBy(IntegrationGuard ponderGuard)
    {
        guard = ponderGuard;
    }

    // An item lying in a chest may be pondered: the player is looking at the real thing. An entry in
    // the book may not, until they have actually held one, or the book would hand out a tour of
    // every machine it lists.
    public static boolean blocksShortcut(ItemStack stack)
    {
        return guard.enabled() && BookTooltips.building() && !stack.isEmpty()
                && !Progression.canPonder(id(stack.getItem()));
    }

    public static boolean isHidden(ItemLike itemLike)
    {
        return guard.enabled() && !Progression.canPonder(id(itemLike));
    }

    // Whether a scene may name this block. Looser than isHidden: the book already names everything
    // whose recipe is unlocked, so a scene can too.
    public static boolean isUnnamed(ItemLike itemLike)
    {
        return guard.enabled() && !Progression.isDiscovered(id(itemLike));
    }

    private static ResourceLocation id(ItemLike itemLike)
    {
        return BuiltInRegistries.ITEM.getKey(itemLike.asItem());
    }

    // Ponder keys entries by item *or* block id, so they are resolved the way Ponder resolves them
    // before being judged; a key naming nothing is left in, for Ponder to draw as a missing entry.
    public static boolean isHidden(ResourceLocation key)
    {
        ItemLike resolved = guard.call(() -> RegisteredObjectsHelper.getItemOrBlock(key), () -> null);
        return resolved != null && isHidden(resolved);
    }

    public static Set<ResourceLocation> visible(Set<ResourceLocation> keys)
    {
        Set<ResourceLocation> shown = new LinkedHashSet<>(keys);
        shown.removeIf(PonderVisibility::isHidden);
        return shown;
    }

    // A category whose every entry is hidden is dropped rather than left as an empty page to click
    // into. Its main item counts as an entry, since the category screen still shows that one.
    public static boolean hasVisibleItems(PonderTag tag)
    {
        Set<ResourceLocation> items = guard.call(() -> PonderIndex.getTagAccess().getItems(tag), () -> null);
        return items == null || items.stream().anyMatch(key -> !isHidden(key));
    }

    private PonderVisibility() {}
}
