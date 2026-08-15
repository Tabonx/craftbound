package com.craftbound.upgrade;

// Rules for the book upgrade, kept free of Minecraft so they can be tested on their own. The
// upgrade is bound into the book by using a Bookbinder's Lens; while bound, the book marks the
// items that would unlock more recipes.
public final class BookUpgrade
{
    // The lens cannot exist on a server without Craftbound, so the marks must show there
    // regardless: gating them on an unobtainable item would hide them forever.
    public static boolean hintsUnlocked(boolean gated, boolean craftboundOnServer, boolean bound)
    {
        return !gated || !craftboundOnServer || bound;
    }

    // Dying unbinds the upgrade and drops the lens with the rest of the player's things, so it can
    // be fetched back from the death pile. keepInventory keeps it bound, like everything else kept.
    public static DeathOutcome onDeath(boolean bound, boolean keepInventory)
    {
        if (!bound)
            return new DeathOutcome(false, false);
        return keepInventory ? new DeathOutcome(true, false) : new DeathOutcome(false, true);
    }

    public record DeathOutcome(boolean keepBound, boolean dropLens) {}

    private BookUpgrade() {}
}
