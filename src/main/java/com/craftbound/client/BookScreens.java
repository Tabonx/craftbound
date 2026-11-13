package com.craftbound.client;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Optional;

import com.craftbound.client.jei.TransferHandlers;
import com.craftbound.client.mixin.ContainerScreenAccessor;
import com.craftbound.client.mixin.ImageButtonAccessor;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.world.inventory.RecipeBookMenu;

// Puts the book on every screen that takes recipes. A recipe-book screen (player inventory,
// crafting table, furnace, smoker, blast furnace) has its vanilla toggle swapped for the book's, and
// places itself through its vanilla book, which RecipeBookComponentMixin answers for. A screen that
// takes recipes from JEI but has no recipe book, like Create's stock keeper, gets the book on the
// side where JEI would show its ingredient list, and ContainerScreenLayoutMixin moves it aside.
// Where the window is too narrow for both, the book hides the screen, as vanilla's does. Nothing here
// holds on to a screen once it closes.
public final class BookScreens
{
    private static final ScreenSides SIDES = new ScreenSides();
    private static final ClickCapture CAPTURE = new ClickCapture();
    private static BookHost host = null;
    private static Reference<AbstractContainerScreen<?>> layingOut = new WeakReference<>(null);

    private BookScreens()
    {
    }

    private static boolean hosted(AbstractContainerScreen<?> screen)
    {
        return screen.getMenu() instanceof RecipeBookMenu;
    }

    private static boolean docksBeside(AbstractContainerScreen<?> screen)
    {
        return !hosted(screen) && TransferHandlers.acceptsRecipes(screen.getMenu());
    }

    // Every container screen starts laying itself out here, before a recipe-book screen asks its book
    // where to stand. A screen beside the book is told the width that centers it beside the book,
    // taken from the window: a screen rebuilt without a resize keeps the width it was given.
    public static void layingOut(AbstractContainerScreen<?> screen, int imageWidth)
    {
        layingOut = new WeakReference<>(screen);
        if (docksBeside(screen))
            screen.width = RecipeBookLayout.screenWidthFor(Minecraft.getInstance().getWindow().getGuiScaledWidth(),
                    imageWidth, SIDES.gap(screen), BookSession.get().isOpen());
    }

    // Asked by the recipe-book screen laying itself out.
    public static int hostedLeftPos(int width, int imageWidth)
    {
        AbstractContainerScreen<?> screen = layingOut.get();
        int gap = screen == null ? RecipeBookLayout.GAP : SIDES.gap(screen);
        return RecipeBookLayout.leftPos(width, imageWidth, gap, BookSession.get().isOpen());
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

    // Known by its sprites, which no other button on these screens wears.
    private static ImageButton findRecipeButton(BookHost.Widgets widgets)
    {
        for (GuiEventListener listener : widgets.listeners())
        {
            if (listener instanceof ImageButton button && RecipeBookComponent.RECIPE_BUTTON_SPRITES
                    .equals(((ImageButtonAccessor) button).craftbound$getSprites()))
                return button;
        }
        return null;
    }

    public static void closing(Screen screen)
    {
        if (on(screen).isPresent())
            host = null;
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
