package com.craftbound.client.create;

import com.craftbound.client.BookIntegration;
import com.craftbound.client.IntegrationGuard;

// Ponder ships with Create but is a mod of its own, versioned on its own, so it has its own guard:
// a Ponder that no longer fits leaves the rest of the Create integration running, and the other way
// round.
public final class PonderIntegration implements BookIntegration
{
    public PonderIntegration(IntegrationGuard guard)
    {
        PonderVisibility.guardedBy(guard);
        guard.run(CraftboundPonderPlugin::register);
    }
}
