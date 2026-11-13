package com.craftbound.client.jei;

import java.util.List;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;

// The areas mods report around a screen so recipe viewers keep clear of them: side tabs, toolbars,
// slot panels.
public final class ScreenAreas
{
    public static List<Rect2i> exclusionAreas(Screen screen)
    {
        return JeiSession.runtime()
                .map(runtime -> JeiOverlayHider.lifted(() -> runtime.getScreenHelper().getGuiExclusionAreas(screen).toList()))
                .orElse(List.of());
    }

    private ScreenAreas() {}
}
