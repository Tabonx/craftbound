package com.craftbound.client.upgrade;

import com.craftbound.CraftboundAttachments;
import com.craftbound.client.ServerSupport;
import com.craftbound.progression.ProgressionConfig;
import com.craftbound.upgrade.BookUpgrade;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

// The client's view of the book upgrade. The attachment is synced, so this is a plain read; the
// only judgement is what to do on a server without Craftbound, where the lens cannot be obtained.
public final class ClientBookUpgrade
{
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
        LocalPlayer player = Minecraft.getInstance().player;
        return player != null && player.getData(CraftboundAttachments.BOOK_UPGRADED);
    }

    private ClientBookUpgrade() {}
}
