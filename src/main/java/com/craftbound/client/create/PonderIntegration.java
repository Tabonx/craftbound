package com.craftbound.client.create;

import com.craftbound.client.integration.BookIntegration;
import com.craftbound.client.integration.IntegrationGuard;

// Ponder is versioned apart from Create, so it has its own guard.
public final class PonderIntegration implements BookIntegration
{
    public PonderIntegration(IntegrationGuard guard)
    {
        PonderVisibility.guardedBy(guard);
        guard.run(CraftboundPonderPlugin::register);
    }
}
