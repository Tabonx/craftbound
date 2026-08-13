package com.craftbound.client.ponder;

import com.craftbound.Craftbound;

import net.createmod.ponder.api.registration.IndexExclusionHelper;
import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.foundation.PonderIndex;

// Registers Craftbound's one exclusion with Ponder: entries the book still hides stay out of the
// item index. Ponder collects these predicates each time the index screen is built, so the rule is
// re-read on every open and needs no invalidating when something unlocks.
//
// This plugin contributes no scenes or tags of its own; it exists purely for the exclusion.
public final class CraftboundPonderPlugin implements PonderPlugin
{
    // Ponder's plugin list is a plain static registry, so this can be claimed as the mod loads;
    // nothing reads the exclusions until an index screen is opened.
    public static void register()
    {
        PonderIndex.addPlugin(new CraftboundPonderPlugin());
    }

    @Override
    public String getModId()
    {
        return Craftbound.MODID;
    }

    @Override
    public void indexExclusions(IndexExclusionHelper helper)
    {
        helper.exclude(PonderVisibility::isHidden);
    }
}
