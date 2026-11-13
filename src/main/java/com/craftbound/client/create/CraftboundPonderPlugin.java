package com.craftbound.client.create;

import com.craftbound.Craftbound;

import net.createmod.ponder.api.registration.IndexExclusionHelper;
import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.foundation.PonderIndex;

// Ponder collects exclusion predicates each time the index screen is built, so this needs no
// invalidating when something unlocks.
final class CraftboundPonderPlugin implements PonderPlugin
{
    static void register()
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
