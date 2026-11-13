package com.craftbound.client.screen;

import com.craftbound.Craftbound;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;

// Hands NeoForge's screen events to BookScreens. The book sees input before the screen does.
@EventBusSubscriber(modid = Craftbound.MODID, value = Dist.CLIENT)
public final class BookScreenEvents
{
    private BookScreenEvents()
    {
    }

    @SubscribeEvent
    public static void onInit(ScreenEvent.Init.Post event)
    {
        BookScreens.init(event.getScreen(),
                new BookHost.Widgets(event.getListenersList(), event::addListener, event::removeListener));
    }

    @SubscribeEvent
    public static void onRenderPre(ScreenEvent.Render.Pre event)
    {
        if (BookScreens.renderInstead(event.getScreen(), event.getGuiGraphics(), event.getMouseX(), event.getMouseY(),
                event.getPartialTick()))
            event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onRender(ScreenEvent.Render.Post event)
    {
        BookScreens.renderAfter(event.getScreen(), event.getGuiGraphics(), event.getMouseX(), event.getMouseY(),
                event.getPartialTick());
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event)
    {
        BookScreens.tick();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onMousePressed(ScreenEvent.MouseButtonPressed.Pre event)
    {
        if (BookScreens.press(event.getScreen(), event.getMouseX(), event.getMouseY(), event.getButton(),
                doubleClick(event)))
            event.setCanceled(true);
    }

    private static boolean doubleClick(ScreenEvent.MouseButtonPressed.Pre event)
    {
        //? if >=1.21.10 {
        /*return event.isDoubleClick();
        *///?} else {
        return false;
        //?}
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onMouseReleased(ScreenEvent.MouseButtonReleased.Pre event)
    {
        if (BookScreens.release())
            event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onMouseDragged(ScreenEvent.MouseDragged.Pre event)
    {
        if (BookScreens.drag())
            event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onOpening(ScreenEvent.Opening event)
    {
        BookScreens.opening();
    }

    @SubscribeEvent
    public static void onClosing(ScreenEvent.Closing event)
    {
        BookScreens.closing(event.getScreen());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onMouseScrolled(ScreenEvent.MouseScrolled.Pre event)
    {
        if (BookScreens.scroll(event.getScreen(), event.getMouseX(), event.getMouseY(), event.getScrollDeltaX(),
                event.getScrollDeltaY()))
            event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onKeyPressed(ScreenEvent.KeyPressed.Pre event)
    {
        if (BookScreens.keyPressed(event.getScreen(), event.getKeyCode(), event.getScanCode(), event.getModifiers()))
            event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onCharTyped(ScreenEvent.CharacterTyped.Pre event)
    {
        if (BookScreens.charTyped(event.getScreen(), event.getCodePoint()))
            event.setCanceled(true);
    }
}
