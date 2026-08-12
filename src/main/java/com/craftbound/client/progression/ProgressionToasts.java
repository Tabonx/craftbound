package com.craftbound.client.progression;

import java.util.List;
import java.util.Optional;

import com.craftbound.Craftbound;
import com.craftbound.client.jei.BookIngredient;
import com.craftbound.client.jei.CraftboundJeiPlugin;
import com.mojang.logging.LogUtils;

import org.slf4j.Logger;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

// Announces newly unlocked recipes with vanilla's recipe toast. Unlocks are noticed here rather
// than in the book because they happen while the player is out mining, not while they are reading:
// the book's own refresh only runs when it is open.
//
// Polled on the same cadence the server sweeps the inventory, since that is how often the obtained
// set can actually change.
@EventBusSubscriber(modid = Craftbound.MODID, value = Dist.CLIENT)
public final class ProgressionToasts
{
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int POLL_INTERVAL_TICKS = 20;

    @SubscribeEvent
    public static void onClientTick(final ClientTickEvent.Post event)
    {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || !CraftboundJeiPlugin.hasRuntime())
            return;
        if (minecraft.player.tickCount % POLL_INTERVAL_TICKS != 0)
            return;

        Progression.refresh();

        List<String> unlocked = Progression.drainNewlyUnlocked();
        if (unlocked.isEmpty())
            return;

        // Anything with no drawable form still counts as an unlock; the toast just shows fewer
        // icons rather than not appearing.
        List<BookIngredient> icons = unlocked.stream()
                .map(Progression::displayFor)
                .flatMap(Optional::stream)
                .toList();

        LOGGER.debug("Craftbound: toasting {} newly unlocked results ({} with an icon)",
                unlocked.size(), icons.size());
        RecipeUnlockToast.addOrUpdate(minecraft.getToasts(), icons);
    }

    private ProgressionToasts() {}
}
