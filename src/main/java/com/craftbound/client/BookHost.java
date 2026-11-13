package com.craftbound.client;

import java.util.List;
import java.util.function.Consumer;

import com.craftbound.client.RecipeBookLayout.Placement;
import com.craftbound.client.jei.JeiSession;
import com.craftbound.client.jei.ScreenAreas;
import com.craftbound.client.mixin.ContainerScreenAccessor;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

// The book on one screen, the toggle beside it, and where they were put. Built anew every time the
// screen is laid out.
final class BookHost
{
    // What a screen being laid out lets the book add and take away.
    record Widgets(List<GuiEventListener> listeners, Consumer<GuiEventListener> add,
            Consumer<GuiEventListener> remove)
    {
    }

    private final AbstractContainerScreen<?> screen;
    private final RecipeBookWidget book;
    private final RecipeBookToggleButton toggle;
    private final Placement placement;
    private final boolean overlay;
    private boolean measured = false;

    private BookHost(AbstractContainerScreen<?> screen, RecipeBookWidget book, RecipeBookToggleButton toggle,
            Placement placement, boolean overlay)
    {
        this.screen = screen;
        this.book = book;
        this.toggle = toggle;
        this.placement = placement;
        this.overlay = overlay;
    }

    // `vanillaToggle` is the screen's own recipe-book button, which the book's toggle replaces, or
    // null on a screen without one.
    static BookHost attach(AbstractContainerScreen<?> screen, Widgets widgets, ScreenSides sides,
            ImageButton vanillaToggle)
    {
        var window = Minecraft.getInstance().getWindow();
        BookSession session = BookSession.get();
        boolean open = session.isOpen();
        Rect bounds = bounds(screen);
        Placement placement = RecipeBookLayout.place(window.getGuiScaledWidth(), window.getGuiScaledHeight(),
                bounds.x(), bounds.y(), bounds.width(), bounds.height(), sides.gap(screen), sides.of(screen).floor());
        boolean overlay = open && placement.overlay();

        ClientSession client = ClientSession.current();
        RecipeBookWidget book = new RecipeBookWidget(session, client.bookState(screen), client.bookCatalog());
        book.visible = open;
        book.bind(Integrations.bind(screen.getMenu()));
        book.anchor(placement.bookRight(), placement.bookTop(), placement.bookHeight());

        RecipeBookToggleButton toggle = new RecipeBookToggleButton(0, 0, b ->
        {
            BookSession.get().toggleOpen();
            Screens.rebuild(screen);
        });
        switch (ToggleSpot.of(vanillaToggle != null, () -> insideFree(screen), overlay, open))
        {
            case VANILLA -> toggle.setPosition(vanillaToggle.getX(), vanillaToggle.getY());
            case INSIDE -> toggle.setPosition(bounds.x() + ToggleSpot.INSIDE_X,
                    bounds.y() + ToggleSpot.insideY(bounds.height()));
            case CARRIED -> book.carry(toggle);
            case OUTSIDE -> toggle.setPosition(
                    Math.max(0, bounds.x() - sides.of(screen).reach() - RecipeBookLayout.TOGGLE_SPACE), bounds.y());
        }

        widgets.add().accept(book);
        widgets.add().accept(toggle);
        return new BookHost(screen, book, toggle, placement, overlay);
    }

    private static boolean insideFree(AbstractContainerScreen<?> screen)
    {
        List<Rect> slots = screen.getMenu().slots.stream().map(slot -> ToggleSpot.slot(slot.x, slot.y)).toList();
        return ToggleSpot.insideFree(screen.getClass().getName(), bounds(screen).height(), slots);
    }

    // The screen's own image: leftPos, topPos and its size.
    static Rect bounds(AbstractContainerScreen<?> screen)
    {
        var accessor = (ContainerScreenAccessor) screen;
        return new Rect(accessor.craftbound$getLeftPos(), accessor.craftbound$getTopPos(),
                accessor.craftbound$getImageWidth(), accessor.craftbound$getImageHeight());
    }

