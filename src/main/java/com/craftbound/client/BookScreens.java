package com.craftbound.client;

import java.util.Optional;

import com.craftbound.client.jei.TransferHandlers;
import com.craftbound.client.mixin.ContainerScreenAccessor;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.RecipeBookMenu;

// Puts the book on every screen that takes recipes. A recipe-book screen (player inventory,
// crafting table, furnace, smoker, blast furnace) has its vanilla toggle swapped for the book's, and
// places itself through its vanilla book, which RecipeBookComponentMixin answers for. A screen that
// takes recipes from JEI but has no recipe book, like Create's stock keeper, gets the book on the
// side where JEI would show its ingredient list, and ContainerScreenLayoutMixin moves it aside.
// Where the window is too narrow for both, the book hides the screen, as vanilla's does.
public final class BookScreens
{
    private static final ScreenSides SIDES = new ScreenSides();
    private static final ClickCapture CAPTURE = new ClickCapture();
    private static BookHost host = null;

    private BookScreens()
    {
    }

    private static boolean hosted(AbstractContainerScreen<?> screen)
    {
        return screen.getMenu() instanceof RecipeBookMenu;
    }

    public static boolean docksBeside(AbstractContainerScreen<?> screen)
    {
        return !hosted(screen) && TransferHandlers.acceptsRecipes(screen.getMenu());
    }

    // Asked by a recipe-book screen placing itself, which is always the one being opened.
    public static int hostedLeftPos(int width, int imageWidth)
    {
        int gap = Screens.current() instanceof AbstractContainerScreen<?> screen ? SIDES.gap(screen) : RecipeBookLayout.GAP;
        return RecipeBookLayout.leftPos(width, imageWidth, gap, BookSession.get().isOpen());
    }

    // The width a screen beside the book is told it has, so that centering itself moves it aside.
    // Taken from the window: a screen rebuilt without a resize keeps the width it was given.
    public static int besideScreenWidth(AbstractContainerScreen<?> screen, int imageWidth)
    {
        return RecipeBookLayout.screenWidthFor(Minecraft.getInstance().getWindow().getGuiScaledWidth(), imageWidth,
                SIDES.gap(screen), BookSession.get().isOpen());
    }

    public static void init(Screen screen, BookHost.Widgets widgets)
    {
        if (!(screen instanceof AbstractContainerScreen<?> container))
            return;
        host = null;
        if (hosted(container))
            host = attachHosted(container, widgets);
        else if (docksBeside(container))
            host = BookHost.attach(container, widgets, SIDES, null);
    }

    private static BookHost attachHosted(AbstractContainerScreen<?> screen, BookHost.Widgets widgets)
    {
        ImageButton vanillaToggle = findRecipeButton(widgets);
        if (vanillaToggle == null)
        {
            // A mod took the toggle away, and the book with it. The screen made room for the book
            // all the same, so it is put back in the middle.
            ((ContainerScreenAccessor) screen).craftbound$setLeftPos((screen.width - BookHost.bounds(screen).width()) / 2);
            return null;
        }
        widgets.remove().accept(vanillaToggle);
        return BookHost.attach(screen, widgets, SIDES, vanillaToggle);
    }

    // The recipe-book toggle is the only 20x18 ImageButton these screens add.
    private static ImageButton findRecipeButton(BookHost.Widgets widgets)
    {
        for (GuiEventListener listener : widgets.listeners())
        {
            if (listener instanceof ImageButton button && button.getWidth() == 20 && button.getHeight() == 18)
                return button;
        }
        return null;
    }

    private static Optional<BookHost> on(Screen screen)
    {
        return Optional.ofNullable(host).filter(attached -> attached.on(screen));
    }

    public static boolean renderInstead(Screen screen, GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
    {
        return on(screen).filter(attached -> attached.renderInstead(graphics, mouseX, mouseY, partialTick)).isPresent();
    }

    public static void renderAfter(Screen screen, GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
    {
        on(screen).ifPresent(attached ->
        {
            attached.renderAfter(graphics, mouseX, mouseY, partialTick);
            attached.learnSides(SIDES);
        });
    }

    public static void tick()
    {
        Screen screen = Screens.current();
        if (SIDES.relayoutDue(screen))
            Screens.rebuild(screen);
        on(screen).ifPresent(BookHost::tick);
    }

    public static boolean press(Screen screen, double mouseX, double mouseY, int button, boolean doubleClick)
    {
        boolean taken = on(screen).filter(attached -> attached.press(mouseX, mouseY, button, doubleClick)).isPresent();
        if (taken)
            CAPTURE.take();
        return taken;
    }

    public static boolean release()
    {
        return CAPTURE.release();
    }

    public static boolean drag()
    {
        return CAPTURE.holding();
    }

    public static void opening()
    {
        CAPTURE.reset();
    }

    public static boolean scroll(Screen screen, double mouseX, double mouseY, double scrollX, double scrollY)
    {
        return on(screen).filter(attached -> attached.scroll(mouseX, mouseY, scrollX, scrollY)).isPresent();
    }

    public static boolean keyPressed(Screen screen, int keyCode, int scanCode, int modifiers)
    {
        return on(screen).filter(attached -> attached.keyPressed(keyCode, scanCode, modifiers)).isPresent();
    }

    public static boolean charTyped(Screen screen, int codePoint)
    {
        return on(screen).filter(attached -> attached.charTyped(codePoint)).isPresent();
    }

    public static boolean isSearching(Screen screen)
    {
        return on(screen).filter(BookHost::searching).isPresent();
    }
}
