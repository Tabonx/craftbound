package com.craftbound.client.jei;

import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

import com.craftbound.client.ScopedFlag;

import mezz.jei.api.gui.handlers.IGlobalGuiHandler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Rect2i;

// JEI shows an overlay only where it has room. Claiming the whole screen as a gui area hides the
// ingredient and bookmark lists without touching the player's JEI settings.
final class JeiOverlayHider implements IGlobalGuiHandler
{
    private static final ScopedFlag LIFTED = new ScopedFlag();

    static <T> T lifted(Supplier<T> read)
    {
        return LIFTED.during(read);
    }

    @Override
    public Collection<Rect2i> getGuiExtraAreas()
    {
        if (LIFTED.active())
            return List.of();
        var window = Minecraft.getInstance().getWindow();
        return List.of(new Rect2i(0, 0, window.getGuiScaledWidth(), window.getGuiScaledHeight()));
    }
}
