package com.craftbound.client.ponder;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.BooleanSupplier;

import com.craftbound.client.progression.Progression;
import com.mojang.logging.LogUtils;

import org.slf4j.Logger;

import net.createmod.catnip.registry.RegisteredObjectsHelper;
import net.createmod.ponder.foundation.PonderIndex;
import net.createmod.ponder.foundation.PonderTag;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;

// What Ponder is allowed to list. Ponder browsing follows the book: an entry the book still hides
// is not named here either, so the two never tell the player different stories about what exists.
//
// Every Ponder listing goes through this one class rather than asking Progression directly, so the
// index and the category screens cannot drift apart as Ponder's own screens change.
public final class PonderVisibility
{
    private static final Logger LOGGER = LogUtils.getLogger();

    // Create is an optional dependency on a version range with no upper bound, so its Ponder API
    // can move under us. Every call into it is answered "show everything" once that happens: the
    // filtering is cosmetic, and losing it beats taking the game down with a link error.
    private static boolean available = true;

    public static boolean isHidden(ItemLike itemLike)
    {
        return guarded(() -> !Progression.isDiscovered(BuiltInRegistries.ITEM.getKey(itemLike.asItem())), false);
    }

    // Ponder keys entries by item *or* block id, so they are resolved the way Ponder resolves them
    // before being judged; a key naming nothing is left in, for Ponder to draw as a missing entry.
    public static boolean isHidden(ResourceLocation key)
    {
        return guarded(() ->
        {
            ItemLike resolved = RegisteredObjectsHelper.getItemOrBlock(key);
            return resolved != null && isHidden(resolved);
        }, false);
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
        return guarded(
                () -> PonderIndex.getTagAccess().getItems(tag).stream().anyMatch(key -> !isHidden(key)),
                true);
    }

    private static boolean guarded(BooleanSupplier call, boolean whenUnavailable)
    {
        if (!available)
            return whenUnavailable;

        try
        {
            return call.getAsBoolean();
        }
        catch (LinkageError e)
        {
            available = false;
            LOGGER.error("Ponder integration disabled: this version of Create no longer fits it", e);
            return whenUnavailable;
        }
    }

    private PonderVisibility() {}
}
