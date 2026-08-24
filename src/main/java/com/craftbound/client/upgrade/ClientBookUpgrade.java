package com.craftbound.client.upgrade;

import com.craftbound.Craftbound;
import com.craftbound.client.ServerSupport;
import com.craftbound.progression.ProgressionConfig;
import com.craftbound.upgrade.BookUpgrade;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;

// The client's view of the book upgrade: whatever the server last told it. The only judgement is
// what to do on a server without Craftbound, where the lens cannot be obtained and nothing is sent.
@EventBusSubscriber(modid = Craftbound.MODID, value = Dist.CLIENT)
public final class ClientBookUpgrade
{
    private static boolean bound = false;

    public static void accept(boolean value)
    {
        bound = value;
    }

    // The next server says whether the book is upgraded there; until it does, it is not.
    @SubscribeEvent
    public static void onLoggingOut(final ClientPlayerNetworkEvent.LoggingOut event)
    {
        bound = false;
    }

    public static boolean hintsUnlocked()
    {
        return BookUpgrade.hintsUnlocked(ProgressionConfig.gateHintsBehindLens(),
                ServerSupport.installed(), bound());
    }

    // What the toggle button draws the lens for: the marks being unlocked says nothing while
    // progression itself is off, since then there is nothing left to mark.
    public static boolean hintsActive()
    {
        return ProgressionConfig.rules().enabled() && hintsUnlocked();
    }

    public static boolean bound()
    {
        return bound;
    }

    private ClientBookUpgrade() {}
}
