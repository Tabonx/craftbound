package com.craftbound.client.upgrade;

import com.craftbound.client.ServerSupport;
import com.craftbound.platform.CraftboundConfig;
import com.craftbound.upgrade.BookUpgrade;

// The client's view of the book upgrade: whatever the server last told it. The only judgement is
// what to do on a server without Craftbound, where the lens cannot be obtained and nothing is sent.
// The next server says whether the book is upgraded there; until it does, it is not.
public final class ClientBookUpgrade
{
    private boolean bound = false;
    private boolean synced = false;

    public void accept(boolean value)
    {
        if (BookUpgrade.announces(synced, bound, value))
            BookUpgradeToast.show();
        bound = value;
        synced = true;
    }

    public boolean hintsUnlocked()
    {
        return BookUpgrade.hintsUnlocked(CraftboundConfig.gateHintsBehindLens(),
                ServerSupport.installed(), bound());
    }

    // What the toggle button draws the lens for: the marks being unlocked says nothing while
    // progression itself is off, since then there is nothing left to mark.
    public boolean hintsActive()
    {
        return CraftboundConfig.progression().enabled() && hintsUnlocked();
    }

    public boolean bound()
    {
        return bound;
    }
}
