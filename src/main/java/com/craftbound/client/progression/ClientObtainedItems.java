package com.craftbound.client.progression;

import java.util.Set;

import com.craftbound.Craftbound;
import com.craftbound.CraftboundAttachments;
import com.craftbound.HeldItems;
import com.craftbound.client.ServerSupport;
import com.craftbound.client.WorldKey;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;

// The client's view of what the player has obtained.
//
// With Craftbound on the server that is simply the synced attachment. Without it nothing syncs, so
// the client sweeps its own copy of the inventory on the same cadence the server would and keeps
// the result in a file next to the bookmarks, since a client-side set would otherwise be lost on
// every rejoin. Only what passes through the inventory can be seen this way, which is the whole
// picture for anything the player actually picks up.
@EventBusSubscriber(modid = Craftbound.MODID, value = Dist.CLIENT)
public final class ClientObtainedItems
{
    private static final String FILE_NAME = "craftbound-obtained.json";
    private static final int SCAN_INTERVAL_TICKS = 20;
    // A multiple of the scan interval, so the flush check rides along with a sweep.
    private static final int SAVE_INTERVAL_TICKS = 200;

    private static ObtainedItemsFile file = null;
    private static ObtainedItemsByWorld tracked = null;
    private static boolean dirty = false;

    public static Set<ResourceLocation> current()
    {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null)
            return Set.of();

        return ServerSupport.installed()
                ? player.getData(CraftboundAttachments.OBTAINED_ITEMS)
                : load().of(WorldKey.current());
    }

    @SubscribeEvent
    public static void onClientTick(final ClientTickEvent.Post event)
    {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || player.tickCount % SCAN_INTERVAL_TICKS != 0 || ServerSupport.installed())
            return;

        dirty |= load().record(WorldKey.current(), HeldItems.of(player));
        if (player.tickCount % SAVE_INTERVAL_TICKS == 0)
            flush();
    }

    // Logging out is the reliable moment to persist; a game killed outright loses at most the last
    // interval, and those items are still in the inventory for the next join's sweep to re-credit.
    @SubscribeEvent
    public static void onLoggingOut(final ClientPlayerNetworkEvent.LoggingOut event)
    {
        flush();
        tracked = null;
    }

    private static void flush()
    {
        if (!dirty)
            return;

        file().write(tracked);
        dirty = false;
    }

    private static ObtainedItemsByWorld load()
    {
        if (tracked == null)
            tracked = file().read();
        return tracked;
    }

    private static ObtainedItemsFile file()
    {
        if (file == null)
            file = new ObtainedItemsFile(FMLPaths.CONFIGDIR.get().resolve(FILE_NAME));
        return file;
    }

    private ClientObtainedItems() {}
}