    boolean on(Screen screen)
    {
        return this.screen == screen;
    }

    // The book takes a click before the screen does, and anywhere in its own column: the screen
    // would hand it to its own widgets first, or read it as a click outside itself. Some screens keep
    // a click on their own field to themselves, so the book lets go of the keyboard on any click it
    // is not under.
    boolean press(double mouseX, double mouseY, int button, boolean doubleClick)
    {
        if (!book.isMouseOver(mouseX, mouseY))
            book.blurSearch();
        if (toggle.isMouseOver(mouseX, mouseY))
        {
            if (!toggle.unbind(mouseX, mouseY, button))
                Input.click(toggle, mouseX, mouseY, button);
            return true;
        }
        if (book.visible && (book.click(mouseX, mouseY, button, doubleClick) || owns(mouseX, mouseY)))
            return true;
        return overlay;
    }

    private boolean owns(double mouseX, double mouseY)
    {
        int left = book.getX() - RecipeBookLayout.RAIL_WIDTH;
        int right = placement.bookRight() + RecipeBookLayout.GAP;
        return mouseX >= left && mouseX < right
                && mouseY >= placement.bookTop() && mouseY < placement.bookTop() + placement.bookHeight();
    }

    // The stock keeper scrolls its own list wherever the cursor is, so the book takes the wheel first
    // while it is under it. A screen hidden behind the book takes none.
    boolean scroll(double mouseX, double mouseY, double scrollX, double scrollY)
    {
        return book.mouseScrolled(mouseX, mouseY, scrollX, scrollY) || overlay;
    }

    // While its search has focus the book takes the keyboard first. Some screens, the stock keeper
    // among them, hand typed keys to their own fields and never to the widgets beside them.
    boolean searching()
    {
        return book.isSearchFocused();
    }

    boolean keyPressed(int keyCode, int scanCode, int modifiers)
    {
        return searching() && Input.keyPressed(book, keyCode, scanCode, modifiers);
    }

    boolean charTyped(int codePoint)
    {
        return searching() && Input.charTyped(book, codePoint, 0);
    }

    // Widgets are not ticked by their screen, and the shown recipe needs it: JEI cycles ingredients
    // that stand for several items (any planks, any log) on a tick, and stands still without one.
    void tick()
    {
        book.tick();
    }

    // With the book over it, the screen is not drawn at all, only what lies behind it. Whether the
    // screen's own drawing is to be skipped.
    boolean renderInstead(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
    {
        if (!overlay)
            return false;
        ((ContainerScreenAccessor) screen).craftbound$setHoveredSlot(null);
        Canvas.backdrop(screen, graphics, mouseX, mouseY, partialTick);
        Canvas.widget(book, graphics, mouseX, mouseY, partialTick);
        Canvas.widget(toggle, graphics, mouseX, mouseY, partialTick);
        book.renderDeferredTooltip(graphics, mouseX, mouseY);
        Canvas.drawTooltips(graphics, mouseX, mouseY, partialTick);
        return true;
    }

    // The book's item tooltip goes last, so it sits above the screen's slot placeholders.
    void renderAfter(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
    {
        if (overlay)
            return;
        book.renderDeferredTooltip(graphics, mouseX, mouseY);
        Canvas.drawTooltips(graphics, mouseX, mouseY, partialTick);
    }

    // Read once the screen has drawn, when its widgets exist and the mods reporting around it know
    // where they are.
    void learnSides(ScreenSides sides)
    {
        if (measured || !JeiSession.running())
            return;
        measured = true;
        Rect bounds = bounds(screen);
        List<SideAreas.Area> areas = ScreenAreas.exclusionAreas(screen).stream()
                .map(area -> new SideAreas.Area(area.getX(), area.getY(), area.getWidth(), area.getHeight()))
                .toList();
        sides.learn(screen, SideAreas.of(areas, bounds.x(), bounds.y(), bounds.height()), BookSession.get().isOpen());
    }
}
